package io.ckl.notifibug.data;

import android.content.Context;
import android.util.Log;

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
import io.ckl.notifibug.events.NotifiBugEventRequest;

public class InternalStorage {

    private final static String FILE_NAME = Config.STORAGE_UNSENT_REPORTS_FILE;
    private ArrayList<NotifiBugEventRequest> unsentRequests;

    public static InternalStorage getInstance() {
        return LazyHolder.instance;
    }

    private static class LazyHolder {
        private static InternalStorage instance = new InternalStorage();
    }

    private InternalStorage() {
        this.unsentRequests = this.readObject(NotifiBug.getInstance().getContext());
    }

    /**
     * Returns list of unsent requests
     *
     * @return ArrayList
     */
    public ArrayList<NotifiBugEventRequest> getUnsentRequests() {
        return unsentRequests;
    }

    /**
     * Adding unsent request to list of unsent requests
     *
     * @param request NotifiBugEventRequest
     */
    public void addRequest(NotifiBugEventRequest request) {
        synchronized (this) {
            Log.i(Config.TAG, "Adding request - " + request.getUuid());
            if (!this.unsentRequests.contains(request)) {
                this.unsentRequests.add(request);
                this.writeObject(NotifiBug.getInstance().getContext(), getUnsentRequests());
            }
        }
    }

    /**
     * Removing request from unsent requests list
     *
     * @param request NotifiBugEventRequest
     */
    public void removeBuilder(NotifiBugEventRequest request) {
        synchronized (this) {
            Log.i(Config.TAG, "Removing request - " + request.getUuid());
            this.unsentRequests.remove(request);
            this.writeObject(NotifiBug.getInstance().getContext(), getUnsentRequests());
        }
    }

    /**
     * Writes list of request to file
     *
     * @param context Context
     * @param requests NotifiBugEventRequest
     */
    private void writeObject(Context context, ArrayList<NotifiBugEventRequest> requests) {
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
     * Reads data from file
     *
     * @param context Context
     * @return ArrayList
     */
    private ArrayList<NotifiBugEventRequest> readObject(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            return (ArrayList<NotifiBugEventRequest>) ois.readObject();
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
