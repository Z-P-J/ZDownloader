package com.zpj.downloader.core.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.DownloaderConfig;
import com.zpj.downloader.core.model.MissionInfo;

public class MissionDownloader extends BaseDownloader<DownloadMission> {

    @Override
    public DownloadMission create(MissionInfo info, Config config) {
        return new DownloadMission(config, info);
    }

    @Override
    public DownloaderConfig getConfig() {
        return null;
    }

    @NonNull
    @Override
    public String getKey() {
        return "default_downloader";
    }

}