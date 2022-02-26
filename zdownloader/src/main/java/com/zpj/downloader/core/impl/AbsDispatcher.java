package com.zpj.downloader.core.impl;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.ThreadPool;
import com.zpj.utils.ContextUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbsDispatcher<T extends Mission> implements Dispatcher<T> {

    private final ConcurrentHashMap<String, MissionDelegate<T>> mDownloadingQueue = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<T> mWaitingQueue = new ConcurrentLinkedQueue<>();

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
        MissionDelegate<T> delegate = mDownloadingQueue.get(mission.getMissionId());
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
            MissionDelegate<T> delegate = mDownloadingQueue.remove(mission.getMissionId());
            if (delegate != null) {
                delegate.stop();
            }
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
        MissionDelegate<T> delegate = mDownloadingQueue.remove(mission.getMissionId());
        if (delegate != null) {
            delegate.stop();
        }
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
        MissionDelegate<T> delegate = mDownloadingQueue.get(mission.getMissionId());
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
            MissionDelegate<T> delegate = new MissionDelegate<>(mission);
            MissionDelegate<T> oldDelegate = mDownloadingQueue.put(mission.getMissionId(), delegate);
            if (oldDelegate != null) {
                oldDelegate.stop();
            }
            delegate.start();
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

    private static final class MissionDelegate<T extends Mission> {

        private static final String TAG = "MissionHandler";

        private final AtomicBoolean isPreparing = new AtomicBoolean(false);

        private final T mission;
        private final Downloader<T> downloader;

        private volatile long lastDownloaded;
        private volatile long lastTime;

        private final ScheduledExecutorService mExecutor;

        private ScheduledFuture<?> mFuture;

        public MissionDelegate(@NonNull T mission) {
            this.mission = mission;
            downloader = ZDownloader.get(mission);

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, TAG + "_Executor");
                }
            });
            executor.allowCoreThreadTimeOut(true);
            mExecutor = Executors.unconfigurableScheduledExecutorService(executor);
        }

        public boolean isPreparing() {
            return isPreparing.get();
        }

        public synchronized boolean onPreparing() {
            return isPreparing.compareAndSet(false, true);
        }

        public void start() {
            stop();
            lastDownloaded = mission.getDownloaded();
            lastTime = SystemClock.elapsedRealtime();

            mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    long downloaded = downloader.getRepository().queryDownloaded(mission);

                    long delta = downloaded - lastDownloaded;

                    long currentTime = SystemClock.elapsedRealtime();

                    Logger.d(TAG, "progressRunnable--downloaded=" + downloaded + " delta=" + delta);

                    if (delta > 0) {

                        final long speed = (long) (delta * ((currentTime - lastTime) / 1000f));
                        mission.getMissionInfo().setDownloaded(downloaded);
                        mission.getMissionInfo().setSpeed(speed);

                        downloader.getRepository().saveMissionInfo(mission);

                        if (shouldStopped()) {
                            return;
                        }

                        lastDownloaded = downloaded;

                        ThreadPool.post(new Runnable() {
                            @Override
                            public void run() {
                                List<Mission.Observer> observers = mission.getObservers();
                                for (Mission.Observer observer : observers) {
                                    observer.onProgress(mission, speed);
                                }

                                Notifier<T> notifier = downloader.getNotifier();
                                if (notifier != null) {
                                    notifier.onProgress(ContextUtils.getApplicationContext(), mission, mission.getProgress(), false);
                                }
                            }
                        });
                    } else if (shouldStopped()) {
                        return;
                    }
                    lastTime = currentTime;
                }
            }, 0, mission.getConfig().getProgressInterval(), TimeUnit.MILLISECONDS);
        }

        private boolean shouldStopped() {
            if (mission.isPaused() || mission.isError() || mission.isComplete()) {
                stop();
                return true;
            }
            return false;
        }

        public void stop() {
            if (mFuture != null) {
                mFuture.cancel(true);
                mFuture = null;
            }
        }

    }

}
