package app.com.example.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
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

    static String formatDate(long dateInMilliseconds)
    {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }
}