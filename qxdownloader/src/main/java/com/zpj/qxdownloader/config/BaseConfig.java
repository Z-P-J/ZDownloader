package com.zpj.qxdownloader.config;

import android.content.Context;

import com.zpj.qxdownloader.constant.DefaultConstant;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Z-P-J
 * */
abstract class BaseConfig {

    private transient Context context;

    ThreadPoolConfig threadPoolConfig = ThreadPoolConfig.build();

    String downloadPath = DefaultConstant.DOWNLOAD_PATH;

    int bufferSize = DefaultConstant.BUFFER_SIZE;

    int blockSize = DefaultConstant.BLOCK_SIZE;

    String userAgent = DefaultConstant.USER_AGENT;

    int retryCount = DefaultConstant.RETRY_COUNT;

    int retryDelay = DefaultConstant.RETRY_DELAY;

    int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;

    int readOutTime = DefaultConstant.READ_OUT_TIME;

    String cookie = "";

    Map<String, String> headers = new HashMap<>();

    Proxy proxy;

    void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public BaseConfig setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    public abstract BaseConfig setThreadPoolConfig(ThreadPoolConfig threadPoolConfig);

    public ThreadPoolConfig getThreadPoolConfig() {
        return threadPoolConfig;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    @Deprecated
    public abstract BaseConfig setThreadCount(int threadCount);

//    {
//        this.threadCount = threadCount;
//        return this;
//    }

//    public int getThreadCount() {
//        return threadCount;
//    }

    public abstract BaseConfig setBufferSize(int bufferSize);

    public int getBufferSize() {
        return bufferSize;
    }

    public abstract BaseConfig setBlockSize(int blockSize);

    public int getBlockSize() {
        return blockSize;
    }

    public abstract BaseConfig setUserAgent(String userAgent);

    public String getUserAgent() {
        return userAgent;
    }

    public abstract BaseConfig setRetryCount(int retryCount);

    public int getRetryCount() {
        return retryCount;
    }

    public abstract BaseConfig setCookie(String cookie);

    public String getCookie() {
        return cookie;
    }

    public abstract BaseConfig setRetryDelay(int retryDelay);

    public int getRetryDelay() {
        return retryDelay;
    }

    public abstract BaseConfig setConnectOutTime(int connectOutTime);

    public int getConnectOutTime() {
        return connectOutTime;
    }

    public abstract BaseConfig setReadOutTime(int readOutTime);

    public int getReadOutTime() {
        return readOutTime;
    }

    public abstract BaseConfig setHeaders(Map<String, String> headers);

    public Map<String, String> getHeaders() {
        return headers;
    }

    public abstract BaseConfig setProxy(Proxy proxy);

    public abstract BaseConfig setProxy(String host, int port);

    public Proxy getProxy() {
        return proxy;
    }
}
