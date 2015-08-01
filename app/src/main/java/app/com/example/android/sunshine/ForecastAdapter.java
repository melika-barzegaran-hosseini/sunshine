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
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    public ForecastAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        int viewType = getItemViewType(cursor.getPosition());

        int layoutId = -1;

        switch (viewType)
        {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;

            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int imageValue = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        long dateValue = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String forecastValue = cursor.getString(ForecastFragment.COL_WEATHER_DESCRIPTION);
        double highValue = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMPERATURE);
        double lowValue = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMPERARURE);

        viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
        viewHolder.dateView.setText(Utility.getFriendlyDate(context, dateValue));
        viewHolder.descriptionView.setText(forecastValue);
        viewHolder.highTemperatureView.setText(Utility.getFriendlyTemperature(context, highValue));
        viewHolder.lowTemperatureView.setText(Utility.getFriendlyTemperature(context, lowValue));
    }

    @Override
    public int getViewTypeCount()
    {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public static class ViewHolder
    {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTemperatureView;
        public final TextView lowTemperatureView;

        public ViewHolder(View view)
        {
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highTemperatureView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowTemperatureView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}