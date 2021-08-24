package com.zpj.downloader.impl;

import android.text.TextUtils;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.ConflictPolicy;

public class DefaultConflictPolicy implements ConflictPolicy {

    @Override
    public boolean isConflict(BaseMission<?> mission, BaseMission<?> conflictMission) {
        return TextUtils.equals(mission.getUrl(), conflictMission.getUrl())
                || TextUtils.equals(mission.getUrl(), conflictMission.getOriginUrl());
    }

    @Override
    public void onConflict(BaseMission<?> mission, Callback callback) {
        callback.onResult(true);
    }
}