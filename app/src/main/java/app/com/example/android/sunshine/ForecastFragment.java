package app.com.example.android.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import app.com.example.android.sunshine.data.WeatherContract;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int FORECAST_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LATITUDE,
            WeatherContract.LocationEntry.COLUMN_COORD_LONGITUDE
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESCRIPTION = 2;
    static final int COL_WEATHER_MAX_TEMPERATURE = 3;
    static final int COL_WEATHER_MIN_TEMPERARURE = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LATITUDE = 7;
    static final int COL_COORD_LONGITUDE = 8;

    private ForecastAdapter forecastAdapter;

    public ForecastFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //initializes the adapter.
        //adapters are the glue that allows us to bind our underlying data to our user interface
        //elements.
        this.forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //inflates layout XML file and turns them into a full hierarchy.
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //gets a reference to the ListView, and attach the Adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
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
                this.updateWeather();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }

    private void updateWeather()
    {
        String location = Utility.getPreferredLocation(getActivity());
        String unit = Utility.getPreferredUnit(getActivity());
        new FetchWeatherTask(getActivity()).execute(location, unit);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
    {
        final String LOCATION_SETTING = Utility.getPreferredLocation(getActivity());
        final long DATE = Utility.getStartOfToday();
        final Uri URI = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(LOCATION_SETTING, DATE);

        final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(), URI, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        forecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        forecastAdapter.swapCursor(null);
    }
}