package app.com.example.android.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import app.com.example.android.sunshine.data.WeatherContract;
import app.com.example.android.sunshine.data.WeatherContract.WeatherEntry;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
{
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private final Context context;
    private ArrayAdapter<String> forecastAdapter;

    private boolean DEBUG = true;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter)
    {
        this.context = context;
        this.forecastAdapter = forecastAdapter;
    }

    @Override
    protected String[] doInBackground(String... params)
    {
        if (params.length == 0)
        {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonString = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        String locationQuery = params[0];

        try
        {
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, params[1])
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null)
            {
                return null;
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
                return null;
            }

            forecastJsonString = buffer.toString();
        }
        catch (IOException e)
        {
            Log.e(this.LOG_TAG, "couldn't connect to the cloud.");
            return null;
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
                    Log.e(this.LOG_TAG, "couldn't close the stream.");
                }
            }
        }

        try
        {
            return getInformationFromJson(forecastJsonString, locationQuery);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, "JSON string couldn't get parsed.", e);
        }

        return null;
    }

    private String[] getInformationFromJson(String forecastJsonString, String locationSetting)
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
                    = new Vector<ContentValues>(weatherArray.length());

            long firstDayInMilliseconds = 0L;

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

                if(counter == 0)
                {
                    firstDayInMilliseconds = dateTime;
                }

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

                weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationId);
                weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_WIND_DIRECTION, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMPERATURE, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMPERATURE, low);
                weatherValues.put(WeatherEntry.COLUMN_DESCRIPTION, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                contentValuesVector.add(weatherValues);
            }

            if (contentValuesVector.size() > 0)
            {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);

                context.getContentResolver().delete(WeatherEntry.CONTENT_URI, null, null);

                context.getContentResolver().bulkInsert(
                        WeatherEntry.CONTENT_URI,
                        contentValuesArray
                );
            }

            String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri =
                    WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                            locationSetting,
                            firstDayInMilliseconds
                    );

            Cursor cursor = context.getContentResolver().query(
                    weatherForLocationUri,
                    null,
                    null,
                    null,
                    sortOrder
            );

            contentValuesVector = new Vector<ContentValues>(cursor.getCount());

            if (cursor.moveToFirst())
            {
                do
                {
                    ContentValues contentValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                    contentValuesVector.add(contentValues);
                }
                while(cursor.moveToNext());
            }

            Log.d(LOG_TAG,
                    "FetchWeatherTask Complete. " + contentValuesVector.size() + "Inserted");

            String[] resultStrings = convertContentValuesToUXFormat(contentValuesVector);

            return resultStrings;

        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
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
        Cursor cursor = context.getContentResolver().query(
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

            Uri insertedUri = context.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    values
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        cursor.close();

        return locationId;
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> vector)
    {
        String[] resultStrings = new String[vector.size()];

        for(int counter = 0; counter < vector.size(); counter++)
        {
            ContentValues weatherValues = vector.elementAt(counter);

            double max = weatherValues.getAsDouble(WeatherEntry.COLUMN_MAX_TEMPERATURE);
            double min = weatherValues.getAsDouble(WeatherEntry.COLUMN_MIN_TEMPERATURE);

            resultStrings[counter] =
                    getReadableDateString(weatherValues.getAsLong(WeatherEntry.COLUMN_DATE)) +
                            " - " + weatherValues.getAsString(WeatherEntry.COLUMN_DESCRIPTION) +
                            " - " + Math.round(max) + "/" + Math.round(min);
        }
        return resultStrings;
    }

    private String getReadableDateString(long time)
    {
        final String dateFormat = "EEE, MMM dd";
        Date date = new Date(time);
        return new SimpleDateFormat(dateFormat).format(date);
    }

    @Override
    protected void onPostExecute(String[] strings)
    {
        if (strings != null && forecastAdapter != null)
        {
            forecastAdapter.clear();

            for(String string : strings)
            {
                forecastAdapter.add(string);
            }
        }
    }
}