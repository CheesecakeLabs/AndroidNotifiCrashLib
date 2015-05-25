package io.ckl.crash.app;

import android.app.Application;

import io.ckl.notifibug.NotifiBug;

public class CrashApplication extends Application {


    private static final String NOTIFIBUG_SERIAL_NUMBER = "PUT_YOUR_NOTIFIBUG_SERIAL_HERE";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable debug (optional)
        NotifiBug.setDebug(true);
        // Initializing NotifiBug library
        NotifiBug.init(this, NOTIFIBUG_SERIAL_NUMBER);

    }

}
