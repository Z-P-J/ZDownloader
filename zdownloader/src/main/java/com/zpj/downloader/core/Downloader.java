package com.zpj.downloader.core;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.DownloaderConfig;
import com.zpj.downloader.core.impl.MissionInfo;

public interface Downloader<T extends Mission> {

    interface DownloaderObserver<T extends Mission> {
        void onMissionAdd(T mission);

        void onMissionDelete(T mission);

        void onMissionFinished(T mission);
    }

    Config config();

    T create(MissionInfo info, Config config);

    DownloaderConfig getConfig();

    /**
     * 一个唯一的key代表该Downloader
     * @return key
     */
    @NonNull
    String getKey();


    void loadMissions(MissionLoader<T> loader);

    void addObserver(DownloaderObserver<T> observer);

    void removeObserver(DownloaderObserver<T> observer);


    BlockDivider<T> getBlockDivider();

    Dispatcher<T> getDispatcher();

    HttpFactory getHttpFactory();

    Initializer<T> getInitializer();

    Notifier<T> getNotifier();

    Transfer<T> getTransfer();

    ExecutorFactory<T> getExecutorFactory();

    Repository<T> getRepository();








    void startMission(T mission);

    void restartMission(T mission);

    void waitingMission(T mission);

    void pauseMission(T mission);

    void deleteMission(T mission);

    void clearMission(T mission);

}
