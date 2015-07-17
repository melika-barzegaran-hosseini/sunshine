package app.com.example.android.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivityFragment extends Fragment
{
    public MainActivityFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        String[] data =
                {
                        "Today - Sunny - 88/63",
                        "Tomorrow - Foggy - 70/46",
                        "Weds - Cloudy - 72/63",
                        "Thurs - Rainy - 64/51",
                        "Fri - Foggy - 70/46",
                        "Sat - Sunny - 76/68",
                        "Today - Sunny - 88/63",
                        "Tomorrow - Foggy - 70/46",
                        "Weds - Cloudy - 72/63",
                        "Thurs - Rainy - 64/51",
                        "Fri - Foggy - 70/46",
                        "Sat - Sunny - 76/68",
                        "Today - Sunny - 88/63",
                        "Tomorrow - Foggy - 70/46",
                        "Weds - Cloudy - 72/63",
                        "Thurs - Rainy - 64/51",
                        "Fri - Foggy - 70/46",
                        "Sat - Sunny - 76/68",
                        "Today - Sunny - 88/63",
                        "Tomorrow - Foggy - 70/46",
                        "Weds - Cloudy - 72/63",
                        "Thurs - Rainy - 64/51",
                        "Fri - Foggy - 70/46",
                        "Sat - Sunny - 76/68"
                };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        //initializes the adapter.
        //adapters are the glue that allows us to bind our underlying data to our user interface elements.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this.getActivity(), //the app's context
                        R.layout.list_item_forecast, //the layout that contains a TextView for each string in the array
                        R.id.list_item_forecast_textview, //the ID of the TextView
                        weekForecast); //the array of strings

        //inflates layout XML file and turns them into a full hierarchy.
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //gets a reference to the ListView, and attach the Adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);

        return rootView;
    }
}