package io.ckl.notificrash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;

import io.ckl.notificrash.data.InternalStorage;
import io.ckl.notificrash.events.EventBuilder;
import io.ckl.notificrash.events.EventLevel;
import io.ckl.notificrash.events.EventRequest;
import io.ckl.notificrash.events.EventsListener;
import io.ckl.notificrash.handlers.CrashExceptionHandler;
import io.ckl.notificrash.helpers.CrashHelper;
import io.ckl.notificrash.tasks.SendEvents;

import static io.ckl.notificrash.helpers.LogHelper.i;

public class NotifiCrash {

    private static final String NOTIFIBUG_THREAD = "NotifiCrashThread";

    private Context mContext;

    private String mBaseUrl;
    private String mPackageName;
    private String mSerialNumber;
    private static Boolean mDebug;
    private HashMap<String, String> extraArguments;

    private EventsListener mCaptureListener;

    private NotifiCrash() {
        extraArguments = new HashMap<>();
    }

    public static NotifiCrash getInstance() {
        return LazyHolder.instance;
    }

    public HashMap<String, String> getExtraArguments() {
        HashMap<String,String> applicationExtraArguments;
        try {
            applicationExtraArguments = ((NotifiCrashArguments) mContext).getExtraArguments();
        }
        catch (ClassCastException e) {
            applicationExtraArguments = null;
        }

        extraArguments.putAll(applicationExtraArguments);

        return extraArguments;
    }

    public void addExtra(String mKey, String mValue) {
        extraArguments.put(mKey, mValue);
    }

    private static class LazyHolder {
        private static NotifiCrash instance = new NotifiCrash();
    }

    /**
     * @param context The current Context or Activity that this method is called from
     * @param key     String
     */
    public static void init(Context context, String key) {
        NotifiCrash.init(context, Config.API_ENDPOINT_URL, key);
    }

    /**
     * @param context      The current Context or Activity that this method is called from
     * @param baseUrl      String
     * @param serialNumber String
     */
    public static void init(Context context, String baseUrl, String serialNumber) {
        NotifiCrash.mDebug = debugEnabled();
        NotifiCrash.getInstance().mContext = context;
        NotifiCrash.getInstance().mBaseUrl = baseUrl;
        NotifiCrash.getInstance().mSerialNumber = serialNumber;
        NotifiCrash.getInstance().mPackageName = context.getPackageName();
        NotifiCrash.getInstance().setupUncaughtExceptionHandler();
    }

    public static void setDebug(boolean debug) {
        NotifiCrash.mDebug = debug;
    }

    /**
     * @return boolean
     */
    public static boolean debugEnabled() {
        return mDebug == null ? false : NotifiCrash.mDebug;
    }

    /**
     * @return Context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * @return String
     */
    public String getBaseUrl() {
        return mBaseUrl;
    }

    /**
     * @return String serial number
     */
    public static String getSerialNumber() {
        return NotifiCrash.getInstance().mSerialNumber;
    }

    /**
     * @return String package name
     */
    public static String getPackageName() {
        return NotifiCrash.getInstance().mPackageName;
    }

    /**
     * Setup uncaught exceptions handler
     */
    private void setupUncaughtExceptionHandler() {

        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            i("Current handler class=" + currentHandler.getClass().getName());
        }

        i("Set NotifiCrash Uncaught Exception Handler");

        // don't register again if already registered
        if (!(currentHandler instanceof CrashExceptionHandler)) {
            // Register default exceptions handler
            Thread.setDefaultUncaughtExceptionHandler(
                    new CrashExceptionHandler(currentHandler));
        }

