package com.dezudio.android.controlsevaluation.buttoncontrolmobile;

import java.util.HashMap;

public class ButtonGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String HEART_RATE_MEASUREMENT = "c97433f1-be8f-4dc8-b6f0-5343e6100eb4";//""00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        //attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("c97433f0-be8f-4dc8-b6f0-5343e6100eb4", "Button Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Button Press Index");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
