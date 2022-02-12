package com.zpj.downloader.core.impl;

import android.webkit.MimeTypeMap;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Mission;
import com.zpj.utils.FormatUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AbsMission implements Mission {

    protected final AtomicLong done = new AtomicLong(0);

    protected final Config config;

    protected volatile String uuid = "";
    protected volatile String name = "";
    protected volatile String url = "";
    protected volatile String originUrl = "";
    protected volatile long createTime = 0;
    protected volatile long finishTime = 0;
    protected volatile long blocks = 1;
    protected volatile long length = 0;
    protected volatile int missionStatus = Status.NEW;
    protected volatile boolean isBlockDownload = false;
    protected volatile int errorCode = -1;
    protected volatile String errorMessage;

    //-----------------------------------------------------transient---------------------------------------------------------------

    protected transient AtomicInteger finishCount;
    protected transient AtomicInteger aliveThreadCount;

    protected transient ArrayList<WeakReference<Observer>> mObservers;

    protected transient volatile float speed = 0f;

    public AbsMission(Config config) {
        this.config = config;
    }

    private static <T extends Mission> void notifyStatus(Class<T> clazz, Mission mission, final int status) {
        ZDownloader.get(clazz).notifyStatus((T) mission, status);
    }

    protected void notifyStatus(final int status) {
        notifyStatus(getClass(), this, status);
    }

    @Override
    public void start() {
        if (missionStatus == Status.NEW) {
            notifyStatus(Status.NEW);
        } if (missionStatus == Status.PREPARING) {
            // TODO 需要判断是否正在准备中
            prepare();
        } else if (canStart()) {
            notifyStatus(Status.PROGRESSING);
        }
    }

    @Override
    public void prepare() {
        notifyStatus(Status.PREPARING);
    }

    @Override
    public void pause() {
        notifyStatus(Status.PAUSED);
    }

    @Override
    public void waiting() {
        notifyStatus(Status.WAITING);
    }

    @Override
    public void restart() {
        setStatus(Status.NEW);
        start();
    }

    @Override
    public void delete() {

    }

    @Override
    public void clear() {

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
    public List<Observer> getObservers() {
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
    public boolean isPrepared() {
        return missionStatus == Status.PREPARING;
    }

    @Override
    public boolean isRunning() {
        return missionStatus == Status.PROGRESSING;
    }

    @Override
    public boolean isWaiting() {
        return missionStatus == Status.WAITING;
    }

    @Override
    public boolean isPause() {
        return missionStatus == Status.PAUSED;
    }

    @Override
    public boolean isFinished() {
        return missionStatus == Status.FINISHED;
    }

    @Override
    public boolean isError() {
        return missionStatus == Status.ERROR;
    }

    @Override
    public boolean canPause() {
        return isRunning() || isWaiting() || isPrepared();
    }

    @Override
    public boolean canStart() {
        return isPause() || isError() || missionStatus == Status.NEW;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getOriginUrl() {
        return originUrl;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public int getAliveThreadCount() {
        return aliveThreadCount.get();
    }

    @Override
    public long getBlocks() {
        return blocks;
    }

    @Override
    public int getFinishCount() {
        return finishCount.get();
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getDone() {
        return done.get();
    }

    @Override
    public int getStatus() {
        return missionStatus;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isBlockDownload() {
        return isBlockDownload;
    }

    @Override
    public boolean hasInit() {
        return missionStatus > Status.PREPARING;
    }

    @Override
    public String getFilePath() {
        String path = config.getDownloadPath();
        if (path.endsWith(File.separator)) {
            return path + name;
        }
        return path + File.separator + name;
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
        return getProgress(getDone(), length);
    }

    private float getProgress(long done, long length) {
        if (missionStatus == Status.FINISHED) {
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
        return FormatUtils.formatSize(length);
    }

    @Override
    public String getDownloadedSizeStr() {
        return FormatUtils.formatSize(done.get());
    }

    @Override
    public float getSpeed() {
        return speed;
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
        return isBlockDownload;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void setStatus(int status) {
        this.missionStatus = status;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    @Override
    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public void setErrorCode(int errCode) {
        this.errorCode = errCode;
    }

    @Override
    public void setErrorMessage(String msg) {
        this.errorMessage = msg;
    }

    @Override
    public void setSupportSlice(boolean support) {
        isBlockDownload = support;
    }
}
