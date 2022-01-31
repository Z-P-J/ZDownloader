package com.zpj.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.utils.ExecutorUtils;
import com.zpj.utils.FileUtils;
import com.zpj.utils.FormatUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Z-P-J
 */
@Keep
public class BaseMission<T extends BaseMission<T>> extends BaseConfig<T> implements Serializable {

    private static final String TAG = BaseMission.class.getSimpleName();

    public interface MissionListener {

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
    public interface Status {
        int MEW = 0;
        int PREPARING = 1;
        int RUNNING = 2;
        int WAITING = 3;
        int PAUSED = 4;
        int ERROR = 5;
        int RETRYING = 6;
        int FINISHED = 7;
    }

    @Keep
    public enum MissionStatus {
        NEW("已创建"),
        PREPARING("准备中"),
        START("已开始"),
        RUNNING("下载中"),
        WAITING("等待中"),
        PAUSED("已暂停"),
        FINISHED("已完成"),
        ERROR("出错了"),
        RETRYING("重试中"),
        DELETE("已删除"),
        CLEAR("已清除");

        private final String statusName;

        MissionStatus(String name) {
            statusName = name;
        }

        @NonNull
        @Override
        public String toString() {
            return statusName;
        }
    }

    protected final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
    protected final ConcurrentLinkedQueue<Long> finished = new ConcurrentLinkedQueue<>();
    protected final ArrayList<Long> speedHistoryList = new ArrayList<>();
    protected final AtomicLong done = new AtomicLong(0);

    protected volatile String uuid = "";
    protected volatile String name = "";
    protected volatile String url = "";
    protected volatile String originUrl = "";
    protected volatile long createTime = 0;
    protected volatile long finishTime = 0;
    protected volatile long blocks = 1;
    protected volatile long length = 0;
    protected volatile MissionStatus missionStatus = MissionStatus.NEW;
    protected volatile boolean isBlockDownload = false;
    protected volatile int errCode = -1;
    protected volatile boolean hasPrepared = false;

    //-----------------------------------------------------transient---------------------------------------------------------------

    protected transient volatile ProgressUpdater progressUpdater;

    protected transient volatile AtomicInteger errorCount;

    protected transient AtomicInteger finishCount;
    protected transient AtomicInteger aliveThreadCount;

    protected transient ArrayList<WeakReference<MissionListener>> mListeners;

    protected transient volatile float speed = 0f;

    protected transient volatile Handler handler;
    protected transient volatile boolean isCreate = false;
    protected transient ThreadPoolExecutor threadPoolExecutor;
    protected transient Error error;


    //------------------------------------------------------runnables---------------------------------------------

