package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.downloader.ZDownloader;

/**
 * @author Z-P-J
 * */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ZDownloader.config(this)
                .setBlockSize(1024 * 1024)
                .setNotificationInterceptor(new DownloadNotificationInterceptor())
//                .setThreadCount(5)
                .setRetryCount(10)
//                .setProxy(Proxy.NO_PROXY)
//                .setProxy("127.0.0.1", 80)
//                .setUserAgent("")
//                .setCookie("")
                .init();
    }

}
