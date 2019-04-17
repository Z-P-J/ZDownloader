package com.zpj.qxdownloader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
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
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(context, "正在使用移动网络", Toast.LENGTH_SHORT).show();
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Toast.makeText(context, "正在使用WIFI", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT).show();
            QianXun.pauseAll();
        }
    }

}
