package com.zpj.qxdownloader.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zpj.qxdownloader.config.MissionConfig;
import com.zpj.qxdownloader.constant.ErrorCode;
import com.zpj.qxdownloader.constant.ResponseCode;
import com.zpj.qxdownloader.jsoup.Jsoup;
import com.zpj.qxdownloader.jsoup.connection.Connection;
import com.zpj.qxdownloader.util.FileUtil;
import com.zpj.qxdownloader.util.ThreadPoolFactory;
import com.zpj.qxdownloader.util.Utility;
import com.zpj.qxdownloader.util.io.BufferedRandomAccessFile;
import com.zpj.qxdownloader.util.notification.NotifyUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Z-P-J
 */
public class DownloadMission {
    private static final String TAG = DownloadMission.class.getSimpleName();

    public interface MissionListener {
        HashMap<MissionListener, Handler> HANDLER_STORE = new HashMap<>();

        void onInit();

        void onStart();

        void onPause();

        void onWaiting();

        void onRetry();

        void onProgress(long done, long total);

        void onFinish();

        void onError(int errCode);
    }

    public enum MissionStatus {
        INITING("初始化中"),
        START("已开始"),
        RUNNING("下载中"),
        WAITING("等待中"),
        PAUSE("已暂停"),
        FINISHED("已完成"),
        ERROR("出错了"),
        RETRY("重试中");

        private String statusName;

        MissionStatus(String name) {
            statusName = name;
        }

        @Override
        public String toString() {
            return statusName;
        }
    }

    private final LongSparseArray<Boolean> blockState = new LongSparseArray<>();

    private String uuid = "";
    private String name = "";
    private String url = "";
    private String redirectUrl = "";
    private String originUrl = "";
    private long createTime = 0;
    private int notifyId = 0;
    private long blocks = 0;
    private int finishCount = 0;
    private long length = 0;
    private long done = 0;
    private List<Long> threadPositions = new ArrayList<>();
    private MissionStatus missionStatus = MissionStatus.INITING;
    private boolean fallback = false;
    private int errCode = -1;
    private boolean hasInit = false;
    private MissionConfig missionConfig = MissionConfig.with();

    //-----------------------------------------------------transient---------------------------------------------------------------

    private transient boolean recovered = false;

    private transient int currentRetryCount = missionConfig.getRetryCount();

    private transient int threadCount = missionConfig.getThreadPoolConfig().getCorePoolSize();

    private transient ArrayList<WeakReference<MissionListener>> mListeners = new ArrayList<>();

    private transient boolean mWritingToFile = false;

    private transient int errorCount = 0;

    private transient ThreadPoolExecutor threadPoolExecutor;

    private transient long lastTimeStamp = -1;
    private transient long lastDone = -1;
    private transient String tempSpeed = "0 KB/s";


    //------------------------------------------------------runnables---------------------------------------------
    private transient Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("Initializer", "run");
                Connection.Response response = Jsoup.connect(url)
                        .method(Connection.Method.HEAD)
                        .followRedirects(false)
                        .proxy(Proxy.NO_PROXY)
                        .userAgent(getUserAgent())
                        .header("Cookie", getCookie())
                        .header("Accept", "*/*")
                        .header("Referer", url)
//						.header("Access-Control-Expose-Headers", "Content-Disposition")
//						.header("Range", "bytes=0-")
                        .headers(getHeaders())
                        .timeout(1000000)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .maxBodySize(0)
                        .execute();

                if (handleResponse(response, DownloadMission.this)) {
                    return;
                }


                response = Jsoup.connect(url)
                        .method(Connection.Method.HEAD)
                        .proxy(Proxy.NO_PROXY)
                        .userAgent(getUserAgent())
                        .header("Cookie", getCookie())
                        .header("Accept", "*/*")
                        .header("Access-Control-Expose-Headers", "Content-Disposition")
                        .header("Referer", url)
                        .header("Pragma", "no-cache")
                        .header("Range", "bytes=0-")
                        .header("Cache-Control", "no-cache")
                        .headers(getHeaders())
                        .timeout(getConnectOutTime())
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
//						.validateTLSCertificates(false)
                        .maxBodySize(0)
                        .execute();

                if (handleResponse(response, DownloadMission.this)) {
                    return;
                }

                if (response.statusCode() != ResponseCode.RESPONSE_206) {
                    // Fallback to single thread if no partial content support
                    fallback = true;

                    Log.d(TAG, "falling back");
                }

