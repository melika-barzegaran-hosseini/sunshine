package app.com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.com.example.android.sunshine.data.WeatherContract;

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
        int maxTemperatureIndex =
                cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMPERATURE);
        int minTemperatureIndex =
                cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMPERATURE);
        int dateIndex =
                cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int descriptionIndex =
                cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION);


        return Utility.formatDate(cursor.getLong(dateIndex))
                + " - " + cursor.getString(descriptionIndex)
                + " - " + Math.round(cursor.getDouble(maxTemperatureIndex))
                + "/" + Math.round(cursor.getDouble(minTemperatureIndex));
    }
}