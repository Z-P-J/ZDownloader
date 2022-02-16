package com.zpj.downloader.core.impl;

import android.support.annotation.NonNull;

public class FileDownloader extends AbsDownloader<DownloadMission> {

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
