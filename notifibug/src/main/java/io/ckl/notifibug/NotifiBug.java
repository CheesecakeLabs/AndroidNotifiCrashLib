package io.ckl.notifibug;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import io.ckl.notifibug.data.InternalStorage;
import io.ckl.notifibug.events.NotifiBugEventBuilder;
import io.ckl.notifibug.events.NotifiBugEventLevel;
import io.ckl.notifibug.events.NotifiBugEventRequest;
import io.ckl.notifibug.events.NotifiBugEventsListener;
import io.ckl.notifibug.handlers.NotifiBugExceptionHandler;
import io.ckl.notifibug.tasks.SendEventsTask;

public class NotifiBug {

    private static final int NO_LINE_NUMBER = -1;
    private static final String NOTIFIBUG_THREAD = "NotifiBugThread";

    private Context context;

    private String baseUrl;
    private String packageName;
    private String serialNumber;

    private NotifiBugEventsListener captureListener;

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
     * @param key String
     */
    public static void init(Context context, String key) {
        NotifiBug.init(context, Config.CRASH_SERVICE_API_ENDPOINT_URL, key);
    }

    /**
     * @param context The current Context or Activity that this method is called from
     * @param baseUrl String
     * @param serialNumber String
     */
    public static void init(Context context, String baseUrl, String serialNumber) {
        NotifiBug.getInstance().context = context;
        NotifiBug.getInstance().baseUrl = baseUrl;
        NotifiBug.getInstance().serialNumber = serialNumber;
        NotifiBug.getInstance().packageName = context.getPackageName();
        NotifiBug.getInstance().setupUncaughtExceptionHandler();
    }

    /**
     * @return Context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return String serial number
     */
    public static String getSerialNumber() {
        return NotifiBug.getInstance().serialNumber;
    }

    /**
     * @return String package name
     */
    public static String getPackageName() {
        return NotifiBug.getInstance().packageName;
    }

    /**
     * Setup uncaught exceptions handler
     */
    private void setupUncaughtExceptionHandler() {

        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            Log.e(Config.TAG, "current handler class=" + currentHandler.getClass().getName());
        }
        Log.e(Config.TAG, "setupUncaughtExceptionHandler");

        // don't register again if already registered
        if (!(currentHandler instanceof NotifiBugExceptionHandler)) {
            // Register default exceptions handler
            Thread.setDefaultUncaughtExceptionHandler(
                    new NotifiBugExceptionHandler(currentHandler, getContext()));
        }

