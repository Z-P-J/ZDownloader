package com.zpj.downloader;

import android.content.Context;

import com.zpj.downloader.config.DownloaderConfig;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadManagerImpl;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.downloader.core.INotificationInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Z-P-J
 * */
public class ZDownloader {

    private static boolean waitingForInternet = false;

    private ZDownloader() {
        throw new RuntimeException("Wrong operation!");
    }

    public static void init(Context context) {
        init(DownloaderConfig.with(context));
    }

    public static <T extends DownloadMission> void init(final DownloaderConfig options, Class<T> clazz) {
        DownloadManagerImpl.register(options, clazz);
    }

    public static void init(final DownloaderConfig options) {
        init(options, DownloadMission.class);
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

    public static List<DownloadMission> getAllMissions() {
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
                mission.getMissionConfig().setEnableNotification(value);
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
