package com.zpj.downloader.core;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.downloader.impl.DefaultConflictPolicy;
import com.zpj.downloader.utils.ContextProvider;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.AutoRenameHelper;
import com.zpj.downloader.utils.ThreadPool;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class BaseDownloader<T extends Mission> implements Downloader<T> {

    private static final String TAG = "BaseDownloader";

    private final ArrayList<WeakReference<DownloaderObserver<T>>> mObservers = new ArrayList<>();

    private final HashMap<T, MissionExecutor<T>> mExecutors = new HashMap<>();

    private final String mKey;

    private final Dispatcher<T> mDispatcher;
    private final Initializer<T> mInitializer;
    private final Notifier<? super T> mNotifier;
    private final Transfer<T> mTransfer;
    private final Repository<T> mRepository;
    private final HttpFactory mHttpFactory;
    private final BlockSplitter<T> mBlockSplitter;
    private final MissionExecutorFactory<T> mMissionExecutorFactory;
    private final ConflictPolicy mConflictPolicy;

    private final ExecutorService mScheduler = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, getKey() + "_EventScheduler");
        }
    });

    public BaseDownloader(@NonNull DownloaderConfig<T> config) {
        this.mKey = config.getKey();
        this.mDispatcher = Objects.requireNonNull(config.getDispatcher(),
                "Dispatcher must not be null!");
        this.mInitializer = Objects.requireNonNull(config.getInitializer(),
                "Initializer must not be null!");
        this.mNotifier = config.getNotifier();
        this.mTransfer = Objects.requireNonNull(config.getTransfer(),
                "Transfer must not be null!");
        this.mRepository = Objects.requireNonNull(config.getRepository(),
                "Repository must not be null!");
        this.mBlockSplitter = Objects.requireNonNull(config.getBlockSplitter(),
                "BlockSplitter must not be null!");
        this.mHttpFactory = Objects.requireNonNull(config.getHttpFactory(),
                "HttpFactory must not be null!");
        this.mMissionExecutorFactory = Objects.requireNonNull(config.getExecutorFactory(),
                "ExecutorFactory must not be null!");
        this.mConflictPolicy = config.getConflictPolicy();
    }

    @Override
    public void addObserver(final DownloaderObserver<T> observer) {
        ThreadPool.post(() -> mObservers.add(new WeakReference<>(observer)));
    }

    @Override
    public void removeObserver(final DownloaderObserver<T> observer) {
        ThreadPool.post(() -> {
            Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
            while (iterator.hasNext()) {
                DownloaderObserver<T> o = iterator.next().get();
                if (o == null || o == observer) {
                    iterator.remove();
                }
            }
        });
    }

    @NonNull
    @Override
    public String getKey() {
        return mKey;
    }

    //    @Override
