package com.zpj.mydownloader.update;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.BaseDownloader;
import com.zpj.downloader.core.DownloaderConfig;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public class UpdateMissionDownloader extends BaseDownloader<UpdateDownloadMission> {

    public UpdateMissionDownloader(@NonNull DownloaderConfig<UpdateDownloadMission> config) {
        super(config);
    }

    @Override
    public UpdateDownloadMission createMission(MissionInfo info, Config config) {
        return new UpdateDownloadMission(info, config);
    }

}
