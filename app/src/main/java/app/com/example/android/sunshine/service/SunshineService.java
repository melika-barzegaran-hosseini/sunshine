package app.com.example.android.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;

import app.com.example.android.sunshine.data.WeatherContract;

public class SunshineService extends IntentService
{
    public static final String LOCATION_EXTRA = "location";
    public static final String UNIT_EXTRA = "unit";

    private static final String LOG_TAG = SunshineService.class.getSimpleName();

    public SunshineService()
    {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String location = intent.getStringExtra(LOCATION_EXTRA);
        String unit = intent.getStringExtra(UNIT_EXTRA);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonString;

        String format = "json";
        int numDays = 14;

        try
        {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, unit)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null)
            {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
            {
                return;
            }

            forecastJsonString = buffer.toString();
            getInformationFromJson(forecastJsonString, location);
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "couldn't connect to the cloud.");
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if(reader != null)
            {
                try
                {
                    reader.close();
                }
                catch(IOException e)
                {
                    Log.e(LOG_TAG, "couldn't close the stream.");
                }
            }
        }
    }

    private void getInformationFromJson(String forecastJsonString, String locationSetting)
            throws JSONException
    {
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WIND_SPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try
        {
            JSONObject forecastJson = new JSONObject(forecastJsonString);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);
            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> contentValuesVector
                    = new Vector<>(weatherArray.length());

            for(int counter = 0; counter < weatherArray.length(); counter++)
            {
                long dateTime;

                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, counter);
                dateTime = calendar.getTimeInMillis();

                JSONObject dayForecast = weatherArray.getJSONObject(counter);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WIND_SPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOCATION_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_DIRECTION, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                contentValuesVector.add(weatherValues);
            }

            int inserted;
            if (contentValuesVector.size() > 0)
            {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);

                getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);

                inserted = getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        contentValuesArray
                );

                Log.d(LOG_TAG, "Update complete. " + inserted + " Inserted");
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting the location string used to request updates from the server
     * @param cityName a human-readable city name
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon)
    {
        Cursor cursor = getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null
        );

        long locationId;

        if(cursor.moveToFirst())
        {
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = cursor.getLong(locationIdIndex);
        }
        else
        {
            ContentValues values = new ContentValues();

            values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE, lat);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE, lon);

            Uri insertedUri = getApplicationContext().getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    values
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        cursor.close();

        return locationId;
    }

    public static class AlarmReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Intent newIntent = new Intent(context, SunshineService.class);

            newIntent.putExtra(LOCATION_EXTRA, intent.getStringExtra(LOCATION_EXTRA));
            newIntent.putExtra(UNIT_EXTRA, intent.getStringExtra(UNIT_EXTRA));

            context.startService(newIntent);
        }
    }
}