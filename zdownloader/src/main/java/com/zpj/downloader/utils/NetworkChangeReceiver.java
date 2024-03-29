package com.zpj.downloader.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Z-P-J
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private volatile static NetworkChangeReceiver networkChangeReceiver;

    public synchronized static NetworkChangeReceiver getInstance() {
        if (networkChangeReceiver == null) {
            synchronized (NetworkChangeReceiver.class) {
                if (networkChangeReceiver == null) {
                    networkChangeReceiver = new NetworkChangeReceiver();
                }
            }
        }
        return networkChangeReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // TODO resumeAll
        } else {
            // TODO waitingAll
        }
    }

}
