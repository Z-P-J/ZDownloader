package com.zpj.downloader;

import android.content.Context;

import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.impl.DownloadMission;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Z-P-J
 * */
public class ZDownloader {

    private static boolean waitingForInternet = false;

    private static final Map<Class<? extends Mission>, Downloader<? extends Mission>> DOWNLOADER_MAP = new HashMap<>();


    public <T extends Mission> ZDownloader register(Class<T> clazz, Downloader<T> downloader) {
        DOWNLOADER_MAP.put(clazz, downloader);
        return this;
    }


    public static  <T extends Mission> Downloader<T> get(Class<T> clazz) {
        return (Downloader<T>) DOWNLOADER_MAP.get(clazz);
    }

    public static  <T extends Mission> void enqueue(Class<T> clazz, Mission mission) {
        get(clazz).enqueue((T) mission);
    }

    public static  <T extends Mission> void pause(Class<T> clazz, Mission mission) {
        get(clazz).pause((T) mission);
    }

    public static  <T extends Mission> void delete(Class<T> clazz, Mission mission) {
        get(clazz).delete((T) mission);
    }

    public static <T extends Mission> void notifyStatus(Class<T> clazz, final T mission, final int status) {
        get(clazz).notifyStatus(mission, status);
    }

























    private ZDownloader() {
        throw new RuntimeException("Wrong operation!");
    }

    public static DownloaderConfig config(Context context) {
        return config(context, DownloadMission.class);
    }

