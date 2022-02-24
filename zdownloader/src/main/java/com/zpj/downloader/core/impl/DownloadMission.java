package com.zpj.downloader.core.impl;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.utils.Logger;
import com.zpj.utils.FormatUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadMission implements Mission {

    private static final String TAG = "DownloadMission";

    protected final Config config;

    protected final MissionInfo info;

    //-----------------------------------------------------transient---------------------------------------------------------------

    protected transient ArrayList<WeakReference<Observer>> mObservers;



//    public DownloadMission(Config config) {
//        this.config = config;
//        this.info = new MissionInfo(config.getMissionId());
//    }

    public DownloadMission(Config config, MissionInfo info) {
        this.config = config;
        this.info = info;
    }

    private static <T extends Mission> void notifyStatus(Class<T> clazz, Mission mission, @Status final int status) {
        ZDownloader.get(clazz).notifyStatus((T) mission, status);
    }

    protected void notifyStatus(@Status final int status) {
        notifyStatus(getClass(), this, status);
    }

    @Override
    public void start() {
        int missionStatus = getStatus();
        Logger.d(TAG, "start missionStatus=" + missionStatus);
        if (canStart()) {

            notifyStatus(Status.CREATED);

//            if (missionStatus == Status.NEW) {
//                notifyStatus(Status.NEW);
//            } else if (getMissionInfo().isPrepared()) {
//                notifyStatus(Status.DOWNLOADING);
//            } else {
//                prepare();
//            }
        }
    }

    @Override
    public void prepare() {
        Logger.d(TAG, "prepare");
        notifyStatus(Status.PREPARING);
    }

    @Override
    public void pause() {
        if (canPause()) {
            // TODO 暂停任务
            notifyStatus(Status.PAUSED);
        }

    }

    @Override
    public void waiting() {
        notifyStatus(Status.WAITING);
    }

    @Override
    public void restart() {
        pause();
        setStatus(Status.CREATED);
        start();
    }

    @Override
    public void delete() {
        pause();
        notifyStatus(Status.DELETE);
    }

    @Override
    public void clear() {
        pause();
        notifyStatus(Status.CLEAR);
    }

    @Override
    public synchronized void addObserver(Observer observer) {
        if (hasObserver(observer)) {
            return;
        }
        if (mObservers == null) {
            mObservers = new ArrayList<>();
        }
        mObservers.add(new WeakReference<>(observer));
    }

    @Override
    public synchronized boolean hasObserver(Observer observer) {
        if (mObservers == null || observer == null) {
            return false;
        }
        for (WeakReference<Observer> weakRef : mObservers) {
            if (weakRef != null && weakRef.get() == observer) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void removeObserver(Observer observer) {
        if (mObservers == null || observer == null) {
            return;
        }
        for (Iterator<WeakReference<Observer>> iterator = mObservers.iterator();
             iterator.hasNext(); ) {
            WeakReference<Observer> weakRef = iterator.next();
            if (observer == weakRef.get()) {
                iterator.remove();
            }
        }
    }

    @Override
    public synchronized List<Observer> getObservers() {
        if (mObservers == null) {
            return Collections.emptyList();
        }
        List<Observer> list = new ArrayList<>();
        for (Iterator<WeakReference<Observer>> iterator = mObservers.iterator();
             iterator.hasNext(); ) {
            Observer observer = iterator.next().get();
            if (observer == null) {
                iterator.remove();
            } else {
                list.add(observer);
            }
        }
        return list;
    }

    @Override
    public synchronized void removeAllObserver() {
        if (mObservers == null) {
            return;
        }
        mObservers.clear();
    }

    @Override
    public boolean isPreparing() {
        Downloader<DownloadMission> downloader = ZDownloader.get(this);
        return downloader.getDispatcher().isPreparing(this);
//        return getStatus() == Status.PREPARING;
    }

    @Override
    public boolean isDownloading() {
        Downloader<DownloadMission> downloader = ZDownloader.get(this);
        return downloader.getDispatcher().isDownloading(this);
//        return getStatus() == Status.DOWNLOADING;
    }

    @Override
    public boolean isWaiting() {
        Downloader<DownloadMission> downloader = ZDownloader.get(this);
        return downloader.getDispatcher().isWaiting(this);
//        return getStatus() == Status.WAITING;
    }

    @Override
    public boolean isPaused() {
        if (getStatus() == Status.PAUSED) {
            return true;
        }
        return !isComplete() && !isError() && !isDownloading() && !isWaiting();

//        return getStatus() == Status.PAUSED;
    }

    @Override
    public boolean isComplete() {
        return getStatus() == Status.COMPLETE;
    }

    @Override
    public boolean isError() {
        return getStatus() == Status.ERROR || getErrorCode() != 0;
    }

    @Override
    public boolean canPause() {
        return isDownloading() || isWaiting() || isPreparing();
    }

    @Override
    public boolean canStart() {
        if (isDownloading() || isWaiting()) {
            return false;
        }
        return isPaused() || isError() || getStatus() == Status.CREATED;
    }

    @Override
    public String getMissionId() {
        return info.missionId;
    }

    @Override
    public String getName() {
        return info.name;
    }

    @Override
    public String getUrl() {
        return info.url;
    }

    @Override
    public String getOriginUrl() {
        return info.originUrl;
    }

    @Override
    public long getCreateTime() {
        return info.createTime;
    }

    @Override
    public long getFinishTime() {
        return info.finishTime;
    }

    @Override
    public long getLength() {
        return info.length;
    }

    @Override
    public long getDownloaded() {
        return info.downloaded;
    }

    @Override
    public int getStatus() {
        return info.missionStatus;
    }

    @Override
    public int getErrorCode() {
        return info.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return info.errorMessage;
    }

    @Override
    public boolean isBlockDownload() {
        return info.isBlockDownload;
    }

    @Override
    public boolean hasInit() {
        return getStatus() > Status.PREPARING;
    }

    @Override
    public String getFilePath() {
        String path = config.getDownloadPath();
        if (path.endsWith(File.separator)) {
            return path + info.name;
        }
        return path + File.separator + info.name;
    }

    @Override
    public File getFile() {
        return new File(getFilePath());
    }

    @Override
    public String getFileSuffix() {
        return MimeTypeMap.getFileExtensionFromUrl(getFile().toURI().toString()).toLowerCase(Locale.US);
    }

    @Override
    public float getProgress() {
        return getProgress(getDownloaded(), info.length);
    }

    private float getProgress(long done, long length) {
        if (getStatus() == Status.COMPLETE) {
            return 100f;
        } else if (length <= 0) {
            return 0f;
        }
        float progress = (float) done / (float) length;
        return progress * 100f;
    }

    @Override
    public String getProgressStr() {
        return String.format(Locale.US, "%.2f%%", getProgress());
    }

    @Override
    public String getFileSizeStr() {
        return FormatUtils.formatSize(info.length);
    }

    @Override
    public String getDownloadedSizeStr() {
        return FormatUtils.formatSize(getDownloaded());
    }

    @Override
    public long getSpeed() {
        return info.speed;
    }

    @Override
    public String getSpeedStr() {
        return FormatUtils.formatSpeed(getSpeed());
    }

    @Override
    public int getNotifyId() {
        return 0;
    }

    @Override
    public String getMissionInfoFilePath() {
        return null;
    }

    @Override
    public boolean isSupportSlice() {
        return isBlockDownload();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public MissionInfo getMissionInfo() {
        return info;
    }

    @Override
    public void setStatus(int status) {
        info.missionStatus = status;
    }

    @Override
    public void setName(String name) {
        info.name = name;
    }

    @Override
    public void setUrl(String url) {
        info.url = url;
    }

    @Override
    public void setOriginUrl(String originUrl) {
        info.originUrl = originUrl;
    }

    @Override
    public void setLength(long length) {
        info.length = length;
    }

    @Override
    public void setErrorCode(int errCode) {
        info.errorCode = errCode;
    }

    @Override
    public void setErrorMessage(String msg) {
        info.errorMessage = msg;
    }

    @Override
    public void setSupportSlice(boolean support) {
        info.isBlockDownload = support;
    }

    @Override
    public String toString() {
        return "DownloadMission{" +
                ", config=" + config +
                ", info=" + info +
                ", mObservers=" + mObservers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return TextUtils.equals(getMissionId(), ((DownloadMission) o).getMissionId());
    }

    @Override
    public int hashCode() {
        return getMissionId().hashCode();
    }
}
