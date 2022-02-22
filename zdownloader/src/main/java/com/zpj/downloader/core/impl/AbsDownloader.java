package com.zpj.downloader.core.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.BlockDivider;
import com.zpj.downloader.core.Dao;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.Initializer;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionLoader;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.ExecutorFactory;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.core.Updater;
import com.zpj.downloader.core.http.UrlConnectionHttpFactory;
import com.zpj.downloader.core.db.MissionDatabase;
import com.zpj.downloader.core.db.RoomDao;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.ThreadPool;
import com.zpj.utils.ContextUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public abstract class AbsDownloader<T extends Mission> implements Downloader<T> {

    private static final String TAG = "AbsDownloader";

//    private static final class InstanceHolder {
//        private static final AbsDownloader INSTANCE = new AbsDownloader();
//    }
//
//    public static AbsDownloader getInstance() {
//        return InstanceHolder.INSTANCE;
//    }

    private final ArrayList<WeakReference<DownloaderObserver<T>>> mObservers = new ArrayList<>();

    private final HashMap<T, ExecutorService> mExecutors = new HashMap<>();

    private Dispatcher<T> mDispatcher = new AbsDispatcher<>();
    private Initializer<T> mInitializer = new MissionInitializer<>();
//    private Serializer<T> mSerializer;
    private Notifier<T> mNotifier;
    private Transfer<T> mTransfer = new BlockTransfer<>();
//    private MissionFactory<T> mMissionFactory;
    private Dao<T> mDao;

    private static class MissionObservers {
        protected ArrayList<WeakReference<Mission.Observer>> mObservers = new ArrayList<>();
    }

    public AbsDownloader() {
        mDao = new RoomDao<>(MissionDatabase.getInstance(getKey()));
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

//    @Override
//    public T download(String url) {
//        return download(url, null);
//    }
//
//    @Override
//    public T download(String url, String name) {
//        Config config = new Config(config());
//
//        AbsMission mission = new AbsMission(config);
//        mission.url = url;
//        mission.originUrl = url;
//        mission.name = name;
//        mission.uuid = UUID.randomUUID().toString();
//        mission.createTime = System.currentTimeMillis();
//        return mission;
//    }

    @Override
    public void setBlockDivider(BlockDivider<T> divider) {

    }

    @Override
    public BlockDivider<T> getBlockDivider() {
        return new BaseBlockDivider<>();
    }

    @Override
    public void setDispatcher(Dispatcher<T> dispatcher) {
        this.mDispatcher = dispatcher;
    }

    @Override
    public Dispatcher<T> getDispatcher() {
        return mDispatcher;
    }

    @Override
    public void setHttpFactory(HttpFactory httpFactory) {

    }

    @Override
    public HttpFactory getHttpFactory() {
        return new UrlConnectionHttpFactory();
    }

    @Override
    public void setInitializer(Initializer<T> initializer) {
        this.mInitializer = initializer;
    }

    @Override
    public Initializer<T> getInitializer() {
        return mInitializer;
    }

    @Override
    public void setNotifier(Notifier<T> notifier) {
        this.mNotifier = notifier;
    }

    @Override
    public Notifier<T> getNotifier() {
        return mNotifier;
    }

//    @Override
//    public void setSerializer(Serializer<T> serializer) {
//        this.mSerializer = serializer;
//    }
//
//    @Override
//    public Serializer<T> getSerializer() {
//        return mSerializer;
//    }

    @Override
    public void setTransfer(Transfer<T> transfer) {
        this.mTransfer = transfer;
    }

    @Override
    public Transfer<T> getTransfer() {
        return mTransfer;
    }

    @Override
    public void setExecutorFactory(ExecutorFactory<T> executorFactory) {

    }

    @Override
    public ExecutorFactory<T> getExecutorFactory() {
        return new AbsExecutorFactory<>();
    }

    @Override
    public void setUpdater(Updater updater) {

    }

    @Override
    public Updater getUpdater() {
        return null;
    }

    @Override
    public Dao<T> getDao() {
        return mDao;
    }

    @Override
    public void loadMissions(final MissionLoader<T> loader) {
        if (loader == null) {
            return;
        }
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final List<T> missions = getDao().queryMissions(AbsDownloader.this);
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
            onMissionStart(mission);
            if (mission.getStatus() == Mission.Status.CREATED
                    || mission.getStatus() == Mission.Status.PREPARING
                    || !mission.getMissionInfo().isPrepared()) {
                mission.prepare();
                return;
            }
            notifyStatus(mission, Mission.Status.DOWNLOADING);
        }
    }

    @Override
    public void notifyStatus(final T mission, @Mission.Status final int status) {
        if (mission == null) {
            return;
        }
        Logger.d(TAG, "status=" + status + " getStatus=" + mission.getStatus());
        if (status != Mission.Status.CREATED) {
            if (mission.getStatus() == status) {
                return;
            }
            mission.setStatus(status);
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mDao.updateStatus(mission, status);
                }
            });
        }
        switch (status) {
            case Mission.Status.CREATED:
                if (mDispatcher.isDownloading(mission) || mDispatcher.isWaiting(mission)) {
                    // 防止多次调用
                    Logger.d(TAG, "mission has in the queue!");
                    return;
                }
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        // TODO 将任务保存至本地，需要判断本地是否存在该任务

                        if (!mDao.hasMission(mission)) {
                            mDao.saveMissionInfo(mission);
                            mDao.saveConfig(mission.getConfig());


                            onMissionAdd(mission);
                        }

                        // 任务排队等待开始下载
                        enqueue(mission);
                    }
                });
                break;
            case Mission.Status.WAITING:
                if (mDispatcher.waiting(mission)) {
                    onMissionWaiting(mission);
                }
                break;
            case Mission.Status.PREPARING:
                if (mDispatcher.prepare(mission)) {
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            Result result = mInitializer.initMission(AbsDownloader.this, mission);
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
                                    location.mkdirs();
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
                                        // 分片失败
                                        notifyStatus(mission, Mission.Status.ERROR);
                                        return;
                                    }
                                    mDao.saveBlocks(blocks);
                                } else {
                                    Block block = new Block(mission.getMissionInfo().getMissionId(), 0, 0);
                                    Logger.d(TAG, "block=" + block);
                                    mDao.saveBlocks(block);
                                }
                                mission.getMissionInfo().isPrepared = true;
                                mDao.saveMissionInfo(mission);
                                notifyStatus(mission, Mission.Status.DOWNLOADING);
                            } else {
                                mission.setErrorCode(result.getCode());
                                mission.setErrorMessage(result.getMessage());
                                notifyStatus(mission, Mission.Status.ERROR);
                                mDao.saveMissionInfo(mission);
                            }
                        }
                    });
                }
                break;
            case Mission.Status.DOWNLOADING:
