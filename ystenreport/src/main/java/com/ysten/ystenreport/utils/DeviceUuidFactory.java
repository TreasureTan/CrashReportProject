package com.ysten.ystenreport.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class DeviceUuidFactory {
	private static final String TAG = "Report_uuidFactory";
	protected static final String PREFS_FILE = "uuid.xml";
	protected static final String PREFS_DEVICE_ID = "uuid";
	protected static UUID uuid;

	public DeviceUuidFactory(Context context) {
		if (uuid == null) {
			synchronized (DeviceUuidFactory.class) {
				if (uuid == null) {
					final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
					String id = prefs.getString(PREFS_DEVICE_ID, null);
					if (id != null) {
						uuid = UUID.fromString(id);
					} else {
						String deviceId = getDeviceId(context);
						uuid = !TextUtils.isEmpty(deviceId) ? UUID.nameUUIDFromBytes(deviceId.getBytes())
								: UUID.randomUUID();

						prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
					}
				}
			}
		}
	}

	private static String getDeviceId(Context context) {
		StringBuilder deviceId = new StringBuilder();
		try {
			// IMEI（imei）
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (tm != null) {
				String imei = tm.getDeviceId();
				if (!TextUtils.isEmpty(imei)) {
					deviceId.append("imei");
					deviceId.append(imei);
				}
				// 序列号（sn）
				String sn = tm.getSimSerialNumber();
				if (!TextUtils.isEmpty(sn)) {
					deviceId.append("sn");
					deviceId.append(sn);
				}
			}
			
			String serial = "";
			try {
				Class clazz = Class.forName("android.os.Build");
				Class paraTypes = Class.forName("java.lang.String");
				Method method = clazz.getDeclaredMethod("getString", paraTypes);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				serial = (String) method.invoke(new Build(), "ro.serialno");
				deviceId.append(serial);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.i(TAG, "android device id:" + deviceId.toString());
		return deviceId.toString();
	}

	public UUID getDeviceUuid() {
		return uuid;
	}
}