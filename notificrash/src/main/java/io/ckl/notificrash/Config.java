package io.ckl.notificrash;

import android.util.Base64;

public class Config {

    public static final String TAG = "NOTIFI_BUG ";
    public static final String STORAGE_UNSENT_REPORTS_FILE = "notifibug-unsent-crashes.data";
    public static final String ENCODED_URL = "aHR0cHM6Ly9ub3RpZmljcmFzaC5jb20vYXBpL3YzL2NyYXNoZXMv";

    public static final String API_ENDPOINT_URL = new String(Base64.decode(ENCODED_URL, Base64.DEFAULT));

}
