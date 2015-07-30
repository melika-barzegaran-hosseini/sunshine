package app.com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastAdapter extends CursorAdapter
{
    Context context;

    public ForecastAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);

        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView textView = (TextView)view;
        textView.setText(convertCursorRowToUXFormat(cursor));
    }

    private String convertCursorRowToUXFormat(Cursor cursor)
    {
        double maxTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMPERATURE);
        double minTemperature = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMPERARURE);
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESCRIPTION);


        return Utility.formatDate(date) + " - " + description
                + " - " + Math.round(maxTemperature) + "/" + Math.round(minTemperature);
    }
}