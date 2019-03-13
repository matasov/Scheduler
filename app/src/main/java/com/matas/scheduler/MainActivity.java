package com.matas.scheduler;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Stream;
import com.applandeo.materialcalendarview.CalendarUtils;
import com.applandeo.materialcalendarview.CalendarView;
//import android.widget.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.matas.scheduler.adapters.DBHelper;

import android.content.ContentValues;

import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements OnSelectDateListener {

    private static DBHelper dbHelper;
    private static MainActivity _this;

    private CalendarView calendarView;
    private TextView dateShow;
    private EditText editTitle;
    private MultiAutoCompleteTextView editEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _this = this;
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        dateShow = (TextView) findViewById(R.id.dateShowText);
//        editTitle = (EditText) findViewById(R.id.editTitle);
//        editEvent = (MultiAutoCompleteTextView) findViewById(R.id.editEvent);
        dbHelper = new DBHelper(this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        //deleteAllData(db);
        //final TextView dateEvent = (TextView) findViewById(R.id.textView);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setEvents(UpdateShowEvents(db));
        /*calendarView.setOnDayClickListener(eventDay ->//eventDay.getCalendar().getTime()
                updateEventsForDate(db, sdf.format(eventDay.getCalendar().getTime())));*/
        calendarView.setOnDayClickListener(eventDay -> {
            Intent simpleEventIntent = new Intent(this, EventsActivity.class);
            simpleEventIntent.putExtra("date", getNormalDateFromCalendar(sdf.format(eventDay.getCalendar().getTime())));
            startActivity(simpleEventIntent);
        });

        /*Button addEvButton = (Button) findViewById(R.id.saveBtn);
        addEvButton.setOnClickListener(v -> {
            if (!editTitle.getText().toString().equals(""))
                for (Calendar calendar : calendarView.getSelectedDates()) {
                    addEvent(db, sdf.format(calendar.getTime()), editTitle.getText().toString(), editEvent.getText().toString());
                }
        });

        Button delEvButton = (Button) findViewById(R.id.delBtn);
        delEvButton.setOnClickListener(v -> {
            for (Calendar calendar : calendarView.getSelectedDates()) {
                deleteEvent(db, sdf.format(calendar.getTime()));
            }
        });*/

    }

    private String getNormalDateFromCalendar(String fromCalendar) {
        String[] separated = fromCalendar.split("/");
        return separated[0] + "/" + (Integer.parseInt(separated[1]) - 0) + "/" + separated[2];
    }

    private String setIncrementDateToCalendar(String toCalendar) {
        String[] separated = toCalendar.split("/");
        return separated[0] + "/" + (Integer.parseInt(separated[1]) - 1) + "/" + separated[2];
    }

    private void updateEventsForDate(SQLiteDatabase db, String date) {
        String dateReal = getNormalDateFromCalendar(date);
        dateShow.setText(dateReal);
        Hashtable<String, List<String>> tableData = checkData(db, date);
        if (!tableData.isEmpty()) {
            ArrayList<String> line = (ArrayList<String>) tableData.get(dateReal);
            editTitle.setText(line.get(3));
            editEvent.setText(line.get(4));
        } else {
            editTitle.setText("");
            editEvent.setText("");
        }
    }

    private List<EventDay> UpdateShowEvents(SQLiteDatabase db) {
        List<EventDay> events = new ArrayList<>();
        Hashtable<String, List<String>> tableData = selectData(db);
        Enumeration e = tableData.keys();
        while (e.hasMoreElements()) {
            ArrayList<String> line = (ArrayList<String>) tableData.get(e.nextElement());
            String[] separated = setIncrementDateToCalendar(line.get(1)).split("/");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(separated[0]), Integer.parseInt(separated[1]), Integer.parseInt(separated[2]));
            String title = "Ev";
            if (!line.get(3).equals("")) {
                title = Character.toString(line.get(3).charAt(0));
                if (line.get(3).length() > 1) {
                    title += Character.toString(line.get(3).charAt(1));
                }
            }
            events.add(new EventDay(calendar, getCircleDrawableWithText(this, title)));
        }
        Toast.makeText(getApplicationContext(),
                "found: " + events.size(),
                Toast.LENGTH_SHORT).show();
        return events;
    }

    private void addEvent(SQLiteDatabase db, String date, String title, String event) {
        String realDate = getNormalDateFromCalendar(date);
        Hashtable<String, List<String>> searchResult = checkData(db, date);
        if (!searchResult.isEmpty()) {
            //update
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("event", event);
            db.update("schedule", values, " date = '" + realDate + "'", null);
        } else {
            ContentValues cv = new ContentValues();
            cv.put("date", realDate);
            cv.put("time", "00:00");
            cv.put("title", title);
            cv.put("event", event);
            cv.put("alarm", "1");
            cv.put("alarm_sound", "");
            db.insert("schedule", null, cv);
        }
        calendarView.setEvents(UpdateShowEvents(db));
    }

    private void deleteEvent(SQLiteDatabase db, String date) {
        System.out.println("delete from schedule where date LIKE '" + getNormalDateFromCalendar(date) + "'");
        int deletedRows = db.delete("schedule", " date LIKE '" + getNormalDateFromCalendar(date) + "'", null);
        calendarView.setEvents(UpdateShowEvents(db));
    }

    private Hashtable<String, List<String>> selectData(SQLiteDatabase db) {
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
                    result.put(c.getString(c.getColumnIndex("date")), line);
                } while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    //for test
    private void deleteAllData(SQLiteDatabase db) {
        db.delete("schedule", null, null);
    }

    private Hashtable<String, List<String>> checkData(SQLiteDatabase db, String date) {
        Hashtable<String, List<String>> result = new Hashtable();
        String sqlQuery = "select * from schedule where date = '" + getNormalDateFromCalendar(date) + "'";
        Cursor c = db.rawQuery(sqlQuery, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ArrayList<String> line = new ArrayList<String>();
                    for (String cn : c.getColumnNames()) {
                        line.add(c.getString(c.getColumnIndex(cn)));
                    }
                    result.put(c.getString(c.getColumnIndex("date")), line);
                } while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    private void logResultData() {

    }

    public static Drawable getCircleDrawableWithText(Context context, String string) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.sample_circle);
        Drawable text = CalendarUtils.getDrawableText(context, string, null, android.R.color.white, 12);

        Drawable[] layers = {background, text};
        return new LayerDrawable(layers);
    }

    /*class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "ForTestDB", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table schedule ("
                    + "id integer primary key autoincrement,"
                    + "date text,"
                    + "time text,"
                    + "title text,"
                    + "event text,"
                    + "alarm text,"
                    + "alarm_sound text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }*/

    public static DBHelper getDbHelper() {
        return dbHelper;
    }

    @Override
    public void onSelect(List<Calendar> calendars) {
        Stream.of(calendars).forEach(calendar ->
                Toast.makeText(getApplicationContext(),
                        calendar.getTime().toString(),
                        Toast.LENGTH_SHORT).show());
    }

    public static void refreshData() {
        _this.calendarView.setEvents(_this.UpdateShowEvents(dbHelper.getReadableDatabase()));
    }
}
