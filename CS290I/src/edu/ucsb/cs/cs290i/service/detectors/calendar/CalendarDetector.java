package edu.ucsb.cs.cs290i.service.detectors.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.common.collect.Lists;

import edu.ucsb.cs.cs290i.R;
import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class CalendarDetector extends Detector {

    public CalendarDetector(String... params) {
        super(params);
    }


    @Override
    public List<Event> getEvents(long startTime, long endTime) {
        List<Event> events = Lists.newArrayList();

        try {
            List<com.google.api.services.calendar.model.Event> calEvents = CalendarDataSource.getInstance().getEvents(
                    startTime, endTime, getParameters()[0]);
            for (com.google.api.services.calendar.model.Event e : calEvents) {
                events.add(new Event("CalendarEvent", e.getSummary(), 1, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return events;
    }

    // The configuration UI Activity. Must have a String[] extra called "config" in its result.
    // Must also be called "Config".
    public static class Config extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.calendar_config);
            final EditText substr = (EditText) findViewById(R.id.cal_substring);
            Button create = (Button) findViewById(R.id.create);
            create.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra("config", new String[] { substr.getText().toString() });
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
            
            ImageButton queryV = (ImageButton) findViewById(R.id.query_vbtn);

            queryV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    voiceToField(substr, "Calendar Query");
                }
            });

        }


        protected void voiceToField(EditText field, String prompt) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            startActivityForResult(intent, field.getId());
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) {
                return;
            }

            if (requestCode != 0) {
                ArrayList<String> matches = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                ((EditText) findViewById(requestCode)).setText(matches.get(0));
            }
        }

    }


    @Override
    public String toString() {
        return "Calendar event about " + getParameters()[0];
    }
}
