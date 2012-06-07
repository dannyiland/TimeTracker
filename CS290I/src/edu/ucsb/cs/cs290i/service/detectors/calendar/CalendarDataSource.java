package edu.ucsb.cs.cs290i.service.detectors.calendar;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.text.format.Time;
import android.util.Log;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.common.collect.ImmutableList;

public class CalendarDataSource {
    private static CalendarDataSource instance;

    protected String authToken;
    private Calendar calendarService;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = new GsonFactory();


    public static CalendarDataSource getInstance() {
        return instance == null ? instance = new CalendarDataSource() : instance;
    }


    protected CalendarDataSource() {
        Logger.getLogger("com.google.api.client").setLevel(Level.ALL);

        calendarService = com.google.api.services.calendar.Calendar.builder(transport, jsonFactory)
                .setApplicationName("Google-CalendarAndroidSample/1.0")
                .setHttpRequestInitializer(new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                        request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(authToken));
                    }
                })
                .setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY))
                .build();
    }


    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    public void printCalendars() {
        CalendarList list = calendarService.calendarList();
        try {
            for (CalendarListEntry c : list.list().execute().getItems()) {
                System.out.println(c.getDescription());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Event> getEvents(long startTime, long endTime, String query) throws IOException {
        Time minTime = new Time();
        Time maxTime = new Time();
        minTime.set(startTime);
        maxTime.set(endTime);

        System.out.println(maxTime.format3339(false));
        System.out.println(minTime.format3339(false));
        System.out.println(authToken);

        System.out.println("Requesting events");
        com.google.api.services.calendar.Calendar.Events.List list = calendarService.events().list("primary")
                .setTimeMin(minTime.format3339(false))
                .setTimeMax(maxTime.format3339(false)).setQ(query);
        list.getUnknownKeys().put("xoauth_requestor_id", "imwhit4email@gmail.com");
        List<Event> events = list.execute().getItems();
        System.out.println("Got events");
        if (events == null) {
            Log.e("CalendarDataSource", "Error: null events returned");
            events = ImmutableList.of();
        }

        return events;

    }
}
