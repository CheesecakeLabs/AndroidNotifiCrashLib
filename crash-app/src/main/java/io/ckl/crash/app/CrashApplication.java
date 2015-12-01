package io.ckl.crash.app;

import android.app.Application;

import java.util.HashMap;

import io.ckl.notificrash.NotifiCrash;
import io.ckl.notificrash.NotifiCrashArguments;

public class CrashApplication extends Application implements NotifiCrashArguments {

    private static final String NOTIFICRASH_SERIAL_NUMBER = "PUT_YOUR_NOTIFICRASH_SERIAL_HERE";

    private String getUser() {
        return "MyUser";
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable debug (optional)
        NotifiCrash.setDebug(true);
        // Initializing NotifiCrash library
        NotifiCrash.init(this, NOTIFICRASH_SERIAL_NUMBER);

        // Add an extra argument to NotifiCrash log
        NotifiCrash.getInstance().addExtra("MyKey", "MyValue");
    }

    // Method that retrieve the extra arguments when an app crashes
    @Override
    public HashMap<String, String> getExtraArguments() {
        HashMap<String, String> extraArguments = new HashMap<>();
        extraArguments.put("User", getUser());
        extraArguments.put("Data", "MyData");
        return extraArguments;
    }
}
