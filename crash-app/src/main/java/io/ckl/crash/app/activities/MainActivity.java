package io.ckl.crash.app.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import io.ckl.crash.app.R;
import io.ckl.crash.app.fragments.MainFragment;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }
}
