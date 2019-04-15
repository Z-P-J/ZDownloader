package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.qxdownloader.QianXun;
import com.zpj.qxdownloader.option.QianXunOptions;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QianXunOptions options = QianXunOptions.with(this)
//                .setBlockSize(1024 * 1024)
                .setThreadCount(5)
                .setRetryCount(10)
//                .setUserAgent("")
                .setCookie("");
        QianXun.register(options);
    }

}
