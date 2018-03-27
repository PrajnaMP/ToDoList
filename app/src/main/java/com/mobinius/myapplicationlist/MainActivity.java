package com.mobinius.myapplicationlist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    private ArrayList<TaskClass> contactList = new ArrayList<>();
    private ArrayList<TaskClass> nearByLocationtList = new ArrayList<>();

    public static TextView mAddEvent;
    public static LinearLayout mSearchLL;
    RecyclerView recyclerView;
    RVAdapter mAdapter;
    private Toolbar toolbar;
    Realm realm;
    public EditText searchView;
    private TextView mDueDate, mCompleted, mCancelSearchAndSort, noResult, noCompletedTask,mCurrentLocation,
    mCurrentLocationView;
    //    private String id, title, description, image;
    private TextView device, cloud;
    public static boolean isCloudClicked;
    private MenuItem addTaskItem;
    //    private EditText mSearchViewCloud;
//    private TextView mCancelSearchCloud;
    GPSTracker gps;
    double latitude;
    double longitude;
    double altitude;
    double accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setTitle("Task List");
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        mAddEvent = (TextView) findViewById(R.id.event_textview);
        searchView = (EditText) findViewById(R.id.search_view);
        mSearchLL = (LinearLayout) findViewById(R.id.search_linear_layout);
        mCancelSearchAndSort = (TextView) findViewById(R.id.cancel);
        mDueDate = (TextView) findViewById(R.id.due_date);
        mCompleted = (TextView) findViewById(R.id.completed);
        noResult = (TextView) findViewById(R.id.no_result_textview);
        noCompletedTask = (TextView) findViewById(R.id.no_completed_textview);
        device = (TextView) findViewById(R.id.device_text);
        cloud = (TextView) findViewById(R.id.cloud_text);
//        mCancelSearchCloud=(EditText)findViewById(R.id.search_view_cloud);
//        mCancelSearchCloud=(TextView)findViewById(R.id.cancel_cloud);
        mCurrentLocation=(TextView)findViewById(R.id.current_location);
        mCurrentLocationView=(TextView)findViewById(R.id.current_location_view);
        displayDeviceData();


        device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCloudClicked = false;
                device.setBackgroundResource(R.color.colorPrimary);
                device.setTextColor(Color.parseColor("#FFFFFF"));
                cloud.setBackgroundResource(R.color.light_gray);
                cloud.setTextColor(Color.parseColor("#FF303F9F"));
                noResult.setVisibility(View.INVISIBLE);
                searchView.setText("");
                addTaskItem.setVisible(true);
                displayDeviceData();

            }
        });

        cloud.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ConnectivityManager ConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected() == true) {
                    isCloudClicked = true;
                    cloud.setBackgroundResource(R.color.colorPrimary);
                    cloud.setTextColor(Color.parseColor("#FFFFFF"));
                    device.setBackgroundResource(R.color.light_gray);
                    device.setTextColor(Color.parseColor("#FF303F9F"));
                    addTaskItem.setVisible(false);
                    searchView.setInputType(InputType.TYPE_NULL);
                    noResult.setVisibility(View.INVISIBLE);
                    searchView.setText("");

                    new TaskListAsyncTask().execute();

                    Toast.makeText(MainActivity.this, "Network Available", Toast.LENGTH_SHORT).show();

                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Connection Failed");
                    alertDialogBuilder.setMessage("Unable to connect. Please review your network settings.");
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    });

                    alertDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

       /* searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCancelSearchAndSort.setVisibility(View.VISIBLE);

//                showSoftKeyboard(view);
            }
        });*/

mCurrentLocation.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            altitude = gps.getAltitude();
            accuracy = gps.getAccuracy();


            mCurrentLocationView.setText(accuracy+","+latitude+","+longitude);

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        nearByLocationtList.clear();
        for (int i = 0; i < contactList.size(); i++) {
            double lat = contactList.get(i).getLattitude();
            double lon = contactList.get(i).getLongituge();

                   /* if ((latitude - 2 < lat && lat < latitude + 2) && (longitude - 2 < lon && lon < longitude + 2)) {
                        nearByLocationtList.add(contactList.get(i));

                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        mAdapter = new RVAdapter(getApplicationContext(), nearByLocationtList);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();

                    }*/

            double near = distance(lat,lon,latitude,longitude);
            Log.v("near",""+near);

            if(near<10){

                int fractionCurrentLocationLongitude = fractionPartFromFloat(longitude);
                int fractionTicketLocationLongitude = fractionPartFromFloat(lon);
                int fractionCurrentLocationLattitude = fractionPartFromLattitude(latitude);
                int fractionTicketLocationLattitude = fractionPartFromLattitude(lat);

                int diff = Math.abs(fractionCurrentLocationLongitude - fractionTicketLocationLongitude);
                int diff_ = Math.abs(fractionCurrentLocationLattitude - fractionTicketLocationLattitude);

                if (diff <=200 && diff_<=200){
                    nearByLocationtList.add(contactList.get(i));
                }
            }
        }
        if (nearByLocationtList.size() == 0) {

            noResult.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noResult.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            mAdapter = new RVAdapter(getApplicationContext(), nearByLocationtList);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }

    }
});


        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCancelSearchAndSort.setVisibility(View.VISIBLE);
