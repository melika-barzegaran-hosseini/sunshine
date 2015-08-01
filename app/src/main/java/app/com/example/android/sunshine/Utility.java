package app.com.example.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility
{
    public static String getPreferredLocation(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default)
        );
    }

    public static boolean isMetric(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)
        ).equals(context.getString(R.string.pref_units_metric));
    }

    public static String getPreferredUnit(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)
        );
    }

    public static String formatDate(long dateInMilliseconds)
    {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    public static long getStartOfToday()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getFriendlyDate(Context context, long dateInMilliseconds)
    {
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMilliseconds);
        int thisDay = calendar.get(Calendar.DAY_OF_MONTH);

        if(currentDay == thisDay)
        {
            return context.getString(R.string.today) + ", "
                    + new SimpleDateFormat("MMMM dd").format(dateInMilliseconds);
        }
        else if(currentDay + 1 == thisDay)
        {
            return context.getString(R.string.tomorrow);
        }
        else if(currentDay + 7 > thisDay)
        {
            return new SimpleDateFormat("EEEE").format(dateInMilliseconds);
        }
        else
        {
            return new SimpleDateFormat("EEE MMM dd").format(dateInMilliseconds);
        }
    }

    public static String getFriendlyDayInWeek(Context context, long dateInMilliseconds)
    {
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMilliseconds);
        int thisDay = calendar.get(Calendar.DAY_OF_MONTH);

        if(currentDay == thisDay)
        {
            return context.getString(R.string.today);
        }
        else if(currentDay + 1 == thisDay)
        {
            return context.getString(R.string.tomorrow);
        }
        else
        {
            return new SimpleDateFormat("EEEE").format(dateInMilliseconds);
        }
    }

    public static String getFriendlyDayInMonth(Context context, long dateInMilliseconds)
    {
        return new SimpleDateFormat("MMMM dd").format(dateInMilliseconds);
    }

    public static String getFriendlyTemperature(Context context, double temperature)
    {
        return context.getString(R.string.format_temperature, Math.round(temperature));
    }

    public static String getFriendlyWind(Context context, double speed, double degrees)
    {
        int windFormat;

        if (Utility.isMetric(context))
        {
            windFormat = R.string.format_wind_kmh;
        }
        else
        {
            windFormat = R.string.format_wind_mph;
        }

        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5)
        {
            direction = "N";
        }
        else if (degrees >= 22.5 && degrees < 67.5)
        {
            direction = "NE";
        }
        else if (degrees >= 67.5 && degrees < 112.5)
        {
            direction = "E";
        }
        else if (degrees >= 112.5 && degrees < 157.5)
        {
            direction = "SE";
        }
        else if (degrees >= 157.5 && degrees < 202.5)
        {
            direction = "S";
        }
        else if (degrees >= 202.5 && degrees < 247.5)
        {
            direction = "SW";
        }
        else if (degrees >= 247.5 && degrees < 292.5)
        {
            direction = "W";
        }
        else if (degrees >= 292.5 && degrees < 337.5)
        {
            direction = "NW";
        }

        return String.format(context.getString(windFormat), Math.round(speed), direction);
    }
}