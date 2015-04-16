package io.ckl.crash.app.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.ckl.crash.app.R;

public class MainFragment extends Fragment {

    private TextView someTv;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        TextView tvAndroidId = (TextView) rootView.findViewById(R.id.tvAndroidId);
        Button b1 = (Button) rootView.findViewById(R.id.btn1);
        Button b2 = (Button) rootView.findViewById(R.id.btn2);
        Button b3 = (Button) rootView.findViewById(R.id.btn3);
        Button b4 = (Button) rootView.findViewById(R.id.btn4);

        tvAndroidId.setText(androidId);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                someTv.setText("Hello world");
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("Custom RuntimeException");
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int z = 1 / 0;
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new ClassCastException("Custom ClassCastException!");
            }
        });

        return rootView;
    }
}
