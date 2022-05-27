package com.zpj.downloader.core;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public interface Downloader<T extends Mission> extends DownloadManager<T> {

    interface DownloaderObserver<T extends Mission> {
        void onMissionAdd(T mission);

        void onMissionDelete(T mission);

        void onMissionFinished(T mission);

//        void onMissionConflict(Mission mission, ConflictPolicy.Callback callback);
    }

    T createMission(MissionInfo info, Config config);

    /**
     * 一个唯一的key代表该Downloader
     * @return key
     */
    @NonNull
    String getKey();


    void loadMissions(MissionLoader<T> loader);

    void addObserver(DownloaderObserver<T> observer);

    void removeObserver(DownloaderObserver<T> observer);


    BlockSplitter<T> getBlockDivider();

    Dispatcher<T> getDispatcher();

    HttpFactory getHttpFactory();

    Initializer<T> getInitializer();

    Notifier<T> getNotifier();

    Transfer<T> getTransfer();

    ExecutorFactory<T> getExecutorFactory();

    Repository<T> getRepository();

}
