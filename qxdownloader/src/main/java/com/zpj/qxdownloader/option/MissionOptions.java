package com.zpj.qxdownloader.option;

public class MissionOptions extends BaseOptions {

    public static MissionOptions with() {
        return new MissionOptions();
    }

    public MissionOptions setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    public MissionOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public MissionOptions setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public MissionOptions setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public MissionOptions setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public MissionOptions setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

}
