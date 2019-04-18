package com.zpj.qxdownloader.config;

import android.content.Context;

import java.util.Map;


/**
* @author Z-P-J
* */
public class QianXunConfig extends BaseConfig {

    private QianXunConfig() {

    }

    public static QianXunConfig with(Context context) {
        QianXunConfig options = new QianXunConfig();
        options.setContext(context);
        return options;
    }

    @Override
    public QianXunConfig setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    @Deprecated
    @Override
    public QianXunConfig setThreadCount(int threadCount) {
        this.threadPoolConfig.setCorePoolSize(threadCount);
        return this;
    }

    @Override
    public QianXunConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    public QianXunConfig setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    @Override
    public QianXunConfig setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public QianXunConfig setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public QianXunConfig setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    @Override
    public QianXunConfig setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    @Override
    public QianXunConfig setConnectOutTime(int connectOutTime) {
        this.connectOutTime = connectOutTime;
        return this;
    }

    @Override
    public QianXunConfig setReadOutTime(int readOutTime) {
        this.readOutTime = readOutTime;
        return this;
    }

    @Override
    public QianXunConfig setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public QianXunConfig setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
        return this;
    }

}
