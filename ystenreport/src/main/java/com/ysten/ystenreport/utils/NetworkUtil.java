/**
 * @name Baikal
 * @class name：com.ysten.baikal.util.NetworkUtil
 * @class describe：NetworkUtil 网络工具类
 * @author Dawn liuwei_wx@ysten.com
 * @time 2018/7/11 15:20
 */

package com.ysten.ystenreport.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkUtil {
    private static final String GLOBAL_TAG = "BAIKAL === ";
    private static final String TAG = GLOBAL_TAG + NetworkUtil.class.getSimpleName();
    private static final String TD_SCDMA = "TD-SCDMA";
    private static final String WCDMA = "WCDMA";
    private static final String CDMA2000 = "CDMA2000";
    public static final int NETWORK_DISCONNECTED = -1;
    public static final int NETWORK_UNKNOWN = 0;
    public static final int NETWORK_LAN = 1;
    public static final int NETWORK_WIFI = 2;
    public static final int NETWORK_2G = 3;
    public static final int NETWORK_3G = 4;
    public static final int NETWORK_4G = 5;

    private static ConnectivityManager connectivityManager;

    public static int getNetworkType(Context context){
        int netType = NETWORK_DISCONNECTED;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null){
            Log.d(TAG, "Constructor ConnectivityManager failed");
            return netType;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            Log.d(TAG, "Constructor ActiveNetworkInfo failed");
            return netType;
        }

        if (networkInfo.isAvailable()) {

            if(networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET){
                netType = NETWORK_LAN;
            }else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NETWORK_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (networkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NETWORK_2G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NETWORK_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NETWORK_4G;
                        break;
                    default:
                        String subtypeName = networkInfo.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase(TD_SCDMA)
                                || subtypeName.equalsIgnoreCase(WCDMA)
                                || subtypeName.equalsIgnoreCase(CDMA2000)){
                            netType = NETWORK_3G;
                        } else {
                            netType = NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NETWORK_UNKNOWN;
            }
        }
        return netType;
    }
    public static String getNetworkTypeStr(Context context){
        String netType = "";
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null){
            Log.d(TAG, "Constructor ConnectivityManager failed");
            return netType;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            Log.d(TAG, "Constructor ActiveNetworkInfo failed");
            return netType;
        }

        if (networkInfo.isAvailable()) {

            if(networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET){
                netType = "lan";
            }else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = "wifi";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (networkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = "2g";
                        break;
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = "3g";
                        break;
                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = "4g";
                        break;
                    default:
                        String subtypeName = networkInfo.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase(TD_SCDMA)
                                || subtypeName.equalsIgnoreCase(WCDMA)
                                || subtypeName.equalsIgnoreCase(CDMA2000)){
                            netType = "3g";
                        } else {
                            netType = "unknow";
                        }
                        break;
                }
            } else {
                netType = "unknow";
            }
        }
        return netType;
    }
}
