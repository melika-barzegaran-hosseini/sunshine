package app.com.example.android.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Calendar;

import app.com.example.android.sunshine.data.WeatherContract;

public class ForecastFragment extends Fragment
{
    private ForecastAdapter forecastAdapter;

    public ForecastFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,
                Calendar.getInstance().getTimeInMillis()
        );

        Cursor cursor = getActivity().getContentResolver().query(
                weatherForLocationUri,
                null,
                null,
                null,
                sortOrder
        );

        //initializes the adapter.
        //adapters are the glue that allows us to bind our underlying data to our user interface
        //elements.
        this.forecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

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
}