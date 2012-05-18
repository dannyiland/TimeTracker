package edu.ucsb.cs.cs290i.service.detectors.calendar;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.CalendarList;

public class CalendarDataSource {
    protected String authToken;
    private SharedPreferences settings;
    private String accountName;
    private GoogleAccountManager accountManager;
    private final Context context;
    private boolean loggedIn;


    public CalendarDataSource(Context context) {
        this.context = context;
        Calendar client = Calendar.builder(AndroidHttp.newCompatibleTransport(), new GsonFactory())
                .setApplicationName("ian")
                .setHttpRequestInitializer(new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                        System.out.println("Token: " + authToken);
                        request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(authToken));
                    }
                })
                .setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY))
                .build();

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        accountName = settings.getString("accountName", null);
        authToken = settings.getString("authToken", null);

        if (accountName == null || authToken == null) {
            Intent intent = new Intent();
            intent.setClass(context, AccountChooser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return; // TODO: After auth.
        }
        System.out.println(accountName);
        System.out.println(authToken);

        CalendarList list = client.calendarList();
        try {
            System.out.println(list.list().execute().getItems().toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
