package com.zpj.downloader;

import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Z-P-J
 * */
public class ZDownloader {

    private static boolean waitingForInternet = false;

    private static final Map<Class<? extends Mission>, Downloader<? extends Mission>> DOWNLOADER_MAP = new HashMap<>();



//    public static DownloadMission builder(String url) {
//        return download(url, null, DownloadMission.class);
//    }
//
//    public static DownloadMission with(String url, String name) {
//        return download(url, name, DownloadMission.class);
//    }



    public static  <T extends Mission> void register(Class<T> clazz, Downloader<T> downloader) {
        DOWNLOADER_MAP.put(clazz, downloader);
    }


    public static <T extends Mission> Downloader<T> get(Class<T> clazz) {
        return (Downloader<T>) DOWNLOADER_MAP.get(clazz);
    }

    public static <T extends Mission> Downloader<T> get(T mission) {
        return (Downloader<T>) DOWNLOADER_MAP.get(mission.getClass());
    }

    public static <T extends Mission> void loadMissions(Class<T> clazz, MissionLoader<T> loader) {
        get(clazz).loadMissions(loader);
    }

    public static <T extends Mission> void addObserver(Class<T> clazz, Downloader.DownloaderObserver<T> observer) {
        get(clazz).addObserver(observer);
    }

    public static <T extends Mission> void removeObserver(Class<T> clazz, Downloader.DownloaderObserver<T> observer) {
        get(clazz).removeObserver(observer);
    }

























    private ZDownloader() {
        throw new RuntimeException("Wrong operation!");
    }

//    public static DownloaderConfig config(Context context) {
//        return config(context, DownloadMission.class);
//    }
//
//    public static DownloaderConfig config(Context context, Class<? extends BaseMission<?>> clazz) {
//        DownloadManagerImpl manager = DownloadManagerImpl.get();
//        if (manager != null && manager.getDownloaderConfig() != null) {
//            return manager.getDownloaderConfig();
//        }
//        return DownloaderConfig.with(context, clazz);
//    }
//
//    public static DownloadMission download(String url) {
//        return download(url, null, DownloadMission.class);
//    }
//
//    public static DownloadMission download(String url, String name) {
//        return download(url, name, DownloadMission.class);
//    }
//
//    public static <T extends BaseMission<?>> T download(String url, Class<T> clazz) {
//        return download(url, null, clazz);
//    }
//
//    public static <T extends BaseMission<?>> T download(String url, String name, Class<T> clazz) {
//        return createMission(url, name, clazz);
//    }
//
//    private static <R extends BaseMission<?>> R createMission(String url, String name, Class<R> clazz) {
//        R mission = null;
//        try {
//            Constructor<R> constructor = clazz.getDeclaredConstructor();
//            constructor.setAccessible(true);
//            mission = constructor.newInstance();
//
//            DownloaderConfig config = DownloadManagerImpl.getInstance().getDownloaderConfig();
//            mission.setContext(config.getContext());
//            mission.setNotificationInterceptor(config.getNotificationInterceptor());
//            mission.setThreadCount(config.getThreadCount());
//            mission.setAllowAllSSL(config.isAllowAllSSL());
//            mission.setBlockSize(config.getBlockSize());
//            mission.setBufferSize(config.getBufferSize());
//            mission.setConnectOutTime(config.getConnectOutTime());
//            mission.setCookie(config.getCookie());
//            mission.setDownloadPath(config.getDownloadPath());
//            mission.setEnableNotification(config.getEnableNotification());
//            mission.setHeaders(config.getHeaders());
//            mission.setConflictPolicy(config.getConflictPolicy());
//            mission.setProgressInterval(config.getProgressInterval());
//            mission.setProxy(config.getProxy());
//            mission.setReadOutTime(config.getReadOutTime());
//            mission.setRetryCount(config.getRetryCount());
//            mission.setRetryDelayMillis(config.getRetryDelayMillis());
//            mission.setUserAgent(config.getUserAgent());
//
//            mission.url = url;
//            mission.originUrl = url;
//            mission.name = name;
//            mission.uuid = UUID.randomUUID().toString();
//            mission.createTime = System.currentTimeMillis();
//            mission.missionStatus = BaseMission.MissionStatus.PAUSED;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mission;
//    }
//
//
//
//
//    public static void pauseAll() {
//        DownloadManagerImpl.getInstance().pauseAllMissions();
//    }
//
//    public static void waitingForInternet() {
//        waitingForInternet = true;
//        for (BaseMission<?> mission : DownloadManagerImpl.getInstance().getMissions()) {
//            if (mission.isRunning()) {
//                mission.waiting();
//            }
//        }
//    }
//
//    public static boolean isWaitingForInternet() {
//        return waitingForInternet;
//    }
//
//    public static void resumeAll() {
//        waitingForInternet = false;
//        for (BaseMission<?> mission : DownloadManagerImpl.getInstance().getMissions()) {
//            if (mission.isWaiting()) {
//                mission.start();
//            }
//        }
//    }
//
//    public static void deleteAll() {
//        DownloadManagerImpl.getInstance().deleteAllMissions();
//    }
//
//    public static void clearAll() {
//        DownloadManagerImpl.getInstance().clearAllMissions();
//    }









}
