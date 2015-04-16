package io.ckl.notifibug.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashHelper {

    private static final int NO_LINE_NUMBER = -1;

    /**
     * @param t Throwable
     * @return String
     */
    public static String findName(Throwable t) {
        return t != null ? t.getClass().getSimpleName() : "none";
    }

    /**
     * @param t Throwable
     * @return String
     */
    public static String findStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * @param t Throwable
     * @return String
     */
    public static String findReason(Throwable t) {
        return t != null ? t.getMessage() : "none";
    }

    /**
     * @param t Throwable
     * @return String
     */
    public static String findClassName(Throwable t) {
        StackTraceElement ste = getStackTraceElement(t);
        return ste != null ? ste.getClassName() : "none";
    }

    /**
     * @param t Throwable
     * @return int
     */
    public static int findLineNumber(Throwable t) {
        StackTraceElement ste = getStackTraceElement(t);
        return ste != null ? ste.getLineNumber() : NO_LINE_NUMBER;
    }

    /**
     * @param t Throwable
     * @return String
     */
    public static String findMethod(Throwable t) {
        StackTraceElement ste = getStackTraceElement(t);
        return ste != null ? ste.getMethodName() : "none";
    }

    /**
     * @return long
     */
    public static long findTime() {
        return System.currentTimeMillis();
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
}
