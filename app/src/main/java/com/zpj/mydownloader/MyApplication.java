package com.zpj.mydownloader;

import android.app.Application;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.Repository;
import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionDownloader;
import com.zpj.mydownloader.update.UpdateDownloadMission;
import com.zpj.mydownloader.update.UpdateMissionDownloader;
import com.zpj.mydownloader.update.UpdateNotifier;
import com.zpj.mydownloader.update.UpdateRepository;
import com.zpj.mydownloader.utils.DownloadNotifierImpl;

import java.util.List;

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


        DownloaderConfig<DownloadMission> config = MissionDownloader.builder()
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