        sendAllCachedCapturedEvents();
    }

    /**
     *
     */
    public void sendAllCachedCapturedEvents() {
        ArrayList<EventRequest> unsentRequests = InternalStorage.getInstance().getUnsentEvents();
        i("Sending up " + unsentRequests.size() + " stored response(s)");
        for (EventRequest request : unsentRequests) {
            i("Sending up " + request + " stored response(s)");
            NotifiCrash.doCaptureEventPost(request);
        }
    }

    /**
     * @param captureListener the mCaptureListener to set
     */
    public static void setCaptureListener(EventsListener captureListener) {
        NotifiCrash.getInstance().mCaptureListener = captureListener;
    }

    /**
     * @param message String
     */
    public static void captureMessage(String message) {
        NotifiCrash.captureMessage(message, EventLevel.INFO);
    }

    /**
     * @param message String
     * @param level   EventLevel
     */
    public static void captureMessage(String message, EventLevel level) {
        NotifiCrash.captureEvent(new EventBuilder()
                        .setMessage(message)
                        .setCrashLevel(level)
        );
    }

    /**
     * @param t Throwable
     */
    public static void captureException(Throwable t) {
        NotifiCrash.captureException(t, EventLevel.ERROR);
    }

    /**
     * @param t     Throwable
     * @param level EventLevel
     */
    public static void captureException(Throwable t, EventLevel level) {

        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setName(CrashHelper.findClassName(t));
        eventBuilder.setReason(CrashHelper.findReason(t));
        eventBuilder.setTimestamp(CrashHelper.findTime());
        eventBuilder.setLineNumber(CrashHelper.findLineNumber(t));
        eventBuilder.setClassName(CrashHelper.findClassName(t));
        eventBuilder.setMethodName(CrashHelper.findMethod(t));
        eventBuilder.setStacktrace(CrashHelper.findStackTrace(t));

        NotifiCrash.captureEvent(eventBuilder);
    }

    /**
     * @param context The current Context or Activity that this method is called from
     * @param t       Throwable
     */
    public static void captureUncaughtException(Context context, Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        try {
            // Random number to avoid duplicate files
            long random = System.currentTimeMillis();

            // Embed version in stacktrace filename
            File stacktrace = new File(getStacktraceLocation(context), "raven-" + random + ".stacktrace");
            i("Writing unhandled exception to: " + stacktrace.getAbsolutePath());

            // Write the stacktrace to disk
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(stacktrace));
            oos.writeObject(t);
            oos.flush();
            // Close up everything
            oos.close();
        } catch (Exception ebos) {
            // Nothing much we can do about this - the game is over
            ebos.printStackTrace();
        }

        i(result.toString());
    }

    /**
     * @param t     Throwable
     * @param cause String
     * @return String
     */
    public static String getCause(Throwable t, String cause) {
        for (StackTraceElement stackTrace : t.getStackTrace()) {
            if (stackTrace.toString().contains(NotifiCrash.getPackageName())) {
                cause = stackTrace.toString();
                break;
            }
        }
        return cause;
    }

    /**
     * @return Application version name string
     */
    public String getAppVersion() {
        String appVersionString;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            appVersionString = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionString = "unknown";
            e.printStackTrace();
        }

        return appVersionString;
    }

    /**
     * @return Information about device
     */
    public String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER.toUpperCase();
        String model = Build.MODEL.toUpperCase();

        return String.format("%s - %s", manufacturer, model);
    }

    /**
     * @return Android sdk version
     */
    public String getAndroidVersion() {
        return String.format("Android %s (%s)", Build.VERSION.RELEASE, Build.VERSION.CODENAME);
    }


    /**
     * @param context The current Context or Activity that this method is called from
     * @return File
     */
    private static File getStacktraceLocation(Context context) {
        return new File(context.getCacheDir(), "crashes");
    }

    /**
     * @return EventRequest
     */
    public EventsListener getCaptureListener() {
        return mCaptureListener;
    }

    /**
     * @param builder EventRequest
     */
    public static void captureEvent(EventBuilder builder) {
        final EventRequest request;
        if (NotifiCrash.getInstance().getCaptureListener() != null) {

            builder = NotifiCrash.getInstance().getCaptureListener().beforeCapture(builder);
            if (builder == null) {
                i("Builder in CaptureEvent is null");
                return;
            }

            request = new EventRequest(builder);
        } else {
            request = new EventRequest(builder);
        }

        // Check if on main thread - if not, run on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doCaptureEventPost(request);
        } else if (NotifiCrash.getInstance().getContext() != null) {

            HandlerThread handler = new HandlerThread(NOTIFIBUG_THREAD);
            handler.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    doCaptureEventPost(request);
                }
            };
            Handler h = new Handler(handler.getLooper());
            h.post(runnable);

        }
    }

    /**
     * @return true if Network is available and should try post
     */
    private static boolean shouldAttemptPost() {
        PackageManager pm = NotifiCrash.getInstance().mContext.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.ACCESS_NETWORK_STATE, NotifiCrash.getInstance().mContext.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_DENIED) {
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) NotifiCrash.getInstance().mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @param request EventRequest
     */
    private static void doCaptureEventPost(EventRequest request) {
        startPostTask(request);
    }

    public static void startPostTask(EventRequest eventRequest) {
        if (shouldAttemptPost()) {
            SendEvents sendCrashes = new SendEvents(eventRequest);
            sendCrashes.start();
        } else {
            InternalStorage.getInstance().storeUnsentEvent(eventRequest);
        }
    }
}
