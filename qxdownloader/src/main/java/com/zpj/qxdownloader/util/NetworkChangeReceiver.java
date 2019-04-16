package com.zpj.qxdownloader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.zpj.qxdownloader.QianXun;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static NetworkChangeReceiver networkChangeReceiver;

    public static NetworkChangeReceiver getInstance() {
        if (networkChangeReceiver == null) {
            networkChangeReceiver = new NetworkChangeReceiver();
        }
        return networkChangeReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            Toast.makeText(context, "当前网络可用", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT).show();
            QianXun.pauseAll();
        }
    }

}
