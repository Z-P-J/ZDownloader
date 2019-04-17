package com.zpj.qxdownloader.option;

import android.content.Context;

import com.zpj.qxdownloader.util.content.SPHelper;


/**
* @author Z-P-J
* */
public class QianXunOptions extends BaseOptions {

    private QianXunOptions() {

    }

    public static QianXunOptions with(Context context) {
        QianXunOptions options = new QianXunOptions();
        options.setContext(context);
        return options;
    }

    @Override
    public QianXunOptions setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    @Override
    public QianXunOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    @Override
    public QianXunOptions setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    @Override
    public QianXunOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public QianXunOptions setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public QianXunOptions setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    @Override
    public QianXunOptions setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }
}
