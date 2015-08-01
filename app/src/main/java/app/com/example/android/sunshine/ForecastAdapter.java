package app.com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_icon);
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        TextView forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);

        int imageValue = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        long dateValue = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String forecastValue = cursor.getString(ForecastFragment.COL_WEATHER_DESCRIPTION);
        double highValue = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMPERATURE);
        double lowValue = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMPERARURE);

        imageView.setImageResource(R.mipmap.ic_launcher);
        dateView.setText(Utility.getFriendlyDate(context, dateValue));
        forecastView.setText(forecastValue);
        highView.setText(Utility.getFriendlyTemperature(highValue));
        lowView.setText(Utility.getFriendlyTemperature(lowValue));
    }
}