    protected Handler getHandler() {
        if (handler == null) {
            synchronized (BaseMission.class) {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

    public void post(Runnable runnable) {
        getHandler().post(runnable);
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        getHandler().postDelayed(runnable, delayMillis);
    }

    private ProgressUpdater getProgressUpdater() {
        if (progressUpdater == null) {
            synchronized (BaseMission.class) {
                if (progressUpdater == null) {
                    progressUpdater = new ProgressUpdater(this);
                }
            }
        }
        return progressUpdater;
    }

    protected void prepareMission() {
        DownloadManagerImpl.getInstance().insertMission(this);
        writeMissionInfo();
        Log.d(TAG, "start hasInit=false initMission");
        notifyStatus(MissionStatus.PREPARING);

        ExecutorUtils.submitIO(new MissionInitializer(this));
    }

    protected BaseMission() {

    }

    //-------------------------下载任务状态-----------------------------------
    public boolean isPrepare() {
        return missionStatus == MissionStatus.PREPARING;
    }

    public boolean isRunning() {
        return missionStatus == MissionStatus.RUNNING;
    }

    public boolean isWaiting() {
        return missionStatus == MissionStatus.WAITING;
    }

    public boolean isPause() {
        return missionStatus == MissionStatus.PAUSED;
    }

    public boolean isFinished() {
        return missionStatus == MissionStatus.FINISHED;
    }

    public boolean isError() {
        return missionStatus == MissionStatus.ERROR;
    }

    public boolean canPause() {
        return isRunning() || isWaiting() || isPrepare();
    }

    public boolean canStart() {
        return isPause() || isError() || missionStatus == MissionStatus.NEW;
    }


    //----------------------------------------------------------operation------------------------------------------------------------
    private void onStart() {
        if (isFinished()) {
            return;
        }
        errCode = -1;
        if (length < 0) {
            length = 0;
        }
        if (errorCount == null) {
            errorCount = new AtomicInteger(0);
        } else {
            errorCount.set(0);
        }
        if (finishCount == null) {
            finishCount = new AtomicInteger(0);
        } else {
            finishCount.set(0);
        }
        if (aliveThreadCount == null) {
            aliveThreadCount = new AtomicInteger(0);
        } else {
            aliveThreadCount.set(0);
        }
        if (!isBlockDownload) {
            setThreadCount(1);
            done.set(0);
            blocks = 1;
            queue.clear();
            queue.add(0L);
        }
        if (hasPrepared) {
            for (long position = 0; position < getBlocks(); position++) {
                if (!queue.contains(position) && !finished.contains(position)) {
                    queue.add(position);
                }
            }
        }
        if (canPause()) {
            missionStatus = MissionStatus.PAUSED;
            writeMissionInfo();
        }
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(threadCount, threadCount * 2,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        } else {
            threadPoolExecutor.setCorePoolSize(threadCount);
            threadPoolExecutor.setMaximumPoolSize(threadCount * 2);
        }
    }

    protected void onCreate() {

    }

    protected void onDestroy() {

    }

    final void firstCreate() {
        if (!isCreate) {
            isCreate = true;
            onCreate();
        }
    }

    public void start() {
        if (!canStart()) {
            return;
        }
        firstCreate();
        onStart();
        if (!hasPrepared) {
            for (BaseMission<?> downloadMission : DownloadManagerImpl.getInstance().getMissions()) {
                final ConflictPolicy policy = getConflictPolicy();
                if (this != downloadMission && policy.isConflict(this, downloadMission)) {
                    Log.d(TAG, "isRejectMission");
                    post(new Runnable() {
                        @Override
                        public void run() {
                            policy.onConflict(BaseMission.this, new ConflictPolicy.Callback() {
                                @Override
                                public void onResult(boolean accept) {
                                    Log.d(TAG, "accept=" + accept);
                                    if (accept) {
                                        prepareMission();
                                    }
                                }
                            });
                        }
                    });
                    return;
                }
            }

            prepareMission();
            return;
        }
        errorCount.set(0);
        if (!isRunning() && !isFinished()) {
            errorCount.set(0);
            if (DownloadManagerImpl.getInstance().shouldMissionWaiting()) {
                waiting();
                return;
            }

            DownloadManagerImpl.increaseDownloadingCount();

            missionStatus = MissionStatus.RUNNING;

            aliveThreadCount.set(threadCount);
            finishCount.set(0);

            writeMissionInfo();

            for (int i = 0; i < threadCount; i++) {
                threadPoolExecutor.submit(new DownloadTransfer(this) {
                    @Override
                    public void onFinished(final DownloadTransfer transfer, Error error) {
                        if (error != null && errorCount.getAndAdd(1) < getRetryCount()) {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    threadPoolExecutor.submit(transfer);
                                }
                            }, retryDelayMillis);
                            return;
                        }
                        int count = aliveThreadCount.decrementAndGet();
                        if (count == 0 && isRunning()) {
                            Log.d(TAG, "doOnComplete length=" + length + " doneLen.get()=" + done.get());
                            if (isBlockDownload || done.get() == length) {
                                onFinish();
                            } else {
                                pause();
                                for (long position = 0; position < getBlocks(); position++) {
                                    if (!queue.contains(position) && !finished.contains(position)) {
                                        queue.add(position);
                                    }
                                }
                                start();
                            }
                        }
                    }
                });
            }

            notifyStatus(MissionStatus.START);
            missionStatus = MissionStatus.RUNNING;
            getProgressUpdater().start();
        }
    }

