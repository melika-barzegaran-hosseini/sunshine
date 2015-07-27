package app.com.example.android.sunshine.data;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestWeatherContract extends AndroidTestCase
{
    private static final String TEST_WEATHER_LOCATION = "/North Pole";

    public void testBuildWeatherLocation()
    {
        Uri locationUri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);
        assertNotNull("ERROR: Null Uri returned.  You must fill-in buildWeatherLocation in" +
                        "WeatherContract.", locationUri);

        assertEquals("ERROR: Weather location not properly appended to the end of the Uri",
                TEST_WEATHER_LOCATION, locationUri.getLastPathSegment());

        assertEquals("ERROR: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://app.com.example.android.sunshine/weather/%2FNorth%20Pole");
    }
}