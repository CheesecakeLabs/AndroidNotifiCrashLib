# NotifiCrash Library for Android

[![Download](https://api.bintray.com/packages/cheesecakelabs/maven/notificrash/images/download.svg)](https://bintray.com/cheesecakelabs/maven/notificrash/_latestVersion)
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)

It does what every bug client needs to do but little earlier and in a smarter way

## Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) ([package](https://bintray.com/cheesecakelabs/maven/notificrash/view) appears immediately after release)

Gradle:

```groovy
compile 'io.ckl.notificrash:notificrash:0.4.0'
```

## How to use it

Below is an example of how to register NotifiCrash library to handle uncaught exceptions of you app and send
crash reports to NotifiCrash service.

### Permissions in manifest

The `AndroidManifest.xml` requires the permission `android.permission.INTERNET` and would like the permission `android.permission.ACCESS_NETWORK_STATE` even though optional.

```xml
<!-- REQUIRED to send captures to NotifiCrash service -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- OPTIONAL but makes NotifiCrash smarter -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Init library in your project

``` java
public class MyApplication extends Application {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// NotifiCrash will look for uncaught exceptions from previous runs and send them
		NotifiCrash.init(this, "YOUR_SERIAL_NUMBER");

	}

}
```

### Enable debug

To enable logcat output in console add `setDebug(true)` just before `init`

```java
	// Enable debug output (optional)
	NotifiCrash.setDebug(true);
	// NotifiCrash init
	NotifiCrash.init(this, "YOUR_SERIAL_NUMBER");
```

### Others crash capture libraries

If you are using other bug capturing libraries make sure that you init NotifiCrash last.

```java
public class MyApplication extends Application {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Other crash libraries
		initFlurry();
		initParse();

		// Make sure that NotifiCrash is initialised last
		NotifiCrash.init(this, "YOUR_SERIAL_NUMBER");

	}

}
```

## Authors and Contributors

Edited for NotifiCrash by Marko Arsic (@marsicdev)

This is refactored and optimised [Sentry for Android](https://github.com/joshdholtz/Sentry-Android) library.
Credits for most of the code goes to @joshdholtz

## Support or Contact

Having trouble with NotifiCrash? Check out the [javadocs](http://cheesecakelabs.github.io/AndroidNotifiCrashLib/javadoc) or contact developer@ckl.io and we’ll help you out.

## License

NotifiCrash is available under the MIT license. See the [LICENSE](https://github.com/CheesecakeLabs/AndroidNotifiCrashLib/blob/master/LICENSE) file for more info.
