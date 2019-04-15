package com.zpj.qxdownloader.option;

import android.content.Context;

public class QianXunOptions extends BaseOptions {

    private QianXunOptions() {

    }

    public static QianXunOptions with(Context context) {
        QianXunOptions options = new QianXunOptions();
        options.setContext(context);
        return options;
    }

    public QianXunOptions setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    public QianXunOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public QianXunOptions setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public QianXunOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public QianXunOptions setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public QianXunOptions setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }
}
