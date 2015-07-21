package app.com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivityFragment extends Fragment
{
    public DetailActivityFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
        {
            String message = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView textView = (TextView) rootView.findViewById(R.id.detail_text);
            textView.setText(message);
        }

        return rootView;
    }
}