package com.zpj.qxdownloader.option;

/**
 * @author Z-P-J
 * */
public class MissionOptions extends BaseOptions {

    public static MissionOptions with() {
        return new MissionOptions();
    }

    @Override
    public MissionOptions setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    @Override
    public MissionOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    @Override
    public MissionOptions setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    @Override
    public MissionOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public MissionOptions setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public MissionOptions setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    @Override
    public MissionOptions setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }
}
