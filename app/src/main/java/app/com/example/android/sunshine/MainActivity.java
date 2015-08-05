package app.com.example.android.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback
{
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private String location;
    private String unit;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.location = Utility.getPreferredLocation(this);
        this.unit = Utility.getPreferredUnit(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.detail_container) != null)
        {
            this.twoPane = true;

            if (savedInstanceState == null)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        else
        {
            this.twoPane = false;
        }

        ForecastFragment forecastFragment =
                ((ForecastFragment)getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_forecast));

        forecastFragment.setUseTodayLayout(!this.twoPane);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_map_location:
                openPreferredLocationInMap();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String location = Utility.getPreferredLocation(this);
        String unit = Utility.getPreferredUnit(this);

        if((location != null && !location.equals(this.location)) ||
                (unit != null && !unit.equals(this.unit)))
        {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);

            if(forecastFragment != null)
            {
                forecastFragment.onSettingsChanged();
            }

            DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);

            if ( null != detailFragment )
            {
                detailFragment.onSettingsChanged();
            }

            this.location = location;
            this.unit = unit;
        }
    }

    private void openPreferredLocationInMap()
    {
        String location = Utility.getPreferredLocation(this);

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
        else
        {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps is installed!");
        }
    }

    @Override
    public void onItemSelected(Uri uri)
    {
        if(this.twoPane)
        {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, uri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        else
        {
            Intent intent = new Intent(this, DetailActivity.class).setData(uri);
            startActivity(intent);
        }
    }
}