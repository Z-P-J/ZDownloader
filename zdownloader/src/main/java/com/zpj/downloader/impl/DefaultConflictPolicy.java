package com.zpj.downloader.impl;

import android.text.TextUtils;

import com.zpj.downloader.core.ConflictPolicy;
import com.zpj.downloader.core.Mission;

public class DefaultConflictPolicy implements ConflictPolicy {

    @Override
    public boolean isConflict(Mission mission, Mission conflictMission) {
        return TextUtils.equals(mission.getUrl(), conflictMission.getUrl())
                || TextUtils.equals(mission.getUrl(), conflictMission.getOriginUrl());
    }

    @Override
    public void onConflict(Mission mission, Callback callback) {
        callback.onResult(true);
    }

}