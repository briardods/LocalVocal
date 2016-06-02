package com.example.briar.localvocal;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AndroidLocationDemoActivity extends Activity
        implements LocationListener, OnClickListener {
    private static final String UPDATES_BUNDLE_KEY
            = "WantsLocationUpdates";
    private Button toggleButton; // toggles whether GPS started/stopped
    private Button btnSend;
    private Button btnSound;
    private TextView locationTextView;
    private TextView tvCity;
    private boolean wantLocationUpdates;
    public static final String JSON_URL = "http://raptor2.aut.ac.nz:8080/LocalVocalServer/LocationServlet?";

    private Location currentLocation;

    private ImageView ivCity;
    private byte[] audioOfCity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_location_demo);
        toggleButton = (Button) findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(this);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnSend.setEnabled(false);
        btnSound = (Button) findViewById(R.id.btnSound);
        btnSound.setOnClickListener(this);
        btnSound.setEnabled(false);

        locationTextView = (TextView) findViewById(R.id.location_textview);
        tvCity = (TextView) findViewById(R.id.tvCityInfo);

        ivCity = (ImageView) findViewById(R.id.ivCity);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(UPDATES_BUNDLE_KEY))
            wantLocationUpdates
                    = savedInstanceState.getBoolean(UPDATES_BUNDLE_KEY);
        else // activity is not being reinitialized from prior start
            wantLocationUpdates = false;
    }

    private void startGPS() {
        LocationManager locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);
        Location lastKnownLocation
                = locationManager.getLastKnownLocation(provider);
        if (lastKnownLocation != null)
            locationTextView.setText(getLocationInfo(lastKnownLocation));
        toggleButton.setText(R.string.button_stop);
    }

    private void stopGPS() {
        LocationManager locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
        toggleButton.setText(R.string.button_start);
    }

    /** Called when the activity is started. */
    @Override
    public void onStart()
    {  super.onStart();
        if (wantLocationUpdates)
            startGPS();
    }

    /** Called when the activity is stopped. */
    @Override
    public void onStop()
    {  super.onStop();
        // stop location updates while the activity is stopped
        stopGPS();
    }

    /** Called when activity is about to be killed to save app state */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {  super.onSaveInstanceState(outState);
        outState.putBoolean(UPDATES_BUNDLE_KEY, wantLocationUpdates);
    }

    // implementation of OnClickListener method
    public void onClick(View view)
    {
        if (view == toggleButton)
        {
            if (wantLocationUpdates)
            {  wantLocationUpdates = false;
                stopGPS();
            }
            else
            {  wantLocationUpdates = true;
                startGPS();
            }
        }
        else if (view == btnSend) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
//          String servletURL = "http://fanyi.youdao.com/openapi.do?keyfrom=testHttpGet&key=850021564&type=data&doctype=xml&version=1.1&q=good";
//            String servletURL = "http://192.168.31.102:8080/LocalVocal/Location?l=-45.88&le=170.48";
            String servletURL = JSON_URL + "lat="
                    + String.valueOf(latitude) + "&lon=" + String.valueOf(longitude);
            try {
                URL url = new URL(servletURL);
                AsyncTask<URL, Void, String> backgroundTask
                        = new HttpCommunicator();
                backgroundTask.execute(url);
            } catch (MalformedURLException e) {
                Toast toast = Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if (view == btnSound){
            playMp3(audioOfCity);
        }
    }

    private String getLocationInfo(Location location){
        String locationInfo = "Lat: " + String.valueOf(location.getLatitude())
                +" Lon: "+String.valueOf(location.getLongitude());
        return locationInfo;
    }

    // implementation of onLocationChanged method
    public void onLocationChanged(Location location)
    {
        locationTextView.setText(getLocationInfo(location));
        Log.w(AndroidLocationDemoActivity.class.getName(),
                "Location: " + location);
        currentLocation = location;
        btnSend.setEnabled(true);
    }

    // implementation of onProviderDisabled method
    public void onProviderDisabled(String provider)
    {
//        locationTextView.setText(R.string.provider_disabled);
        btnSend.setEnabled(false);
    }

    // implementation of onProviderEnabled method
    public void onProviderEnabled(String provider)
    {
//        locationTextView.setText(R.string.provider_enabled);
    }

    // implementation of onStatusChanged method
    public void onStatusChanged(String provider, int status,
                                Bundle extras)
    {
//        locationTextView.setText(R.string.provider_status_changed);
    }


    // inner class that handles HTTP in a separate thread and then
// updates the user interface in the UI thread
    private class HttpCommunicator extends AsyncTask<URL, Void, String>
    {
        // method executed for task in new thread
        @Override
        protected String doInBackground(URL... urls)
        {	 HttpURLConnection conn = null;
            if (urls == null || urls.length == 0)
                return "No URL specified";
            URL url = urls[0];
            StringBuilder stringBuilder = new StringBuilder();
            try
            {  conn = (HttpURLConnection) url.openConnection();
//                Document doc = parseXML(conn.getInputStream());

                BufferedReader br = new BufferedReader
                        (new InputStreamReader(conn.getInputStream()));
                String line = br.readLine();
                while (line != null)
                {  stringBuilder.append(line);
                    line = br.readLine();
                }
                br.close();
//                parseJson(stringBuilder.toString());
//                AuthMsg msg = new Gson().fromJson(stringBuilder.toString(), AuthMsg.class);
            }
            catch (MalformedURLException e)
            {  stringBuilder.append("malformedURLException: " + e);
            }
            catch (IOException e)
            {  stringBuilder.append("IOException: " + e);
            }
            catch(Exception ex)
            {
                stringBuilder.append("IOException: " + ex);
            }
            finally
            {  if (conn != null)
                conn.disconnect();
            }
            return stringBuilder.toString();
        }

        @Override
        // method executed in UI thread once task completed
        protected void onPostExecute(String response)
        {
            parseJson(response);
//            // if necessary discard the oldest response
//            if (serverResponseList.size() >= MAX_RESPONSE_DISPLAY)
//                serverResponseList.remove(0);
//            serverResponseList.add(response);
//            listAdapter.notifyDataSetChanged();
        }
    }
    private void parseJson(String jsonString){
        try {
            JSONObject jsonCity = new JSONObject(jsonString);
            String city = jsonCity.getString("city");
            tvCity.setText(city);

            final byte[] imageData = Base64.decode(jsonCity.getString("image"), Base64.DEFAULT);
            Bitmap bitmap =  BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            ivCity.setImageBitmap(bitmap);
            audioOfCity = Base64.decode(jsonCity.getString("audio"), Base64.DEFAULT);
            btnSound.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
            tvCity.setText(e.getMessage());
        }
    }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("city", "m4a", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // Tried reusing instance of media player
            // but that resulted in system crashes...
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
}