                Log.d("mission.name", "mission.name444=" + name);
                if (TextUtils.isEmpty(name)) {
                    Log.d("Initializer", "getMissionNameFromUrl--url=" + url);
                    name = getMissionNameFromUrl(DownloadMission.this, url);
                }

                Log.d("mission.name", "mission.name555=" + name);

                for (DownloadMission downloadMission : DownloadManagerImpl.ALL_MISSIONS) {
                    if (!downloadMission.isIniting() && TextUtils.equals(name, downloadMission.name) &&
                            (TextUtils.equals(downloadMission.originUrl.trim(), url.trim()) ||
                                    TextUtils.equals(downloadMission.redirectUrl.trim(), url.trim()))) {
                        downloadMission.start();
                        return;
                    }
                }

                blocks = length / getBlockSize();

                if (threadCount > blocks) {
                    threadCount = (int) blocks;
                }

                if (threadCount <= 0) {
                    threadCount = 1;
                }

                if (blocks * getBlockSize() < length) {
                    blocks++;
                }


                File loacation = new File(getDownloadPath());
                if (!loacation.exists()) {
                    loacation.mkdirs();
                }
                File file = new File(getFilePath());
                if (!file.exists()) {
                    file.createNewFile();
                }

                Log.d(TAG, "storage=" + Utility.getAvailableSize());
                hasInit = true;

                BufferedRandomAccessFile af = new BufferedRandomAccessFile(getFilePath(), "rw");
                af.setLength(length);
                af.close();

