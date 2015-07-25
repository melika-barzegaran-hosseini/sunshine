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
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)

        // Insert ContentValues into database and get a row ID back

        // Query the database and receive a Cursor back

        // Move the cursor to a valid database row

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }
}