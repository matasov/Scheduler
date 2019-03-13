package com.matas.scheduler.events;

import java.util.ArrayList;

public class SimpleEvent {

    private String id;
    private String date;
    private String time;
    private String title;
    private String event;
    private String alarm;
    private String alarmSound;

    public SimpleEvent(String id, String date, String time, String title, String event, String alarm, String alarmSound) {
        this.date = date;
        this.time = time;
        this.title = title;
        this.event = event;
    }

    public String getID() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getEvent() {
        return event;
    }

    public String getAlarm() {
        return alarm;
    }

    public String getAlarmSound() {
        return alarmSound;
    }

    public ArrayList<String> getValues() {
        return new ArrayList<String>() {{
            add(id);
            add(date);
            add(time);
            add(title);
            add(event);
            add(alarm);
            add(alarmSound);
        }};
    }
}