//                if (mission.isBlockDownload()) {
//                    ThreadPool.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            for (final Block block : getDao().queryUnfinishedBlocks(mission)) {
//                                execute(mission, new BlockTask<>(AbsDownloader.this, mission, block));
//                            }
//                        }
//                    });
//                } else {
//                    execute(mission, new BlockTask<>(this, mission, null));
//                }
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Block> blocks = getDao().queryShouldDownloadBlocks(mission);
                        Logger.d(TAG, "blocks=" + blocks);
                        if (blocks == null || blocks.isEmpty()) {
                            Logger.e(TAG, "blocks is empty!");
//                            mission.setErrorCode(-1);
//                            mission.setErrorMessage("blocks is empty!");
//                            notifyStatus(mission, Mission.Status.ERROR);

                            notifyStatus(mission, Mission.Status.COMPLETE);

                        } else {
                            for (final Block block : blocks) {
                                execute(mission, new BlockTask<>(AbsDownloader.this, mission, block));
                            }
                        }
                    }
                });
                // TODO notify
                if (mNotifier != null) {
                    mNotifier.onProgress(ContextUtils.getApplicationContext(),
                            mission, mission.getProgress(), false);
                }

                break;
            case Mission.Status.PAUSED:
                boolean downloading = mDispatcher.isDownloading(mission);
                if (mDispatcher.remove(mission)) {
                    ExecutorService executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdown();
                    }
                    onMissionPaused(mission);
                    if (downloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
            case Mission.Status.ERROR:
                boolean isDownloading = mDispatcher.isDownloading(mission);
                Logger.d(TAG, "onError isDownloading=" + isDownloading);
                if (mDispatcher.remove(mission)) {
                    ExecutorService executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdown();
                    }
                    onMissionError(mission);
                    if (isDownloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
//            case Mission.Status.RETRYING:
//                // TODO notify
//                ThreadPool.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mission.start();
//                    }
//                }, mission.getConfig().getRetryDelayMillis());
//                break;
            case Mission.Status.CLEAR:
                onMissionClear(mission);
                break;
            case Mission.Status.DELETE:
//                delete(mission);
                onMissionDelete(mission);
                break;
            case Mission.Status.COMPLETE:
                // TODO notify
                if (mDispatcher.remove(mission)) {
                    ExecutorService executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdown();
                    }
                    onMissionFinished(mission);
                    enqueue(mDispatcher.nextMission());
                }
                break;
            default:
                break;
        }
        notifyStatus(mission);

    }

    protected void notifyStatus(final T mission) {
        final int missionStatus = mission.getStatus();
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    switch (missionStatus) {
                        case Mission.Status.PREPARING:
                            observer.onPrepare();
                            break;
//                        case Mission.Status.START:
//                            observer.onStart();
//                            break;
                        case Mission.Status.DOWNLOADING:
                            observer.onProgress(mission, 0);
                            break;
                        case Mission.Status.WAITING:
                            observer.onWaiting();
                            break;
                        case Mission.Status.PAUSED:
                            observer.onPaused();
                            break;
//                        case Mission.Status.RETRYING:
//                            observer.onRetrying();
//                            break;
                        case Mission.Status.ERROR:
                            // TODO
                            observer.onError(new Error("" + mission.getErrorCode()));
                            break;
                        case Mission.Status.COMPLETE:
                            observer.onProgress(mission, 0);
                            observer.onFinished();
                            break;
                        case Mission.Status.DELETE:
                            observer.onDelete();
                            break;
                        case Mission.Status.CLEAR:
                            observer.onClear();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
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
        Logger.d(TAG, "onMissionError mission=" + mission.getUrl());
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                List<Mission.Observer> observers = mission.getObservers();
                for (Mission.Observer observer : observers) {
                    observer.onStart();
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
        Executor executor = mExecutors.remove(mission);
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdownNow();
        }

        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
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

        private final AbsDownloader<T> downloader;
        private final T mission;
        @NonNull
        private final Block block;

        public BlockTask(AbsDownloader<T> downloader, T mission, @NonNull Block block) {
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
//                downloader.getDao().updateBlockDownloaded(block, block.)
                block.setStatus(1);
                downloader.getDao().updateBlock(block);
//                if (mission.isBlockDownload()) {
//                    // TODO
//
//                } else {
//                    downloader.notifyStatus(mission, Mission.Status.COMPLETE);
//                }
                List<Block> blocks = downloader.getDao().queryShouldDownloadBlocks(mission);
                Logger.d(TAG, "unfinishedBlocks=" + blocks);
                if (blocks.isEmpty()) {
                    downloader.notifyStatus(mission, Mission.Status.COMPLETE);
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
                    mission.pause();
                    mission.setErrorCode(result.getCode());
                    downloader.notifyStatus(mission, result.getCode());
                }
            }
        }
    }

}
