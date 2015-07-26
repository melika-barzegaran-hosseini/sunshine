package app.com.example.android.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase
{
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void setUp()
    {
        deleteTheDatabase();
    }

    void deleteTheDatabase()
    {
        this.getContext().deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testCreateDb() throws Throwable
    {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        SQLiteDatabase db = new WeatherDbHelper(this.getContext()).getWritableDatabase();
        assertTrue("ERROR: The database is not open.", db.isOpen() == true);

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                cursor.moveToFirst());

        do
        {
            tableNameHashSet.remove(cursor.getString(0));
        }
        while(cursor.moveToNext());
        assertTrue("ERROR: Your database was created without both the location entry and weather " +
                        "entry tables", tableNameHashSet.isEmpty());

        cursor = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("ERROR: This means that we were unable to query the database for table" +
                        "information.", cursor.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        do
        {
            String columnName = cursor.getString(cursor.getColumnIndex("name"));
            locationColumnHashSet.remove(columnName);
        }
        while(cursor.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());

        db.close();
    }

    public void testLocationTable()
    {
        this.insertLocation();
    }

    public long insertLocation()
    {
        SQLiteDatabase database = new WeatherDbHelper(this.getContext()).getWritableDatabase();
        ContentValues values = TestUtilities.createNorthPoleLocationValues();

        long rowId = database.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
        assertTrue("ERROR: That's not a valid row ID.", rowId != -1);

        Cursor cursor = database
                .query(WeatherContract.LocationEntry.TABLE_NAME,
                        null, null, null, null, null, null);
        assertTrue("ERROR: No records are returned from location query.", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("ERROR: Location query validation failed.",
                cursor, values);

        assertFalse("ERROR: More than one record returned from location query.",
                cursor.moveToNext());

        cursor.close();
        database.close();

        return rowId;
    }

    public void testWeatherTable()
    {
        SQLiteDatabase database = new WeatherDbHelper(this.getContext()).getWritableDatabase();

        Long locationRowId = this.insertLocation();
        ContentValues values = TestUtilities.createWeatherValues(locationRowId);

        long rowId = database.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
        assertTrue("ERROR: That's not a valid row ID.", rowId != -1);

        Cursor cursor = database
                .query(WeatherContract.WeatherEntry.TABLE_NAME,
                        null, null, null, null, null, null);
        assertTrue("ERROR: No records are returned from weather query.", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("ERROR: Weather query validation failed.",
                cursor, values);

        assertFalse("ERROR: More than one record returned from weather query.",
                cursor.moveToNext());

        cursor.close();
        database.close();
    }
}