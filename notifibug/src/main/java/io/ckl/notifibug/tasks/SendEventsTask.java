package io.ckl.notifibug.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Response;

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
        Response response = null;

        try {
            Log.i(Config.TAG, "NotifiCrash trying to post data: " + eventRequest.getRequestData());
            response = NotifiBug.tryCrashPost(eventRequest.getRequestData());
            sendSuccessful = response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(Config.TAG, "NotifiCrash response message: " + response.message());
        } finally {
            Log.i(Config.TAG, "NotifiCrash response message: " + response.message());
            Log.i(Config.TAG, "NotifiCrash post to server successful " + String.valueOf(sendSuccessful).toUpperCase());
        }

        if (sendSuccessful) {
            InternalStorage.getInstance().removeBuilder(eventRequest);
        } else {
            InternalStorage.getInstance().addRequest(eventRequest);
        }

        return null;
    }
}
