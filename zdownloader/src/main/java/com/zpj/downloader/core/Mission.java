package com.zpj.downloader.core;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.DownloadManagerImpl;
import com.zpj.downloader.ProgressUpdater;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.impl.Config;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public interface Mission {

    interface Observer {

        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onRetrying();

        void onProgress(ProgressUpdater update);

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

    void pause();

    void waiting();

    void restart();

    void delete();

    void clear();

    public void addObserver(Observer listener);

    public boolean hasObserver(Observer listener);

    public void removeObserver(Observer listener);

    public void removeAllObserver();

    //-------------------------下载任务状态-----------------------------------
    boolean isPrepare();

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

    int getAliveThreadCount();

    long getBlocks();

    int getFinishCount();

    long getLength();

    long getDone();

    int getStatus();

    int getErrCode();

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


    //-----------------------------------------------------setter-----------------------------------------------------------------


    void setName(String name);

    void setUrl(String url);

    void setOriginUrl(String originUrl);

    void setLength(long length);

    void setErrCode(int errCode);

    void setSupportSlice(boolean support);


}
