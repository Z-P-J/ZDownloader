package com.zpj.downloader.core;

import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

public interface MissionFactory<T extends Mission> {

     T createMission(MissionInfo info, Config config);

}
