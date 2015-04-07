# NotifiBug Library for Android

[![Download](https://api.bintray.com/packages/cheesecakelabs/maven/notifibug/images/download.svg)](https://bintray.com/cheesecakelabs/maven/notifibug/_latestVersion)
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)

It does what every bug client needs to do but little earlier and in a smarter way

## Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release)

Gradle:

```groovy
compile 'io.ckl.notifibug:notifibug:0.1.0-snapshot'
```

## How to use it

Below is an example of how to register NotifiBug library to handle uncaught exceptions of you app and send 
crash reports to NotifiCrash service.

### Permissions in manifest

The `AndroidManifest.xml` requires the permission `android.permission.INTERNET` and would like the permission `android.permission.ACCESS_NETWORK_STATE` even though optional.

```xml
<!-- REQUIRED to send captures to NotifiCrash service -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- OPTIONAL but makes NotifiBug smarter -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Init library in your project

``` java
public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// NotifiBug will look for uncaught exceptions from previous runs and send them
		NotifiBug.init(this.getApplicationContext(), "YOUR_SERIAL_NUMBER");

	}

}
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

## Authors and Contributors

Edited for NotifiCrash by Marko Arsic (@marsicdev)

This is refactored and optimised [Sentry for Android](https://github.com/joshdholtz/Sentry-Android) library.
Credits for most of the code goes to @joshdholtz

## Support or Contact

Having trouble with NotifiBug? Check out the [javadocs]() or contact marko@ckl.io and we’ll help you out.

## License

NotifiBug is available under the MIT license. See the [LICENSE](https://github.com/CheesecakeLabs/AndroidNotifiBug/blob/master/LICENSE) file for more info.
