package com.zpj.downloader.core.impl;

import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.utils.ThreadPool;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AbsDispatcher<T extends Mission> implements Dispatcher<T> {


    private final ConcurrentLinkedQueue<T> mDownloadingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<T> mWaitingQueue = new ConcurrentLinkedQueue<>();

    private final HashSet<T> mPreparingMissions = new HashSet<>();


    @Override
    public Executor createExecutor(T mission) {
        return Executors.newFixedThreadPool(mission.getConfig().getThreadCount());
    }

    @Override
    public boolean isDownloading(T mission) {
        return mDownloadingQueue.contains(mission);
    }

    @Override
    public boolean isPreparing(T mission) {
        return mPreparingMissions.contains(mission);
    }

    @Override
    public boolean isWaiting(T mission) {
        return mWaitingQueue.contains(mission);
    }

    @Override
    public boolean remove(T mission) {
        if (isDownloading(mission)) {
            mDownloadingQueue.remove(mission);
            mPreparingMissions.remove(mission);
            // TODO pause

            // 从等待队列中的一个任务加入到下载队列
            enqueue(mWaitingQueue.poll());
            return true;
        } else if (isWaiting(mission)) {
            mWaitingQueue.remove(mission);
            return true;
        }
        return false;
    }

    @Override
    public boolean wait(T mission) {
        mDownloadingQueue.remove(mission);
        mPreparingMissions.remove(mission);
        return mWaitingQueue.offer(mission);
    }

    @Override
    public boolean prepare(T mission) {
        if (mPreparingMissions.contains(mission) || !isDownloading(mission)) {
            return false;
        }
        return mPreparingMissions.add(mission);
    }

    @Override
    public boolean enqueue(T mission) {
        if (mission == null) {
            return false;
        }
        if (mDownloadingQueue.size() >= 3) {
            return mWaitingQueue.offer(mission);
        } else {
            return mDownloadingQueue.offer(mission);
        }
    }

    @Override
    public boolean canRetry(T mission, int code, String msg) {
        return false;
    }

    @Override
    public boolean onError(T mission, int code, String msg) {
        return false;
    }

}
