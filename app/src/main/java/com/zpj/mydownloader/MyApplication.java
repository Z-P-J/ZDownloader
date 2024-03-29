package com.zpj.mydownloader;

import android.app.Activity;
import android.app.Application;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.impl.DefaultConflictPolicy;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionDownloader;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.mydownloader.utils.ActivityManager;
import com.zpj.mydownloader.utils.DownloadNotifierImpl;

import java.lang.ref.WeakReference;

/**
 * @author Z-P-J
 */
public class MyApplication extends Application {

    private static WeakReference<Activity> sActivity;

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


        ActivityManager.init(this);


        DownloaderConfig<DownloadMission> config = MissionDownloader.config()
                // 设置状态栏通知回调
                .setNotifier(new DownloadNotifierImpl())
                // 设置任务冲突处理策略
                .setConflictPolicy(new DefaultConflictPolicy() {

                    @Override
                    public void onConflict(Mission mission, Callback callback) {
                        Activity activity = ActivityManager.getCurrentActivity();
                        if (activity == null) {
                            return;
                        }
                        ZDialog.alert()
                                .setTitle("任务已存在")
                                .setContent("下载任务已存在，是否继续下载？")
                                .setPositiveButton((fragment, which) -> callback.onResult(true))
                                .setNegativeButton((fragment, which) -> callback.onResult(false))
                                .show(activity);
                    }
                })
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
