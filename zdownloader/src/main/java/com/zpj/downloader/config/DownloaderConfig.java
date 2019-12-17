package com.zpj.downloader.config;

import android.content.Context;

import com.zpj.downloader.constant.DefaultConstant;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;


/**
* @author Z-P-J
* */
public class DownloaderConfig extends BaseConfig {

    private int concurrentMissionCount = DefaultConstant.CONCURRENT_MISSION_COUNT;

    private DownloaderConfig() {

    }

    public static DownloaderConfig with(Context context) {
        DownloaderConfig options = new DownloaderConfig();
        options.setContext(context);
        return options;
    }

    @Override
    public DownloaderConfig setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    @Deprecated
    @Override
    public DownloaderConfig setThreadCount(int threadCount) {
        this.threadPoolConfig.setCorePoolSize(threadCount);
        return this;
    }

    @Override
    public DownloaderConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    public DownloaderConfig setProgressInterval(long progressInterval) {
        this.progressInterval = progressInterval;
        return this;
    }

    @Override
    public DownloaderConfig setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    @Override
    public DownloaderConfig setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public DownloaderConfig setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public DownloaderConfig setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    @Override
    public DownloaderConfig setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    @Override
    public DownloaderConfig setConnectOutTime(int connectOutTime) {
        this.connectOutTime = connectOutTime;
        return this;
    }

    @Override
    public DownloaderConfig setReadOutTime(int readOutTime) {
        this.readOutTime = readOutTime;
        return this;
    }

    @Override
    public DownloaderConfig setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    @Override
    public BaseConfig addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    @Override
    public DownloaderConfig setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
        return this;
    }

    @Override
    public DownloaderConfig setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    @Override
    public DownloaderConfig setProxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
        return this;
    }

    @Override
    public DownloaderConfig setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
        return this;
    }

    public int getConcurrentMissionCount() {
        return concurrentMissionCount;
    }

    public DownloaderConfig setConcurrentMissionCount(int concurrentMissionCount) {
        this.concurrentMissionCount = concurrentMissionCount;
        return this;
    }
}
