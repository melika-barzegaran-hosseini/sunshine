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
            String today = context.getString(R.string.today);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd");
            return today + ", " + dateFormat.format(dateInMilliseconds);
        }
        else if(currentDay + 1 == thisDay)
        {
            return context.getString(R.string.tomorrow);
        }
        else if(currentDay + 7 > thisDay)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            return dateFormat.format(dateInMilliseconds);
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
            return dateFormat.format(dateInMilliseconds);
        }
    }

    public static String getFriendlyTemperature(double temperature)
    {
        return String.valueOf(Math.round(temperature));
    }
}