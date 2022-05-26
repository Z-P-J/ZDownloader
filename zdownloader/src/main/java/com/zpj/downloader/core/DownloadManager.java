package com.zpj.downloader.core;

public interface DownloadManager<T extends Mission> {

    void startMission(T mission);

    void restartMission(T mission);

    void waitingMission(T mission);

    void pauseMission(T mission);

    void deleteMission(T mission);

    void clearMission(T mission);

}
