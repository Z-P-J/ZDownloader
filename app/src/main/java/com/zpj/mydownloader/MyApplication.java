package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.config.QianXunConfig;

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
        ZDownloader.init(options);
    }

}
