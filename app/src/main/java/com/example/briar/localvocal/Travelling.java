package com.example.briar.localvocal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Travelling extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    private List<String> phoneNumbers;
    private List<Destination> destinations;
    public static final String JSON_URL = "http://raptor2.aut.ac.nz:8080/LocalVocalServer/LocationList";
    public static final String JSON_LOCATION_URL = "http://raptor2.aut.ac.nz:8080/LocalVocalServer/Location?city=";
    private int proximity;
    private String destinationJSONString;
    private JSONObject destinationDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_travelling);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        sendRequest();

        proximity = 10000;
        destinations = destinationList();
        phoneNumbers = getIntent().getStringArrayListExtra("phone_numbers");
        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000)        // 3 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in millisecond
    }

    private List<Destination> destinationList() {
        List<Destination> locs = new ArrayList<>();

        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                FeedReaderDbHelper.FeedEntry.COLUMN_NAME_CITY,
                FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LAT,
                FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LONG

        };

        Cursor c = db.query(
                FeedReaderDbHelper.FeedEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        while (c.moveToNext()) {
            Double latitude = Double.valueOf(c.getString(c.getColumnIndexOrThrow("latitude")));
            Double longitude = Double.valueOf(c.getString(c.getColumnIndexOrThrow("longitude")));
            String city = c.getString(c.getColumnIndexOrThrow("city"));

            Destination dest = new Destination(latitude, longitude, city);
            locs.add(dest);
            Log.e("destination example: ", latitude + ", " + longitude + ", " + city);
        }
        return locs;
    }

    private void sendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        saveJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Travelling.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(Travelling.this);
        requestQueue.add(stringRequest);
    }

    private void sendLocationDetailRequest(String city) {
        //final JSONArray[] local = new JSONArray[1];
        String params = JSON_LOCATION_URL + city;
//        StringRequest n = new StringRequest()
        StringRequest stringRequest = new StringRequest(Request.Method.POST, params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        saveJSONString(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Travelling.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(Travelling.this);
        requestQueue.add(stringRequest);
    }


    private void saveJSONString(String response) {
        destinationJSONString = response;
    }


    private void saveJSON(String json) {
        ParseJsonArray pj = new ParseJsonArray(json);
        pj.parseJsonArray(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.d("is connecting: ", String.valueOf(mGoogleApiClient.isConnecting()));
            Log.d("is connected: ", String.valueOf(mGoogleApiClient.isConnected()));
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("is connected: ", String.valueOf(mGoogleApiClient.isConnected()));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (mLastLocation != null) {
            newLocation(mLastLocation);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void newLocation(Location location) {
        double lat = location.getLatitude(), lon = location.getLongitude();
        allLocations(lat, lon);
    }

    public void allLocations(double lat, double lon) {
        for (Destination dest : destinations) {
            float[] result = new float[2];
            Location.distanceBetween(lat, lon, dest.getLatitude(), dest.getLongitude(), result);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            1);
                }
            }
            if (result[0] < proximity) {
                //send message
                sendLocationDetailRequest(String.valueOf(dest.getCity()));
                Log.d("Welcome to ", dest.getCity());

                if (destinationJSONString != null) {
                    try {
                        JSONObject destDetail = new JSONObject(destinationJSONString);
                        Log.e("Dest detail: ", destDetail.getString("city"));
                        Log.e("Dest detail: ", destDetail.getString("population"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                for (String phoneNumber : phoneNumbers) {
                    String smsBody = "Welcome to " + dest.getCity();
                    SmsManager smsManager = SmsManager.getDefault();
                    // Send a text based SMS
                    smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);

                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Connection error", " failure");
    }


    @Override
    public void onLocationChanged(Location location) {
        newLocation(location);
    }
}