                start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private transient Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            notifyStatus(MissionStatus.RUNNING);
            if (missionConfig.getEnableNotificatio()) {
                NotifyUtil.with(getContext())
                        .buildProgressNotify()
                        .setProgressAndFormat(getProgress(), false, "")
                        .setContentTitle(name)
                        .setId(getNotifyId())
                        .show();
            }
        }
    };

    private transient Runnable writeMissionInfoRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (blockState) {
                Utility.writeToFile(getMissionInfoFilePath(), new Gson().toJson(DownloadMission.this));
                mWritingToFile = false;
            }
        }
    };

    private DownloadMission() {

    }

    public static DownloadMission create(String url, String name, MissionConfig config) {
        DownloadMission mission = new DownloadMission();
        mission.url = url;
        mission.originUrl = url;
        mission.name = name;
        mission.uuid = UUID.randomUUID().toString();
        mission.createTime = System.currentTimeMillis();
//        mission.timestamp = mission.createTime;
        mission.missionStatus = DownloadMission.MissionStatus.INITING;
        mission.missionConfig = config;
        return mission;
    }

    //-------------------------下载任务状态-----------------------------------
    public boolean isIniting() {
        return missionStatus == MissionStatus.INITING;
    }

    public boolean isRunning() {
        return missionStatus == MissionStatus.RUNNING;
    }

    public boolean isWaiting() {
        return missionStatus == MissionStatus.WAITING;
    }

    public boolean isPause() {
        return missionStatus == MissionStatus.PAUSE;
    }

    public boolean isFinished() {
        return missionStatus == MissionStatus.FINISHED;
    }

    public boolean isError() {
        return missionStatus == MissionStatus.ERROR;
    }


    //----------------------------------------------------------operation------------------------------------------------------------
    public void init() {
        if (threadPoolExecutor == null || threadPoolExecutor.getCorePoolSize() != 2 * threadCount) {
            threadPoolExecutor = ThreadPoolFactory.newFixedThreadPool(missionConfig.getThreadPoolConfig());
        }
        if (hasInit) {
            notifyStatus(MissionStatus.INITING);
        } else {
            writeMissionInfo();
            threadPoolExecutor.submit(initRunnable);
        }
    }

    public void start() {
        errorCount = 0;
        if (!isRunning() && !isFinished()) {
            initCurrentRetryCount();
            if (DownloadManagerImpl.getInstance().shouldMissionWaiting()) {
                waiting();
                return;
            }

            DownloadManagerImpl.increaseDownloadingCount();

//			waiting = false;
//			running = true;
            missionStatus = MissionStatus.RUNNING;

//			ExecutorService executorService;
            if (!fallback) {
//				executorService = Executors.newFixedThreadPool(threadCount);
//				for (int i = 0; i < threadCount; i++) {
//					if (threadPositions.size() <= i && !recovered) {
//						threadPositions.add((long) i);
//					}
//					executorService.submit(new DownloadRunnable(this, i));
////					new Thread(new DownloadRunnable(this, i)).start();
//				}
            } else {
                // In fallback mode, resuming is not supported.
                missionConfig.getThreadPoolConfig().setCorePoolSize(1);
                threadCount = 1;
                done = 0;
                blocks = 0;
//				executorService = Executors.newFixedThreadPool(1);
//				executorService.submit(new DownloadRunnableFallback(this));
            }

            if (threadPoolExecutor == null || threadPoolExecutor.getCorePoolSize() != 2 * threadCount) {
                threadPoolExecutor = ThreadPoolFactory.newFixedThreadPool(missionConfig.getThreadPoolConfig());
            }
            for (int i = 0; i < threadCount; i++) {
                if (threadPositions.size() <= i && !recovered) {
                    threadPositions.add((long) i);
                }
                threadPoolExecutor.submit(new DownloadRunnable(this, i));
            }

            writeMissionInfo();
            notifyStatus(MissionStatus.START);
        }
    }

    public void pause() {
        initCurrentRetryCount();
        if (isRunning() || isWaiting()) {
            missionStatus = MissionStatus.PAUSE;
            recovered = true;
            writeMissionInfo();
            notifyStatus(missionStatus);

            if (missionStatus != MissionStatus.WAITING) {
                DownloadManagerImpl.decreaseDownloadingCount();
            }

            if (missionConfig.getEnableNotificatio()) {
                NotifyUtil.with(getContext())
                        .buildProgressNotify()
                        .setProgressAndFormat(getProgress(), false, "")
                        .setId(getNotifyId())
                        .setContentTitle("已暂停：" + name)
                        .show();
            }
        }
    }

    public void waiting() {
        missionStatus = MissionStatus.WAITING;
        notifyStatus(missionStatus);
        pause();
    }

    public void delete() {
        deleteMissionInfo();
        new File(missionConfig.getDownloadPath() + File.separator + name).delete();
    }

    public void openFile(Context context) {
        File file = getFile();
        if (file.exists()) {
            FileUtil.openFile(context, getFile());
        } else {
            Toast.makeText(context, "下载文件不存在!", Toast.LENGTH_SHORT).show();
        }
    }

    //------------------------------------------------------------notify------------------------------------------------------------
    public synchronized void notifyProgress(long deltaLen) {
        if (missionStatus != MissionStatus.RUNNING) {
            return;
        }

        if (recovered) {
            recovered = false;
        }

        done += deltaLen;

        if (done > length) {
            done = length;
        }

        long now = System.currentTimeMillis();
        if (lastTimeStamp == -1) {
            lastTimeStamp = now;
        }
        long deltaTime = now - lastTimeStamp;
        if (deltaTime < 1000) {
            return;
        }
        lastTimeStamp = now;
        if (lastDone == -1) {
            lastDone = done;
        }
        long deltaDone = done - lastDone;
        lastDone = done;

        if (deltaDone <= 0) {
            tempSpeed = "0 KB/s";
        } else {
            float speed = (float) deltaDone / deltaTime;
            tempSpeed = Utility.formatSpeed(speed * 1000);
        }

        if (done != length) {
            Log.d(TAG, "已下载");
            writeMissionInfo();
            threadPoolExecutor.submit(progressRunnable);

//			for (WeakReference<MissionListener> ref: mListeners) {
//				final MissionListener listener = ref.get();
//				if (listener != null) {
//					MissionListener.HANDLER_STORE.get(listener).post(new Runnable() {
//						@Override
//						public void run() {
//							listener.onProgressUpdate(done, length);
//						}
//					});
//				}
//			}
        }
    }

    public synchronized void notifyFinished() {
        if (errCode > 0) {
            return;
        }

        finishCount++;

        if (finishCount == threadCount) {
            onFinish();
        }
    }

    synchronized void notifyError(int err) {
        if (!(err == ErrorCode.ERROR_WITHOUT_STORAGE_PERMISSIONS || err == ErrorCode.ERROR_FILE_NOT_FOUND)) {
            errorCount++;
            if (errorCount == threadCount) {
                currentRetryCount--;
                if (currentRetryCount >= 0) {
                    pause();
                    notifyStatus(MissionStatus.RETRY);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            start();
                        }
                    }, missionConfig.getRetryDelay());
                    return;
                }
            }
        }

        missionStatus = MissionStatus.ERROR;

        currentRetryCount = missionConfig.getRetryCount();

        errCode = err;

        Log.d("eeeeeeeeeeeeeeeeeeee", "error:" + errCode);

        writeMissionInfo();

        notifyStatus(missionStatus);

        DownloadManagerImpl.decreaseDownloadingCount();

        if (missionConfig.getEnableNotificatio()) {
            NotifyUtil.with(getContext())
                    .buildNotify()
                    .setContentTitle("下载出错" + errCode + ":" + name)
                    .setId(getNotifyId())
                    .show();
        }
    }

    private void notifyStatus(final MissionStatus status) {
        for (WeakReference<MissionListener> ref : mListeners) {
            final MissionListener listener = ref.get();
            if (listener != null) {
                MissionListener.HANDLER_STORE.get(listener).post(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case INITING:
                                listener.onInit();
                                break;
                            case START:
                                listener.onStart();
                                break;
                            case RUNNING:
                                listener.onProgress(done, length);
                                break;
                            case WAITING:
                                listener.onWaiting();
                                break;
                            case PAUSE:
                                listener.onPause();
                                break;
                            case ERROR:
                                listener.onError(errCode);
                                break;
                            case RETRY:
                                listener.onRetry();
                                break;
                            case FINISHED:
                                listener.onFinish();
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    private void onFinish() {
        if (errCode > 0) {
            return;
        }
        Log.d(TAG, "onFinish");

        missionStatus = MissionStatus.FINISHED;

        writeMissionInfo();

        notifyStatus(missionStatus);

        DownloadManagerImpl.decreaseDownloadingCount();

        if (missionConfig.getEnableNotificatio()) {
            NotifyUtil.with(getContext())
                    .buildNotify()
                    .setContentTitle(name)
                    .setContentText("下载已完成")
                    .setId(getNotifyId())
                    .show();
        }
        if (DownloadManagerImpl.getInstance().getDownloadManagerListener() != null) {
            DownloadManagerImpl.getInstance().getDownloadManagerListener().onMissionFinished();
        }
    }

    public synchronized void addListener(MissionListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        MissionListener.HANDLER_STORE.put(listener, handler);
        mListeners.add(new WeakReference<>(listener));
    }

    public synchronized void removeListener(MissionListener listener) {
        for (Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<MissionListener> weakRef = iterator.next();
            if (listener != null && listener == weakRef.get()) {
                iterator.remove();
            }
        }
    }

    public void writeMissionInfo() {
        if (!mWritingToFile) {
            mWritingToFile = true;
            if (threadPoolExecutor == null) {
                threadPoolExecutor = ThreadPoolFactory.newFixedThreadPool(missionConfig.getThreadPoolConfig());
            }
            threadPoolExecutor.submit(writeMissionInfoRunnable);
        }
    }

    public void deleteMissionInfo() {
        File file = new File(getMissionInfoFilePath());
        if (file.exists()) {
            file.delete();
        }
    }

    private void initCurrentRetryCount() {
        if (currentRetryCount != missionConfig.getRetryCount()) {
            currentRetryCount = missionConfig.getRetryCount();
        }
    }

    //--------------------------------------------------------------getter-----------------------------------------------
    private Context getContext() {
        return DownloadManagerImpl.getInstance().getContext();
    }

    public String getUuid() {
        return uuid;
    }

    public String getTaskName() {
        return name;
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

    public long getBlocks() {
        return blocks;
    }

    public int getFinishCount() {
        return finishCount;
    }

    public long getLength() {
        return length;
    }

    public long getDone() {
        return done;
    }

    public List<Long> getThreadPositions() {
        return threadPositions;
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
        return hasInit;
    }

    public MissionConfig getMissionConfig() {
        return missionConfig;
    }

    public boolean isRecovered() {
        return recovered;
    }

    public String getDownloadPath() {
        return missionConfig.getDownloadPath();
    }

    public String getFilePath() {
        return getDownloadPath() + File.separator + name;
    }

    public File getFile() {
        return new File(getFilePath());
    }

    public String getMimeType() {
        return FileUtil.getMIMEType(getFile());
    }

    public String getFileSuffix() {
        return MimeTypeMap.getFileExtensionFromUrl(getFile().toURI().toString()).toLowerCase(Locale.US);
    }

    public String getUserAgent() {
        return missionConfig.getUserAgent();
    }

    public String getCookie() {
        return missionConfig.getCookie();
    }

    public int getBlockSize() {
        return missionConfig.getBlockSize();
    }

    public int getConnectOutTime() {
        if (fallback) {
            return missionConfig.getConnectOutTime() * 10;
        }
        return missionConfig.getConnectOutTime();
    }

    public int getReadOutTime() {
        if (fallback) {
            return missionConfig.getConnectOutTime() * 10;
        }
        return missionConfig.getReadOutTime();
    }

    Map<String, String> getHeaders() {
        return missionConfig.getHeaders();
    }

    public float getProgress() {
        if (missionStatus == MissionStatus.FINISHED) {
            return 100f;
        } else if (length <= 0) {
            return 0f;
        }
        float progress = (float) done / (float) length;
        return progress * 100f;
    }

    public String getProgressStr() {
        return String.format(Locale.US, "%.2f%%", getProgress());
    }

    public String getFileSizeStr() {
        return Utility.formatSize(length);
    }

    public String getDownloadedSizeStr() {
        return Utility.formatSize(done);
    }

    public String getSpeed() {
        return tempSpeed;
    }

    private int getNotifyId() {
        if (notifyId == 0) {
            notifyId = (int) (createTime / 10000) + (int) (createTime % 10000) * 100000;
        }
        return notifyId;
    }

    public long getPosition(int id) {
        return threadPositions.get(id);
    }

    public String getMissionInfoFilePath() {
        return DownloadManagerImpl.TASK_PATH + File.separator + uuid + DownloadManagerImpl.MISSION_INFO_FILE_SUFFIX_NAME;
    }


    //-----------------------------------------------------setter-----------------------------------------------------------------


    public void setTaskName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setThreadPosition(int id, long position) {
        threadPositions.set(id, position);
    }

    public void setRecovered(boolean recovered) {
        this.recovered = recovered;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }



    //----------------------------------------------------------------other

    public boolean isBlockPreserved(long block) {
        Boolean state = blockState.get(block);
        return state != null && state;
    }

    public void preserveBlock(long block) {
        synchronized (blockState) {
            blockState.put(block, true);
        }
    }

    private boolean handleResponse(Connection.Response response, DownloadMission mission) {
        Log.d("statusCode11111111", "       " + response.statusCode());
        Log.d("response.headers()", "1111" + response.headers());
        if (TextUtils.isEmpty(mission.name)) {
            mission.name = getMissionNameFromResponse(response);
            Log.d("mission.name", "mission.name333=" + mission.name);
        }
        if (response.statusCode() == ResponseCode.RESPONSE_302
                || response.statusCode() == ResponseCode.RESPONSE_301
                || response.statusCode() == ResponseCode.RESPONSE_300) {
            String redictUrl = response.header("location");
            Log.d(TAG, "redirectUrl=" + redictUrl);
            if (redictUrl != null) {
                mission.url = redictUrl;
                mission.redirectUrl = redictUrl;
            }
        } else if (response.statusCode() == ErrorCode.ERROR_SERVER_404) {
            mission.errCode = ErrorCode.ERROR_SERVER_404;
            mission.notifyError(ErrorCode.ERROR_SERVER_404);
            return true;
        } else if (response.statusCode() == ResponseCode.RESPONSE_206) {
            String contentLength = response.header("Content-Length");
            if (contentLength != null) {
                mission.length = Long.parseLong(contentLength);
            }
            Log.d("mission.length", "mission.length=" + mission.length);
            return !checkLength(mission);
        }
        return false;
    }

    private String getMissionNameFromResponse(Connection.Response response) {
        String contentDisposition = response.header("Content-Disposition");
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

    private String getMissionNameFromUrl(DownloadMission mission, String url) {
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
                    if (FileUtil.checkFileType(originName) != FileUtil.FILE_TYPE.UNKNOWN) {
                        Log.d("getMissionNameFromUrl", "4");
                        return originName;
                    }
                }

                if (FileUtil.checkFileType(name) != FileUtil.FILE_TYPE.UNKNOWN || name.contains(".")) {
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

    private boolean checkLength(DownloadMission mission) {
        if (mission.length <= 0) {
            mission.errCode = ErrorCode.ERROR_SERVER_UNSUPPORTED;
            mission.notifyError(ErrorCode.ERROR_SERVER_UNSUPPORTED);
            return false;
        } else if (mission.length >= Utility.getAvailableSize()) {
            mission.errCode = ErrorCode.ERROR_NO_ENOUGH_SPACE;
            mission.notifyError(ErrorCode.ERROR_NO_ENOUGH_SPACE);
            return false;
        }
        return true;
    }

}
