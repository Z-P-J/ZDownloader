package com.zpj.downloader.core;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.MissionInfo;

import java.io.File;
import java.util.List;

public interface Mission {

    interface Observer {

        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onRetrying();

        void onProgress(Mission mission, float speed);

        void onFinished();

        void onError(Error e);

        void onDelete();

        void onClear();
    }

    // TODO
    interface Status {
        int NEW = 0;
        int PREPARING = 1;
        int WAITING = 2;
        int START = 3;
        int PROGRESSING = 4;
        int PAUSED = 5;
        int ERROR = 6;
        int RETRYING = 7;
        int FINISHED = 8;

        int DELETE = 9;
        int CLEAR = 10;
    }

    interface Lifecycle {
        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onRetrying();

        void onFinished();

        void onError(Error e);

        void onDelete();

        void onClear();
    }


    void start();

    void prepare();

    void pause();

    void waiting();

    void restart();

    void delete();

    void clear();

    public void addObserver(Observer listener);

    public boolean hasObserver(Observer listener);

    public void removeObserver(Observer listener);

    public void removeAllObserver();

    List<Observer> getObservers();

    //-------------------------下载任务状态-----------------------------------
    boolean isPrepared();

    boolean isRunning();

    boolean isWaiting();

    boolean isPause();

    boolean isFinished();

    boolean isError();

    boolean canPause();

    boolean canStart();


    //--------------------------------------------------------------getter-----------------------------------------------

    String getUuid();

    String getName();

    String getUrl();

    String getOriginUrl();

    long getCreateTime();

    long getFinishTime();

    int getFinishCount();

    long getLength();

    long getDownloaded();

    int getStatus();

    int getErrorCode();

    String getErrorMessage();

    boolean isBlockDownload();

    boolean hasInit();

    String getFilePath();

    File getFile();

    String getFileSuffix();

    float getProgress();

    String getProgressStr();

    String getFileSizeStr();

    String getDownloadedSizeStr();

    float getSpeed();

    String getSpeedStr();

    int getNotifyId();

    String getMissionInfoFilePath();

    boolean isSupportSlice();

    Config getConfig();

    MissionInfo getMissionInfo();


    //-----------------------------------------------------setter-----------------------------------------------------------------
    void setStatus(int status);

    void setName(String name);

    void setUrl(String url);

    void setOriginUrl(String originUrl);

    void setLength(long length);

    void setErrorCode(int errCode);

    void setErrorMessage(String msg);

    void setSupportSlice(boolean support);


}
