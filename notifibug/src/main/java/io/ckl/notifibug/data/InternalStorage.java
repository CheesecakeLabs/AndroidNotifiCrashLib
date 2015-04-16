package io.ckl.notifibug.data;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import io.ckl.notifibug.Config;
import io.ckl.notifibug.NotifiBug;
import io.ckl.notifibug.events.EventRequest;

import static io.ckl.notifibug.helpers.LogHelper.i;

public class InternalStorage {

    private final static String FILE_NAME = Config.STORAGE_UNSENT_REPORTS_FILE;
    private ArrayList<EventRequest> mUnsentEvents;

    public static InternalStorage getInstance() {
        return LazyHolder.instance;
    }

    private static class LazyHolder {
        private static InternalStorage instance = new InternalStorage();
    }

    private InternalStorage() {
        mUnsentEvents = this.readObject(NotifiBug.getInstance().getContext());
    }

    /**
     * Returns list of unsent requests
     *
     * @return ArrayList
     */
    public ArrayList<EventRequest> getUnsentEvents() {
        return mUnsentEvents;
    }

    /**
     * Adding unsent events to list of unsent requests
     *
     * @param eventRequest EventRequest
     */
    public void storeUnsentEvent(EventRequest eventRequest) {
        synchronized (this) {
            i("Storing unsent event - " + eventRequest.getUuid());
            if (!this.mUnsentEvents.contains(eventRequest)) {
                this.mUnsentEvents.add(eventRequest);
                this.writeObject(NotifiBug.getInstance().getContext(), getUnsentEvents());
            }
        }
    }

    /**
     * Removing request from unsent requests list
     *
     * @param eventRequest EventRequest
     */
    public void removeSentEvent(EventRequest eventRequest) {
        synchronized (this) {
            i("Removing sent event - " + eventRequest.getUuid());
            this.mUnsentEvents.remove(eventRequest);
            this.writeObject(NotifiBug.getInstance().getContext(), getUnsentEvents());
        }
    }

    /**
     * Writes list of recorded events to file
     *
     * @param context  Context
     * @param requests EventRequest
     */
    private void writeObject(Context context, ArrayList<EventRequest> requests) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(requests);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads events data from file
     *
     * @param context Context
     * @return ArrayList
     */
    private ArrayList<EventRequest> readObject(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);

            return (ArrayList<EventRequest>) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
