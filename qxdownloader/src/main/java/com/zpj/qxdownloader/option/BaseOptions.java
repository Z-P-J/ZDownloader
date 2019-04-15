package com.zpj.qxdownloader.option;

import android.content.Context;

public class BaseOptions {

    private Context context;

    protected String downloadPath = DefaultOptions.DOWNLOAD_PATH;

    protected int threadCount = DefaultOptions.THREAD_COUNT;

    protected int blockSize = DefaultOptions.BLOCK_SIZE;

    protected String userAgent = DefaultOptions.USER_AGENT;

    protected int retryCount = DefaultOptions.RETRY_COUNT;

    protected String cookie = "";

    protected void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public BaseOptions setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public BaseOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public BaseOptions setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public BaseOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public BaseOptions setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public BaseOptions setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    public String getCookie() {
        return cookie;
    }
}
