package com.zpj.downloader;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Z-P-J
 * */
public class ZDownloader {

    private static boolean waitingForInternet = false;

    private ZDownloader() {
        throw new RuntimeException("Wrong operation!");
    }

    public static DownloaderConfig config(Context context) {
        return config(context, DownloadMission.class);
    }

    public static DownloaderConfig config(Context context, Class<? extends DownloadMission> clazz) {
        DownloadManagerImpl manager = DownloadManagerImpl.get();
        if (manager != null && manager.getDownloaderConfig() != null) {
            return manager.getDownloaderConfig();
        }
        return DownloaderConfig.with(context, clazz);
    }

    public static DownloadMission download(String url) {
        return download(url, null, DownloadMission.class);
    }

    public static DownloadMission download(String url, String name) {
        return download(url, name, DownloadMission.class);
    }

    public static <T extends DownloadMission> T download(String url, Class<T> clazz) {
        return download(url, null, clazz);
    }

    public static <T extends DownloadMission> T download(String url, String name, Class<T> clazz) {
        return createMission(url, name, clazz);
    }

    private static <R extends DownloadMission> R createMission(String url, String name, Class<R> clazz) {
        R mission = null;
        try {
//				mission = clazz.newInstance();
            Constructor<R> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            mission = constructor.newInstance();
            mission.url = url;
            mission.originUrl = url;
            mission.name = name;
            mission.uuid = UUID.randomUUID().toString();
            mission.createTime = System.currentTimeMillis();
            mission.missionStatus = DownloadMission.MissionStatus.INITING;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mission;
    }



    public static void setDownloadConcurrentCount(int count) {
        DownloadManagerImpl.getInstance().getDownloaderConfig().setConcurrentMissionCount(count);
    }

    public static void setDownloadThreadCount(int count) {
        getDownloadManager().getDownloaderConfig().setThreadCount(count);
    }

    public static void onDestroy() {
        DownloadManagerImpl.unRegister();
//        System.exit(0);
    }

    public static void pauseAll() {
        DownloadManagerImpl.getInstance().pauseAllMissions();
    }

    public static void waitingForInternet() {
        waitingForInternet = true;
        for (DownloadMission mission : DownloadManagerImpl.getInstance().getMissions()) {
            if (mission.isRunning()) {
                mission.waiting();
            }
        }
    }

    public static boolean isWaitingForInternet() {
        return waitingForInternet;
    }

    public static void resumeAll() {
        waitingForInternet = false;
        for (DownloadMission mission : DownloadManagerImpl.getInstance().getMissions()) {
            if (mission.isWaiting()) {
                mission.start();
            }
        }
    }

    public static void deleteAll() {
        DownloadManagerImpl.getInstance().deleteAllMissions();
    }

    public static void clearAll() {
        DownloadManagerImpl.getInstance().clearAllMissions();
    }

    public static DownloadManager getDownloadManager() {
        return DownloadManagerImpl.getInstance();
    }

    public static Context getContext() {
        return getDownloadManager().getContext();
    }

    public static List<? extends DownloadMission> getAllMissions() {
        return DownloadManagerImpl.getInstance().getMissions();
    }

    public static <T extends DownloadMission> List<T> getAllMissions(Class<T> clazz) {
        List<T> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            downloadMissionList.add((T) mission);
        }
        return downloadMissionList;
    }

    public static List<DownloadMission> getAllMissions(boolean downloading) {
        List<DownloadMission> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (mission.isFinished() != downloading) {
                downloadMissionList.add(mission);
            }
        }
        return downloadMissionList;
    }

    public static <T extends DownloadMission> List<T> getAllMissions(boolean downloading, Class<T> clazz) {
        List<T> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (mission.isFinished() != downloading) {
                downloadMissionList.add((T) mission);
            }
        }
        return downloadMissionList;
    }

    public static <T extends DownloadMission> List<T> getRunningMissions(Class<T> clazz) {
        List<T> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (mission.isRunning()) {
                downloadMissionList.add((T) mission);
            }
        }
        return downloadMissionList;
    }

    public static List<DownloadMission> getRunningMissions() {
        List<DownloadMission> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (mission.isRunning()) {
                downloadMissionList.add(mission);
            }
        }
        return downloadMissionList;
    }

    public static List<DownloadMission> getMissions(DownloadMission.MissionStatus status) {
        List<DownloadMission> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (status == mission.getStatus()) {
                downloadMissionList.add(mission);
            }
        }
        return downloadMissionList;
    }

    public static <T extends DownloadMission> List<T> getMissions(DownloadMission.MissionStatus status, Class<T> clazz) {
        List<T> downloadMissionList = new ArrayList<>();
        for (DownloadMission mission : getAllMissions()) {
            if (status == mission.getStatus()) {
                downloadMissionList.add((T) mission);
            }
        }
        return downloadMissionList;
    }

    public static void setEnableNotification(boolean value) {
        setEnableNotification(value, true);
    }

    public static void setEnableNotification(boolean value, boolean affectPresent) {
        DownloaderConfig config = DownloadManagerImpl.getInstance().getDownloaderConfig();
        config.setEnableNotification(value);
        if (affectPresent) {
            for (DownloadMission mission : getAllMissions()) {
                mission.setEnableNotification(value);
            }
            if (!value) {
                INotificationInterceptor interceptor = config.getNotificationInterceptor();
                if (interceptor != null) {
                    interceptor.onCancelAll(DownloadManagerImpl.getInstance().getContext());
                }
            }
        }
    }

}
