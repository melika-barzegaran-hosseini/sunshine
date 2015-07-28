package app.com.example.android.sunshine.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import app.com.example.android.sunshine.utils.PollingCheck;

public class TestUtilities extends AndroidTestCase
{
    static void validateCursor(String error, Cursor cursor, ContentValues expectedValues)
    {
        assertTrue("Empty cursor returned. ERROR: " + error, cursor.moveToFirst());
        validateCurrentRecord(error, cursor, expectedValues);
        cursor.close();
    }

    static void validateCurrentRecord(String error, Cursor cursor, ContentValues expectedValues)
    {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for(Map.Entry<String, Object> entry : valueSet)
        {
            String expectedColumnName = entry.getKey();
            int index = cursor.getColumnIndex(expectedColumnName);
            assertFalse("Column '" + expectedColumnName + "' not found. ERROR: " + error
                    , index == -1);

            String expectedValue = entry.getValue().toString();
            String value = cursor.getString(index);
            assertEquals("Value '" + value + "' did not match the expected value '"
                    + expectedValue + "'. ERROR: " + error, expectedValue, value);
        }
    }

    static ContentValues createNorthPoleLocationValues()
    {
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE, 64.7488);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE, -147.353);

        return values;
    }

    static long insertNorthPoleLocationValues(Context context)
    {
        SQLiteDatabase database = new WeatherDbHelper(context).getWritableDatabase();
        ContentValues values = TestUtilities.createNorthPoleLocationValues();

        long locationRowId = database.insert(WeatherContract.LocationEntry.TABLE_NAME,
                null, values);

        assertTrue("Error: Failure to insert location entry values", locationRowId != -1);

        return locationRowId;
    }

    static ContentValues createWeatherValues(long locationRowId)
    {
        ContentValues values = new ContentValues();

        values.put(WeatherContract.WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);

        values.put(WeatherContract.WeatherEntry.COLUMN_DATE, "SUN, JUL 26");
        values.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, "Asteroids");
        values.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        values.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE, 65);
        values.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE, 75);

        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_DIRECTION, 1.1);

        return values;
    }

    static void insertWeatherValues(Context context, long locationRowId)
    {
        SQLiteDatabase database = new WeatherDbHelper(context).getWritableDatabase();
        ContentValues values = TestUtilities.createWeatherValues(locationRowId);

        long weatherRowId = database.insert(WeatherContract.WeatherEntry.TABLE_NAME,
                null, values);

        assertTrue("ERROR: Failure to insert weather entry values", weatherRowId != -1);
    }


    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}