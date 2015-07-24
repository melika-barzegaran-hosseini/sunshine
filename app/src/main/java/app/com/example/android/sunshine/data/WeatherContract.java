package app.com.example.android.sunshine.data;

import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WeatherContract
{
    public static String normalizeDate(int offset)
    {
        final String dateFormat = "EEE, MMM dd";

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offset);

        return new SimpleDateFormat(dateFormat).format(calendar.getTime());
    }

    public static final class LocationEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COORD_LATITUDE = "coord_latitude";
        public static final String COLUMN_COORD_LONGITUDE = "coord_longitude";
    }

    public static final class WeatherEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_LOCATION_KEY = "location_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_MIN_TEMPERATURE = "min_pressure";
        public static final String COLUMN_MAX_TEMPERATURE = "max_pressure";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND_SPEED = "wind_speed";
        public static final String COLUMN_WIND_DIRECTION = "wind_direction";
    }
}