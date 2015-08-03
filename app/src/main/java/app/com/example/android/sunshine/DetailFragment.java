package app.com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.com.example.android.sunshine.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String[] DETAILS_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WIND_DIRECTION,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESCRIPTION = 2;
    public static final int COL_WEATHER_MAX_TEMPERATURE = 3;
    public static final int COL_WEATHER_MIN_TEMPERATURE = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_WIND_DIRECTION = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAILS_LOADER_ID = 0;
    static final String DETAIL_URI = "URI";

    private String forecast;
    private ShareActionProvider shareActionProvider;
    private Uri uri;

    private ImageView iconView;
    private TextView dayView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment()
    {
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        Bundle arguments = getArguments();

        if (arguments != null)
        {
            this.uri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        dayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        this.shareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(menu.findItem(R.id.action_share));

        if(this.shareActionProvider != null)
        {
            this.shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent()
    {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(DETAILS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
    {
        if(uri != null)
        {
            return new CursorLoader(
                    getActivity(),
                    this.uri,
                    DETAILS_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        if(!cursor.moveToFirst() || cursor == null)
        {
            return;
        }

        int imageValue = cursor.getInt(COL_WEATHER_CONDITION_ID);
        long dateValue = cursor.getLong(COL_WEATHER_DATE);
        String forecastValue = cursor.getString(COL_WEATHER_DESCRIPTION);
        double highValue = cursor.getDouble(COL_WEATHER_MAX_TEMPERATURE);
        double lowValue = cursor.getDouble(COL_WEATHER_MIN_TEMPERATURE);
        double humidityValue = cursor.getDouble(COL_WEATHER_HUMIDITY);
        double windSpeedValue = cursor.getDouble(COL_WEATHER_WIND_SPEED);
        double windDirectionValue = cursor.getDouble(COL_WEATHER_WIND_DIRECTION);
        double pressureValue = cursor.getDouble(COL_WEATHER_PRESSURE);

        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(imageValue));
        dayView.setText(Utility.getFriendlyDayInWeek(getActivity(), dateValue));
        dateView.setText(Utility.getFriendlyDayInMonth(getActivity(), dateValue));
        descriptionView.setText(forecastValue);
        highTempView.setText(Utility.getFriendlyTemperature(getActivity(), highValue));
        lowTempView.setText(Utility.getFriendlyTemperature(getActivity(), lowValue));
        humidityView.setText(String.format(getString(R.string.format_humidity), Math.round(humidityValue)));
        pressureView.setText(String.format(getString(R.string.format_pressure), Math.round(pressureValue)));
        windView.setText(
                Utility.getFriendlyWind(getActivity(), windSpeedValue, windDirectionValue)
        );

        this.forecast = String.format("%s - %s - %s/%s",
                Utility.getFriendlyDate(getActivity(), dateValue),
                forecastValue,
                Utility.getFriendlyTemperature(getActivity(), highValue),
                Utility.getFriendlyTemperature(getActivity(), lowValue)
        );

        if(this.shareActionProvider != null)
        {
            this.shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader){}

    void onSettingsChanged()
    {
        if (this.uri != null)
        {
            String locationSettings = Utility.getPreferredLocation(getActivity());
            long date = WeatherContract.WeatherEntry.getDateFromUri(this.uri);
            this.uri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(locationSettings, date);
            getLoaderManager().restartLoader(DETAILS_LOADER_ID, null, this);
        }
    }
}