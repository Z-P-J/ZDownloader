package com.zpj.downloader.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.BaseDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public class MissionDownloader extends BaseDownloader<DownloadMission> {

    public static final String KEY_DEFAULT_DOWNLOADER = "default_downloader";

    public MissionDownloader() {
        this(config().build());
    }

    public MissionDownloader(DownloaderConfig<DownloadMission> config) {
        super(config);
    }

    public static DownloaderConfig.Builder<DownloadMission> config() {
        return new DownloaderConfig.Builder<>(KEY_DEFAULT_DOWNLOADER);
    }

    public static DownloaderConfig.Builder<DownloadMission> config(@NonNull String key) {
        return new DownloaderConfig.Builder<>(key);
    }

    @Override
    public DownloadMission createMission(MissionInfo info, Config config) {
        return new DownloadMission(info, config);
    }

}
