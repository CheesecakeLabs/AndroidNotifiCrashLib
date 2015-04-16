package io.ckl.notifibug;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import io.ckl.notifibug.data.InternalStorage;
import io.ckl.notifibug.events.EventBuilder;
import io.ckl.notifibug.events.EventLevel;
import io.ckl.notifibug.events.EventRequest;
import io.ckl.notifibug.events.EventsListener;
import io.ckl.notifibug.handlers.CrashExceptionHandler;
import io.ckl.notifibug.helpers.CrashHelper;
import io.ckl.notifibug.tasks.SendEvents;

import static io.ckl.notifibug.helpers.LogHelper.i;

public class NotifiBug {

    private static final String NOTIFIBUG_THREAD = "NotifiBugThread";

    private Context mContext;

    private String mBaseUrl;
    private String mPackageName;
    private String mSerialNumber;
    private static Boolean mDebug;

    private EventsListener mCaptureListener;

    private NotifiBug() {
    }

    public static NotifiBug getInstance() {
        return LazyHolder.instance;
    }

    private static class LazyHolder {
        private static NotifiBug instance = new NotifiBug();
    }

    /**
     * @param context The current Context or Activity that this method is called from
     * @param key     String
     */
    public static void init(Context context, String key) {
        NotifiBug.init(context, Config.API_ENDPOINT_URL, key);
    }

    /**
     * @param context      The current Context or Activity that this method is called from
     * @param baseUrl      String
     * @param serialNumber String
     */
    public static void init(Context context, String baseUrl, String serialNumber) {
        NotifiBug.mDebug = debugEnabled();
        NotifiBug.getInstance().mContext = context;
        NotifiBug.getInstance().mBaseUrl = baseUrl;
        NotifiBug.getInstance().mSerialNumber = serialNumber;
        NotifiBug.getInstance().mPackageName = context.getPackageName();
        NotifiBug.getInstance().setupUncaughtExceptionHandler();
    }

    public static void setDebug(boolean debug) {
        NotifiBug.mDebug = debug;
    }

    /**
     * @return boolean
     */
    public static boolean debugEnabled() {
        return mDebug == null ? false : NotifiBug.mDebug;
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
        return NotifiBug.getInstance().mSerialNumber;
    }

    /**
     * @return String package name
     */
    public static String getPackageName() {
        return NotifiBug.getInstance().mPackageName;
    }

    /**
     * Setup uncaught exceptions handler
     */
    private void setupUncaughtExceptionHandler() {

        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            i("Current handler class=" + currentHandler.getClass().getName());
        }

        i("Set NotifiBug Uncaught Exception Handler");

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
            NotifiBug.doCaptureEventPost(request);
        }
    }

    /**
     * @param captureListener the mCaptureListener to set
     */
    public static void setCaptureListener(EventsListener captureListener) {
        NotifiBug.getInstance().mCaptureListener = captureListener;
    }

    /**
     * @param message String
     */
    public static void captureMessage(String message) {
        NotifiBug.captureMessage(message, EventLevel.INFO);
    }

    /**
     * @param message String
     * @param level   EventLevel
     */
    public static void captureMessage(String message, EventLevel level) {
        NotifiBug.captureEvent(new EventBuilder()
                        .setMessage(message)
                        .setCrashLevel(level)
        );
    }

    /**
     * @param t Throwable
     */
    public static void captureException(Throwable t) {
        NotifiBug.captureException(t, EventLevel.ERROR);
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

        NotifiBug.captureEvent(eventBuilder);
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
            if (stackTrace.toString().contains(NotifiBug.getPackageName())) {
                cause = stackTrace.toString();
                break;
            }
        }
        return cause;
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
        if (NotifiBug.getInstance().getCaptureListener() != null) {

            builder = NotifiBug.getInstance().getCaptureListener().beforeCapture(builder);
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
        } else if (NotifiBug.getInstance().getContext() != null) {

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
        PackageManager pm = NotifiBug.getInstance().mContext.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.ACCESS_NETWORK_STATE, NotifiBug.getInstance().mContext.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_DENIED) {
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) NotifiBug.getInstance().mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
