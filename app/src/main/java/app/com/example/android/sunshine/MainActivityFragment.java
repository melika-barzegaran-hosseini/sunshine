package app.com.example.android.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
                        "Sat - Sunny - 76/68"
                };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
