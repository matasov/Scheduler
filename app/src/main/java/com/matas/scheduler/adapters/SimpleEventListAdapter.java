package com.matas.scheduler.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.matas.scheduler.EventsActivity;
import com.matas.scheduler.MainActivity;
import com.matas.scheduler.R;

import java.util.ArrayList;

public class SimpleEventListAdapter extends BaseExpandableListAdapter {
    private ArrayList<ArrayList<String>> mGroups;
    private Context mContext;
    private DBHelper dbHelper;

    public SimpleEventListAdapter(Context context, ArrayList<ArrayList<String>> groups) {
        mContext = context;
        mGroups = groups;
        dbHelper = MainActivity.getDbHelper();
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return mGroups.get(groupPosition).size();
        //return mGroups.size();
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).get(0);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        System.out.println("start use element: " + groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_event, null);
        }

        if (isExpanded) {

        } else {

        }

        TextView textTime = (TextView) convertView.findViewById(R.id.textTime);
        String timeEvent = mGroups.get(groupPosition).get(2);
        if (timeEvent == "") {
            timeEvent = "00:00";
        }
        textTime.setText(timeEvent);

        TextView textDate = (TextView) convertView.findViewById(R.id.textDate);
        textDate.setText(mGroups.get(groupPosition).get(3));
        android.support.constraint.ConstraintLayout background = (android.support.constraint.ConstraintLayout) convertView.findViewById(R.id.groupLayout);

        background.setBackgroundColor(mGroups.get(groupPosition).get(5).equals("1") ?
                Color.parseColor("#ccffff") : Color.parseColor("#ffcccc"));
        final TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        mGroups.get(groupPosition).set(2, hourOfDay + ":" + minute);
                        textTime.setText(hourOfDay + ":" + minute);
                        mGroups.get(groupPosition).set(0, EventsActivity.addEvent(dbHelper.getReadableDatabase()
                                , mGroups.get(groupPosition).get(0)
                                , mGroups.get(groupPosition).get(1)
                                , mGroups.get(groupPosition).get(2)
                                , mGroups.get(groupPosition).get(3)
                                , mGroups.get(groupPosition).get(4)
                                , mGroups.get(groupPosition).get(5)
                                , mGroups.get(groupPosition).get(6)));
                        EventsActivity.refreshData(mGroups);
                    }
                }, 0, 15, false);

        textTime.setOnClickListener(v -> {
            timePickerDialog.show();
        });

        /*CheckBox onOff = (CheckBox) convertView.findViewById(R.id.sOnOff);
        onOff.setChecked(mGroups.get(groupPosition).get(5).equals("1"));*/

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        System.out.println("start child view");
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.inner_event, null);
        }

        TextView textTitle = (TextView) convertView.findViewById(R.id.titleSimpleText);
        textTitle.setText(mGroups.get(groupPosition).get(3));
        TextView textEvent = (TextView) convertView.findViewById(R.id.editEventText);
        textEvent.setText(mGroups.get(groupPosition).get(4));
        Button button = (Button) convertView.findViewById(R.id.delEventBtn);
        android.support.constraint.ConstraintLayout background = convertView.findViewById(R.id.backgroundLayout);
        button.setOnClickListener(v -> {
            EventsActivity.deleteEvent(dbHelper.getReadableDatabase(), mGroups.get(groupPosition).get(0));
            mGroups.remove(groupPosition);
            EventsActivity.refreshData(mGroups);
        });
        Button updateBtn = (Button) convertView.findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(v -> {
            System.out.println("update btn clicked");
            mGroups.get(groupPosition).set(3, textTitle.getText().toString());
            mGroups.get(groupPosition).set(4, textEvent.getText().toString());
            mGroups.get(groupPosition).set(0, EventsActivity.addEvent(dbHelper.getReadableDatabase()
                    , mGroups.get(groupPosition).get(0)
                    , mGroups.get(groupPosition).get(1)
                    , mGroups.get(groupPosition).get(2)
                    , mGroups.get(groupPosition).get(3)
                    , mGroups.get(groupPosition).get(4)
                    , mGroups.get(groupPosition).get(5)
                    , mGroups.get(groupPosition).get(6)));
            EventsActivity.refreshData(mGroups);
        });
        Switch onOff = (Switch) convertView.findViewById(R.id.onOff);
        onOff.setChecked(mGroups.get(groupPosition).get(5).equals("1"));
        onOff.setOnClickListener(v -> {
            System.out.println("alarm btn clicked");
            if (background != null)
                background.setBackgroundColor(mGroups.get(groupPosition).get(5).equals("1") ?
                        Color.parseColor("#ccffff") : Color.parseColor("#ffcccc"));
            mGroups.get(groupPosition).set(5, (onOff.isChecked() ? "1" : "0"));

            mGroups.get(groupPosition).set(0, EventsActivity.addEvent(dbHelper.getReadableDatabase()
                    , mGroups.get(groupPosition).get(0)
                    , mGroups.get(groupPosition).get(1)
                    , mGroups.get(groupPosition).get(2)
                    , mGroups.get(groupPosition).get(3)
                    , mGroups.get(groupPosition).get(4)
                    , mGroups.get(groupPosition).get(5)
                    , mGroups.get(groupPosition).get(6)));
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
