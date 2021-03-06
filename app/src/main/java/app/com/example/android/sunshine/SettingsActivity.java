package app.com.example.android.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        this.addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        this.bindPreferenceSummaryToValue
                (this.findPreference(this.getString(R.string.pref_location_key)));
        this.bindPreferenceSummaryToValue
                (this.findPreference(this.getString(R.string.pref_units_key)));
    }

    private void bindPreferenceSummaryToValue(Preference preference)
    {
        preference.setOnPreferenceChangeListener(this);

        this.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences
                        (preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value)
    {
        String stringValue = value.toString();

        if(preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            int preferenceIndex = listPreference.findIndexOfValue(stringValue);

            if (preferenceIndex >= 0)
            {
                preference.setSummary(listPreference.getEntries()[preferenceIndex]);
            }
        }
        else
        {
            preference.setSummary(stringValue);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent()
    {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}