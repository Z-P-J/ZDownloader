//package com.zpj.downloader.core.model;
//
//import android.text.TextUtils;
//
//import com.zpj.downloader.constant.DefaultConstant;
//import com.zpj.downloader.utils.ContextProvider;
//
//import java.io.File;
//
//
///**
//* @author Z-P-J
//* */
//public class DownloaderConfig extends Config {
//
//    private static final String MISSIONS_PATH = "missions";
//
//    private transient String taskPath;
//
//    private int concurrentMissionCount = DefaultConstant.CONCURRENT_MISSION_COUNT;
//
//    public DownloaderConfig() {
//        super("");
//    }
//
//    public int getConcurrentMissionCount() {
//        return concurrentMissionCount;
//    }
//
//    public DownloaderConfig setConcurrentMissionCount(int concurrentMissionCount) {
//        this.concurrentMissionCount = concurrentMissionCount;
//        return this;
//    }
//
//    public String getTaskPath() {
//        if (TextUtils.isEmpty(taskPath)) {
//            File file = new File(ContextProvider.getApplicationContext().getFilesDir(), MISSIONS_PATH);
//            taskPath = file.getAbsolutePath();
//        } else {
//            File file = new File(taskPath);
//            if (file.isDirectory()) {
//                return taskPath;
//            } else {
//                taskPath = null;
//                return getTaskPath();
//            }
//        }
//        return taskPath;
//    }
//
//    public File getTaskFolder() {
//        return new File(getTaskPath());
////        if (TextUtils.isEmpty(taskPath)) {
////            File file = new File(getContext().getFilesDir(), MISSIONS_PATH);
////            taskPath = file.getAbsolutePath();
////            return file;
////        }
////        File file = new File(taskPath);
////        if (file.isDirectory()) {
////            return file;
////        }
////        taskPath = null;
////        return getTaskFolder();
//    }
//
//    public void init() {
//        File path = new File(ContextProvider.getApplicationContext().getFilesDir(), MISSIONS_PATH);
//        if (!path.exists()) {
//            path.mkdirs();
//        }
//        taskPath = path.getAbsolutePath();
//        File file = new File(getDownloadPath());
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//
////        ContextUtils.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
////            @Override
////            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//////                DownloadManagerImpl.register(DownloaderConfig.this, clazz);
////            }
////
////            @Override
////            public void onActivityStarted(Activity activity) {
////
////            }
////
////            @Override
////            public void onActivityResumed(Activity activity) {
////
////            }
////
////            @Override
////            public void onActivityPaused(Activity activity) {
////
////            }
////
////            @Override
////            public void onActivityStopped(Activity activity) {
////
////            }
////
////            @Override
////            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
////
////            }
////
////            @Override
////            public void onActivityDestroyed(Activity activity) {
////
////            }
////        });
//
////        DownloadManagerImpl.register(this, clazz);
//    }
//
//}
