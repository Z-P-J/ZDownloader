package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionDownloader;
import com.zpj.mydownloader.utils.DownloadNotifierImpl;

/**
 * @author Z-P-J
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        ZDownloader.config(this)
//                .setBlockSize(1024 * 1024)
//                .setNotificationInterceptor(new DownloadNotificationInterceptor())
////                .setThreadCount(5)
//                .setRetryCount(10)
////                .setProxy(Proxy.NO_PROXY)
////                .setProxy("127.0.0.1", 80)
////                .setUserAgent("")
////                .setCookie("")
//                .setConflictPolicy(new DefaultConflictPolicy())
//                .init();

        // 注册Downloader
//        ZDownloader.register(DownloadMission.class, new MissionDownloader());


        DownloaderConfig<DownloadMission> config = MissionDownloader.config()
                // 设置状态栏通知回调
                .setNotifier(new DownloadNotifierImpl())
                .build();

        ZDownloader.register(DownloadMission.class, new MissionDownloader(config));


//        DownloaderConfig<UpdateDownloadMission> updateConfig = new DownloaderConfig
//                .Builder<UpdateDownloadMission>("downloader_app_update")
//                // 设置状态栏通知回调
//                .setNotifier(new UpdateNotifier())
//                .setRepository(new UpdateRepository(this))
//                .build();
//
//        ZDownloader.register(UpdateDownloadMission.class, new UpdateMissionDownloader(updateConfig));

    }

}
