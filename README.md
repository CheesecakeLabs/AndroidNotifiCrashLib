# NotifiBug Library for Android

It does what every bug client needs to do but little earlier

Below is an example of how to register NotifiBug library to handle uncaught exceptions of you app and send 
crash reports to NotifiCrash service.

```xml
<!-- REQUIRED to send captures to NotifiCrash service -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- OPTIONAL but makes NotifiBug smarter -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

``` java
public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Sentry will look for uncaught exceptions from previous runs and send them
		NotifiBug.init(this.getApplicationContext(), "YOUR_SERIAL_NUMBER");

	}

}
```

### Updates

Version | Changes
--- | ---
**0.1.0** | Initial pre-release

## This Is How We Do It

### Permissions in manifest

The AndroidManifest.xml requires the permission `android.permission.INTERNET` and would like the permission `android.permission.ACCESS_NETWORK_STATE` even though optional.

```xml
<!-- REQUIRED to send captures to NotifiCrash service -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- OPTIONAL but makes NotifiBug smarter -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Capture a message
``` java
    NotifiBug.captureMessage("Something significant may have happened");
```

### Set a listener to intercept the NotifiBugEventBuilder before each capture
``` java
// CALL THIS BEFORE CALLING NotifiBug.init()
// Sets a listener to intercept the NotifiBugEventBuilder before
// each capture to set values that could change state
NotifiBug.setCaptureListener(new NotifiBugEventCaptureListener() {

	@Override
	public NotifiBugEventBuilder beforeCapture(NotifiBugEventBuilder builder) {

		// Needs permission - <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		// Sets extra key if wifi is connected
		try {
			builder.getExtra().put("wifi", String.valueOf(mWifi.isConnected()));
			builder.getTags().put("tag_1", "value_1");
		} catch (JSONException e) {}

		return builder;
	}

});

```

## Edited for NotifiCrash

by Marko Arsić (@marsicdev)

## Credits

This is refactored and optimised [Sentry for Android](https://github.com/joshdholtz/Sentry-Android) library.
Credits for most of the code goes to @joshdholtz

## License

NotifiBug is available under the MIT license. See the [LICENSE](https://github.com/CheesecakeLabs/AndroidNotifiBug/blob/master/LICENSE) file for more info.