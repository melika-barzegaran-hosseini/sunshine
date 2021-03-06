package app.com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

import app.com.example.android.sunshine.data.WeatherContract;
import app.com.example.android.sunshine.sync.SunshineSyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER_ID = 0;
    private static final String POSITION = "position";

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
    static final int COL_WEATHER_MIN_TEMPERATURE = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LATITUDE = 7;
    static final int COL_COORD_LONGITUDE = 8;

    private ForecastAdapter forecastAdapter;
    private int position = ListView.INVALID_POSITION;
    private ListView listView;
    private boolean useTodayLayout;

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
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(this.forecastAdapter);
        listView.setEmptyView(rootView.findViewById(R.id.listview_forecast_empty));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if(cursor != null)
                {
                    String locationSettings = Utility.getPreferredLocation(getActivity());
                    long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
                    Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(locationSettings, date);

                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry
                                    .buildWeatherLocationWithDate(locationSettings, date));
                }

                ForecastFragment.this.position = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION))
        {
            position = savedInstanceState.getInt(POSITION);
        }

        forecastAdapter.setUseTodayLayout(useTodayLayout);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        if (this.position != ListView.INVALID_POSITION)
        {
            bundle.putInt(POSITION, position);
        }

        super.onSaveInstanceState(bundle);
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


            case R.id.action_map_location:
                openPreferredLocationInMap();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather()
    {
        SunshineSyncAdapter.syncImmediately(getActivity());
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
        final long DATE = Calendar.getInstance().getTimeInMillis();
        final Uri URI = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(LOCATION_SETTING, DATE);

        final String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(), URI, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        forecastAdapter.swapCursor(cursor);

        if (position != ListView.INVALID_POSITION)
        {
            listView.smoothScrollToPosition(position);
        }

        updateEmptyView();
    }

    private void updateEmptyView()
    {
        if(this.listView.getCount() == 0)
        {
            TextView emptyView = (TextView)
                    getActivity().findViewById(R.id.listview_forecast_empty);

            if(emptyView != null)
            {
                String message = getString(R.string.no_weather_information_available);
                if(!Utility.isNetworkAvailable(getActivity()))
                {
                    message = getString(R.string.no_weather_information_available_no_network);
                }
                emptyView.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        forecastAdapter.swapCursor(null);
    }

    void onSettingsChanged()
    {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        this.useTodayLayout = useTodayLayout;
        if (forecastAdapter != null)
        {
            forecastAdapter.setUseTodayLayout(this.useTodayLayout);
        }
    }

    private void openPreferredLocationInMap()
    {
        if (forecastAdapter != null)
        {
            Cursor cursor = forecastAdapter.getCursor();

            if(cursor != null)
            {
                cursor.moveToFirst();

                String posLat = cursor.getString(COL_COORD_LATITUDE);
                String posLong = cursor.getString(COL_COORD_LONGITUDE);

                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                {
                    startActivity(intent);
                }
                else
                {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString()
                            + ", no receiving apps installed!");
                }
            }

        }
    }

    public interface Callback
    {
        public void onItemSelected(Uri dateUri);
    }
}