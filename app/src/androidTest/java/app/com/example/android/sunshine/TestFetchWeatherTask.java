package app.com.example.android.sunshine;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import app.com.example.android.sunshine.data.WeatherContract;

public class TestFetchWeatherTask extends AndroidTestCase
{
    static final String ADD_LOCATION_SETTING = "Sunnydale, CA";
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static final double ADD_LOCATION_LAT = 34.425833;
    static final double ADD_LOCATION_LON = -119.714167;

    @TargetApi(11)
    public void testAddLocation()
    {
        getContext().getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getContext(), null);
        long locationId = fetchWeatherTask.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);

        assertFalse("ERROR: addLocation returned an invalid ID on insert", locationId == -1);

        for ( int counter = 0; counter < 2; counter++)
        {
            Cursor locationCursor = getContext().getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            WeatherContract.LocationEntry._ID,
                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                            WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE,
                            WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE
                    },
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null);

            if (locationCursor.moveToFirst())
            {
                assertEquals("ERROR: the queried value of locationId does not match the returned " +
                        "value from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("ERROR: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("ERROR: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("ERROR: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("ERROR: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            }
            else
            {
                fail("Error: the id you used to query returned an empty cursor");
            }

            assertFalse("ERROR: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            long newLocationId = fetchWeatherTask.addLocation(
                    ADD_LOCATION_SETTING,
                    ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT,
                    ADD_LOCATION_LON);

            assertEquals("ERROR: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }

        getContext().getContentResolver().delete(
                WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING}
        );

        getContext().getContentResolver().
                acquireContentProviderClient(WeatherContract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}