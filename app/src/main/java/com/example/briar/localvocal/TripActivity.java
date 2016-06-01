package com.example.briar.localvocal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TripActivity extends AppCompatActivity {
    TextView textView;
    ListView listView;
    private static final int PICK_CONTACT = 1000;
    private List<String> contactList;
    private List<String> contactDetails;
    private ArrayAdapter mAdapter;
    private static final String TAG = TripActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.contact_number);
        listView = (ListView) findViewById(R.id.listView);
        contactList = new ArrayList<>();
        contactDetails = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, R.layout.list_item, contactList);
        listView.setAdapter(mAdapter);
    }

    //Todo when button is clicked
    public void pickAContactNumber(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        String num = getPhoneNumber(data.getData());

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor phone = getContentResolver().query(contactData, null, null, null, null);
                    if (phone.moveToFirst()) {
                        String contactNumberName = phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        //String contactNumber = phone.getString(phone.getColumnIndex(ContactsContract.Contacts));
                        // Todo something when contact number selected

                        contactDetails.add(num);
                        mAdapter.add(contactNumberName + ", " + num);
                    }
                }
                break;
        }
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

    private String getPhoneNumber(Uri data) {
        String contactNumber = null;
        String contactID = null;


        // getting contacts ID
        Cursor cursorID = getContentResolver().query(data,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
//        ContentResolver resolvr = getContentResolver();
//        Cursor cursorPhone = resolvr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        String num = contactNumber;
        cursorPhone.close();
        return num;
    }

    public void startTrip(View view) {
        Intent intent = new Intent(this, Travelling.class);
        intent.putStringArrayListExtra("phone_numbers", (ArrayList<String>) contactDetails);
        startActivity(intent);
    }
}
