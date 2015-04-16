package io.ckl.notifibug.handlers;

import java.lang.Thread.UncaughtExceptionHandler;

import io.ckl.notifibug.NotifiBug;
import io.ckl.notifibug.events.EventBuilder;
import io.ckl.notifibug.events.EventRequest;

import static io.ckl.notifibug.helpers.LogHelper.i;

public class CrashExceptionHandler implements UncaughtExceptionHandler {

    private static UncaughtExceptionHandler defaultExceptionHandler;

    // constructor
    public CrashExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler) {
        defaultExceptionHandler = pDefaultExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Here you should have a more robust, permanent record of problems
        EventBuilder builder = new EventBuilder(throwable);

        if (NotifiBug.getInstance().getCaptureListener() != null) {
            builder = NotifiBug.getInstance().getCaptureListener().beforeCapture(builder);
        }

        if (builder != null) {
            NotifiBug.startPostTask(new EventRequest(builder));
        } else {
            i("Builder in UncaughtException is null");
        }

        //call original handler
        defaultExceptionHandler.uncaughtException(thread, throwable);
    }
}
