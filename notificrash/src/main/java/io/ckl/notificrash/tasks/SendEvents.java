package io.ckl.notificrash.tasks;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.ckl.notificrash.NotifiCrash;
import io.ckl.notificrash.data.InternalStorage;
import io.ckl.notificrash.events.EventRequest;

import static io.ckl.notificrash.helpers.LogHelper.i;

public class SendEvents extends Thread {

    private EventRequest mEventRequest;

    public SendEvents(EventRequest eventRequest) {
        mEventRequest = eventRequest;
    }

    @Override
    public void run() {

        boolean sendSuccessful = false;
        Response response = null;

        try {
            i("Trying to send crash data");
            response = tryCrashPost(mEventRequest.getEventData());
            sendSuccessful = response.isSuccessful();

            if (!sendSuccessful) {
                i(response.message());
                i(String.valueOf(response.code()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            String sendStatus = sendSuccessful ? "successful" : "failed";
            i("Event post to server " + sendStatus);
        }

        if (sendSuccessful) {
            InternalStorage.getInstance().removeSentEvent(mEventRequest);
        } else {
            InternalStorage.getInstance().storeUnsentEvent(mEventRequest);
        }
    }

    /**
     * @param json String
     * @return Response
     * @throws java.io.IOException IOException
     */
    private Response tryCrashPost(String json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(NotifiCrash.getInstance().getBaseUrl())
                .post(body)
                .build();
        return client.newCall(request).execute();
    }
}
