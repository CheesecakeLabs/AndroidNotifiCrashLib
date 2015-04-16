package io.ckl.notifibug.helpers;

import android.util.Log;

import io.ckl.notifibug.Config;
import io.ckl.notifibug.NotifiBug;

@SuppressWarnings("UnusedDeclaration")
public class LogHelper {

    /**
     * @param tag      String
     * @param messages Object
     */
    public static void v(String tag, Object... messages) {
        // Only log VERBOSE if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(tag, Log.VERBOSE, null, messages);
        }
    }

    /**
     * @param tag      String
     * @param messages Object
     */
    public static void d(String tag, Object... messages) {
        // Only log DEBUG if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(tag, Log.DEBUG, null, messages);
        }
    }

    /**
     * @param tag      String
     * @param messages Object
     */
    public static void e(String tag, Object... messages) {
        // Only log ERROR if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(tag, Log.ERROR, null, messages);
        }
    }

    /**
     * @param messages Object
     */
    public static void e(Object... messages) {
        // Only log ERROR if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(Config.TAG, Log.ERROR, null, messages);
        }
    }

    /**
     * @param tag      String
     * @param messages Object
     */
    public static void i(String tag, Object... messages) {
        // Only log ERROR if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(tag, Log.INFO, null, messages);
        }
    }

    /**
     * @param messages String
     */
    public static void i(String messages) {
        // Only log ERROR if NotifiBug.setDebug(true)
        if (NotifiBug.debugEnabled()) {
            log(Config.TAG, Log.INFO, null, messages);
        }
    }

    /**
     * @param tag      String
     * @param level    Int
     * @param t        Throwable
     * @param messages Object...
     */
    public static void log(String tag, int level, Throwable t, Object... messages) {
        if (Log.isLoggable(tag, level)) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
        }
    }
}