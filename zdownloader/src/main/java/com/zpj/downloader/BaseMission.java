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

import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.ErrorCode;
import com.zpj.downloader.constant.ResponseCode;
import com.zpj.utils.FileUtils;
import com.zpj.utils.FormatUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
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

        void onInit();

        void onStart();

        void onPause();

        void onWaiting();

        void onRetry();

        void onProgress(ProgressInfo update);

        void onFinish();

        void onError(Error e);

        void onDelete();

        void onClear();
    }

    @Keep
    public enum MissionStatus {
        PREPARING("准备中"),
        START("已开始"),
        RUNNING("下载中"),
        WAITING("等待中"),
        PAUSED("已暂停"),
        FINISHED("已完成"),
        ERROR("出错了"),
        RETRYING("重试中");

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

    private final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> finished = new ConcurrentLinkedQueue<>();
    private final ArrayList<Long> speedHistoryList = new ArrayList<>();

    protected String uuid = "";
    protected String name = "";
    protected String url = "";
    protected String redirectUrl = "";
    protected String originUrl = "";
    protected long createTime = 0;
    protected long finishTime = 0;
    protected long blocks = 1;
    protected long length = 0;
    protected AtomicLong done = new AtomicLong(0);
    protected MissionStatus missionStatus = MissionStatus.PREPARING;
    protected boolean fallback = false;
    protected int errCode = -1;
    protected boolean hasPrepared = false;

    //-----------------------------------------------------transient---------------------------------------------------------------

    private transient int currentRetryCount;

    protected transient AtomicInteger finishCount;
    private transient AtomicInteger aliveThreadCount;

    protected transient ArrayList<WeakReference<MissionListener>> mListeners;

    private transient int errorCount = 0;
    private transient long lastDone = -1;
    private transient String tempSpeed = "0 KB/s";
    private transient ProgressInfo progressInfo;
    private transient Handler handler;
    private transient ConcurrentLinkedQueue<DownloadBlock> blockQueue;
    private transient Runnable progressRunnable;
    private transient boolean isCreate = false;
    private transient ThreadPoolExecutor threadPoolExecutor;


    //------------------------------------------------------runnables---------------------------------------------

    protected ConcurrentLinkedQueue<DownloadBlock> getBlockQueue() {
        if (blockQueue == null) {
            synchronized (BaseMission.class) {
                if (blockQueue == null) {
                    blockQueue = new ConcurrentLinkedQueue<>();
                }
            }
        }
        return blockQueue;
    }

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

    public ProgressInfo getProgressInfo() {
        if (progressInfo == null) {
            synchronized (BaseMission.class) {
                if (progressInfo == null) {
                    progressInfo = new ProgressInfo();
                }
            }
        }
        return progressInfo;
    }

    private static class PrepareStrategy {


    }

    protected void prepareMission() {
        notifyStatus(MissionStatus.PREPARING);

        new ObservableTask<Void>() {

            @Override
            protected Void run() throws Exception {
                HttpURLConnection connection = HttpUrlConnectionFactory.getFileInfo(BaseMission.this);
                if (handleResponse(connection, BaseMission.this)) {
                    Log.d(TAG, "handleResponse--222");
                    return null;
                }

                if (connection.getResponseCode() != ResponseCode.RESPONSE_206) {
                    // Fallback to single thread if no partial content support
                    fallback = true;

                    Log.d(TAG, "falling back");
                }

                Log.d("mission.name", "mission.name444=" + name);
                if (TextUtils.isEmpty(name)) {
                    Log.d("Initializer", "getMissionNameFromUrl--url=" + url);
                    name = getMissionNameFromUrl(BaseMission.this, url);
                }

                Log.d("mission.name", "mission.name555=" + name);

                for (BaseMission<?> downloadMission : DownloadManagerImpl.getInstance().getMissions()) {
                    if (!TextUtils.equals(uuid, downloadMission.uuid) && TextUtils.equals(name, downloadMission.name) && // !downloadMission.isIniting() &&
                            (TextUtils.equals(downloadMission.originUrl.trim(), url.trim()) ||
                                    TextUtils.equals(downloadMission.redirectUrl.trim(), url.trim()))) {
                        Log.d(TAG, "has mission---url=" + downloadMission.url);
                        downloadMission.start();
                        return null;
                    }
                }

                if (fallback) {
                    blocks = 1;
                } else {
                    blocks = length / getBlockSize();
                    if (blocks * getBlockSize() < length) {
                        blocks++;
                    }
                }
                Log.d(TAG, "blocks=" + blocks);

                queue.clear();
                for (long position = 0; position < blocks; position++) {
                    Log.d(TAG, "initQueue add position=" + position);
                    queue.add(position);
                }


                File loacation = new File(getDownloadPath());
                if (!loacation.exists()) {
                    loacation.mkdirs();
                }
                File file = new File(getFilePath());
                if (!file.exists()) {
                    file.createNewFile();
                }

                Log.d(TAG, "storage=" + FileUtils.getAvailableSize());
                hasPrepared = true;

                saveMissionInfo();
                start();
                return null;
            }
        }
                .onError(new ObservableTask.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        notifyError(new Error(throwable.getMessage()));
                    }
                })
                .execute();
    }

    private Runnable getProgressRunnable() {
        if (progressRunnable == null) {
            synchronized (BaseMission.class) {
                if (progressRunnable == null) {
                    progressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "progressRunnable--start isRunning=" + isRunning() + " missionStatus=" + missionStatus + " aliveThreadCount=" + aliveThreadCount.get());
                            if (isFinished() || errCode != -1 || aliveThreadCount.get() < 1 || !isRunning()) {
                                getHandler().removeCallbacks(getProgressRunnable());
                                notifyStatus(missionStatus);
                                return;
                            }
                            postDelayed(getProgressRunnable(), getProgressInterval());
                            long downloaded = done.get();
                            long delta = downloaded - lastDone;
                            Log.d(TAG, "progressRunnable--delta=" + delta);
                            speedHistoryList.add(delta);
                            if (delta > 0) {
                                lastDone = downloaded;
                                double speed = delta * (getProgressInterval() / 1000f);
                                tempSpeed = FormatUtils.formatSpeed(speed);
                            }
                            String downloadedSizeStr = FormatUtils.formatSize(downloaded);
                            float progress = getProgress(downloaded, length);
                            Log.d(TAG, "progressRunnable--tempSpeed=" + tempSpeed);
                            ProgressInfo progressInfo = getProgressInfo();
                            progressInfo.setDone(downloaded);
                            progressInfo.setSize(length);
                            progressInfo.setProgress(progress);
                            progressInfo.setFileSizeStr(getFileSizeStr());
                            progressInfo.setDownloadedSizeStr(downloadedSizeStr);
                            progressInfo.setProgressStr(String.format(Locale.US, "%.2f%%", progress));
                            progressInfo.setSpeedStr(tempSpeed);
                            writeMissionInfo();
                            notifyStatus(MissionStatus.RUNNING);
                            if (getEnableNotification() && getNotificationInterceptor() != null) {
                                getNotificationInterceptor().onProgress(getContext(), BaseMission.this, getProgress(), false);
                            }
                        }
                    };
                }
            }
        }
        return progressRunnable;
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
        return isPause() || isError(); //  || isIniting()
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
        currentRetryCount = getRetryCount();
        if (fallback) {
            setThreadCount(1);
            done.set(0);
            blocks = 1;
            queue.clear();
            queue.add(0L);
        }
        lastDone = done.get();
        if (hasPrepared) {
            for (long position = 0; position < getBlocks(); position++) {
                if (!queue.contains(position) && !finished.contains(position)) {
                    queue.add(position);
                }
            }
        }
        if (canPause()) {
            missionStatus = MissionStatus.PAUSED;
            try {
                saveMissionInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        firstCreate();
        onStart();
        if (!hasPrepared) {
            currentRetryCount = getRetryCount();
            DownloadManagerImpl.getInstance().insertMission(this);
//            init();
            writeMissionInfo();
            Log.d(TAG, "start hasInit=false initMission");
            prepareMission();
            return;
        }
        errorCount = 0;
        if (!isRunning() && !isFinished()) {
            initCurrentRetryCount();
            if (DownloadManagerImpl.getInstance().shouldMissionWaiting()) {
                waiting();
                return;
            }

//            if (fallback) {
//                threadCount = 1;
//                setThreadCount(1);
////                done = 0;
//                done.set(0);
//                blocks = 1;
//                queue.clear();
//                queue.add(0L);
//            }

            DownloadManagerImpl.increaseDownloadingCount();

            missionStatus = MissionStatus.RUNNING;

            aliveThreadCount.set(threadCount);
            finishCount.set(0);

            writeMissionInfo();

            for (int i = 0; i < threadCount; i++) {
                threadPoolExecutor.submit(new DownloadTransfer(this) {
                    @Override
                    public void onFinished() {
                        int count = aliveThreadCount.decrementAndGet();
                        if (count == 0 && isRunning()) {
                            Log.d(TAG, "doOnComplete length=" + length + " doneLen.get()=" + done.get());
                            if (isFallback() || done.get() == length) {
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
            post(getProgressRunnable());
        }
    }

    public void restart() {
        pause();
//        done = 0;
        finished.clear();
        queue.clear();
        speedHistoryList.clear();
        done.set(0);
        finishTime = 0;
        finishCount.set(0);
        aliveThreadCount.set(0);
        lastDone = 0;
        hasPrepared = false;
        url = originUrl;
        redirectUrl = "";
        name = "";
        missionStatus = MissionStatus.PREPARING;
        fallback = false;
        errCode = -1;


        currentRetryCount = DefaultConstant.RETRY_COUNT;
        errorCount = 0;
        lastDone = -1;
        tempSpeed = "0 KB/s";
        progressInfo = null;
        blockQueue = null;

        start();
    }

    public void pause() {
        if (canPause()) {
            initCurrentRetryCount();
            missionStatus = MissionStatus.PAUSED;
            writeMissionInfo();
            notifyStatus(missionStatus);

            if (missionStatus != MissionStatus.WAITING) {
                DownloadManagerImpl.decreaseDownloadingCount();
            }

            if (getEnableNotification() && getNotificationInterceptor() != null) {
                getNotificationInterceptor().onProgress(getContext(), this, getProgress(), true);
            }
        }
    }

    public void waiting() {
        missionStatus = MissionStatus.WAITING;
        writeMissionInfo();
        notifyStatus(missionStatus);
//        pause();
    }

    public void delete() {
        if (getNotificationInterceptor() != null) {
            getNotificationInterceptor().onCancel(getContext(), this);
        }
        pause();
        deleteMissionInfo();
        new File(getDownloadPath() + File.separator + name).delete();
        DownloadManagerImpl.getInstance().getMissions().remove(this);
        DownloadManagerImpl.onMissionDelete(this);
        post(new Runnable() {
            @Override
            public void run() {
                if (mListeners == null) {
                    return;
                }
                for (WeakReference<MissionListener> ref : mListeners) {
                    final MissionListener listener = ref.get();
                    if (listener != null) {
                        listener.onDelete();
                    }
                }
            }
        });
    }

    public void clear() {
        if (getNotificationInterceptor() != null) {
            getNotificationInterceptor().onCancel(getContext(), this);
        }
        pause();
        deleteMissionInfo();
        DownloadManagerImpl.getInstance().getMissions().remove(this);
        post(new Runnable() {
            @Override
            public void run() {
                if (mListeners == null) {
                    return;
                }
                for (WeakReference<MissionListener> ref : mListeners) {
                    final MissionListener listener = ref.get();
                    if (listener != null) {
                        listener.onClear();
                    }
                }
            }
        });
    }

    public boolean renameTo(String newFileName) {
        File file2Rename = new File(getDownloadPath() + File.separator + newFileName);
        boolean success = getFile().renameTo(file2Rename);
        if (success) {
            setTaskName(newFileName);
            writeMissionInfo();
        }
        return success;
    }

    public boolean openFile(Context context) {
        File file = new File(getFilePath());
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, FileUtils.getFileProviderName(context), file);

            context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, FileUtils.getMIMEType(file));
        } else {
            intent.setDataAndType(uri, FileUtils.getMIMEType(file));
        }
        context.startActivity(intent);
        return true;
//        return FileUtils.openFile(context, getFilePath());
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
        missionStatus = MissionStatus.ERROR;

        // TODO 出错重试
        currentRetryCount = getRetryCount();

        errCode = 1;

        Log.d("eeeeeeeeeeeeeeeeeeee", "error:" + errCode);

        writeMissionInfo();

        post(new Runnable() {
            @Override
            public void run() {
                if (mListeners == null) {
                    return;
                }
                for (WeakReference<MissionListener> ref : mListeners) {
                    final MissionListener listener = ref.get();
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        });

        DownloadManagerImpl.decreaseDownloadingCount();

        if (getEnableNotification() && getNotificationInterceptor() != null) {
            getNotificationInterceptor().onError(getContext(), this, errCode);
        }
    }

    protected void notifyError(final Error e) {
        notifyError(e, false);
    }

    protected void notifyStatus(final MissionStatus status) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mListeners == null) {
                    return;
                }
                for (WeakReference<MissionListener> ref : mListeners) {
                    final MissionListener listener = ref.get();
                    if (listener != null) {
                        switch (status) {
                            case PREPARING:
                                listener.onInit();
                                break;
                            case START:
                                listener.onStart();
                                break;
                            case RUNNING:
                                listener.onProgress(getProgressInfo());
                                break;
                            case WAITING:
                                listener.onWaiting();
                                break;
                            case PAUSED:
                                listener.onPause();
                                break;
                            case RETRYING:
                                listener.onRetry();
                                break;
                            case FINISHED:
                                ProgressInfo info = getProgressInfo();
                                info.setDone(getDone());
                                info.setSize(getLength());
                                info.setProgress(100);
                                info.setFileSizeStr(getFileSizeStr());
                                info.setDownloadedSizeStr(getDownloadedSizeStr());
                                info.setProgressStr(String.format(Locale.US, "%.2f%%", getProgress()));
                                info.setSpeedStr(tempSpeed);
                                listener.onProgress(info);
                                listener.onFinish();
                                progressInfo = null;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
    }

    protected void onFinish() {
        if (errCode > 0) {
            return;
        }
        Log.d(TAG, "onFinish");
//        done = length;
        done.set(length);
        getHandler().removeCallbacks(getProgressRunnable());
        this.progressRunnable = null;

        missionStatus = MissionStatus.FINISHED;
        finishTime = System.currentTimeMillis();
        writeMissionInfo();

        notifyStatus(missionStatus);

        DownloadManagerImpl.decreaseDownloadingCount();

        if (getEnableNotification() && getNotificationInterceptor() != null) {
            getNotificationInterceptor().onFinished(getContext(), this);
        }
        DownloadManagerImpl.onMissionFinished(this);
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
        if (mListeners == null) {
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
        if (mListeners == null) {
            return;
        }
        for (Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<MissionListener> weakRef = iterator.next();
            if (listener != null && listener == weakRef.get()) {
                iterator.remove();
            }
        }
    }

    public synchronized void removeAllListener() {
        if (mListeners == null) {
            return;
        }
        for (Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<MissionListener> weakRef = iterator.next();
            iterator.remove();
        }
    }

    private void writeMissionInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    saveMissionInfo();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void saveMissionInfo() throws Exception {
        synchronized (BaseMission.class) {
            BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(getMissionInfoFilePath()));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(BaseMission.this);
            out.close();
            fileOut.close();
        }
    }

    private void deleteMissionInfo() {
        File file = new File(getMissionInfoFilePath());
        if (file.exists()) {
            file.delete();
        }
    }

    private void initCurrentRetryCount() {
        if (currentRetryCount != getRetryCount()) {
            currentRetryCount = getRetryCount();
        }
    }

    //--------------------------------------------------------------getter-----------------------------------------------
    public Context getContext() {
        return DownloadManagerImpl.getInstance().getContext();
    }

    public String getUuid() {
        return uuid;
    }

    public String getTaskName() {
        if (TextUtils.isEmpty(name)) {
            return getTaskNameFromUrl();
        }
        return name;
    }

    public String getTaskNameFromUrl() {
        return getMissionNameFromUrl(this, url);
    }

    public String getUrl() {
        return url;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
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

    public boolean isFallback() {
        return fallback;
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

    public String getSpeed() {
        return tempSpeed;
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
        return DownloadManagerImpl.getInstance().getDownloaderConfig().getTaskPath() + File.separator + uuid + DownloadManagerImpl.MISSION_INFO_FILE_SUFFIX_NAME;
    }


    //-----------------------------------------------------setter-----------------------------------------------------------------


    public void setTaskName(String name) {
        this.name = name;
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
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


    //----------------------------------------------------------------other

    public boolean isBlockFinished(long block) {
        return finished.contains(block);
    }

    void onBlockFinished(long block) {
        Log.d("DownloadRunnableLog", block + " finished");
        finished.add(block);
    }

    private boolean handleResponse(HttpURLConnection connection, BaseMission<?> mission) throws Exception {
        if (TextUtils.isEmpty(mission.name)) {
            mission.name = getMissionNameFromResponse(connection);
            Log.d("mission.name", "mission.name333=" + mission.name);
        }
        int statusCode = connection.getResponseCode();
        if (statusCode == ResponseCode.RESPONSE_302
                || statusCode == ResponseCode.RESPONSE_301
                || statusCode == ResponseCode.RESPONSE_300) {
            String redirectUrl = connection.getHeaderField("location");
            Log.d(TAG, "redirectUrl=" + redirectUrl);
            if (!TextUtils.isEmpty(redirectUrl)) {
                mission.url = redirectUrl;
                mission.redirectUrl = redirectUrl;
            }
        } else if (statusCode == ErrorCode.ERROR_SERVER_404) {
            mission.errCode = ErrorCode.ERROR_SERVER_404;
            mission.notifyError(Error.HTTP_404, false);
            return true;
        } else if (statusCode == ResponseCode.RESPONSE_206) {
            String contentLength = connection.getHeaderField("Content-Length");
            if (contentLength != null) {
                mission.length = Long.parseLong(contentLength);
            }
            Log.d("mission.length", "mission.length=" + mission.length);
            return !checkLength(mission);
        }
        return false;
    }

    private String getMissionNameFromResponse(HttpURLConnection connection) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        Log.d("contentDisposition", "contentDisposition=" + contentDisposition);
        if (contentDisposition != null) {
            String[] dispositions = contentDisposition.split(";");
            for (String disposition : dispositions) {
                Log.d("disposition", "disposition=" + disposition);
                if (disposition.contains("filename=")) {
                    return disposition.replace("filename=", "").trim();
                }
            }
        }
        return "";
    }

    protected String getMissionNameFromUrl(BaseMission<?> mission, String url) {
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

                if (!TextUtils.isEmpty(mission.originUrl) && !TextUtils.equals(url, mission.originUrl)) {
                    String originName = getMissionNameFromUrl(mission, mission.originUrl);
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
        return "未知文件.ext";
    }

    private boolean checkLength(BaseMission<?> mission) {
        if (mission.length <= 0) {
            mission.errCode = ErrorCode.ERROR_SERVER_UNSUPPORTED;
            mission.notifyError(Error.SERVER_UNSUPPORTED, false);
            return false;
        } else if (mission.length >= FileUtils.getAvailableSize()) {
            mission.errCode = ErrorCode.ERROR_NO_ENOUGH_SPACE;
            mission.notifyError(Error.NO_ENOUGH_SPACE, false);
            return false;
        }
        return true;
    }

    public static class ProgressInfo {

        private long size;
        private long done;
        private float progress;
        private String fileSizeStr;
        private String downloadedSizeStr;
        private String progressStr;
        private String speedStr;

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getDone() {
            return done;
        }

        public void setDone(long done) {
            this.done = done;
        }

        public float getProgress() {
            return progress;
        }

        void setProgress(float progress) {
            this.progress = progress;
        }

        public String getFileSizeStr() {
            return fileSizeStr;
        }

        void setFileSizeStr(String fileSizeStr) {
            this.fileSizeStr = fileSizeStr;
        }

        public String getDownloadedSizeStr() {
            return downloadedSizeStr;
        }

        void setDownloadedSizeStr(String downloadedSizeStr) {
            this.downloadedSizeStr = downloadedSizeStr;
        }

        public String getProgressStr() {
            return progressStr;
        }

        void setProgressStr(String progressStr) {
            this.progressStr = progressStr;
        }

        public String getSpeedStr() {
            return speedStr;
        }

        void setSpeedStr(String speedStr) {
            this.speedStr = speedStr;
        }
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
                ", redirectUrl='" + redirectUrl + '\'' +
                ", originUrl='" + originUrl + '\'' +
                ", createTime=" + createTime +
                ", finishTime=" + finishTime +
                ", blocks=" + blocks +
                ", length=" + length +
                ", done=" + done +
                ", missionStatus=" + missionStatus +
                ", fallback=" + fallback +
                ", errCode=" + errCode +
                ", hasInit=" + hasPrepared +
                ", currentRetryCount=" + currentRetryCount +
                ", finishCount=" + finishCount +
                ", aliveThreadCount=" + aliveThreadCount +
                ", threadCount=" + threadCount +
                ", mListeners=" + mListeners +
                ", errorCount=" + errorCount +
                ", lastDone=" + lastDone +
                ", tempSpeed='" + tempSpeed + '\'' +
                ", progressInfo=" + progressInfo +
                ", handler=" + handler +
                ", blockQueue=" + blockQueue +
                ", progressRunnable=" + progressRunnable +
                ", notificationInterceptor=" + notificationInterceptor +
                ", downloadPath='" + downloadPath + '\'' +
                ", bufferSize=" + bufferSize +
                ", progressInterval=" + progressInterval +
                ", blockSize=" + blockSize +
                ", userAgent='" + userAgent + '\'' +
                ", retryCount=" + retryCount +
                ", retryDelay=" + retryDelay +
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