        sendAllCachedCapturedEvents();
    }

    /**
     *
     */
    public void sendAllCachedCapturedEvents() {
        ArrayList<NotifiBugEventRequest> unsentRequests = InternalStorage.getInstance().getUnsentRequests();
        Log.i(Config.TAG, "Sending up " + unsentRequests.size() + " cached response(s)");
        for (NotifiBugEventRequest request : unsentRequests) {
            Log.i(Config.TAG, "Sending up " + request + " cached response(s)");
            NotifiBug.doCaptureEventPost(request);
        }
    }

    /**
     * @param captureListener the captureListener to set
     */
    public static void setCaptureListener(NotifiBugEventsListener captureListener) {
        NotifiBug.getInstance().captureListener = captureListener;
    }

    /**
     * @param message String
     */
    public static void captureMessage(String message) {
        NotifiBug.captureMessage(message, NotifiBugEventLevel.INFO);
    }

    /**
     * @param message String
     * @param level NotifiBugEventLevel
     */
    public static void captureMessage(String message, NotifiBugEventLevel level) {
        NotifiBug.captureEvent(new NotifiBugEventBuilder()
                        .setMessage(message)
                        .setCrashLevel(level)
        );
    }

    /**
     * @param t Throwable
     */
    public static void captureException(Throwable t) {
        NotifiBug.captureException(t, NotifiBugEventLevel.ERROR);
    }

    /**
     * @param t Throwable
     * @param level NotifiBugEventLevel
     */
    public static void captureException(Throwable t, NotifiBugEventLevel level) {
        String cause = getCause(t, t.getMessage());

        NotifiBug.captureEvent(new NotifiBugEventBuilder()
                        .setMessage(t.getMessage())
                        .setCrashType(cause)
                        .setCrashLevel(level)
                        .setCrashClass(t.getClass().getSimpleName())
                        .setStacktrace(getStackTrace(t))
        );
    }

    /**
     * @param context The current Context or Activity that this method is called from
     * @param t Throwable
     */
    public static void captureUncaughtException(Context context, Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        try {
            // Random number to avoid duplicate files
            long random = System.currentTimeMillis();

            // Embed version in stacktrace filename
            File stacktrace = new File(getStacktraceLocation(context), "raven-" + String.valueOf(random) + ".stacktrace");
            Log.d(Config.TAG, "Writing unhandled exception to: " + stacktrace.getAbsolutePath());

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

        Log.d(Config.TAG, result.toString());
    }

    /**
     * @param t Throwable
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
     * @param t Throwable
     * @return String
     */
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * @param t Throwable
     * @return String
     */
    public static String getCrashClassName(Throwable t){
        StackTraceElement ste = getStackTraceElement(t);

        return ste != null ? ste.getClassName() : "";
    }

    /**
     * @param t Throwable
     * @return int
     */
    public static int getLineNumber(Throwable t) {

        StackTraceElement ste = getStackTraceElement(t);

        return ste != null ? ste.getLineNumber() : NO_LINE_NUMBER;
    }

    /**
     * @param t Throwable
     * @return StackTraceElement
     */
    public static StackTraceElement getStackTraceElement(Throwable t) {

        for (StackTraceElement ste : t.getStackTrace()) {
            if (!ste.isNativeMethod()) {
                return ste;
            }
        }
        return null;
    }

    /**
     * @return NotifiBugEventRequest
     */
    public NotifiBugEventsListener getCaptureListener() {
        return captureListener;
    }

    /**
     * @param builder NotifiBugEventRequest
     */
    public static void captureEvent(NotifiBugEventBuilder builder) {
        final NotifiBugEventRequest request;
        if (NotifiBug.getInstance().getCaptureListener() != null) {

            builder = NotifiBug.getInstance().getCaptureListener().beforeCapture(builder);
            if (builder == null) {
                Log.e(Config.TAG, "NotifiBugEventBuilder in captureEvent is null");
                return;
            }

            request = new NotifiBugEventRequest(builder);
        } else {
            request = new NotifiBugEventRequest(builder);
        }

        Log.d(Config.TAG, "Request - " + request.getRequestData());

        // Check if on main thread - if not, run on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doCaptureEventPost(request);
        } else if (NotifiBug.getInstance().getContext() != null) {

            HandlerThread thread = new HandlerThread(NOTIFIBUG_THREAD) {
            };
            thread.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    doCaptureEventPost(request);
                }
            };
            Handler h = new Handler(thread.getLooper());
            h.post(runnable);

        }
    }

    /**
     * @return true if Network is available and should try post
     */
    private static boolean shouldAttemptPost() {
        PackageManager pm = NotifiBug.getInstance().context.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.ACCESS_NETWORK_STATE, NotifiBug.getInstance().context.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_DENIED) {
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) NotifiBug.getInstance().context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @param request NotifiBugEventRequest
     */
    private static void doCaptureEventPost(NotifiBugEventRequest request) {

        if (!shouldAttemptPost()) {
            InternalStorage.getInstance().addRequest(request);
            return;
        }

        SendEventsTask sendCrashes = new SendEventsTask(request);
        sendCrashes.execute();

    }

    /**
     * @param json String
     * @return Response
     * @throws java.io.IOException IOException
     */
    public static Response tryCrashPost(String json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;"), json);
        Request request = new Request.Builder()
                .url(Config.CRASH_SERVICE_API_ENDPOINT_URL)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }
}
