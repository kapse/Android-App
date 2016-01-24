/*
 *
 *  Proprietary and confidential. Property of Kellton Tech Solutions Ltd. Do not disclose or distribute.
 *  You must have written permission from Kellton Tech Solutions Ltd. to use this code.
 *
 */

package com.eew.balloonride.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <br/>
 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 */
public class ConnectivityUtils {

    private static final String LOG_TAG = "ConnectivityUtils";

    /**
     * @param pContext
     * @return
     * @note android.permission.ACCESS_NETWORK_STATE is required
     */
    public static boolean isNetworkEnabled(Context pContext) {
        NetworkInfo activeNetwork = getActiveNetwork(pContext);
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * @param pContext
     * @return
     * @note android.permission.ACCESS_NETWORK_STATE is required
     */
    public static void logNetworkState(Context pContext) {
        NetworkInfo activeNetwork = getActiveNetwork(pContext);
        if (activeNetwork == null) {
            return;
        }
    }

    /**
     * @param pContext
     * @return
     * @note android.permission.ACCESS_NETWORK_STATE is required
     */
    public static NetworkInfo getActiveNetwork(Context pContext) {
        ConnectivityManager conMngr = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMngr == null ? null : conMngr.getActiveNetworkInfo();
    }


    /**
     * @param pContext
     * @return
     * @note android.permission.READ_PHONE_STATE is required
     */
    public static String getDeviceId(Context pContext) {
        TelephonyManager telephonyManager = (TelephonyManager) pContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}