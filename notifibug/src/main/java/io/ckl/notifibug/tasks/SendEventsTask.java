package io.ckl.notifibug.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import io.ckl.notifibug.Config;
import io.ckl.notifibug.NotifiBug;
import io.ckl.notifibug.data.InternalStorage;
import io.ckl.notifibug.events.NotifiBugEventRequest;

public class SendEventsTask extends AsyncTask<Void, Void, Void> {

    private NotifiBugEventRequest eventRequest;

    public SendEventsTask(NotifiBugEventRequest eventRequest) {
        this.eventRequest = eventRequest;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.e(Config.TAG, eventRequest.getRequestData());
        boolean sendSuccessful = false;
        try {
            Log.d(Config.TAG, "NotifiCrash trying to post data " + eventRequest.getRequestData());
            sendSuccessful = NotifiBug.tryCrashPost(eventRequest.getRequestData()).isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(Config.TAG, "NotifiCrash sending failed");
        } finally {
            Log.d(Config.TAG, "NotifiCrash post to server successful " + String.valueOf(sendSuccessful).toUpperCase());
        }

        if (sendSuccessful) {
            InternalStorage.getInstance().removeBuilder(eventRequest);
        } else {
            InternalStorage.getInstance().addRequest(eventRequest);
        }

        return null;
    }
}
