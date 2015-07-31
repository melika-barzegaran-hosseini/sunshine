package app.com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivityFragment extends Fragment
{
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String message;

    public DetailActivityFragment()
    {
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null)
        {
            this.message = intent.getDataString();
        }

        if(this.message != null)
        {
            TextView textView = (TextView) rootView.findViewById(R.id.detail_text);
            textView.setText(this.message);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(menu.findItem(R.id.action_share));

        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareIntent());
        }
        else
        {
            Log.d(LOG_TAG, "share action provider is null.");
        }
    }

    private Intent createShareIntent()
    {
        Intent shareIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, message + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}