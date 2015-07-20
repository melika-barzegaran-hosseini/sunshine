package app.com.example.android.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Calendar;

public class ForecastFragment extends Fragment
{
    private ArrayAdapter<String> forecastAdapter;

    public ForecastFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //initializes the adapter.
        //adapters are the glue that allows us to bind our underlying data to our user interface
        //elements.
        this.forecastAdapter =
                new ArrayAdapter<String>(
                        this.getActivity(), //the app's context
                        R.layout.list_item_forecast, //the layout that contains a TextView for each
                                                     // string in the array
                        R.id.list_item_forecast_textview, //the ID of the TextView
                        new ArrayList<String>()); //the array of strings

        //inflates layout XML file and turns them into a full hierarchy.
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //gets a reference to the ListView, and attach the Adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(this.forecastAdapter);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_refresh:
                FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
                fetchWeatherTask.execute("tehran,iran");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String[] getInformationFromJson(String forecastJsonString) throws JSONException
        {
            JSONArray jsonArray = new JSONObject(forecastJsonString).getJSONArray("list");

            String[] results = new String[jsonArray.length()];
            for(int counter = 0; counter < jsonArray.length(); counter++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(counter);

                String date = this.getFormattedDate(counter);
                long min = Math.round(jsonObject.getJSONObject("temp").getDouble("min"));
                long max = Math.round(jsonObject.getJSONObject("temp").getDouble("max"));
                String weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                        .getString("main");

                results[counter] = date + " - " + weather + " - " + max + "/" + min;
            }

            return results;
        }

        private String getFormattedDate(int offset)
        {
            final String dateFormat = "EEE, MMM dd";

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, offset);

            return new SimpleDateFormat(dateFormat).format(calendar.getTime());
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
            String forecastJsonString;

            String mode = "json";
            String units = "metric";
            String days = "7";

            try
            {
                final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String MODE_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(MODE_PARAM, mode)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, days)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null)
                {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer buffer = new StringBuffer();
                String line;
                while((line = reader.readLine()) != null)
                {
                    buffer.append(line).append("\n");
                }

                if(buffer.length() == 0)
                {
                    return null;
                }

                forecastJsonString = buffer.toString();
            }
            catch (java.io.IOException e)
            {
                Log.e(this.LOG_TAG, "couldn't connect to the cloud.");
                return null;
            }
            finally
            {
                if(urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if(reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException e)
                    {
                        Log.e(this.LOG_TAG, "couldn't close the stream.");
                    }
                }
            }

            try
            {
                return this.getInformationFromJson(forecastJsonString);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, "JSON string couldn't get parsed.", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] strings)
        {
            if(strings != null)
            {
                forecastAdapter.clear();
                for(String string : strings)
                {
                    forecastAdapter.add(string);
                }
            }
        }
    }
}