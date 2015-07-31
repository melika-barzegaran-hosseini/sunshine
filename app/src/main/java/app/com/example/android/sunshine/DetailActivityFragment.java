package app.com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.TextView;

import app.com.example.android.sunshine.data.WeatherContract;

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String[] DETAILS_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE,
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESCRIPTION = 2;
    static final int COL_WEATHER_MAX_TEMPERATURE = 3;
    static final int COL_WEATHER_MIN_TEMPERARURE = 4;

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAILS_LOADER_ID = 0;

    private String forecast;

    private ShareActionProvider shareActionProvider;

    public DetailActivityFragment()
    {
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_detail, container, false);
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
        Intent intent = getActivity().getIntent();

        if(intent == null)
        {
            return null;
        }
        else
        {
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    DETAILS_COLUMNS,
                    null,
                    null,
                    null
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        if(!cursor.moveToFirst())
        {
            return;
        }

        String date = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));
        String description = cursor.getString(COL_WEATHER_DESCRIPTION);
        int max = cursor.getInt(COL_WEATHER_MAX_TEMPERATURE);
        int min = cursor.getInt(COL_WEATHER_MIN_TEMPERARURE);

        this.forecast = String.format("%s - %s - %d/%d", date, description, max, min);

        TextView textView = (TextView) this.getView().findViewById(R.id.detail_text);

        textView.setText(forecast);

        if(this.shareActionProvider != null)
        {
            this.shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader){}
}