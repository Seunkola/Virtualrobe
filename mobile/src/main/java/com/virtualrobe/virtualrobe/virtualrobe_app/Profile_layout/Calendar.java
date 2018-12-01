package com.virtualrobe.virtualrobe.virtualrobe_app.Profile_layout;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.desai.vatsal.mydynamiccalendar.EventModel;
import com.desai.vatsal.mydynamiccalendar.GetEventListListener;
import com.desai.vatsal.mydynamiccalendar.MyDynamicCalendar;
import com.desai.vatsal.mydynamiccalendar.OnDateClickListener;
import com.desai.vatsal.mydynamiccalendar.OnEventClickListener;
import com.desai.vatsal.mydynamiccalendar.OnWeekDayViewClickListener;
import com.medialablk.easygifview.EasyGifView;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Calendar extends AppCompatActivity {

   @BindView(R.id.toolbar)
   android.support.v7.widget.Toolbar toolbar;

   @BindView(R.id.myCalendar)
    MyDynamicCalendar myCalendar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calender);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        SpannableString s = new SpannableString("Choose Event Date");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Update the action bar title with the TypefaceSpan instance
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(s);
        }
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#efc04a")));

        }

        initialize();

        invalidateOptionsMenu();
    }

    private void initialize() {
        //myCalendar.showMonthView();
        showMonthViewWithBelowEvents();
        myCalendar.setHeaderBackgroundColor("#E1EFC04A");
        myCalendar.setHeaderTextColor("#ffffff");
        myCalendar.setNextPreviousIndicatorColor("#ffffff");
        myCalendar.setCurrentDateBackgroundColor("#E1EFC04A");
        myCalendar.setCurrentDateTextColor("#F63006");
        myCalendar.setEventCellBackgroundColor("#ffffff");
        myCalendar.setEventCellTextColor("#F63006");
        myCalendar.setExtraDatesOfMonthBackgroundColor("#8ec5c4c4");
        myCalendar.setExtraDatesOfMonthTextColor("#555555");

        myCalendar.addEvent("19-10-2018", "8:00", "8:15", "Today Event 1");
        myCalendar.addEvent("19-10-2016", "8:15", "8:30", "Today Event 2");
        myCalendar.addEvent("05-10-2018", "8:30", "8:45", "Today Event 3");
        myCalendar.addEvent("05-10-2018", "8:45", "9:00", "Today Event 4");
        myCalendar.addEvent("8-10-2018", "8:00", "8:30", "Today Event 5");
        myCalendar.addEvent("08-10-2018", "9:00", "10:00", "Today Event 6");

        myCalendar.getEventList(new GetEventListListener() {
            @Override
            public void eventList(ArrayList<EventModel> eventList) {

                //Log.e("tag", "eventList.size():-" + eventList.size());
                for (int i = 0; i < eventList.size(); i++) {
                    //Log.e("tag", "eventList.getStrName:-" + eventList.get(i).getStrName());
                }

            }
        });

        myCalendar.setHolidayCellClickable(true);
        myCalendar.addHoliday("2-11-2018");
        myCalendar.addHoliday("8-11-2018");
        myCalendar.addHoliday("12-11-2018");
        myCalendar.addHoliday("13-11-2018");
        myCalendar.addHoliday("8-10-2018");
        myCalendar.addHoliday("10-12-2018");

    }

    private void showMonthView() {

        myCalendar.showMonthView();

        myCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(Date date) {
                //Log.e("date", String.valueOf(date));

            }

            @Override
            public void onLongClick(Date date) {
                //Log.e("date", String.valueOf(date));
            }
        });

    }

    private void showMonthViewWithBelowEvents() {

        myCalendar.showMonthViewWithBelowEvents();

        myCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(Date date) {
                /*Add new event to calendar*/
                Event_dialog(date);
            }

            @Override
            public void onLongClick(Date date) {
                //Log.e("date", String.valueOf(date));
                showDayView();
            }
        });

    }

    private void showWeekView() {

        myCalendar.showWeekView();

        myCalendar.setOnEventClickListener(new OnEventClickListener() {
            @Override
            public void onClick() {
                //Log.e("showWeekView","from setOnEventClickListener onClick");
            }

            @Override
            public void onLongClick() {
               // Log.e("showWeekView","from setOnEventClickListener onLongClick");

            }
        });

        myCalendar.setOnWeekDayViewClickListener(new OnWeekDayViewClickListener() {
            @Override
            public void onClick(String date, String time) {
               // Log.e("showWeekView", "from setOnWeekDayViewClickListener onClick");
               // Log.e("tag", "date:-" + date + " time:-" + time);

            }

            @Override
            public void onLongClick(String date, String time) {
               // Log.e("showWeekView", "from setOnWeekDayViewClickListener onLongClick");
               // Log.e("tag", "date:-" + date + " time:-" + time);

            }
        });


    }

    private void showDayView() {

        myCalendar.showDayView();

        myCalendar.setOnEventClickListener(new OnEventClickListener() {
            @Override
            public void onClick() {
               // Log.e("showDayView", "from setOnEventClickListener onClick");
                showMonthViewWithBelowEvents();
            }

            @Override
            public void onLongClick() {
               // Log.e("showDayView", "from setOnEventClickListener onLongClick");
                showAgendaView();
            }
        });

        myCalendar.setOnWeekDayViewClickListener(new OnWeekDayViewClickListener() {
            @Override
            public void onClick(String date, String time) {
               // Log.e("showDayView", "from setOnWeekDayViewClickListener onClick");
                //Log.e("tag", "date:-" + date + " time:-" + time);
            }

            @Override
            public void onLongClick(String date, String time) {
               // Log.e("showDayView", "from setOnWeekDayViewClickListener onLongClick");
               // Log.e("tag", "date:-" + date + " time:-" + time);
            }
        });

    }

    private void showAgendaView() {

        myCalendar.showAgendaView();

        myCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(Date date) {
               // Log.e("date", String.valueOf(date));
                showMonthViewWithBelowEvents();
            }

            @Override
            public void onLongClick(Date date) {
                //Log.e("date", String.valueOf(date));
                showDayView();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Snackbar.make(myCalendar,"Select the date on the calendar", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.remove:
                myCalendar.deleteEvent(2);
                Snackbar.make(myCalendar,"Event succesfully Deleted", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void Event_dialog(Date date){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Calendar.this);
        LayoutInflater inflater = LayoutInflater.from(Calendar.this);
        View view = inflater.inflate(R.layout.calendar_dialog,null);
        builder.setView(view);

        /*Assigning custom dialog button*/
        Button Ok = view.findViewById(R.id.ok_button);
        Button Cancel = view.findViewById(R.id.cancel_button);

        /*Assigning views*/
        Spinner start_time = view.findViewById(R.id.start_time);
        Spinner start_time_end = view.findViewById(R.id.start_time_end);
        Spinner start_time_period = view.findViewById(R.id.start_time_period);

        Spinner end_time = view.findViewById(R.id.end_time);
        Spinner end_time_end = view.findViewById(R.id.end_time_end);
        Spinner end_time_period = view.findViewById(R.id.end_time_period);

        EditText event_name = view.findViewById(R.id.event_name);

        /*Spinners*/
        List<String> time_list_start = new ArrayList<>();
        for (int i=1; i<13; i++){
            String time = String.valueOf(i);
            time_list_start.add(time);
        }

        List<String> time_list_end = new ArrayList<>();
        for (int i=0; i<61; i++){
            if (i<10) {
                String time = "0"+String.valueOf(i);
                time_list_end.add(time);
            }
            else {
                String time = String.valueOf(i);
                time_list_end.add(time);
            }
        }

        List<String> time_period = new ArrayList<>();
        time_period.add("AM");
        time_period.add("PM");

        //populate Start time spinners
        ArrayAdapter<String> start_time_adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,time_list_start);
        start_time_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        start_time.setAdapter(start_time_adapter);
        end_time.setAdapter(start_time_adapter);

        //populate End time spinners
        ArrayAdapter<String> end_time_adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,time_list_end);
        start_time_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        start_time_end.setAdapter(end_time_adapter);
        end_time_end.setAdapter(end_time_adapter);

        //populate period spinners
        ArrayAdapter<String> period_adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,time_period);
        start_time_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        start_time_period.setAdapter(period_adapter);
        end_time_period.setAdapter(period_adapter);


        final AlertDialog dialog = builder.create();
        /*call on actions*/
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (event_name!=null) {
                    myCalendar.addEvent(String.valueOf(date), String.valueOf(start_time.getSelectedItem())
                                    + ":" + String.valueOf(start_time_end.getSelectedItem()) +
                                    String.valueOf(start_time_period.getSelectedItem()), String.valueOf(end_time.getSelectedItem())
                                    + ":" + String.valueOf(end_time_end.getSelectedItem()) + String.valueOf(end_time_period.getSelectedItem()),
                            event_name.getText().toString());

                    Snackbar.make(view,"Event succesfully added", Toast.LENGTH_SHORT).show();
                }
                else {
                    Snackbar.make(view,"Add Event Name", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,"Add Event succesfully cancelled", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });


        /*Assign gif animation*/
        EasyGifView easyGifView = view.findViewById(R.id.easyGifView);
        easyGifView.setGifFromResource(R.drawable.boy);

        dialog.show();
    }
}
