package com.dezudio.android.controlsevaluation.labstudy;


import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SessionContent {

    // List of user sessions
    public static List<SessionContent.SessionItem> ITEMS = new ArrayList<>();

    // User session details
    public static Map<String, SessionContent.SessionItem> ITEM_MAP = new HashMap<>();

    private static final String SESSIONS_FILENAME = "sessions2.txt";
    private static final String TIMINGS_FILENAME = "timings2.txt";

    public SessionContent(Context context) {
        readFromInternalStorage(context);
    }

    public void saveToInternalStorage(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(SESSIONS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(ITEMS);
            of.flush();
            of.close();
            fos.close();

            fos = context.openFileOutput(TIMINGS_FILENAME, Context.MODE_PRIVATE);
            of = new ObjectOutputStream(fos);
            of.writeObject(ITEM_MAP);
            of.flush();
            of.close();
            fos.close();
        } catch (Exception e) {
            Log.e("InternalStorage", e.getMessage());
        }
    }

    public void readFromInternalStorage(Context context) {
        try {
            FileInputStream fis = context.openFileInput(SESSIONS_FILENAME);
            ObjectInputStream oi = new ObjectInputStream(fis);
            ITEMS = (List<SessionItem>) oi.readObject();
            oi.close();

            fis = context.openFileInput(TIMINGS_FILENAME);
            oi = new ObjectInputStream(fis);
            ITEM_MAP = (Map<String, SessionContent.SessionItem>) oi.readObject();
        } catch (Exception e) {
            Log.e("InternalStorageII", e.getMessage());
        }

    }

    public void addItem(Context context, SessionContent.SessionItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
        saveToInternalStorage(context);
    }

    private static SessionContent.SessionItem createSessionItem(int position) {
        return new SessionContent.SessionItem(String.valueOf(position), "Item " + position);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A session item representing one user's session
     */
    public static class SessionItem implements Serializable {
        public final String id;
        public final String content;

        public SessionItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
