package com.zpj.downloader.core.impl;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.downloader.utils.FormatUtils;
import com.zpj.downloader.utils.Logger;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DownloadMission implements Mission {

    private static final String TAG = "DownloadMission";

    protected final Config config;

    protected final MissionInfo info;

    protected transient ArrayList<WeakReference<Observer>> mObservers;

    public DownloadMission(Config config, MissionInfo info) {
        this.config = config;
        this.info = info;
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        ZDownloader.get(this).startMission(this);

    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        ZDownloader.get(this).pauseMission(this);
    }

    @Override
    public void waiting() {
        ZDownloader.get(this).waitingMission(this);
    }

    @Override
    public void restart() {
        ZDownloader.get(this).restartMission(this);
    }

    @Override
    public void delete() {
        ZDownloader.get(this).deleteMission(this);
    }

    @Override
    public void clear() {
        ZDownloader.get(this).clearMission(this);
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
        if (getStatus() != Status.PREPARING) {
            return false;
        }
        if (getMissionInfo().isPrepared()) {
            return false;
        }
        Downloader<DownloadMission> downloader = ZDownloader.get(this);
        return downloader.getDispatcher().isPreparing(this);
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
        return info.getMissionId();
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getUrl() {
        return info.getUrl();
    }

    @Override
    public String getOriginUrl() {
        return info.getOriginUrl();
    }

    @Override
    public long getCreateTime() {
        return info.getCreateTime();
    }

    @Override
    public long getFinishTime() {
        return info.getFinishTime();
    }

    @Override
    public long getLength() {
        return info.getLength();
    }

    @Override
    public long getDownloaded() {
        return info.getDownloaded();
    }

    @Override
    public int getStatus() {
        return info.getMissionStatus();
    }

    @Override
    public int getErrorCode() {
        return info.getErrorCode();
    }

    @Override
    public String getErrorMessage() {
        return info.getErrorMessage();
    }

    @Override
    public boolean isBlockDownload() {
        return info.isBlockDownload();
    }

    @Override
    public boolean hasInit() {
        return getStatus() > Status.PREPARING;
    }

    @Override
    public String getFilePath() {
        String path = config.getDownloadPath();
        if (path.endsWith(File.separator)) {
            return path + info.getName();
        }
        return path + File.separator + info.getName();
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
        return getProgress(getDownloaded(), getLength());
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
        return FormatUtils.formatSize(getLength());
    }

    @Override
    public String getDownloadedSizeStr() {
        return FormatUtils.formatSize(getDownloaded());
    }

    @Override
    public long getSpeed() {
        return info.getSpeed();
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
        info.setMissionStatus(status);
    }

    @Override
    public void setName(String name) {
        info.setName(name);
    }

    @Override
    public void setUrl(String url) {
        info.setUrl(url);
    }

    @Override
    public void setOriginUrl(String originUrl) {
        info.setOriginUrl(originUrl);
    }

    @Override
    public void setLength(long length) {
        info.setLength(length);
    }

    @Override
    public void setErrorCode(int errCode) {
        info.setErrorCode(errCode);
    }

    @Override
    public void setErrorMessage(String msg) {
        info.setErrorMessage(msg);
    }

    @Override
    public void setBlockDownload(boolean support) {
        info.setBlockDownload(support);
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
