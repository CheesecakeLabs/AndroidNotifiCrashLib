package io.ckl.crash.app;

import android.app.Application;

import io.ckl.notifibug.NotifiBug;

public class CrashApplication extends Application {


    private static final String NOTIFIBUG_SERIAL_NUMBER = "1255f5292a006c4c3bc50ffe428339";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable debug (optional)
        NotifiBug.setDebug(true);
        // Initializing NotifiBug library
        NotifiBug.init(this, NOTIFIBUG_SERIAL_NUMBER);

    }

}
