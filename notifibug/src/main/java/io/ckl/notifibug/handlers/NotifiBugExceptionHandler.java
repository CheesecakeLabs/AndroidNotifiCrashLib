package io.ckl.notifibug.handlers;

import android.content.Context;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;

import io.ckl.notifibug.Config;
import io.ckl.notifibug.NotifiBug;
import io.ckl.notifibug.events.NotifiBugEventBuilder;
import io.ckl.notifibug.events.NotifiBugEventLevel;
import io.ckl.notifibug.events.NotifiBugEventRequest;
import io.ckl.notifibug.tasks.SendEventsTask;

public class NotifiBugExceptionHandler implements UncaughtExceptionHandler {

    private static UncaughtExceptionHandler defaultExceptionHandler;

    // constructor
    public NotifiBugExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler, Context context) {
        defaultExceptionHandler = pDefaultExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Here you should have a more robust, permanent record of problems
        NotifiBugEventBuilder builder = new NotifiBugEventBuilder(throwable, NotifiBugEventLevel.FATAL);
        if (NotifiBug.getInstance().getCaptureListener() != null) {
            builder = NotifiBug.getInstance().getCaptureListener().beforeCapture(builder);
        }

        if (builder != null) {
            NotifiBugEventRequest request = new NotifiBugEventRequest(builder);

            SendEventsTask sendCrashes = new SendEventsTask(request);
            sendCrashes.execute();

        } else {
            Log.e(Config.TAG, "NotifiBugEventBuilder in uncaughtException is null");
        }

        //call original handler
        defaultExceptionHandler.uncaughtException(thread, throwable);
    }
}
