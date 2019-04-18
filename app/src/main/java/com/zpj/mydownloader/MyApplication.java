package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.qxdownloader.QianXun;
import com.zpj.qxdownloader.config.QianXunConfig;
import com.zpj.qxdownloader.config.ThreadPoolConfig;

/**
 * @author Z-P-J
 * */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QianXunConfig options = QianXunConfig.with(this)
                .setBlockSize(1024 * 1024)
//                .setThreadCount(5)
                .setThreadPoolConfig(ThreadPoolConfig.build().setCorePoolSize(5))
                .setRetryCount(10)
//                .setUserAgent("")
                .setCookie("");
        QianXun.init(options);
    }

}
