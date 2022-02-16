package com.zpj.downloader.core;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.MissionInfo;
import com.zpj.downloader.utils.MissionIdGenerator;
import com.zpj.downloader.utils.SerializableProxy;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Mission {

    interface Observer {

        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onRetrying();

        void onProgress(Mission mission, float speed);

        void onFinished();

        void onError(Error e);

        void onDelete();

        void onClear();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.NEW, Status.PREPARING, Status.WAITING,  Status.DOWNLOADING,
            Status.PAUSED, Status.ERROR, Status.RETRYING, Status.COMPLETE})
    @interface Status {
        int NEW = 0;
        int PREPARING = 1;
        int WAITING = 2;
        int DOWNLOADING = 4;
        int PAUSED = 5;
        int ERROR = 6;
        int RETRYING = 7;
        int COMPLETE = 8;

        int DELETE = 9;
        int CLEAR = 10;
    }

    interface Lifecycle {
        void onPrepare();

        void onStart();

        void onPaused();

        void onWaiting();

        void onRetrying();

        void onFinished();

        void onError(Error e);

        void onDelete();

        void onClear();
    }


    void start();

    void prepare();

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

    boolean isRetrying();

    boolean isComplete();

    boolean isError();

    boolean canPause();

    boolean canStart();


    //--------------------------------------------------------------getter-----------------------------------------------

    String getUuid();

    String getName();

    String getUrl();

    String getOriginUrl();

    long getCreateTime();

    long getFinishTime();

    int getFinishCount();

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

    float getSpeed();

    String getSpeedStr();

    int getNotifyId();

    String getMissionInfoFilePath();

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

    void setSupportSlice(boolean support);




    public static class Builder {

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

        boolean allowAllSSL = true;

        final HashMap<String, String> headers = new HashMap<>();

        SerializableProxy proxy;

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

        public boolean isAllowAllSSL() {
            return allowAllSSL;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Proxy getProxy() {
            if (proxy == null) {
                return null;
            }
            return proxy.proxy();
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
            this.userAgent = userAgent;
            return this;
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder setCookie(String cookie) {
            this.cookie = cookie;
            return this;
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

        public Builder setAllowAllSSL(boolean allowAllSSL) {
            this.allowAllSSL = allowAllSSL;
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

        public Builder setProxy(SerializableProxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder setProxy(Proxy proxy) {
            if (proxy == null) {
                return this;
            }
            return setProxy(SerializableProxy.with(proxy));
        }

        public Builder setProxy(String host, int port) {
            return setProxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port)));
        }

        public Builder setEnableNotification(boolean enableNotification) {
            this.enableNotification = enableNotification;
            return this;
        }

        public <T extends Mission> T build(Class<T> clazz) {
            // TODO UUID.randomUUID();
            String missionId = String.valueOf(MissionIdGenerator.getInstance().generateValidId());
            MissionInfo info = new MissionInfo(missionId, url, name);
            return ZDownloader.get(clazz).create(info, new Config(missionId, this));
        }

    }

}
