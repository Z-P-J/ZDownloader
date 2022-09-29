package com.zpj.downloader;

import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionLoader;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionDownloader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Z-P-J
 * */
public class ZDownloader {

    private static boolean waitingForInternet = false;

    private static final Map<Class<? extends Mission>, Downloader<? extends Mission>> DOWNLOADER_MAP = new HashMap<>();


    static {
        ZDownloader.register(DownloadMission.class, new MissionDownloader());
    }


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

}