    public void reset() {
        threadPoolExecutor.shutdownNow();
        threadPoolExecutor = null;
        finished.clear();
        queue.clear();
        speedHistoryList.clear();
        done.set(0);
        finishTime = 0;
        finishCount.set(0);
        aliveThreadCount.set(0);
        hasPrepared = false;
        url = originUrl;
        name = "";
        missionStatus = MissionStatus.NEW;
        isBlockDownload = false;
        errCode = -1;
        error = null;
        errorCount.set(0);
        speed = 0f;
        deleteMissionInfo();
    }

    public void restart() {
        reset();
        start();
    }

    public void pause() {
        if (canPause()) {
            errorCount.set(0);
            missionStatus = MissionStatus.PAUSED;
            writeMissionInfo();
            notifyStatus(missionStatus);
            DownloadManagerImpl.decreaseDownloadingCount();
        }
    }

    public void waiting() {
        missionStatus = MissionStatus.WAITING;
        writeMissionInfo();
        notifyStatus(missionStatus);
    }

    public void delete() {
        reset();
        ExecutorUtils.submitIO(new Runnable() {
            @Override
            public void run() {
                File file = getFile();
                if (file.exists()) {
                    file.delete();
                }
            }
        });
        notifyStatus(MissionStatus.DELETE);
    }

    public void clear() {
        reset();
        notifyStatus(MissionStatus.CLEAR);
    }

    public boolean renameTo(String newFileName) {
        File file2Rename = new File(getDownloadPath() + File.separator + newFileName);
        boolean success = getFile().renameTo(file2Rename);
        if (success) {
            setName(newFileName);
            writeMissionInfo();
        }
        return success;
    }

    public boolean openFile(Context context) {
        File file = getFile();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, FileUtils.getFileProviderName(context), file);

