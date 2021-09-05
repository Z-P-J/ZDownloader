package com.zpj.downloader.impl;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.ProgressUpdater;
import com.zpj.downloader.constant.Error;

public class EmptyMissionListener implements BaseMission.MissionListener {
    @Override
    public void onPrepare() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onWaiting() {

    }

    @Override
    public void onRetrying() {

    }

    @Override
    public void onProgress(ProgressUpdater update) {

    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(Error e) {

    }

    @Override
    public void onDelete() {

    }

    @Override
    public void onClear() {

    }
}
