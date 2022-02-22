package com.zpj.downloader.core.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.utils.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbsDispatcher<T extends Mission> implements Dispatcher<T> {

    private final ConcurrentHashMap<String, MissionHandler<T>> mDownloadingQueue = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<T> mWaitingQueue = new ConcurrentLinkedQueue<>();

    private static final class MissionHandler<T extends Mission> {

        private static final String TAG = "MissionHandler";

        private final AtomicBoolean isPreparing = new AtomicBoolean(false);

        @NonNull
        private final T mission;
//        private final Downloader<T> downloader;
        private final ProgressUpdater<T> updater;

        public MissionHandler(@NonNull T mission) {
            this.mission = mission;
            updater = new ProgressUpdater<>(mission);
//            downloader = ZDownloader.get(mission);
        }

        public boolean isPreparing() {
            return isPreparing.get();
        }

        public synchronized boolean onPreparing() {
            return isPreparing.compareAndSet(false, true);
        }

        public void start() {
            Logger.d(TAG, "start");
            updater.start();
        }

        public void stop() {
            Logger.d(TAG, "stop");
            updater.stop();
        }

    }


    @Override
    public Executor createExecutor(T mission) {
        return Executors.newFixedThreadPool(mission.getConfig().getThreadCount());
    }

    @Override
    public boolean isDownloading(T mission) {
        return mDownloadingQueue.containsKey(mission.getMissionId());
    }

    @Override
    public boolean isPreparing(T mission) {
        MissionHandler<T> delegate = mDownloadingQueue.get(mission.getMissionId());
        return delegate != null && delegate.isPreparing();
    }

    @Override
    public boolean isWaiting(T mission) {
        return mWaitingQueue.contains(mission);
    }

    @Override
    public boolean remove(T mission) {
        if (mission == null) {
            return false;
        }
        if (isDownloading(mission)) {
            MissionHandler<T> handler = mDownloadingQueue.remove(mission.getMissionId());
            if (handler != null) {
                handler.stop();
            }
            // TODO pause

            // 从等待队列中的一个任务加入到下载队列
//            enqueue(mWaitingQueue.poll());
            return true;
        } else if (isWaiting(mission)) {
            mWaitingQueue.remove(mission);
            return true;
        }
        return false;
    }

    @Override
    public boolean waiting(T mission) {
        if (mission == null) {
            return false;
        }
        mDownloadingQueue.remove(mission.getMissionId());
        return mWaitingQueue.add(mission);
    }

    @Override
    public T nextMission() {
        return mWaitingQueue.poll();
    }

    @Override
    public boolean prepare(T mission) {
        if (mission == null || !isDownloading(mission)) {
            return false;
        }
        MissionHandler<T> delegate = mDownloadingQueue.get(mission.getMissionId());
        if (delegate == null || delegate.isPreparing()) {
            return false;
        }
        return delegate.onPreparing();
    }

    @Override
    public boolean enqueue(T mission) {
        if (mission == null) {
            return false;
        }
        if (mDownloadingQueue.size() >= 3) {
            return mWaitingQueue.offer(mission);
        } else {
            MissionHandler<T> handler = new MissionHandler<>(mission);
            MissionHandler<T> oldHandler = mDownloadingQueue.put(mission.getMissionId(), handler);
            if (oldHandler != null) {
                oldHandler.stop();
            }
            handler.start();
            return true;
        }
    }

    @Override
    public boolean canRetry(T mission, int code, String msg) {
        if (!mission.isDownloading()) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onError(T mission, int code, String msg) {
        return false;
    }

}
