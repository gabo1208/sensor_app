package com.thesis.gabilloillo.sensorapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "SensorAppPrefs";
    private String emergency_number = "";
    private String caller_number = "";
    private String system_url_root ="http://www.google.com";
    private String system_url_endpoint ="/";
    private String sensor_iri ="";
    private String name = "";
    private TextView location_stream;
    private TextView alerts_stream;
    private Boolean streaming = false;

    // Initialize Request system to comunicate with the sensor network
    private RequestQueue queue;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize view items references
        location_stream = (TextView) findViewById(R.id.location_stream);
        alerts_stream = (TextView) findViewById(R.id.alerts_stream);
        queue = Volley.newRequestQueue(getApplicationContext());
        final EditText caller_et =(EditText)findViewById(R.id.caller_number);
        final EditText emergency_et =(EditText)findViewById(R.id.emergency_number);
        final EditText system_et =(EditText)findViewById(R.id.system_url);
        final EditText endpoint_et =(EditText)findViewById(R.id.endpoint_url);
        final EditText sensor_et =(EditText)findViewById(R.id.sensor_iri);
        final EditText name_et =(EditText)findViewById(R.id.caller_name);
        Button save_numbers = (Button)findViewById(R.id.save_numbers);
        final Button stream_data = (Button)findViewById(R.id.stream_measures);

        // Shared preferences for number update
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Initialize view items values if they exists
        caller_number = settings.getString("user_phone", "");
        emergency_number = settings.getString("emergency_phone", "");
        system_url_root = settings.getString("system_url_root", "");
        system_url_endpoint = settings.getString("system_url_endpoint", "");
        sensor_iri = settings.getString("sensor_iri", "");
        name = settings.getString("name", "");
        Log.d("caller_check", "c_check:" + caller_number);
        Log.d("emergency_check", "em_check:" + emergency_number);
        Log.d("endpoint_check", "em_check:" + system_url_root);
        Log.d("endpoint_check", "em_check:" + system_url_endpoint);
        Log.d("name_check", "em_check:" + name);

        // Initialize caller and emergency numbers in the view
        if(!caller_number.equals("")) {
            caller_et.setText(caller_number);
        }
        if(!system_url_root.equals("")) {
            system_et.setText(system_url_root);
        }
        if(!system_url_endpoint.equals("")) {
            endpoint_et.setText(system_url_endpoint);
        }
        if(!sensor_iri.equals("")) {
            sensor_et.setText(sensor_iri);
        }
        if(!name.equals("")) {
            name_et.setText(name);
        }
        emergency_et.setText(emergency_number);

        // Save numbers button listener
        save_numbers.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String caller_aux = String.valueOf(caller_et.getText());
                String emergency_aux = String.valueOf(emergency_et.getText());
                String systemurl_aux = String.valueOf(system_et.getText());
                String systemendpoint_aux = String.valueOf(endpoint_et.getText());
                String sensoruri_aux = String.valueOf(sensor_et.getText());
                String name_aux = String.valueOf(name_et.getText());
                Boolean empty = false;
                String user_feedback = "Data Saved";

                // Modify vars, views and settings
                Log.d("caller_aux", caller_aux);
                if(!caller_aux.equals("")) {
                    if(!caller_aux.equals(caller_number)) {
                        caller_number = caller_aux;
                        settings.edit().putString("user_phone", caller_number).apply();
                        caller_et.setText(caller_number);
                    }
                }else{
                    empty = true;
                }

                Log.d("emergency_aux", emergency_aux);
                if(!emergency_aux.equals("")) {
                    if(!emergency_aux.equals(emergency_number)) {
                        emergency_number = emergency_aux;
                        settings.edit().putString("emergency_phone", emergency_number).apply();
                        emergency_et.setText(emergency_number);
                    }
                }else{
                    empty = true;
                }

                Log.d("systemurl_aux", systemurl_aux);
                if(!systemurl_aux.equals("")) {
                    if(!systemurl_aux.equals(system_url_root)) {
                        system_url_root = systemurl_aux;
                        settings.edit().putString("system_url_root", system_url_root).apply();
                        system_et.setText(system_url_root);
                    }
                }else{
                    empty = true;
                }

                if(!systemendpoint_aux.equals("")) {
                    if(!systemendpoint_aux.equals(system_url_endpoint)) {
                        system_url_endpoint = systemendpoint_aux;
                        settings.edit().putString("system_url_endpoint", system_url_endpoint).apply();
                        endpoint_et.setText(system_url_endpoint);
                    }
                }else{
                    empty = true;
                }

                Log.d("sensoruri_aux", sensoruri_aux);
                if(!sensoruri_aux.equals("")) {
                    if(!sensoruri_aux.equals(sensor_iri)) {
                        sensor_iri = sensoruri_aux;
                        settings.edit().putString("sensor_iri", sensor_iri).apply();
                        sensor_et.setText(sensor_iri);
                    }
                }else{
                    empty = true;
                }

                Log.d("name_aux", name_aux);
                if(!name_aux.equals("")) {
                    if(!name_aux.equals(name)) {
                        name = name_aux;
                        settings.edit().putString("name", name).apply();
                        name_et.setText(name);
                    }
                }else{
                    empty = true;
                }

                if(empty){
                    user_feedback += ", please fill all the items";
                }
                Toast.makeText(getApplicationContext(), user_feedback,
                        Toast.LENGTH_LONG).show();
            }
        });

        // Data Streaming button listener
        stream_data.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                streaming = !streaming;
                if(streaming) {
                    stream_data.setText("Stop data Streaming");
                    queue.start();
                    Toast.makeText(getApplicationContext(), "Streaming location and data to Sensor Network.",
                            Toast.LENGTH_LONG).show();
                }else{
                    stream_data.setText("Stream data");
                    Toast.makeText(getApplicationContext(), "Stoped streaming.",
                            Toast.LENGTH_LONG).show();
                }
            }

        });

        if(checkAndRequestPermissions()) {
            // Location related function
            locationRelated(this, location_stream);
            callRelated(this);
        }else{
            alerts_stream.setText("Error on permissions.");
        }

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***
     * Check all needed Permissions
     */
    private  boolean checkAndRequestPermissions() {
        int permissionCallPhone = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        int locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCallPhone != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(
                            new String[listPermissionsNeeded.size()]),1234
            );
            return false;
        }
        return true;
    }

    /**
     ***************** Location Related **************************
     */
    @SuppressLint("MissingPermission")
    private void locationRelated(Context context, final TextView location_stream) {
        // Initialize location system
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(mLocationManager.
            getBestProvider(new Criteria(), true));

        if (location != null) {
            Log.d("loc/once", location.toString());
        }

        // Location listener
        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                location_stream.setText(
                        "lat: " + location.getLatitude() + ", long: " + location.getLongitude()
                );
                //Log.d("locChange", location.toString());
                if(streaming){
                    Map<String, String> postParams = new HashMap<String, String>();
                    postParams.put("lat", String.valueOf(location.getLatitude()));
                    postParams.put("lon", String.valueOf(location.getLongitude()));
                    if(!system_url_root.equals("")) {
                        requestRelated(getApplicationContext(), queue, postParams, "location");
                    }else{
                        streaming = false;
                        Toast.makeText(getApplicationContext(),
                                "Please check the System url", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (!alerts_stream.getText().equals("")) {
                        alerts_stream.setText("");
                        queue.cancelAll("location");
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("statChange", String.valueOf(status) + " " + extras.toString());
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("provEnabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("provDisabled", provider);
            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                0.0f, mLocationListener);
    }

    /*
     ********************** Emergency Call Related ****************************
     */
    @SuppressLint("MissingPermission")
    private void callRelated(Context context){
        // Emergency button click action
        FloatingActionButton emergency_call = (FloatingActionButton) findViewById(R.id.emergency_call);
        emergency_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start emergency call
                Intent callIntent = new Intent(Intent.ACTION_CALL); //use ACTION_CALL class
                if (!emergency_number.equals("")) {
                    // Show actions description to user
                    Snackbar.make(view, "Calling the emergency number and sending location and nearby sensors info",
                            Snackbar.LENGTH_LONG).setAction("", null).show();
                    callIntent.setData(Uri.parse("tel:" + emergency_number));    //this is the phone number calling
                    try {
                        startActivity(callIntent);  //call activity and make phone call
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(),
                                "Something went wrong on emergency call", Toast.LENGTH_SHORT).show();
                        Log.d("error_emer", "SensorApp MainActivity was not found");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please, enter an emergency number.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     ********************* Requests Related **********************
     */
    // Request a string response from the provided URL
    private void requestRelated(Context context, RequestQueue queue, final Map<String, String> postParams,
                                String tag) {
        StringRequest snrequest = new StringRequest(Request.Method.POST, system_url_root +
                system_url_endpoint + sensor_iri,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                Log.d("response", "Response is: " + response);
                alerts_stream.setText("Comunicating with Sensor Network... " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("responserr", "That didn't work!");
                alerts_stream.setText("Sensor net response error " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = postParams;
                return params;
            }
        };

        // Add the request to the RequestQueue.
        snrequest.setTag(tag);
        queue.add(snrequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Boolean error = false;

        switch (requestCode) {
            case 1234 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationRelated(this, location_stream);
                } else {
                    error = true;
                    Toast.makeText(getApplicationContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                if (grantResults.length > 1
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    callRelated(this);
                } else {
                    error = true;
                    Toast.makeText(getApplicationContext(), "Call permission denied", Toast.LENGTH_SHORT).show();
                }
                if(error) {
                    Toast.makeText(getApplicationContext(), "SensorApp Need its Permissions to work properly!.",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
