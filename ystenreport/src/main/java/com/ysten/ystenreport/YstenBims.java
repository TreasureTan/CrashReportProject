package com.ysten.ystenreport;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * Created by tanxiaozhong
 * on 2019-10-25 13:53.
 * describe:
 */
public class YstenBims {
    private static final String TAG = YstenBims.class.getSimpleName();
    public static final String COLUMN_KEY = "Config_Key";
    public static final String COLUMN_VALUE = "Config_Value";
    public static final String TAB_NAME = "Config";

    public static String getValue(Context context, Uri uri, String key) {
        Log.d(TAG, "getValue() called with: context = [" + context + "], uri = [" + uri + "], key = [" + key + "]");
        if (context == null) {
            Log.e(TAG, "Error : getValue context is null.");
            return "";
        }
        String result = "";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{COLUMN_KEY, COLUMN_VALUE}, COLUMN_KEY + " = ?",
                new String[]{key}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String sKey = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
                    result = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
                    Log.d(TAG, "getValue: Key->" + sKey + " Value->" + result);
                }
            }
            cursor.close();
        }
        return result;
    }

    public static Uri getConnectUri(String packageName) {
        String AUTHORITIES = packageName + ".DataProvider";
        return Uri.parse("content://" + AUTHORITIES + "/" +TAB_NAME);
    }
}
