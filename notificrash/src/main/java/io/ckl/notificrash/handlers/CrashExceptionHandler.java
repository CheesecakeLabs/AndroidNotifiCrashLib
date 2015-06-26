package io.ckl.notificrash.handlers;

import java.lang.Thread.UncaughtExceptionHandler;

import io.ckl.notificrash.NotifiCrash;
import io.ckl.notificrash.events.EventBuilder;
import io.ckl.notificrash.events.EventRequest;

import static io.ckl.notificrash.helpers.LogHelper.i;

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

        if (NotifiCrash.getInstance().getCaptureListener() != null) {
            builder = NotifiCrash.getInstance().getCaptureListener().beforeCapture(builder);
        }

        if (builder != null) {
            NotifiCrash.startPostTask(new EventRequest(builder));
        } else {
            i("Builder in UncaughtException is null");
        }

        //call original handler
        defaultExceptionHandler.uncaughtException(thread, throwable);
    }
}
