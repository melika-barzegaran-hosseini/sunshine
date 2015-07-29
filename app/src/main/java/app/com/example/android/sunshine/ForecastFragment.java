package app.com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

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
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(this.forecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String message = forecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(intent);
            }
        });

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
        String currentLocation = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity())
                .getString(
                        getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default));

        String currentUnits = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity())
                .getString(
                        getString(R.string.pref_units_key),
                        getString(R.string.pref_units_metric));

        new FetchWeatherTask(getActivity(), forecastAdapter).execute(currentLocation, currentUnits);
    }
}