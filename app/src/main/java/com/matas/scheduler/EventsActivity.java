package com.matas.scheduler;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TimePicker;

import com.matas.scheduler.adapters.DBHelper;
import com.matas.scheduler.adapters.SimpleEventListAdapter;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

public class EventsActivity extends AppCompatActivity {

    private static DBHelper dbHelper;
    private static EventsActivity _this;
    private SimpleEventListAdapter simpleAdapter;
    private ArrayList<ArrayList<String>> eventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        _this = this;
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.exp_list_view);
        dbHelper = MainActivity.getDbHelper();
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        String queryDate = getIntent().getStringExtra("date");
        //Hashtable<String, ArrayList<String>> resultForDate = checkData(db, getIntent().getStringExtra("date"));
        Hashtable<String, ArrayList<String>> resultForDate = checkDate(db, queryDate);
        eventsList = new ArrayList<ArrayList<String>>(resultForDate.values());
        if (!eventsList.isEmpty())
            System.out.println("found: " + eventsList.size() + ", " + eventsList.get(0).get(1));
        simpleAdapter = new SimpleEventListAdapter(this, eventsList);
        //SimpleEventListAdapter simpleAdapter = new SimpleEventListAdapter(this, (HashMap)resultForDate);
        expandableListView.setAdapter(simpleAdapter);
        android.support.design.widget.FloatingActionButton addEvButton = (android.support.design.widget.FloatingActionButton) findViewById(R.id.newElementFab);
        addEvButton.setOnClickListener(v -> {
            ArrayList<String> blank = new ArrayList<String>();
            blank.add("");
            blank.add(queryDate);
            blank.add("00:00");
            blank.add("new event");
            blank.add("");
            blank.add("0");
            blank.add("");
            eventsList.add(blank);
            simpleAdapter.notifyDataSetChanged();
        });
        //Calendar c = Calendar.getInstance();

        // Launch Time Picker Dialog

    }

    public static String getNormalDateFromCalendar(String fromCalendar) {
        String[] separated = fromCalendar.split("/");
        return separated[0] + "/" + (Integer.parseInt(separated[1]) - 0) + "/" + separated[2];
    }

    public static String setIncrementDateToCalendar(String toCalendar) {
        String[] separated = toCalendar.split("/");
        return separated[0] + "/" + (Integer.parseInt(separated[1]) - 1) + "/" + separated[2];
    }

    public static String addEvent(SQLiteDatabase db, String id, String date, String time, String title,
                                  String event, String alarm, String typeAlarm) {
        System.out.println("start add event: id=" + id + ", time=" + time + ", date=" + date + ", title=" + title + ", event=" + event + ", alarm=" + alarm);
        String realDate = getNormalDateFromCalendar(date);
        Hashtable<String, ArrayList<String>> searchResult = checkID(db, id);
        if (!id.equals("") && !searchResult.isEmpty()) {
            //update
            ContentValues values = new ContentValues();
            values.put("time", time);
            values.put("date", realDate);
            values.put("title", title);
            values.put("event", event);
            values.put("alarm", alarm);
            values.put("alarm_sound", typeAlarm);
            db.update("schedule", values, " id = '" + id + "'", null);
            return id;
        } else {
            ContentValues cv = new ContentValues();
            cv.put("date", realDate);
            cv.put("time", time);
            cv.put("title", title);
            cv.put("event", event);
            cv.put("alarm", alarm);
            cv.put("alarm_sound", typeAlarm);
            return db.insert("schedule", null, cv) + "";
        }
        //calendarView.setEvents(UpdateShowEvents(db));
    }

    public static void deleteEvent(SQLiteDatabase db, String id) {
        int deletedRows = db.delete("schedule", " id LIKE '" + id + "'", null);
        //calendarView.setEvents(UpdateShowEvents(db));
    }

    public static Hashtable<String, ArrayList<String>> selectData(SQLiteDatabase db) {
        Hashtable result = new Hashtable();
        String sqlQuery = "select * from schedule";
        Cursor c = db.rawQuery(sqlQuery, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ArrayList<String> line = new ArrayList<String>();
                    for (String cn : c.getColumnNames()) {
                        line.add(c.getString(c.getColumnIndex(cn)));
                    }
                    result.put(c.getString(c.getColumnIndex("id")), line);
                } while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    public static Hashtable<String, ArrayList<String>> checkDate(SQLiteDatabase db, String date) {
        Hashtable<String, ArrayList<String>> result = new Hashtable();
        String sqlQuery = "select * from schedule where date = '" + getNormalDateFromCalendar(date) + "'";
        Cursor c = db.rawQuery(sqlQuery, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ArrayList<String> line = new ArrayList<String>();
                    for (String cn : c.getColumnNames()) {
                        line.add(c.getString(c.getColumnIndex(cn)));
                    }
                    result.put(c.getString(c.getColumnIndex("id")), line);
                } while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    public static Hashtable<String, ArrayList<String>> checkID(SQLiteDatabase db, String id) {
        Hashtable<String, ArrayList<String>> result = new Hashtable();
        String sqlQuery = "select * from schedule where id = '" + id + "'";
        Cursor c = db.rawQuery(sqlQuery, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ArrayList<String> line = new ArrayList<String>();
                    for (String cn : c.getColumnNames()) {
                        line.add(c.getString(c.getColumnIndex(cn)));
                    }
                    result.put(c.getString(c.getColumnIndex("id")), line);
                } while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    public static void refreshData(ArrayList<ArrayList<String>> eventsList) {
        _this.eventsList = eventsList;
        _this.simpleAdapter.notifyDataSetChanged();
        MainActivity.refreshData();
    }
}
