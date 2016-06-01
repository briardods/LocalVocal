package com.example.briar.localvocal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by Briar on 26/05/2016.
 */
public class ParseJsonArray {
    public static String[] cities;
    public static String[] lats;
    public static String[] longs;

    public static final String JSON_ARRAY = "result";
    public static final String KEY_CITY = "city";
    public static final String KEY_LAT = "latitude";
    public static final String KEY_LONG = "longitude";

    private String json;
    private Cursor mCursor;

    public ParseJsonArray(String json) {
        this.json = json;
    }

    private JSONObject jsonObject;

    protected void parseJsonArray(Context context) {

        try {
            jsonObject = new JSONObject(json);
            JSONArray users = jsonObject.getJSONArray(JSON_ARRAY);

            cities = new String[users.length()];
            lats = new String[users.length()];
            longs = new String[users.length()];

            for (int i = 0; i < users.length(); i++) {
                JSONObject jo = users.getJSONObject(i);
                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                //mCursor = db.rawQuery("SELECT city" + " FROM " + FeedReaderDbHelper.FeedEntry.TABLE_NAME + " WHERE city = ?", new String[]{jo.getString(KEY_CITY)});

                String city = jo.getString(KEY_CITY);
                String sql = "SELECT * FROM " + FeedReaderDbHelper.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderDbHelper.FeedEntry.COLUMN_NAME_CITY  + " = ?";
                Cursor cursor = db.rawQuery(sql, new String[]{city});
                if (cursor == null || !cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_CITY, jo.getString(KEY_CITY));
                    values.put(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LAT, (new DecimalFormat("###.000###")).format(new BigDecimal(jo.getString(KEY_LAT))));
                    values.put(FeedReaderDbHelper.FeedEntry.COLUMN_NAME_LONG, (new DecimalFormat("###.000###")).format(new BigDecimal(jo.getString(KEY_LONG))));

                    db.insert(
                            FeedReaderDbHelper.FeedEntry.TABLE_NAME,
                            null,
                            values);
                }
                db.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public JSONObject getJSON(){
        return jsonObject;
    }
}