    public static DownloaderConfig config(Context context, Class<? extends BaseMission<?>> clazz) {
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

    public static <T extends BaseMission<?>> T download(String url, Class<T> clazz) {
        return download(url, null, clazz);
    }

    public static <T extends BaseMission<?>> T download(String url, String name, Class<T> clazz) {
        return createMission(url, name, clazz);
    }

    private static <R extends BaseMission<?>> R createMission(String url, String name, Class<R> clazz) {
        R mission = null;
        try {
            Constructor<R> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            mission = constructor.newInstance();

            DownloaderConfig config = DownloadManagerImpl.getInstance().getDownloaderConfig();
            mission.setContext(config.getContext());
            mission.setNotificationInterceptor(config.getNotificationInterceptor());
            mission.setThreadCount(config.getThreadCount());
            mission.setAllowAllSSL(config.isAllowAllSSL());
            mission.setBlockSize(config.getBlockSize());
            mission.setBufferSize(config.getBufferSize());
            mission.setConnectOutTime(config.getConnectOutTime());
            mission.setCookie(config.getCookie());
            mission.setDownloadPath(config.getDownloadPath());
            mission.setEnableNotification(config.getEnableNotification());
            mission.setHeaders(config.getHeaders());
            mission.setConflictPolicy(config.getConflictPolicy());
            mission.setProgressInterval(config.getProgressInterval());
            mission.setProxy(config.getProxy());
            mission.setReadOutTime(config.getReadOutTime());
            mission.setRetryCount(config.getRetryCount());
            mission.setRetryDelayMillis(config.getRetryDelayMillis());
            mission.setUserAgent(config.getUserAgent());

            mission.url = url;
            mission.originUrl = url;
            mission.name = name;
            mission.uuid = UUID.randomUUID().toString();
            mission.createTime = System.currentTimeMillis();
            mission.missionStatus = BaseMission.MissionStatus.PAUSED;
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
        DownloadManagerImpl.getInstance().onDestroy();
//        System.exit(0);
    }

    public static void pauseAll() {
        DownloadManagerImpl.getInstance().pauseAllMissions();
    }

    public static void waitingForInternet() {
        waitingForInternet = true;
        for (BaseMission<?> mission : DownloadManagerImpl.getInstance().getMissions()) {
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
        for (BaseMission<?> mission : DownloadManagerImpl.getInstance().getMissions()) {
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

    public static List<? extends BaseMission<?>> getAllMissions() {
        return DownloadManagerImpl.getInstance().getMissions();
    }

    public static void getAllMissions(DownloadManager.OnLoadMissionListener<BaseMission<?>> listener) {
        DownloadManagerImpl.getInstance().loadMissions(listener);
    }

//    public static <T extends BaseMission<T>> List<T> getAllMissions(Class<T> clazz) {
//        List<T> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            downloadMissionList.add(clazz.cast(mission));
//        }
//        return downloadMissionList;
//    }

    public static <T extends BaseMission<T>> void getAllMissions(final Class<T> clazz, final DownloadManager.OnLoadMissionListener<T> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<T> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : missions) {
                    downloadMissionList.add(clazz.cast(mission));
                }
                listener.onLoaded(downloadMissionList);
            }
        });
    }

//    public static List<BaseMission<?>> getAllMissions(boolean downloading) {
//        List<BaseMission<?>> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (mission.isFinished() != downloading) {
//                downloadMissionList.add(mission);
//            }
//        }
//        return downloadMissionList;
//    }

    public static void getAllMissions(final boolean downloading, final DownloadManager.OnLoadMissionListener<BaseMission<?>> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<BaseMission<?>> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : missions) {
                    if (mission.isFinished() != downloading) {
                        downloadMissionList.add(mission);
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

//    public static <T extends BaseMission<?>> List<T> getAllMissions(boolean downloading, Class<T> clazz) {
//        List<T> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (mission.isFinished() != downloading) {
//                downloadMissionList.add(clazz.cast(mission));
//            }
//        }
//        return downloadMissionList;
//    }

    public static <T extends BaseMission<?>> void getAllMissions(final boolean downloading, final Class<T> clazz,
                                                                 final DownloadManager.OnLoadMissionListener<T> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<T> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : missions) {
                    if (mission.isFinished() != downloading) {
                        downloadMissionList.add(clazz.cast(mission));
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

//    public static <T extends BaseMission<?>> List<T> getRunningMissions(Class<T> clazz) {
//        List<T> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (mission.isRunning()) {
//                downloadMissionList.add(clazz.cast(mission));
//            }
//        }
//        return downloadMissionList;
//    }

    public static <T extends BaseMission<?>> void getRunningMissions(final Class<T> clazz,
                                                                     final DownloadManager.OnLoadMissionListener<T> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<T> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : missions) {
                    if (mission.isRunning()) {
                        downloadMissionList.add(clazz.cast(mission));
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

//    public static List<BaseMission<?>> getRunningMissions() {
//        List<BaseMission<?>> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (mission.isRunning()) {
//                downloadMissionList.add(mission);
//            }
//        }
//        return downloadMissionList;
//    }

    public static void getRunningMissions(final DownloadManager.OnLoadMissionListener<BaseMission<?>> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<BaseMission<?>> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : getAllMissions()) {
                    if (mission.isRunning()) {
                        downloadMissionList.add(mission);
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

//    public static List<BaseMission<?>> getMissions(DownloadMission.MissionStatus status) {
//        List<BaseMission<?>> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (status == mission.getStatus()) {
//                downloadMissionList.add(mission);
//            }
//        }
//        return downloadMissionList;
//    }

    public static void getMissions(final DownloadMission.MissionStatus status,
                                                   final DownloadManager.OnLoadMissionListener<BaseMission<?>> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<BaseMission<?>> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : getAllMissions()) {
                    if (status == mission.getStatus()) {
                        downloadMissionList.add(mission);
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

//    public static <T extends BaseMission<T>> List<T> getMissions(DownloadMission.MissionStatus status, Class<T> clazz) {
//        List<T> downloadMissionList = new ArrayList<>();
//        for (BaseMission<?> mission : getAllMissions()) {
//            if (status == mission.getStatus()) {
//                downloadMissionList.add(clazz.cast(mission));
//            }
//        }
//        return downloadMissionList;
//    }

    public static <T extends BaseMission<T>> void getMissions(final DownloadMission.MissionStatus status, final Class<T> clazz,
                                                                 final DownloadManager.OnLoadMissionListener<T> listener) {
        getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
            @Override
            public void onLoaded(List<BaseMission<?>> missions) {
                List<T> downloadMissionList = new ArrayList<>();
                for (BaseMission<?> mission : getAllMissions()) {
                    if (status == mission.getStatus()) {
                        downloadMissionList.add(clazz.cast(mission));
                    }
                }
                if (listener != null) {
                    listener.onLoaded(downloadMissionList);
                }
            }
        });
    }

    public static void setEnableNotification(boolean value) {
        setEnableNotification(value, true);
    }

    public static void setEnableNotification(boolean value, boolean affectPresent) {
        DownloaderConfig config = DownloadManagerImpl.getInstance().getDownloaderConfig();
        config.setEnableNotification(value);
        if (affectPresent) {
            for (BaseMission<?> mission : getAllMissions()) {
                mission.setEnableNotification(value);
            }
            if (!value) {
                Notifier interceptor = config.getNotificationInterceptor();
                if (interceptor != null) {
                    interceptor.onCancelAll(DownloadManagerImpl.getInstance().getContext());
                }
            }
        }
    }

}
