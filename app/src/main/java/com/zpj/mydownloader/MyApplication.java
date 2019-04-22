package com.zpj.mydownloader;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.zpj.qxdownloader.QianXun;
import com.zpj.qxdownloader.config.QianXunConfig;

import java.net.Proxy;
import java.security.MessageDigest;
import java.util.Locale;

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
//                .setThreadPoolConfig(
//                        ThreadPoolConfig.build()
//                        .setCorePoolSize(5)
//                        .setMaximumPoolSize(36)
//                        .setKeepAliveTime(60)
//                        .setWorkQueue(new LinkedBlockingQueue<Runnable>())
//                        .setHandler(new ThreadPoolExecutor.AbortPolicy())
//                        .setThreadFactory(new ThreadFactory() {
//                            @Override
//                            public Thread newThread(Runnable r) {
//                                return new Thread(r);
//                            }
//                        })
//                )
                .setRetryCount(10)
//                .setProxy(Proxy.NO_PROXY)
//                .setProxy("127.0.0.1", 80)
//                .setUserAgent("")
                .setCookie("");
        QianXun.init(options);
    }

}
