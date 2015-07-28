package app.com.example.android.sunshine.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import app.com.example.android.sunshine.data.WeatherContract.LocationEntry;
import app.com.example.android.sunshine.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase
{
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry()
    {
        PackageManager packageManager = this.getContext().getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(this.getContext().getPackageName(),
                WeatherProvider.class.getName());
        try
        {
            ProviderInfo providerInfo = packageManager.getProviderInfo(componentName, 0);
            assertEquals("ERROR: WeatherProvider registered with authority: " +
                            providerInfo.authority + " instead of authority: " +
                            WeatherContract.CONTENT_AUTHORITY, providerInfo.authority,
                    WeatherContract.CONTENT_AUTHORITY);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            assertTrue("ERROR: WeatherProvider is not registered at " + this.getContext()
                            .getPackageName(), false);
        }
    }

    public void testGetType()
    {
        // content://com.example.android.sunshine.app/weather/
        String type = this.getContext().getContentResolver().getType(WeatherEntry.CONTENT_URI);

        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("ERROR: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = this.getContext().getContentResolver()
                .getType(WeatherEntry.buildWeatherLocation(testLocation));

        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("ERROR: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = this.getContext().getContentResolver()
                .getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));

        // vnd.android.cursor.item/com.example.android.sunshine.app/weather/1419120000
        assertEquals("ERROR: the WeatherEntry CONTENT_URI with location and date should return " +
                        "WeatherEntry.CONTENT_ITEM_TYPE", WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = this.getContext().getContentResolver().getType(LocationEntry.CONTENT_URI);

        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("ERROR: the LocationEntry CONTENT_URI should return " +
                        "LocationEntry.CONTENT_TYPE", LocationEntry.CONTENT_TYPE, type);
    }

    public void testBasicWeatherQuery()
    {
        ContentValues locationValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = TestUtilities.insertNorthPoleLocationValues(this.getContext());

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
        TestUtilities.insertWeatherValues(this.getContext(), locationRowId);

        Cursor weatherCursor = this.getContext().getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicWeatherQuery", weatherCursor, weatherValues);
    }

    public void testBasicLocationQueries()
    {
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = TestUtilities.insertNorthPoleLocationValues(this.getContext());

        Cursor locationCursor = this.getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicLocationQueries", locationCursor, testValues);

        if(Build.VERSION.SDK_INT >= 19)
        {
            assertEquals("ERROR: Location Query did not properly set NotificationUri",
                    locationCursor.getNotificationUri(), LocationEntry.CONTENT_URI);
        }
    }

    public void testInsertReadProvider()
    {
        //------------------------------------------------------------------------------------------
        ContentValues locationValues = TestUtilities.createNorthPoleLocationValues();
        TestUtilities.TestContentObserver testContentObserver
                = TestUtilities.getTestContentObserver();

        getContext().getContentResolver()
                .registerContentObserver(LocationEntry.CONTENT_URI, true, testContentObserver);
        Uri locationUri = getContext().getContentResolver()
                .insert(LocationEntry.CONTENT_URI, locationValues);
        testContentObserver.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(testContentObserver);

        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);

        Cursor cursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. ERROR: LocationEntry is not valid.",
                cursor, locationValues);

        //------------------------------------------------------------------------------------------
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
        testContentObserver = TestUtilities.getTestContentObserver();

        getContext().getContentResolver()
                .registerContentObserver(WeatherEntry.CONTENT_URI, true, testContentObserver);
        Uri weatherInsertUri = getContext().getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        testContentObserver.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(testContentObserver);

        assertTrue(weatherInsertUri != null);

        Cursor weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                weatherCursor, weatherValues);

        //------------------------------------------------------------------------------------------
        weatherValues.putAll(locationValues);
        //------------------------------------------------------------------------------------------
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor
                ("testInsertReadProvider. ERROR: joint of weather and location data is not valid.",
                        weatherCursor, weatherValues);

        //------------------------------------------------------------------------------------------
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate
                        (TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor
                ("testInsertReadProvider. ERROR: joint of weather and location data with start " +
                                "date is not valid.", weatherCursor, weatherValues);

        //------------------------------------------------------------------------------------------
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate
                        (TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  ERROR: joint of weather and " +
                        "location data for a specific date is not valid.",
                weatherCursor, weatherValues);
    }

    public void testUpdateLocation()
    {
        ContentValues values = TestUtilities.createNorthPoleLocationValues();

        Uri locationUri = getContext().getContentResolver()
                .insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        Cursor locationCursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.TestContentObserver testContentObserver =
                TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(testContentObserver);

        int count = getContext().getContentResolver().update(
                LocationEntry.CONTENT_URI,
                updatedValues,
                LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)}
        );

        assertEquals(count, 1);

        testContentObserver.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(testContentObserver);
        locationCursor.close();

        Cursor cursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                LocationEntry._ID + " = " + locationRowId,
                null,
                null
        );

        TestUtilities.validateCursor("testUpdateLocation. ERROR: location entry update is not " +
                        "valid.", cursor, updatedValues);
        cursor.close();
    }

    public void deleteAllRecordsFromProvider()
    {
        getContext().getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        getContext().getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = getContext().getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("ERROR: Records not deleted from Weather table during delete.",
                0, cursor.getCount());
        cursor.close();

        cursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("ERROR: Records not deleted from Location table during delete.",
                0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords()
    {
        deleteAllRecordsFromProvider();
    }

    public void testDeleteRecords()
    {
        testInsertReadProvider();

        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver
                (LocationEntry.CONTENT_URI, true, locationObserver);

        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver
                (WeatherEntry.CONTENT_URI, true, weatherObserver);

        deleteAllRecordsFromProvider();

        locationObserver.waitForNotificationOrFail();
        weatherObserver.waitForNotificationOrFail();

        getContext().getContentResolver().unregisterContentObserver(locationObserver);
        getContext().getContentResolver().unregisterContentObserver(weatherObserver);
    }

    private static final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertWeatherValues(long locationRowId)
    {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000 * 60 * 60 * 24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int counter = 0; counter < BULK_INSERT_RECORDS_TO_INSERT; counter++,
                currentTestDate+= millisecondsInADay)
        {
            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationRowId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, currentTestDate);
            weatherValues.put(WeatherEntry.COLUMN_WIND_DIRECTION, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2 + 0.01 * (float) counter);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3 - 0.01 * (float) counter);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMPERATURE, 75 + counter);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMPERATURE, 65 - counter);
            weatherValues.put(WeatherEntry.COLUMN_DESCRIPTION, "Asteroids");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) counter);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            returnContentValues[counter] = weatherValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert()
    {
        ContentValues values = TestUtilities.createNorthPoleLocationValues();
        Uri locationUri = getContext().getContentResolver()
                .insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        assertTrue(locationRowId != -1);

        Cursor cursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBulkInsert. ERROR: location entry is not valid.",
                cursor, values);

        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues(locationRowId);
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        getContext().getContentResolver()
                .registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
        int insertCount = getContext().getContentResolver()
                .bulkInsert(WeatherEntry.CONTENT_URI, bulkInsertContentValues);

        weatherObserver.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = getContext().getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                WeatherEntry.COLUMN_DATE + " ASC"
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();

        for (int counter = 0; counter < BULK_INSERT_RECORDS_TO_INSERT; counter++,
                cursor.moveToNext())
        {
            TestUtilities.validateCurrentRecord("testBulkInsert. ERROR: weather entry is not " +
                    "valid at" + counter + ".", cursor, bulkInsertContentValues[counter]);
        }

        cursor.close();
    }
}