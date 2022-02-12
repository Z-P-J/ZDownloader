package com.zpj.downloader.core;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.DownloaderConfig;

public interface Downloader<T extends Mission> {

    interface DownloaderObserver<T extends Mission> {
        void onMissionAdd(T mission);

        void onMissionDelete(T mission);

        void onMissionFinished(T mission);
    }

    T download(String url);

    T download(String url, String name);

    Config config();

    T create(String url, String name, Config config);

    DownloaderConfig getConfig();


    void loadMissions(MissionLoader<T> loader);

    void addObserver(DownloaderObserver<T> observer);

    void removeObserver(DownloaderObserver<T> observer);




    void setMissionFactory(MissionFactory<T> missionFactory);

    MissionFactory<T> getMissionFactory();

    void setBlockDivider(BlockDivider<T> divider);

    BlockDivider<T> getBlockDivider();

    void setDispatcher(Dispatcher<T> dispatcher);

    Dispatcher<T> getDispatcher();

    void setHttpFactory(HttpFactory httpFactory);

    HttpFactory getHttpFactory();

    void setInitializer(Initializer<T> initializer);

    Initializer<T> getInitializer();

    void setNotifier(Notifier<T> notifier);

    Notifier<T> getNotifier();

//    void setSerializer(Serializer<T> serializer);
//
//    Serializer<T> getSerializer();

    void setTransfer(Transfer<T> transfer);

    Transfer<T> getTransfer();

    void setExecutorFactory(ExecutorFactory<T> executorFactory);

    ExecutorFactory<T> getExecutorFactory();

    void setUpdater(Updater updater);

    Updater getUpdater();

    Dao<T> getDao();









    void notifyStatus(final T mission, final int status);

}
