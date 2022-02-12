package com.zpj.downloader.core;

import java.util.List;

public interface MissionLoader<T extends Mission> {

    void onLoad(List<T> missions);

}
