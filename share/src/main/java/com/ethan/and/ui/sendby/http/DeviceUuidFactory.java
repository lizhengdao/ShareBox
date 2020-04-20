package com.ethan.and.ui.sendby.http;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class DeviceUuidFactory {


    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected volatile static UUID uuid;

    public DeviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        try {
                            uuid = UUID.fromString(id);
                        } catch (Exception e) {
                            generateAndSave(prefs);
                        }
                    } else {
                        generateAndSave(prefs);
                    }
                }
            }
        }
    }

    private void generateAndSave(SharedPreferences prefs) {
        try {
            uuid = UUID.randomUUID();
            prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a unique UUID for the current android device. As with all UUIDs,
     * this unique ID is "very highly likely" to be unique across all Android
     * devices. Much more so than ANDROID_ID is.
     * <p>
     * The UUID is generated by using ANDROID_ID RegionLevel the base key if appropriate,
     * falling back on TelephonyManager.getDeviceID() if ANDROID_ID is known to
     * be incorrect, and finally falling back on a random UUID that's persisted
     * to SharedPreferences if getDeviceID() does not return a usable value.
     * <p>
     * In some rare circumstances, this ID may change. In particular, if the
     * device is factory reset a new device ID may be generated. In addition, if
     * a user upgrades their phone from certain buggy implementations of Android
     * 2.2 to a newer, non-buggy version of Android, the device ID may change.
     * Or, if a user uninstalls your app on a device that has neither a proper
     * Android ID nor a Device ID, this ID may change on reinstallation.
     * <p>
     * Note that if the code falls back on using TelephonyManager.getDeviceId(),
     * the resulting ID will NOT change after a factory reset. Something to be
     * aware of.
     * <p>
     * Works around a bug in Android 2.2 for many devices when using ANDROID_ID
     * directly.
     *
     * @return a UUID that may be used to uniquely identify your device for most
     * purposes.
     * @see https://code.google.com/p/android/issues/detail?id=10603
     */
    public UUID getDeviceUuid() {
        return uuid;
    }

//    @Nullable
//    public static String getUUidString(@NotNull Context ctx) {
//        String KEY_UUID = "key_uuid";
//        DocumentMessage doc = DocumentMessage.Companion.getDoc();
//        String uuid = doc.getMessage(KEY_UUID,null);
//        doc.reset();
//        if (uuid != null && uuid.trim().length() != 0)
//            return uuid;
//
//        uuid = UUID.randomUUID().toString();
//        uuid = Base64.encodeToString(uuid.getBytes(), Base64.DEFAULT);
//        DocumentMessage.Companion.getDoc().putMessage(KEY_UUID,uuid).reset();
//        return uuid;
//    }
}