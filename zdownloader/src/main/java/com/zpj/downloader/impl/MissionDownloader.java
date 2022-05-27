package com.zpj.downloader.impl;

import com.zpj.downloader.core.BaseDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public class MissionDownloader extends BaseDownloader<DownloadMission> {

    public MissionDownloader() {
        this(new DownloaderConfig.Builder<DownloadMission>("default_downloader").build());
    }

    public MissionDownloader(DownloaderConfig<DownloadMission> config) {
        super(config);
    }

    @Override
    public DownloadMission createMission(MissionInfo info, Config config) {
        return new DownloadMission(config, info);
    }

}
