package com.thesis.gabilloillo.sensorapp;

import android.Manifest;
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

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "SensorAppPrefs";
    private String emergeny_number = "";
    private String caller_number = "";
    String system_url ="http://www.google.com";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize Request system to comunicate with the sensor network
        RequestQueue queue = Volley.newRequestQueue(this);
        // Initialize view items references
        final TextView location_stream = (TextView) findViewById(R.id.location_stream);
        final EditText caller_et =(EditText)findViewById(R.id.caller_number);
        final EditText emergency_et =(EditText)findViewById(R.id.emergency_number);
        Button save_numbers = (Button)findViewById(R.id.save_numbers);
        // Initialize location system
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Shared preferences for number update
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Initialize view items values if they exists
        caller_number = settings.getString("user_phone", "");
        emergeny_number = settings.getString("emergency_phone", "");
        Log.d("caller_check", "c_check:" + caller_number);
        Log.d("emergency_check", "em_check:" + emergeny_number);
        Log.d("halp", "halp!");

        // Initialize caller and emergency numbers in the view
        if(!caller_number.equals("")) {
            caller_et.setText(caller_number);
        }
        emergency_et.setText(emergeny_number);

        // Save numbers button listener
        save_numbers.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String caller_aux = String.valueOf(caller_et.getText());
                String emergency_aux = String.valueOf(emergency_et.getText());
                Log.d("caller_aux", caller_aux);
                // Modify vars, views and settings
                if(!caller_aux.equals("") && !caller_aux.equals(caller_number)) {
                    caller_number = caller_aux;
                    settings.edit().putString("user_phone", caller_number).apply();
                    caller_et.setText(caller_number);
                }

                Log.d("emergency_aux", emergency_aux);
                if(!emergency_aux.equals("") && !emergency_aux.equals(emergeny_number)) {
                    emergeny_number = emergency_aux;
                    settings.edit().putString("emergency_phone", emergeny_number).apply();
                    emergency_et.setText(emergeny_number);
                }
            }
        });

        /**
         ***************** Location Related **************************
         */
        // Location Permision initialization for fine_location (gps and network, > coarse_location, only network)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1234);
            return;
        }

        Location location = mLocationManager.getLastKnownLocation(mLocationManager.
                getBestProvider(new Criteria(), true));

        if(location != null) {
            Log.d("loc/once", location.toString());
        }

        // Location listener
        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                location_stream.setText(
                    "lat: " +location.getLatitude() + ", long: " + location.getLongitude()
                );
                Log.d("locChange", location.toString());
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

        /*
        ********************** Emergency Call Related ****************************
         */
        // Emergency button click action
        FloatingActionButton emergency_call = (FloatingActionButton) findViewById(R.id.emergency_call);
        emergency_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show actions description to user
                Snackbar.make(view, "Calling the emergency number and sending location and nearby sensors info",
                        Snackbar.LENGTH_LONG).setAction("", null).show();
                // Start emergency call
                Intent callIntent = new Intent(Intent.ACTION_CALL); //use ACTION_CALL class
                if(!emergeny_number.equals("")) {
                    callIntent.setData(Uri.parse("tel:" + emergeny_number));    //this is the phone number calling
                    //check permission
                    //If the device is running Android 6.0 (API level 23) and the app's targetSdkVersion is 23 or higher,
                    //the system asks the user to grant approval.
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        //request permission from user if the app hasn't got the required permission
                        ActivityCompat.requestPermissions(getParent(),
                                new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                                10);
                        return;
                    } else {     //have got permission
                        try {
                            startActivity(callIntent);  //call activity and make phone call
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getApplicationContext(),
                                "SensorApp MainActivity was not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Please, enter an emergency number.",
                        Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
        ********************* Request Related **********************
         */
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, system_url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    Log.d("response","Response is: "+ response.substring(0,50));
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("responserr","That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
