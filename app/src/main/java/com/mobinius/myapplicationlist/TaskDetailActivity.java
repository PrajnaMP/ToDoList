package com.mobinius.myapplicationlist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by prajna on 31/5/17.
 */

public class TaskDetailActivity extends AppCompatActivity {
    private EditText mName, mDescription;
    private TextView mDate, mTime,mLocation;
    public TextView mStatusTextView, mStatusView, mCompleted;
    private Toolbar toolbar;
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private boolean editClicked;
    private MenuItem edititem;
    private MenuItem editdoneitem;
    private MenuItem deleteitem;
    public static boolean status = false;
    private SwitchCompat mChangeStatus;

    private Date status_;
    Integer position;
    public static boolean deleteClicked = false;
    Realm realm;
    String id;
    Date date;
//    RVAdapter mAdapter;
//Date date1;
GPSTracker gps;
    double latitude;
    double longitude;
    double altitude;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_main);
        realm = Realm.getDefaultInstance();

        toolbar = (Toolbar) findViewById(R.id.add_task_tool_bar);
        toolbar.setTitle("Task Details");

        setSupportActionBar(toolbar);
//                hideSoftKeyboard();

        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        mName = (EditText) findViewById(R.id.add_task_name_edittext);
        mDescription = (EditText) findViewById(R.id.add_task_description_edittext);
        mDate = (TextView) findViewById(R.id.add_task_date_text);
        mTime = (TextView) findViewById(R.id.add_task_time_text);
        mLocation = (TextView) findViewById(R.id.add_task_location_text);
        mChangeStatus = (SwitchCompat) findViewById(R.id.change_status_switch);
        mCompleted = (TextView) findViewById(R.id.completed_textview);
        mStatusTextView = (TextView) findViewById(R.id.add_task_status_textview);
        mStatusView = (TextView) findViewById(R.id.add_task_status_text);
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        mChangeStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    realm.beginTransaction();
                    Log.v("realm", "begin5");
                    mStatusView.setText("Completed");  //To change the text near to switch
                    date = getDate();
                    realm.commitTransaction();
                    Log.v("realm", "close5");

                } else {
                    realm.beginTransaction();
                    Log.v("realm", "begin6");
                    date = null;
                    mStatusView.setText("Pending");   //To change the text near to switch
                    realm.commitTransaction();
                    Log.v("realm", "close6");

                }
            }
        });

        if (RVAdapter.swiped == true) {
            mName.setFocusable(true);
            mDescription.setFocusable(true);
            mChangeStatus.setEnabled(true);
            setDate();
            setTime();
            setLocation();
        } else {
            mName.setFocusable(false);
            mDescription.setFocusable(false);
            mChangeStatus.setEnabled(false);
        }

        final String name = getIntent().getStringExtra("name.");
        String description = getIntent().getStringExtra("description.");
        Date date_ = (Date) getIntent().getExtras().get("date.");
        String time = getIntent().getStringExtra("time.");
        String location = getIntent().getStringExtra("location.");
        status_ = (Date) getIntent().getExtras().get("status.");
        position = getIntent().getIntExtra("position.", 0);
        id = getIntent().getStringExtra("id.");

        String cd = String.valueOf(date_);
        String result2 = null;
        try {
            SimpleDateFormat parseFormat =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Date date = parseFormat.parse(String.valueOf(cd));
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            result2 = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mName.setText(name);
        mDescription.setText(description);

        mDate.setText(result2);
        mTime.setText(time);
        mLocation.setText(location);
        if (status_ == null) {
            mStatusTextView.setVisibility(View.VISIBLE);
            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.setText("Pending");
            mChangeStatus.setVisibility(View.VISIBLE);
            mChangeStatus.setChecked(false);
            mCompleted.setVisibility(View.VISIBLE);

            status = false;
        } else {

            mStatusTextView.setVisibility(View.VISIBLE);
            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.setText("Completed");
            mChangeStatus.setVisibility(View.VISIBLE);
            mChangeStatus.setChecked(true);
            mCompleted.setVisibility(View.VISIBLE);
            status = true;
        }

    }

    private Date getDate() {
        Date date = new Date();
        return date;
    }
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        edititem = menu.findItem(R.id.edit_button);
        editdoneitem = menu.findItem(R.id.edit_done_button);
        deleteitem = menu.findItem(R.id.delete_button);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (RVAdapter.swiped == true) {
            editdoneitem.setVisible(true);
            edititem.setVisible(false);
        } else {
            editdoneitem.setVisible(false);
            edititem.setVisible(true);
        }
        return true;
    }

    public void hide() {
        edititem.setVisible(false);
        editdoneitem.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.edit_button:
                hide();
                editClicked = true;
                mStatusView.setText("Pending");
                date = null;
                mChangeStatus.setChecked(false);
                mName.setFocusableInTouchMode(true);
                mDescription.setFocusableInTouchMode(true);
                mChangeStatus.setEnabled(true);
                setDate();
                setTime();
                setLocation();
                break;

            case R.id.edit_done_button:
                deleteClicked = false;
                Intent intent = new Intent(TaskDetailActivity.this, MainActivity.class);
             /*   intent.putExtra("u_name", mName.getText().toString());
                Log.v("www", mName.getText().toString());
                intent.putExtra("u_description", mDescription.getText().toString());
                intent.putExtra("u_date", mDate.getText().toString());
                intent.putExtra("u_time", mTime.getText().toString());
                intent.putExtra("u_status", mStatusView.getText().toString());
                intent.putExtra("u_position", position);
                setResult(2, intent);
                finish();*/
                updateTask(id);
                startActivity(intent);
                break;

           /* case R.id.delete_button:
                deleteClicked = true;
                Intent i = new Intent();
                i.putExtra("u_position", position);
                Log.v("qqqqq", "" + position);
                setResult(2, i);
                finish();
                break;*/

            case R.id.delete_button:
                Intent i = new Intent(TaskDetailActivity.this, MainActivity.class);
                deleteTask(id, position);
                startActivity(i);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void DateDialog() {

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear += 1;
                mDate.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
            }
        };
        DatePickerDialog dpDialog = new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();
    }

    public void setDate() {
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateDialog();
            }
        });
    }

    public void setTime() {
        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TaskDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }
public void setLocation(){
    mLocation.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gps = new GPSTracker(TaskDetailActivity.this);

            // check if GPS enabled
            if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                altitude = gps.getAltitude();

                // \n is for new line
                   /* Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    Log.v("latitude", "" + latitude);
                    Log.v("longitude", "" + longitude);
                    Log.v("altitude", "" + altitude);*/
                mLocation.setText("lat"+latitude+"lon"+longitude);

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
        }
    });
}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void updateTask(String id) {
        RealmResults<TaskClass> results = realm.where(TaskClass.class).equalTo("id", id).findAll();

        realm.beginTransaction();

        for (TaskClass taskClass1 : results) {
            taskClass1.setName(mName.getText().toString());
//            Log.v("updated", mName.getText().toString());
            taskClass1.setDescription(mDescription.getText().toString());
            String sDate1 = mDate.getText().toString();
            Log.d("date>",""+sDate1);
            System.out.println("zzzzzz"+sDate1);

            Date date1 = null;

            try {
                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//                date1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(sDate1);
                Log.d("date>............",""+date1);
                System.out.println("zzzzzz........."+date1);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            taskClass1.setDate(date1);
            taskClass1.setTime(mTime.getText().toString());
            taskClass1.setLocation(mLocation.getText().toString());
            taskClass1.setIsCompleted(date);
//            Log.v("updated", "" + date);

        }
        realm.commitTransaction();
    }

    public void deleteTask(String id, int position) {

        RealmResults<TaskClass> results = realm.where(TaskClass.class).equalTo("id", id).findAll();
        realm.beginTransaction();
        results.deleteAllFromRealm();
        realm.commitTransaction();

    }

}