//    public Config config() {
//        return new DownloaderConfig();
//    }

    @Override
    public BlockSplitter<T> getBlockDivider() {
        return mBlockSplitter;
    }

    @Override
    public Dispatcher<T> getDispatcher() {
        return mDispatcher;
    }

    @Override
    public HttpFactory getHttpFactory() {
        return mHttpFactory;
    }

    @Override
    public Initializer<T> getInitializer() {
        return mInitializer;
    }

    @Override
    public Notifier<? super T> getNotifier() {
        return mNotifier;
    }

    @Override
    public Transfer<T> getTransfer() {
        return mTransfer;
    }

    @Override
    public MissionExecutorFactory<T> getExecutorFactory() {
        return mMissionExecutorFactory;
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
        ThreadPool.execute(() -> {
            final List<T> missions = getRepository().queryMissions(BaseDownloader.this);
            Logger.d(TAG, "loadMissions missions=%s", missions);
            ThreadPool.post(() -> loader.onLoad(missions));
        });
    }

    private void execute(T mission, BlockTask<T> block) {
        MissionExecutor<T> executor = mExecutors.get(mission);
        if (executor == null) {
            synchronized (mExecutors) {
                executor = mExecutors.get(mission);
                if (executor == null) {
                    executor = getExecutorFactory().createExecutor(mission);
                    mExecutors.put(mission, executor);
                }
            }
        }
        executor.execute(block);
    }

    private void enqueue(final T mission) {
        if (mission == null) {
            return;
        }
        Logger.d(TAG, "enqueue next mission=%s", mission);
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

    private void sendEvent(@NonNull final T mission, @Event final int event) {
        Logger.d(TAG, "sendEvent name=%s event=%s", mission.getName(), eventToString(event));
        mScheduler.execute(() -> handleEvent(mission, event));
    }

    private void handleEvent(final T mission, @Event int event) {
        if (mission == null) {
            Logger.d(TAG, "handleEvent mission is null!");
            return;
        }
        Logger.d(TAG, "handleEvent name=%s event=%s", mission.getName(), eventToString(event));
        switch (event) {
            case Event.CREATE:
                if (mDispatcher.isDownloading(mission) || mDispatcher.isWaiting(mission)) {
                    // 防止多次调用
                    Logger.d(TAG, "mission has in the queue!");
                    return;
                }

                if (mRepository.queryMissionInfo(mission.getMissionId()) == null) {
                    Mission oldMission = mRepository.queryMissionByUrl(this, mission.getUrl());
                    if (oldMission != null) {
                        if (mConflictPolicy == null) {
                            if (new DefaultConflictPolicy().isConflict(mission, oldMission)) {
                                renameOnConflict(mission);
                            }
                        } else if (mConflictPolicy.isConflict(mission, oldMission)) {
                            ThreadPool.post(() -> {
                                mConflictPolicy.onConflict(mission, accept -> {
                                    mScheduler.execute(() -> {
                                        if (accept) {
                                            if (!TextUtils.isEmpty(mission.getName())) {
                                                File file = AutoRenameHelper.renameFile(mission.getFile());
                                                mission.setName(file.getName());
                                            }
                                            mRepository.saveMissionInfo(mission);
                                            mRepository.saveConfig(mission.getConfig());
                                            onMissionAdd(mission);
                                            // 任务排队等待开始下载
                                            enqueue(mission);
                                        } else {
                                            Logger.w(TAG, "onConflict reject mission: %s", mission.getUrl());
                                        }
                                    });
                                });
                            });
                            return;
                        }
                    }

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
                    onMissionPrepare(mission);
                }
                break;
            case Event.DOWNLOAD:
                ThreadPool.execute(() -> {

                    if (mission.getStatus() == Mission.Status.DOWNLOADING) {
                        return;
                    }
                    setStatus(mission, Mission.Status.DOWNLOADING);
                    ThreadPool.post(() -> {
                        if (mNotifier != null) {
                            mNotifier.onProgress(ContextProvider.getApplicationContext(),
                                    mission, mission.getProgress(), false);
                        }
                    });

                    List<Block> blocks = getRepository().queryShouldDownloadBlocks(mission);
                    Logger.d(TAG, "blocks=%s", blocks);
                    if (blocks == null || blocks.isEmpty()) {
                        Logger.w(TAG, "blocks is empty!");
                        sendEvent(mission, Event.COMPLETE);
                    } else {
                        for (final Block block : blocks) {
                            execute(mission, new BlockTask<>(BaseDownloader.this, mission, block));
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
                    MissionExecutor<T> executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdown();
                    }
                    if (downloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
            case Event.ERROR:
                boolean isDownloading = mDispatcher.isDownloading(mission);
                Logger.d(TAG, "onError isDownloading=%s", isDownloading);
                if (mDispatcher.remove(mission)) {
                    setStatus(mission, Mission.Status.ERROR);
                    MissionExecutor<T> executor = mExecutors.remove(mission);
                    if (executor != null) {
                        executor.shutdown();
                    }
                    if (isDownloading) {
                        enqueue(mDispatcher.nextMission());
                    }
                }
                break;
            case Event.COMPLETE:
                if (mDispatcher.remove(mission)) {
                    setStatus(mission, Mission.Status.COMPLETE);
                    MissionInfo info = mission.getMissionInfo();
                    if (info.getLength() < 0) {
                        info.setLength(info.getDownloaded());
                    }
                    mRepository.updateMissionInfo(mission);
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
                ThreadPool.execute(() -> {
                    getRepository().deleteMission(mission);
                    ThreadPool.post(() -> onMissionDelete(mission));
                });
                break;
            case Event.CLEAR:
                handleEvent(mission, Event.PAUSE);
                ThreadPool.execute(() -> {
                    getRepository().deleteMission(mission);
                    ThreadPool.post(() -> onMissionDelete(mission));
                    mission.getFile().delete();
                    ThreadPool.post(() -> onMissionClear(mission));
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

    protected void setStatus(final T mission, final @Mission.Status int status) {
        Logger.d(TAG, "setStatus name=%s status=%d", mission.getName(), status);
        mission.setStatus(status);
        mRepository.updateStatus(mission, status);
        switch (status) {
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
            case Mission.Status.CREATED:
            case Mission.Status.PREPARING:
            default:
                break;
        }

    }

    protected void onMissionAdd(@NonNull final T mission) {
        Logger.d(TAG, "onMissionAdd mission=%s", mission.getName());
        ThreadPool.post(() -> {
            Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
            while (iterator.hasNext()) {
                DownloaderObserver<T> observer = iterator.next().get();
                if (observer == null) {
                    iterator.remove();
                } else {
                    observer.onMissionAdd(mission);
                }
            }
        });
    }

    protected void onMissionPrepare(@NonNull final T mission) {
        ThreadPool.execute(() -> {
            Result result = mInitializer.initMission(BaseDownloader.this, mission);
            Logger.d(TAG,
                    "mission init result=%s missionId=%s configMissionId=%s name=%s url=%s isBlockDownload=%s",
                    result, mission.getMissionInfo().getMissionId(),
                    mission.getConfig().getMissionId(), mission.getName(),
                    mission.getUrl(), mission.isBlockDownload());
            if (!mission.isDownloading()) {
                return;
            }
            if (result.isOk()) {

                if (mRepository.hasMission(mission)) {
                    renameOnConflict(mission);
                }

                File location = new File(mission.getConfig().getDownloadPath());
                if (!location.exists() && !location.mkdirs()) {
                    // 文件保存路径创建失败
                    mission.setErrorCode(-1);
                    mission.setErrorMessage("download path create failed! path=" + location);
                    // 创建下载路径失败
                    sendEvent(mission, Event.ERROR);
                    return;
                }

                if (mission.isBlockDownload()) {
                    List<Block> blocks = getBlockDivider().divide(mission);
                    Logger.d(TAG, "blocks=%s", blocks);
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
                    Logger.d(TAG, "block=%s", block);
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
        });
    }

    protected void onMissionStart(@NonNull final T mission) {
        Logger.d(TAG, "onMissionStart mission=%s", mission.getName());
        ThreadPool.post(() -> {
            List<Mission.Observer> observers = mission.getObservers();
            for (Mission.Observer observer : observers) {
                observer.onStart();
            }
        });
    }

    protected void onMissionPaused(@NonNull final T mission) {
        Logger.d(TAG, "onMissionPaused mission=%s", mission.getName());
        ThreadPool.post(() -> {
            List<Mission.Observer> observers = mission.getObservers();
            for (Mission.Observer observer : observers) {
                observer.onPaused();
            }

            if (mNotifier != null) {
                mNotifier.onProgress(ContextProvider.getApplicationContext(),
                        mission, mission.getProgress(), true);
            }
        });
    }

    protected void onMissionError(@NonNull final T mission) {
        Logger.d(TAG, "onMissionError mission=%s code=%d msg=%s",
                mission.getName(), mission.getErrorCode(), mission.getErrorMessage());
        ThreadPool.post(() -> {
            List<Mission.Observer> observers = mission.getObservers();
            for (Mission.Observer observer : observers) {
                observer.onError(mission.getErrorCode(), mission.getErrorMessage());
            }

            if (mNotifier != null) {
                mNotifier.onError(ContextProvider.getApplicationContext(),
                        mission, mission.getErrorCode());
            }
        });

    }

    protected void onMissionWaiting(@NonNull final T mission) {
        Logger.d(TAG, "onMissionWaiting mission=%s", mission.getName());
        ThreadPool.post(() -> {
            List<Mission.Observer> observers = mission.getObservers();
            for (Mission.Observer observer : observers) {
                observer.onWaiting();
            }
        });
    }

    protected void onMissionClear(T mission) {
        Logger.d(TAG, "onMissionClear mission=%s", mission.getName());
        // TODO 移除本地记录
    }

    protected void onMissionDelete(@NonNull final T mission) {
        Logger.d(TAG, "onMissionDelete mission=%s", mission.getName());
        ThreadPool.post(() -> {
            Iterator<WeakReference<DownloaderObserver<T>>> iterator = mObservers.iterator();
            while (iterator.hasNext()) {
                DownloaderObserver<T> observer = iterator.next().get();
                if (observer == null) {
                    iterator.remove();
                } else {
                    observer.onMissionDelete(mission);
                }
            }
        });
    }

    protected void onMissionFinished(@NonNull final T mission) {
        Logger.d(TAG, "onMissionFinished mission=%s", mission.getName());
        // 销毁线程池
        MissionExecutor<T> executor = mExecutors.remove(mission);
        if (executor != null) {
            executor.shutdown();
        }

        ThreadPool.post(() -> {
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
                mNotifier.onFinished(ContextProvider.getApplicationContext(), mission);
            }
        });
    }

    private void renameOnConflict(@NonNull final T mission) {
        String oldName = mission.getName();
        if (TextUtils.isEmpty(oldName)) {
            Logger.w(TAG, "renameOnConflict skip because mission name is empty!");
        } else {
            File file = AutoRenameHelper.renameFile(mission.getFile());
            mission.setName(file.getName());
            Logger.d(TAG, "renameOnConflict rename '%s' to '%s'", oldName, mission.getName());
        }
    }

    public static class BlockTask<T extends Mission> implements Runnable {

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
            Logger.d(TAG, "result=%s", result);
            if (result.getCode() == Result.CANCEL_BY_PAUSED) {
                Logger.d(TAG, "block transfer cancel by paused!");
                return;
            }
            if (result.isOk()) {
                // TODO 通知block下载完成
                block.setStatus(1);
                downloader.getRepository().updateBlock(block);
                List<Block> blocks = downloader.getRepository().queryShouldDownloadBlocks(mission);
                Logger.d(TAG, "unfinishedBlocks=%s", blocks);
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

    private static String eventToString(@Event int event) {
        switch (event) {
            case Event.CREATE:
                return "create";
            case Event.PREPARE:
                return "prepare";
            case Event.WAIT:
                return "wait";
            case Event.DOWNLOAD:
                return "download";
            case Event.PROGRESS:
                return "progress";
            case Event.PAUSE:
                return "pause";
            case Event.ERROR:
                return "error";
            case Event.RETRY:
                return "retry";
            case Event.COMPLETE:
                return "complete";
            case Event.RESTART:
                return "restart";
            case Event.DELETE:
                return "delete";
            case Event.CLEAR:
                return "clear";
            default:
                return "unknown";
        }
    }

}
