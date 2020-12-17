package com.zpj.downloader.config;

import com.zpj.downloader.core.DownloadManagerImpl;

/**
 * @author Z-P-J
 * */
public class MissionConfig extends BaseConfig<MissionConfig> {

    private MissionConfig() {

    }

    public static MissionConfig with() {
        DownloaderConfig config = DownloadManagerImpl.getInstance().getDownloaderConfig();
        if (config == null) {
            config = DownloaderConfig.with(DownloadManagerImpl.getInstance().getContext());
        }
        return new MissionConfig()
                .setNotificationInterceptor(config.notificationInterceptor)
                .setDownloadPath(config.downloadPath)
                .setBufferSize(config.bufferSize)
                .setProgressInterval(config.progressInterval)
                .setBlockSize(config.blockSize)
                .setRetryCount(config.retryCount)
                .setRetryDelay(config.retryDelay)
                .setConnectOutTime(config.connectOutTime)
                .setReadOutTime(config.readOutTime)
                .setUserAgent(config.userAgent)
                .setCookie(config.cookie)
                .setEnableNotification(config.enableNotification);
    }

}
