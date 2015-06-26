package io.ckl.crash.app;

import android.app.Application;

import io.ckl.notificrash.NotifiCrash;

public class CrashApplication extends Application {


    private static final String NOTIFICRASH_SERIAL_NUMBER = "PUT_YOUR_NOTIFICRASH_SERIAL_HERE";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable debug (optional)
        NotifiCrash.setDebug(true);
        // Initializing NotifiCrash library
        NotifiCrash.init(this, NOTIFICRASH_SERIAL_NUMBER);

    }

}
