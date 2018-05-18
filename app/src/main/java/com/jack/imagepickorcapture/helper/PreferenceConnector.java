package com.jack.imagepickorcapture.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceConnector {
	public static final String PREF_NAME = "PEOPLE_PREFERENCESN";
	public static final int MODE = Context.MODE_PRIVATE;

	public static void writeBoolean(Context context, String key, boolean value) {
		SharedPreferencesCompat
				.apply(getEditor(context).putBoolean(key, value));
	}

	public static boolean readBoolean(Context context, String key,
			boolean defValue) {
		return getPreferences(context).getBoolean(key, defValue);
	}

	public static void writeInteger(Context context, String key, int value) {
		SharedPreferencesCompat.apply(getEditor(context).putInt(key, value));
	}

	public static int readInteger(Context context, String key, int defValue) {
		return getPreferences(context).getInt(key, defValue);
	}

	public static void writeString(Context context, String key, String value) {
		SharedPreferencesCompat.apply(getEditor(context).putString(key, value));
	}

	public static String readString(Context context, String key, String defValue) {
		return getPreferences(context).getString(key, defValue);
	}

	public static void writeFloat(Context context, String key, float value) {
		SharedPreferencesCompat.apply(getEditor(context).putFloat(key, value));
	}

	public static float readFloat(Context context, String key, float defValue) {
		return getPreferences(context).getFloat(key, defValue);
	}

	public static void writeLong(Context context, String key, long value) {
		SharedPreferencesCompat.apply(getEditor(context).putLong(key, value));
	}

	public static long readLong(Context context, String key, long defValue) {
		return getPreferences(context).getLong(key, defValue);
	}

	public static SharedPreferences getPreferences(Context context) {
		if (context != null) {
			return context.getSharedPreferences(PREF_NAME, MODE);
		} else {
			return null;
		}
	}

	public static Editor getEditor(Context context) {
		return getPreferences(context).edit();
	}

	public static void deleteVariable(Context context, String key) {
		getEditor(context).remove(key);
	}
}
