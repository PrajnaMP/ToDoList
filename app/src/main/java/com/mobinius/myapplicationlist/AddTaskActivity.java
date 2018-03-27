package com.mobinius.myapplicationlist;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;

/**
 * Created by prajna on 30/5/17.
 */

public class AddTaskActivity extends AppCompatActivity {
    private EditText mNameView, mDescriptionView;
    private TextView mDateView, mTimeView,mLocationView;
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private Toolbar toolbar;
    Realm realm;
//    Date date1;
    GPSTracker gps;
    double latitude;
    double longitude;
    double altitude;
    private static final int REQUEST_CODE_PERMISSION = 2;

    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_main);
        realm = Realm.getDefaultInstance();
        toolbar = (Toolbar) findViewById(R.id.add_task_tool_bar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setTitle("Add Task");
        setSupportActionBar(toolbar);
        mNameView = (EditText) findViewById(R.id.add_task_name_edittext);
        mDescriptionView = (EditText) findViewById(R.id.add_task_description_edittext);
        mDateView = (TextView) findViewById(R.id.add_task_date_text);
        mDateView.setText("Set Date");
        mTimeView = (TextView) findViewById(R.id.add_task_time_text);
        mTimeView.setText("Set Time");

        mLocationView = (TextView) findViewById(R.id.add_task_location_text);
        mLocationView.setText("Set Location");
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateDialog();
            }
        });

        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mTimeView.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        mLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gps = new GPSTracker(AddTaskActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    altitude = gps.getAltitude();

                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    Log.v("latitude", "" + latitude);
                    Log.v("longitude", "" + longitude);
                    Log.v("altitude", "" + altitude);
                    mLocationView.setText("lat"+latitude+"lon"+longitude);
                    GeocoderHandler geocoderHandler=new GeocoderHandler();
                    getAddressFromLocation(latitude,longitude,getApplicationContext(),geocoderHandler);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.done_button:

                if (mNameView.getText().toString() == null || mNameView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter name", Toast.LENGTH_SHORT).show();
                } else if (mDescriptionView.getText().toString().isEmpty() || mDescriptionView.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "Enter description", Toast.LENGTH_SHORT).show();
                }
                else if (mDateView.getText().toString() == "Set Date"||mDateView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter date", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
                    addTaskToDatabase();
//                    intent.putExtra("name", mNameView.getText().toString());
//                    intent.putExtra("description", mDescriptionView.getText().toString());
//                    intent.putExtra("date", mDateView.getText().toString());
//                    intent.putExtra("time", mTimeView.getText().toString());
//                    setResult(1, intent);
//                    finish();
                    startActivity(intent);
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void DateDialog() {

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear += 1;
                mDateView.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
            }
        };

        DatePickerDialog dpDialog = new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
        startActivity(intent);
    }


    public void addTaskToDatabase() {
        realm.beginTransaction();

//        TaskClass taskClass = realm.createObject(TaskClass.class);
        TaskClass taskClass = realm.createObject(TaskClass.class, UUID.randomUUID().toString());
        taskClass.setName(mNameView.getText().toString());
        taskClass.setDescription(mDescriptionView.getText().toString());
        String sDate1 = mDateView.getText().toString();
        String nLocation = mLocationView.getText().toString();
        double lat =latitude;
        double lon = longitude;
        Date date1 = null;
        String result = null;
        try {
            date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        taskClass.setDate(date1);
        taskClass.setTime(mTimeView.getText().toString());
        taskClass.setLocation(nLocation);
        taskClass.setLattitude(lat);
        taskClass.setLongituge(lon);
        Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show();
        realm.commitTransaction();
    }

    public static void getAddressFromLocation(final double latitude, final double longitude, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List < Address > addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)); //.append("\n");
                        }
                        sb.append(address.getAddressLine(0)).append("\n");
                        sb.append(address.getLocality()).append("\n");
                        sb.append(address.getAdminArea()).append("\n");
                        sb.append(address.getCountryName()).append("\n");
                        sb.append(address.getPostalCode()).append("\n");
                        sb.append(address.getFeatureName()).append("\n");

                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e("Location Address Loader", "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = " Unable to get address for this location.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}

