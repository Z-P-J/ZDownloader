package com.zpj.downloader.core;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.http.HttpHeader;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.MissionIdGenerator;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Mission {

    interface Observer {

        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onProgress(Mission mission, float speed);

        void onFinished();

        void onError(int errorCode, String errorMessage);

        void onDelete();

        void onClear();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.CREATED, Status.PREPARING, Status.WAITING,  Status.DOWNLOADING,
            Status.PAUSED, Status.ERROR, Status.COMPLETE, Status.DELETE, Status.CLEAR})
    @interface Status {
        int CREATED = 0;
        int PREPARING = 1;
        int WAITING = 2;
        int DOWNLOADING = 4;
        int PAUSED = 5;
        int ERROR = 6;
        int COMPLETE = 8;

        int DELETE = 9;
        int CLEAR = 10;
    }


    void start();

    void pause();

    void waiting();

    void restart();

    void delete();

    void clear();

    public void addObserver(Observer listener);

    public boolean hasObserver(Observer listener);

    public void removeObserver(Observer listener);

    public void removeAllObserver();

    List<Observer> getObservers();

    //-------------------------下载任务状态-----------------------------------
    boolean isPreparing();

    boolean isDownloading();

    boolean isWaiting();

    boolean isPaused();

    boolean isComplete();

    boolean isError();

    boolean canPause();

    boolean canStart();


    //--------------------------------------------------------------getter-----------------------------------------------

    String getMissionId();

    String getName();

    String getUrl();

    String getOriginUrl();

    long getCreateTime();

    long getFinishTime();

    long getLength();

    long getDownloaded();

    @Status
    int getStatus();

    int getErrorCode();

    String getErrorMessage();

    boolean isBlockDownload();

    boolean hasInit();

    String getFilePath();

    File getFile();

    String getFileSuffix();

    float getProgress();

    String getProgressStr();

    String getFileSizeStr();

    String getDownloadedSizeStr();

    long getSpeed();

    String getSpeedStr();

    int getNotifyId();

    boolean isSupportSlice();

    Config getConfig();

    MissionInfo getMissionInfo();


    //-----------------------------------------------------setter-----------------------------------------------------------------
    void setStatus(int status);

    void setName(String name);

    void setUrl(String url);

    void setOriginUrl(String originUrl);

    void setLength(long length);

    void setErrorCode(int errCode);

    void setErrorMessage(String msg);

    void setBlockDownload(boolean support);




    class Builder {

        private static final String TAG = "Mission.Builder";

        private String url;
        private String name;

        /*
         * 下载线程数
         * */
        int threadCount = DefaultConstant.THREAD_COUNT;

        /**
         * 下载路径
         * */
        String downloadPath = DefaultConstant.DOWNLOAD_PATH;

        /**
         * 下载缓冲大小
         * */
        int bufferSize = DefaultConstant.BUFFER_SIZE;

        /**
         * 进度更新频率，默认1000ms更新一次（单位ms）
         * */
        long progressInterval =DefaultConstant.PROGRESS_INTERVAL;

        /**
         * 下载块大小
         * */
        int blockSize = DefaultConstant.BLOCK_SIZE;

        /**
         * 默认UserAgent
         * */
        String userAgent = DefaultConstant.USER_AGENT;

        /**
         * 下载出错重试次数
         * */
        int retryCount = DefaultConstant.RETRY_COUNT;

        /**
         * 下载出错重试延迟时间（单位ms）
         * */
        int retryDelayMillis = DefaultConstant.RETRY_DELAY_MILLIS;

        /**
         * 下载连接超时
         * */
        int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;

        /**
         * 下载链接读取超时
         * */
        int readOutTime = DefaultConstant.READ_OUT_TIME;

        /**
         * 是否允许在通知栏显示任务下载进度
         * */
        boolean enableNotification = true;

        /**
         * 下载时传入的cookie额值
         * */
        String cookie = "";

        final HashMap<String, String> headers = new HashMap<>();

        public Builder(String url) {
            this(url, null);
        }

        public Builder(String url, String name) {
            this.url = url;
            this.name = name;
        }

        //-----------------------------------------------------------getter-------------------------------------------------------------

        public int getThreadCount() {
            if (threadCount < 1) {
                threadCount = 1;
            }
            return threadCount;
        }

        public String getDownloadPath() {
            if (TextUtils.isEmpty(downloadPath)) {
                return DefaultConstant.DOWNLOAD_PATH;
            }
            return downloadPath;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public long getProgressInterval() {
            return progressInterval;
        }

        public int getBlockSize() {
            return blockSize;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public String getCookie() {
            return cookie == null ? "" : cookie;
        }

        public int getRetryDelayMillis() {
            return retryDelayMillis;
        }

        public int getConnectOutTime() {
            return connectOutTime;
        }

        public int getReadOutTime() {
            return readOutTime;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public boolean getEnableNotification() {
            return enableNotification;
        }

        //-----------------------------------------------------------------setter------------------------------------------------------


        public Builder setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
            return this;
        }

        public Builder setThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setProgressInterval(long progressInterval) {
            this.progressInterval = progressInterval;
            return this;
        }

        public Builder setBlockSize(int blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            return addHeader(HttpHeader.USER_AGENT, userAgent);
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder setCookie(String cookie) {
            return addHeader(HttpHeader.COOKIE, cookie);
        }

        public Builder setRetryDelayMillis(int retryDelayMillis) {
            this.retryDelayMillis = retryDelayMillis;
            return this;
        }

        public Builder setConnectOutTime(int connectOutTime) {
            this.connectOutTime = connectOutTime;
            return this;
        }

        public Builder setReadOutTime(int readOutTime) {
            this.readOutTime = readOutTime;
            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers.clear();
            this.headers.putAll(headers);
            return this;
        }

        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder setEnableNotification(boolean enableNotification) {
            this.enableNotification = enableNotification;
            return this;
        }

        public <T extends Mission> T build(Class<T> clazz) {
            // TODO UUID.randomUUID();
            String missionId = String.valueOf(MissionIdGenerator.getInstance().generateValidId());
            Logger.d(TAG, "missionId=" + missionId);
            MissionInfo info = new MissionInfo(missionId, url, name);
            return ZDownloader.get(clazz).createMission(info, new Config(missionId, this));
        }

    }

}
