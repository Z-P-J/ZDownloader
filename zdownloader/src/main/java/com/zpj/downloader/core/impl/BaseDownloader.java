package com.zpj.downloader.core.impl;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.BlockSplitter;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.ExecutorFactory;
import com.zpj.downloader.core.Initializer;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionLoader;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.core.Repository;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.core.db.MissionDatabase;
import com.zpj.downloader.core.db.MissionRepository;
import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.http.UrlConnectionHttpFactory;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.DownloaderConfig;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.ThreadPool;
import com.zpj.utils.ContextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class BaseDownloader<T extends Mission> implements Downloader<T> {

    private static final String TAG = "AbsDownloader";

    private final ArrayList<WeakReference<DownloaderObserver<T>>> mObservers = new ArrayList<>();

    private final HashMap<T, ExecutorService> mExecutors = new HashMap<>();

    private Dispatcher<T> mDispatcher = new MissionDispatcher<>();
    private Initializer<T> mInitializer = new MissionInitializer<>();
    private Notifier<T> mNotifier;
    private Transfer<T> mTransfer = new MissionBlockTransfer<>();
    private final Repository<T> mRepository;

    private final ExecutorService mScheduler = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, getKey() + "_EventScheduler");
        }
    });

    public BaseDownloader() {
        mRepository = new MissionRepository<>(MissionDatabase.getInstance(getKey()));
    }

    @Override
    public void addObserver(DownloaderObserver<T> observer) {
        synchronized (mObservers) {
            mObservers.add(new WeakReference<>(observer));
        }
    }

    @Override
    public void removeObserver(DownloaderObserver<T> observer) {
        synchronized (mObservers) {
            Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
            while (iterator.hasNext()) {
                DownloaderObserver<T> o = iterator.next().get();
                if (o == null || o == observer) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public Config config() {
        return new DownloaderConfig();
    }

    @Override
    public BlockSplitter<T> getBlockDivider() {
        return new MissionBlockSplitter<>();
    }

    @Override
    public Dispatcher<T> getDispatcher() {
        return mDispatcher;
    }

    @Override
    public HttpFactory getHttpFactory() {
        return new UrlConnectionHttpFactory();
    }

    @Override
    public Initializer<T> getInitializer() {
        return mInitializer;
    }

    @Override
    public Notifier<T> getNotifier() {
        return mNotifier;
    }

    @Override
    public Transfer<T> getTransfer() {
        return mTransfer;
    }

    @Override
    public ExecutorFactory<T> getExecutorFactory() {
        return new MissionExecutorFactory<>();
    }

    @Override
    public Repository<T> getRepository() {
        return mRepository;
    }

    @Override
    public void loadMissions(final MissionLoader<T> loader) {
        if (loader == null) {
            return;
        }
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final List<T> missions = getRepository().queryMissions(BaseDownloader.this);
                Logger.d(TAG, "loadMissions missions=" + missions);
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        loader.onLoad(missions);
                    }
                });
            }
        });
    }

    private void execute(T mission, Runnable runnable) {
        ExecutorService executor = mExecutors.get(mission);
        if (executor == null) {
            synchronized (mExecutors) {
                executor = mExecutors.get(mission);
                if (executor == null) {
                    executor = getExecutorFactory().createExecutor(mission);
                    mExecutors.put(mission, executor);
                }
            }
        }
        executor.execute(runnable);
    }

    private void enqueue(final T mission) {
        Logger.d(TAG, "enqueue mission=" + mission);
        if (mDispatcher.enqueue(mission) && mDispatcher.isDownloading(mission)) {
            mission.setErrorCode(0);
            mission.setErrorMessage(null);
            onMissionStart(mission);
            if (mission.getStatus() == Mission.Status.CREATED
                    || mission.getStatus() == Mission.Status.PREPARING
                    || !mission.getMissionInfo().isPrepared()) {
                sendEvent(mission, Event.PREPARE);
                return;
            }
            sendEvent(mission, Event.DOWNLOAD);
        }
    }

    private void sendEvent(final T mission, @Event final int event) {
        Logger.d(TAG, "sendEvent name=" + mission.getName() + " event=" + event);
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                handleEvent(mission, event);
            }
        });
    }

    private void handleEvent(final T mission, @Event int event) {
        if (mission == null) {
            Logger.d(TAG, "handleEvent mission is null!");
            return;
        }
        Logger.d(TAG, "handleEvent name=" + mission.getName() + " event=" + event);
        switch (event) {
            case Event.CREATE:
                if (mDispatcher.isDownloading(mission) || mDispatcher.isWaiting(mission)) {
                    // 防止多次调用
                    Logger.d(TAG, "mission has in the queue!");
                    return;
                }
                if (!mRepository.hasMission(mission)) {
                    mRepository.saveMissionInfo(mission);
                    mRepository.saveConfig(mission.getConfig());
                    onMissionAdd(mission);
                }
                // 任务排队等待开始下载
                enqueue(mission);
                break;
            case Event.WAIT:
                if (mDispatcher.waiting(mission)) {
                    setStatus(mission, Mission.Status.WAITING);
                }
                break;
            case Event.PREPARE:
                if (mDispatcher.prepare(mission)) {
                    setStatus(mission, Mission.Status.PREPARING);
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            Result result = mInitializer.initMission(BaseDownloader.this, mission);
                            Logger.d(TAG, "mission init result=" + result
                                    + " missionId=" + mission.getMissionInfo().getMissionId()
                                    + " configMissionId=" + mission.getConfig().getMissionId()
                                    + " name=" + mission.getName()
                                    + " url=" + mission.getUrl()
                                    + " isBlockDownload=" + mission.isBlockDownload());
                            if (!mission.isDownloading()) {
                                return;
                            }
                            if (result.isOk()) {
                                File location = new File(mission.getConfig().getDownloadPath());
                                if (!location.exists()) {
                                    if (!location.mkdirs()) {
                                        mission.setErrorCode(-1);
                                        mission.setErrorMessage("download path create failed! path=" + location);
                                        // 创建下载路径失败
                                        sendEvent(mission, Event.ERROR);
                                        return;
                                    }
                                }
//                                File file = new File(mission.getFilePath());
//                                if (!file.exists()) {
//                                    file.createNewFile();
//                                }

                                if (mission.isBlockDownload()) {
                                    List<Block> blocks = getBlockDivider().divide(mission);
                                    Logger.d(TAG, "blocks=" + blocks);
                                    if (blocks == null || blocks.isEmpty()) {
                                        mission.setErrorCode(-1);
                                        mission.setErrorMessage("divide block failed!");
                                        // 分片失败
                                        sendEvent(mission, Event.ERROR);
                                        return;
                                    }
                                    mRepository.saveBlocks(blocks);
                                } else {
                                    Block block = new Block(mission.getMissionInfo().getMissionId(), 0, 0);
                                    Logger.d(TAG, "block=" + block);
                                    mRepository.saveBlocks(block);
                                }
                                mission.getMissionInfo().setPrepared(true);
                                mRepository.saveMissionInfo(mission);
                                sendEvent(mission, Event.DOWNLOAD);
                            } else {
                                mission.setErrorCode(result.getCode());
                                mission.setErrorMessage(result.getMessage());
                                mRepository.saveMissionInfo(mission);
                                sendEvent(mission, Event.ERROR);
                            }
                        }
                    });
                }
                break;
            case Event.DOWNLOAD:
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        if (mission.getStatus() == Mission.Status.DOWNLOADING) {
                            return;
                        }
                        setStatus(mission, Mission.Status.DOWNLOADING);
                        ThreadPool.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO notify
                                if (mNotifier != null) {
                                    mNotifier.onProgress(ContextUtils.getApplicationContext(),
                                            mission, mission.getProgress(), false);
                                }
                            }
                        });

                        List<Block> blocks = getRepository().queryShouldDownloadBlocks(mission);
                        Logger.d(TAG, "blocks=" + blocks);
                        if (blocks == null || blocks.isEmpty()) {
                            Logger.e(TAG, "blocks is empty!");
                            sendEvent(mission, Event.COMPLETE);
                        } else {
                            for (final Block block : blocks) {
                                execute(mission, new BlockTask<>(BaseDownloader.this, mission, block));
                            }
                        }
                    }
                });

                break;
            case Event.PROGRESS:
                break;
            case Event.PAUSE:
                boolean downloading = mDispatcher.isDownloading(mission);
                if (mDispatcher.remove(mission)) {
                    setStatus(mission, Mission.Status.PAUSED);
                    ExecutorService executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdownNow();
                    }
                    if (downloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
            case Event.ERROR:
                boolean isDownloading = mDispatcher.isDownloading(mission);
                Logger.d(TAG, "onError isDownloading=" + isDownloading);
                if (mDispatcher.remove(mission)) {
                    setStatus(mission, Mission.Status.ERROR);
                    ExecutorService executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdownNow();
                    }
                    if (isDownloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
            case Event.COMPLETE:
                if (mDispatcher.remove(mission)) {
                    setStatus(mission, Mission.Status.COMPLETE);
                    enqueue(mDispatcher.nextMission());
                }
                break;
            case Event.RESTART:
                mDispatcher.remove(mission);

                MissionInfo info = mission.getMissionInfo();
                info.setPrepared(false);
                info.setDownloaded(0);
                info.setSpeed(0);
                info.setMissionStatus(Mission.Status.CREATED);
                info.setErrorCode(0);
                info.setErrorMessage(null);
                info.setBlockDownload(false);
                info.setLength(0);
                info.setUrl(info.getOriginUrl());
                mRepository.updateMissionInfo(mission);
                mRepository.deleteBlocks(mission);
                setStatus(mission, Mission.Status.CREATED);

                mission.start();

                break;
            case Event.DELETE:
                handleEvent(mission, Event.PAUSE);
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        getRepository().deleteMission(mission);
                    }
                });
                break;
            case Event.CLEAR:
                handleEvent(mission, Event.PAUSE);
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        getRepository().deleteMission(mission);
                        mission.getFile().delete();
                    }
                });
                break;
            case Event.RETRY:
            default:
                break;
        }
    }

    @Override
    public void startMission(final T mission) {
        if (mission != null && mission.canStart()) {
            sendEvent(mission, Event.CREATE);
        }
    }

    @Override
    public void restartMission(T mission) {
        if (mission != null) {
            sendEvent(mission, Event.RESTART);
        }
    }

    @Override
    public void waitingMission(T mission) {
        if (mission != null) {
            sendEvent(mission, Event.WAIT);
        }
    }

    @Override
    public void pauseMission(final T mission) {
        if (mission != null && mission.canPause()) {
            sendEvent(mission, Event.PAUSE);
        }
    }

    @Override
    public void deleteMission(T mission) {
        if (mission != null) {
            sendEvent(mission, Event.DELETE);
        }
    }

    @Override
    public void clearMission(T mission) {
        if (mission != null) {
            sendEvent(mission, Event.CLEAR);
        }
    }

    private void setStatus(final T mission, final @Mission.Status int status) {
        Logger.d(TAG, "setStatus name=" + mission.getName() + " status=" + status);
        mission.setStatus(status);
        mRepository.updateStatus(mission, status);
        switch (status) {
            case Mission.Status.PREPARING:

                break;
            case Mission.Status.DOWNLOADING:
                onMissionStart(mission);
                break;
            case Mission.Status.WAITING:
                onMissionWaiting(mission);
                break;
            case Mission.Status.PAUSED:
                onMissionPaused(mission);
                break;
            case Mission.Status.ERROR:
                onMissionError(mission);
                break;
            case Mission.Status.COMPLETE:
                onMissionFinished(mission);
                break;
            case Mission.Status.DELETE:
                onMissionDelete(mission);
                break;
            case Mission.Status.CLEAR:
                onMissionClear(mission);
                break;
            default:
                break;
        }

    }

    private void onMissionAdd(final T mission) {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
                while (iterator.hasNext()) {
                    DownloaderObserver<T> observer = iterator.next().get();
                    if (observer == null) {
                        iterator.remove();
                    } else {
                        observer.onMissionAdd(mission);
                    }
                }
            }
        });
    }

    private void onMissionStart(final T mission) {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onStart();
                }
            }
        });
    }

    private void onMissionPaused(final T mission) {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onPaused();
                }

                if (mNotifier != null) {
                    mNotifier.onProgress(ContextUtils.getApplicationContext(),
                            mission, mission.getProgress(), true);
                }
            }
        });
    }

    private void onMissionError(final T mission) {
        Logger.d(TAG, "onMissionError mission=" + mission.getName());
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onError(new Error(mission.getErrorMessage()));
                }

                if (mNotifier != null) {
                    mNotifier.onError(ContextUtils.getApplicationContext(),
                            mission, mission.getErrorCode());
                }
            }
        });

    }

    private void onMissionWaiting(final T mission) {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onWaiting();
                }
            }
        });
    }

    private void onMissionClear(T mission) {
        // TODO 移除本地记录
    }

    private void onMissionDelete(final T mission) {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                onMissionClear(mission);
                Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
                while (iterator.hasNext()) {
                    DownloaderObserver<T> observer = iterator.next().get();
                    if (observer == null) {
                        iterator.remove();
                    } else {
                        observer.onMissionDelete(mission);
                    }
                }
            }
        });
    }

    private void onMissionFinished(final T mission) {
        // 销毁线程池
        ExecutorService executor = mExecutors.remove(mission);
        if (executor != null) {
            executor.shutdownNow();
        }

        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onFinished();
                }

                Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
                while (iterator.hasNext()) {
                    DownloaderObserver<T> observer = iterator.next().get();
                    if (observer == null) {
                        iterator.remove();
                    } else {
                        observer.onMissionFinished(mission);
                    }
                }

                if (mNotifier != null) {
                    mNotifier.onFinished(ContextUtils.getApplicationContext(), mission);
                }
            }
        });
    }

    private static class BlockTask<T extends Mission> implements Runnable {

        private static final String TAG = "BlockTask";

        private final BaseDownloader<T> downloader;
        private final T mission;
        @NonNull
        private final Block block;

        public BlockTask(BaseDownloader<T> downloader, T mission, @NonNull Block block) {
            this.downloader = downloader;
            this.mission = mission;
            this.block = block;
        }

        @Override
        public void run() {
            if (!mission.isDownloading()) {
                Logger.d(TAG, "return by paused!");
                return;
            }
            Result result = downloader.getTransfer().transfer(mission, block);
            Logger.d(TAG, "result=" + result);
            if (result.getCode() == Result.CANCEL_BY_PAUSED) {
                Logger.d(TAG, "block transfer cancel by paused!");
                return;
            }
            if (result.isOk()) {
                // TODO 通知block下载完成
                block.setStatus(1);
                downloader.getRepository().updateBlock(block);
                List<Block> blocks = downloader.getRepository().queryShouldDownloadBlocks(mission);
                Logger.d(TAG, "unfinishedBlocks=" + blocks);
                if (blocks.isEmpty()) {
                    downloader.sendEvent(mission, Event.COMPLETE);
                }
            } else {

                if (downloader.getDispatcher().canRetry(mission, result.getCode(), result.getMessage())) {
                    ThreadPool.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloader.execute(mission, BlockTask.this);
                        }
                    }, mission.getConfig().getRetryDelayMillis());
                } else {
                    mission.setErrorCode(result.getCode());
                    mission.setErrorMessage(result.getMessage());
                    downloader.sendEvent(mission, Event.ERROR);
                }
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            Event.CREATE, Event.PREPARE, Event.WAIT, Event.DOWNLOAD, Event.PROGRESS,
            Event.PAUSE, Event.ERROR, Event.RETRY, Event.COMPLETE, Event.RESTART, Event.DELETE, Event.CLEAR
    })
    public @interface Event {
        int CREATE = 0;
        int PREPARE = 1;
        int WAIT = 2;
        int DOWNLOAD = 3;
        int PROGRESS = 4;
        int PAUSE = 5;
        int ERROR = 6;
        int RETRY = 7;
        int COMPLETE = 8;

        int RESTART = 9;

        int DELETE = 10;
        int CLEAR = 11;
    }

}
