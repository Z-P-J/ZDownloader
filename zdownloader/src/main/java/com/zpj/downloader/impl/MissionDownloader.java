package com.zpj.downloader.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.BaseDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public class MissionDownloader extends BaseDownloader<DownloadMission> {

    public static final String KEY_DEFAULT_DOWNLOADER = "default_downloader";

    public MissionDownloader() {
        this(builder().build());
    }

    public MissionDownloader(DownloaderConfig<DownloadMission> config) {
        super(config);
    }

    public static DownloaderConfig.Builder<DownloadMission> builder() {
        return new DownloaderConfig.Builder<DownloadMission>(KEY_DEFAULT_DOWNLOADER);
    }

    public static DownloaderConfig.Builder<DownloadMission> builder(@NonNull String key) {
        return new DownloaderConfig.Builder<DownloadMission>(key);
    }

    @Override
    public DownloadMission createMission(MissionInfo info, Config config) {
        return new DownloadMission(info, config);
    }

}
