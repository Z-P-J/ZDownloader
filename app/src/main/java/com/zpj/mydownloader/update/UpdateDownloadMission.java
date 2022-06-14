package com.zpj.mydownloader.update;

import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.downloader.impl.DownloadMission;

public class UpdateDownloadMission extends DownloadMission {

    public UpdateDownloadMission(MissionInfo info, Config config) {
        super(info, config);
    }

}