//                                showSoftKeyboard(view);

                searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                searchView.requestFocus();
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(searchView, InputMethodManager.SHOW_FORCED);

                return false;
            }
        });
    }

    public int fractionPartFromFloat(double value){
        String s = Double.toString(value);
        String[] componets = s.split("\\.");
        String fractionPartString = componets[1];
        return Integer.parseInt(fractionPartString);
    }

    public int fractionPartFromLattitude(double value){
        String s = Double.toString(value);
        String[] componets = s.split("\\.");
        String fractionPartString = componets[1];
        return Integer.parseInt(fractionPartString);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    private void displayDeviceData() {
        searchView.setInputType(InputType.TYPE_NULL);
        try {
            contactList = viewTaskList();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (contactList != null) {
            recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new RVAdapter(this, contactList);
            mAdapter.notifyDataSetChanged();
            mAddEvent.setVisibility(View.GONE);
            mSearchLL.setVisibility(View.VISIBLE);
            mDueDate.setVisibility(View.VISIBLE);
            mCompleted.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(mAdapter);
            setSwipeForRecyclerView();
            addTextListener(contactList);
        }
    }


    public void addTextListener(final ArrayList<TaskClass> contactList1) {

        searchView.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence query, int start, int before, int count) {

                query = query.toString().toLowerCase();

                final ArrayList<TaskClass> filteredList = new ArrayList<>();

                for (int i = 0; i < contactList1.size(); i++) {

                    final String text = contactList1.get(i).getName().toLowerCase();
                    final String text1 = contactList1.get(i).getDescription().toLowerCase();
                    if (text.contains(query) || text1.contains(query)) {

                        filteredList.add(contactList1.get(i));
                    }
                }
                if (filteredList.size() == 0) {

                    noResult.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noResult.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mAdapter = new RVAdapter(getApplicationContext(), filteredList);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();  // data set changed
//                    hideSoftKeyboard();
                }
            }
        });
        mCancelSearchAndSort.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                mCancelSearchAndSort.setVisibility(View.INVISIBLE);
                noCompletedTask.setVisibility(View.INVISIBLE);
                hideSoftKeyboard();
                searchView.setText("");
                noResult.setVisibility(View.GONE);
                if (isCloudClicked == true) {

                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mAdapter = new RVAdapter(getApplicationContext(), MainActivity.this.contactList);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mDueDate.setOnClickListener(new View.OnClickListener() {
            final ArrayList<TaskClass> dueDateList = new ArrayList<>();

            @Override
            public void onClick(View view) {
                mCancelSearchAndSort.setVisibility(View.VISIBLE);
                noCompletedTask.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);

                dueDateList.clear();
                dueDateList.addAll(MainActivity.this.contactList);


               /* Calendar cal = new GregorianCalendar();
                cal.set(2015, 4, 24, 14, 00);
                Date dt1 = cal.getTime();

                cal = new GregorianCalendar();
                cal.set(2015, 4, 24, 9, 00);
                Date dt2 = cal.getTime();

                cal = new GregorianCalendar();
                cal.set(2015, 4, 25, 15, 00);
                Date dt3 = cal.getTime();*/

                Collections.sort(dueDateList, new Comparator<TaskClass>() {
                    @Override
                    public int compare(TaskClass object1, TaskClass object2) {
                        return (int) (object1.getDate().compareTo(object2.getDate()));
                    }
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter = new RVAdapter(getApplicationContext(), dueDateList);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

            }

        });

        mCompleted.setOnClickListener(new View.OnClickListener() {

            final ArrayList<TaskClass> completedList = new ArrayList<>();

            @Override
            public void onClick(View view) {
                mCancelSearchAndSort.setVisibility(View.VISIBLE);

                completedList.clear();
                for (int i = 0; i < MainActivity.this.contactList.size(); i++) {
                    if (MainActivity.this.contactList.get(i).getIsCompleted() == null) {

                    } else {

                        Log.v("date", "" + MainActivity.this.contactList.get(i).getIsCompleted());

                        completedList.add(MainActivity.this.contactList.get(i));

                    }

                   /* Calendar cal = new GregorianCalendar();
                    cal.set(2015, 4, 24, 14, 00);
                    Date dt1 = cal.getTime();

                    cal = new GregorianCalendar();
                    cal.set(2015, 4, 24, 9, 00);
                    Date dt2 = cal.getTime();

                    cal = new GregorianCalendar();
                    cal.set(2015, 4, 25, 15, 00);
                    Date dt3 = cal.getTime();*/

                    Collections.sort(completedList, new Comparator<TaskClass>() {
                        @Override
                        public int compare(TaskClass object1, TaskClass object2) {
                            return (int) (object1.getIsCompleted().compareTo(object2.getIsCompleted()));
                        }
                    });
                }
                if (completedList.size() == 0) {
                    noCompletedTask.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                } else {
                    noCompletedTask.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mAdapter = new RVAdapter(getApplicationContext(), completedList);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();  // data set changed
                }
            }
        });
    }


    private void setSwipeForRecyclerView() {

        SwipeUtil swipeHelper = new SwipeUtil(0, ItemTouchHelper.LEFT, this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPosition = viewHolder.getAdapterPosition();
                RVAdapter adapter = (RVAdapter) recyclerView.getAdapter();
                adapter.pendingRemoval(swipedPosition);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                RVAdapter adapter = (RVAdapter) recyclerView.getAdapter();
                if (adapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(swipeHelper);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        //set swipe label


        swipeHelper.setLeftSwipeLable("");
        //set swipe background-Color
//        swipeHelper.setLeftcolorCode(ContextCompat.getColor(this, R.color.yellow));
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            String name = data.getStringExtra("name");
//            String description = data.getStringExtra("description");
//            String date = data.getStringExtra("date");
//            String time = data.getStringExtra("time");

//            TaskClass task = new TaskClass();
//            task.setName(name);
//            task.setDescription(description);
//            task.setDate(date);
//            task.setTime(time);

//           contactList= viewTaskList1();

//            contactList.add(task);
//            Log.d("listsize", "listsize " + contactList.size());
//            mAdapter.notifyDataSetChanged();
//            mAddEvent.setVisibility(View.GONE);

//        } else
       /* if (requestCode == 2 && TaskDetailActivity.deleteClicked == true) {
            Log.v("zzz", "dddddddddddd");

            Integer position = data.getIntExtra("u_position", 0);
            mAdapter.remove(position);
            mAdapter.notifyDataSetChanged();
        }*/


//        if (requestCode == 2) {
//            String name = data.getStringExtra("u_name");
//            Log.v("zzz", name);
//            String description = data.getStringExtra("u_description");
//            String date = data.getStringExtra("u_date");
//            String time = data.getStringExtra("u_time");
//            String status = data.getStringExtra("u_statuslkklkl");
//            Toast.makeText(this, "" + status, Toast.LENGTH_SHORT).show();


//            Integer position = data.getIntExtra("u_position", 0);
//            Boolean status_ = data.getExtras().getBoolean("status.");

//            Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "" + status_, Toast.LENGTH_SHORT).show();


//            TaskClass task = new TaskClass();
//            task.setName(name);
//            task.setDescription(description);
//            task.setDate(date);
//            task.setTime(time);
//            Log.v("status", status.toString());

//            if (status.equals("Completed")) {
//                Log.v("status", status.toString());

//                task.setIsCompleted(getCompletedDate());

//            } else if (status.equals("Pending")) {

//                task.setIsCompleted(null);

//            }
//            contactList.set(position, task);
//
//            mAdapter.notifyDataSetChanged();
//        }

//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        addTaskItem = menu.findItem(R.id.add_button);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.add_button:
                intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                startActivityForResult(intent, 1);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void showText() {
        mAddEvent.setVisibility(View.VISIBLE);
        mSearchLL.setVisibility(View.GONE);
    }


    public ArrayList<TaskClass> viewTaskList() throws ParseException {
        ArrayList<TaskClass> TaskList = new ArrayList<TaskClass>();
        RealmResults<TaskClass> results = realm.where(TaskClass.class).findAll();

        for (TaskClass task : results) {
            task.getName();
            task.getDescription();
            task.getDate();
            task.getTime();
            task.getLocation();
            TaskList.add(task);
        }
        return TaskList;

    }

/*

    ArrayList<TaskClass> TaskList = new ArrayList<TaskClass>();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class)
                .equalTo("name", "prajna")
                .or()
                .equalTo("name", "Peter")
                .NotEqualTo("name", "Peter")
                .findAll();
                    .in("name", new String[]{"Meeting", "William", "Trillian"})

      Date  date1 = new SimpleDateFormat("dd/MM/yyyy").parse("2/6/2017");
      Date  date2 = new SimpleDateFormat("dd/MM/yyyy").parse("3/6/2017");

        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).between("date",date1,date2).findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).greaterThan("date",date1).findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).lessThan("date",date1).findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).greaterThanOrEqualTo("date",date1).findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).lessThanOrEqualTo("date",date1).findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).endsWith("name","ting").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).like("name","Meeting").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).like("name", "?ill*").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).like("name", "*in?").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).isEmpty("name").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).isNotEmpty("name").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).isNull("name").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).isNotNull("name").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).equalTo("name","y").or().equalTo("name","t").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).not().equalTo("name","y").or().equalTo("name","t").findAll();
        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).findAll();
        result2 = result2.sort("date"); // Sort ascending
        result2 = result2.sort("date", Sort.DESCENDING);

        RealmResults<TaskClass> result2 = realm.where(TaskClass.class).distinct("name");
        for (TaskClass task : result2) {
        task.getName();
        task.getDescription();
        task.getDate();
        task.getTime();
        TaskList.add(task);
    }

        return TaskList;}
*/


    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    private class TaskListAsyncTask extends AsyncTask<Void, String, String> {
        private ArrayList<TaskClass> cloudList = new ArrayList<>();

        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Loading");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";
            String Urls = "http://192.168.2.7:3006/user/nagesh.chandan@mobinius.com/projects";

            //create an object of Httpclient
            HttpClient httpclient = new DefaultHttpClient();
            //create an object of Httppost
            HttpPost httpPost = new HttpPost(Urls);
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            try {
                //Add POST parameters

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deviceType", "android");

                //Encode POST data
                StringEntity se = new StringEntity(jsonObject.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);
                // httpPost.setEntity(jsonObject));
                //make HTTPPOST request
                httpResponse = httpclient.execute(httpPost);

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                httpEntity = httpResponse.getEntity();
                try {
                    response = EntityUtils.toString(httpEntity);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.v("response", response.toString());
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            progressDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject projects = jsonObject.getJSONObject("projects");

                ArrayList<String> projectIdList = new ArrayList<>();
                projectIdList.add("com_edcontrols_48a4ffaf-6d07-4d8f-e3ab-de98d13ecc82");
                projectIdList.add("com_imtech_zuidholland_dvs_zuidholland");
                projectIdList.add("com_strukton_middenmeer_ams05");
                projectIdList.add("nl_isolectra_4f6a2aa8-2dfc-e98a-105d-cf8bc594bf1c");
                projectIdList.add("nl_montfortbouw_f907f35a-712e-78b7-07c5-55dc0eab2719");
                projectIdList.add("de_vanwijnen_2f2a5f1b-35ce-a3b4-de42-af080f2bb36c");
                projectIdList.add("de_geigergruppe_1060e866-a98c-8184-0bb1-1508cc5f08fb");
                projectIdList.add("com_mobinius_72576fa4-f401-29b2-4785-266b69090f60");
                projectIdList.add("grace_com_mobinius_ef0c258f-3c6a-da54-0ba1-f0d61c68eca4");
                projectIdList.add("com_volkerwessels_emmen_yorneo_43114");
                projectIdList.add("nl_waal_utrecht_020082_johanna_printtest");

                for (int i = 0; i < projectIdList.size(); i++) {
                    TaskClass t = new TaskClass();

                    JSONObject projectId = projects.getJSONObject(projectIdList.get(i));
                    String projecName = projectId.getString("projectName");
                    String location = projectId.getString("location");
                    String image = projectId.getString("thumbImage");
                    t.setName(projecName);
                    t.setDescription(location);
                    t.setImage(image);
                    cloudList.add(t);
                    mSearchLL.setVisibility(View.VISIBLE);
                    mDueDate.setVisibility(View.GONE);
                    mCompleted.setVisibility(View.GONE);
                    mCancelSearchAndSort.setVisibility(View.INVISIBLE);
//                    mSearchViewCloud.setVisibility(View.VISIBLE);
                    addTextListener(cloudList);
                }

                recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
                mAddEvent.setVisibility(View.INVISIBLE);
                mAdapter = new RVAdapter(MainActivity.this, cloudList);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(mAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}