            context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, FileUtils.getMIMEType(file));
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, FileUtils.getMIMEType(file));
        }
        context.startActivity(intent);
        return true;
    }

    public boolean openFile() {
        return openFile(getContext());
    }

    //------------------------------------------------------------notify------------------------------------------------------------
    void notifyDownloaded(long deltaLen) {
        if (done.addAndGet(deltaLen) > length) {
            done.set(length);
        }
    }

    synchronized void notifyError(final Error e, boolean fromThread) {
        Log.d(TAG, "err=" + e.getErrorMsg() + " fromThread=" + fromThread);
        error = e;
        missionStatus = MissionStatus.ERROR;

        errCode = 1;

        Log.d("eeeeeeeeeeeeeeeeeeee", "error:" + errCode);

        writeMissionInfo();
        DownloadManagerImpl.decreaseDownloadingCount();

        notifyStatus(missionStatus);
    }

    protected void notifyError(final Error e) {
        notifyError(e, false);
    }

    protected void notifyStatus(final MissionStatus status) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mListeners != null) {
                    Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
                    while (iterator.hasNext()) {
                        MissionListener listener = iterator.next().get();
                        if (listener == null) {
                            iterator.remove();
                        } else {
                            switch (status) {
                                case PREPARING:
                                    listener.onPrepare();
                                    break;
                                case START:
                                    listener.onStart();
                                    break;
                                case RUNNING:
                                    listener.onProgress(getProgressUpdater());
                                    break;
                                case WAITING:
                                    listener.onWaiting();
                                    break;
                                case PAUSED:
                                    listener.onPaused();
                                    break;
                                case RETRYING:
                                    listener.onRetrying();
                                    break;
                                case ERROR:
                                    listener.onError(error);
                                    break;
                                case FINISHED:
                                    done.set(length);
                                    listener.onProgress(getProgressUpdater());
                                    listener.onFinished();
                                    break;
                                case DELETE:
                                    listener.onDelete();
                                    break;
                                case CLEAR:
                                    listener.onClear();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }

                if (getEnableNotification() && getNotificationInterceptor() != null) {
                    Notifier notificationInterceptor = getNotificationInterceptor();
                    if (status == MissionStatus.RUNNING) {
                        notificationInterceptor.onProgress(getContext(), BaseMission.this, getProgress(), false);
                    } else if (status == MissionStatus.PAUSED) {
                        notificationInterceptor.onProgress(getContext(), BaseMission.this, getProgress(), true);
                    } else if (status == MissionStatus.FINISHED) {
                        notificationInterceptor.onFinished(getContext(), BaseMission.this);
                    } else if (status == MissionStatus.ERROR) {
                        notificationInterceptor.onError(getContext(), BaseMission.this, errCode);
                    } else if (status == MissionStatus.DELETE || status == MissionStatus.CLEAR) {
                        notificationInterceptor.onCancel(getContext(), BaseMission.this);
                    }
                }

                if (status == MissionStatus.FINISHED) {
                    DownloadManagerImpl.onMissionFinished(BaseMission.this);
                } else if (status == MissionStatus.DELETE) {
                    DownloadManagerImpl.onMissionDelete(BaseMission.this);
                } else if (status == MissionStatus.CLEAR) {
                    DownloadManagerImpl.onMissionClear(BaseMission.this);
                }
            }
        });
    }

    protected void onFinish() {
        if (errCode > 0) {
            return;
        }
        Log.d(TAG, "onFinish");
        done.set(length);
        getProgressUpdater().stop();

        missionStatus = MissionStatus.FINISHED;
        finishTime = System.currentTimeMillis();
        writeMissionInfo();

        DownloadManagerImpl.decreaseDownloadingCount();
        notifyStatus(missionStatus);
    }

    public synchronized T addListener(MissionListener listener) {
        if (hasListener(listener)) {
            return (T) this;
        }
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(new WeakReference<>(listener));
        return (T) this;
    }

    public synchronized boolean hasListener(MissionListener listener) {
        if (mListeners == null || listener == null) {
            return false;
        }
        for (WeakReference<MissionListener> weakRef : mListeners) {
            if (weakRef != null && weakRef.get() == listener) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removeListener(MissionListener listener) {
        if (mListeners == null || listener == null) {
            return;
        }
        for (Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<MissionListener> weakRef = iterator.next();
            if (listener == weakRef.get()) {
                iterator.remove();
            }
        }
    }

    public synchronized void removeAllListener() {
        if (mListeners == null) {
            return;
        }
        mListeners.clear();
    }

    protected void writeMissionInfo() {
        ExecutorUtils.submitIO(new Runnable() {
            @Override
            public void run() {
                DownloadManagerImpl.getInstance().writeMission(BaseMission.this);
            }
        });
    }

    private void deleteMissionInfo() {
        File file = new File(getMissionInfoFilePath());
        if (file.exists()) {
            file.delete();
        }
    }

    //--------------------------------------------------------------getter-----------------------------------------------
    public Context getContext() {
        return DownloadManagerImpl.getInstance().getContext();
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        if (TextUtils.isEmpty(name)) {
            return generateFileNameFromUrl(url);
        }
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    int getAliveThreadCount() {
        return aliveThreadCount.get();
    }

    public long getBlocks() {
        return blocks;
    }

    public int getFinishCount() {
        return finishCount.get();
    }

    public long getLength() {
        return length;
    }

    public long getDone() {
        return done.get();
    }

    public MissionStatus getStatus() {
        return missionStatus;
    }

    public int getErrCode() {
        return errCode;
    }

    public boolean isBlockDownload() {
        return isBlockDownload;
    }

    public boolean hasInit() {
        return hasPrepared;
    }

    public String getFilePath() {
        String path = getDownloadPath();
        if (path.endsWith(File.separator)) {
            return path + name;
        }
        return path + File.separator + name;
    }

    public File getFile() {
        return new File(getFilePath());
    }

    public String getFileSuffix() {
        return MimeTypeMap.getFileExtensionFromUrl(getFile().toURI().toString()).toLowerCase(Locale.US);
    }

    private float getProgress(long done, long length) {
        if (missionStatus == MissionStatus.FINISHED) {
            return 100f;
        } else if (length <= 0) {
            return 0f;
        }
        float progress = (float) done / (float) length;
        return progress * 100f;
    }

    public float getProgress() {
        if (isFinished()) {
            return 100f;
        }
        return getProgress(getDone(), length);
    }

    public String getProgressStr() {
        return String.format(Locale.US, "%.2f%%", getProgress());
    }

    public String getFileSizeStr() {
        return FormatUtils.formatSize(length);
    }

    public String getDownloadedSizeStr() {
        return FormatUtils.formatSize(done.get());
    }

    public float getSpeed() {
        return speed;
    }

    public String getSpeedStr() {
        return FormatUtils.formatSpeed(speed);
    }

    public int getNotifyId() {
        return uuid.hashCode();
    }

    long getNextPosition() {
        if (queue.isEmpty()) {
            return -1;
        }
        return queue.poll();
    }

    void onPositionDownloadFailed(long position) {
        queue.add(position);
    }

    public String getMissionInfoFilePath() {
        return DownloadManagerImpl.getInstance().getDownloaderConfig().getTaskPath()
                + File.separator + uuid + DownloadManagerImpl.MISSION_INFO_FILE_SUFFIX_NAME;
    }


    //-----------------------------------------------------setter-----------------------------------------------------------------


    public void setName(String name) {
        this.name = name;
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    void setLength(long length) {
        this.length = length;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }


    //----------------------------------------------------------------other------------------------------------------------------

    public boolean isBlockFinished(long block) {
        return finished.contains(block);
    }

    void onBlockFinished(long block) {
        Log.d("DownloadRunnableLog", block + " finished");
        finished.add(block);
    }

    protected String generateFileNameFromUrl(String url) {
        Log.d("getMissionNameFromUrl", "1");
        if (!TextUtils.isEmpty(url)) {
            int index = url.lastIndexOf("/");

            if (index > 0) {
                int end = url.lastIndexOf("?");
                if (end < index) {
                    end = url.length();
                }
                String name = url.substring(index + 1, end);
                Log.d("getMissionNameFromUrl", "2");
                if (!TextUtils.isEmpty(originUrl) && !TextUtils.equals(url, originUrl)) {
                    String originName = generateFileNameFromUrl(originUrl);
                    Log.d("getMissionNameFromUrl", "3");
                    if (FileUtils.getFileType(originName) != FileUtils.FileType.UNKNOWN) {
                        Log.d("getMissionNameFromUrl", "4");
                        return originName;
                    }
                }

                if (FileUtils.getFileType(name) != FileUtils.FileType.UNKNOWN || name.contains(".")) {
                    Log.d("getMissionNameFromUrl", "5");
                    return name;
                } else {
                    Log.d("getMissionNameFromUrl", "6");
                    return name + ".ext";
                }
            }
        }
        Log.d("getMissionNameFromUrl", "7");
        return "Unknown.ext";
    }

    @Override
    public String toString() {
        return "BaseMission{" +
                "queue=" + queue +
                ", finished=" + finished +
                ", speedHistoryList=" + speedHistoryList +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", originUrl='" + originUrl + '\'' +
                ", createTime=" + createTime +
                ", finishTime=" + finishTime +
                ", blocks=" + blocks +
                ", length=" + length +
                ", done=" + done +
                ", missionStatus=" + missionStatus +
                ", isBlockDownload=" + isBlockDownload +
                ", errCode=" + errCode +
                ", hasInit=" + hasPrepared +
                ", finishCount=" + finishCount +
                ", aliveThreadCount=" + aliveThreadCount +
                ", threadCount=" + threadCount +
                ", mListeners=" + mListeners +
                ", errorCount=" + errorCount +
                ", progressInfo=" + progressUpdater +
                ", handler=" + handler +
                ", notificationInterceptor=" + notificationInterceptor +
                ", downloadPath='" + downloadPath + '\'' +
                ", bufferSize=" + bufferSize +
                ", progressInterval=" + progressInterval +
                ", blockSize=" + blockSize +
                ", userAgent='" + userAgent + '\'' +
                ", retryCount=" + retryCount +
                ", retryDelay=" + retryDelayMillis +
                ", connectOutTime=" + connectOutTime +
                ", readOutTime=" + readOutTime +
                ", enableNotification=" + enableNotification +
                ", cookie='" + cookie + '\'' +
                ", allowAllSSL=" + allowAllSSL +
                ", headers=" + headers +
                ", proxy=" + proxy +
                '}';
    }
}
