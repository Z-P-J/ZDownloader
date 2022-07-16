package com.zpj.downloader.core;

import android.support.annotation.MainThread;

import java.util.List;

public interface MissionLoader<T extends Mission> {

    @MainThread
    void onLoad(List<T> missions);

}
