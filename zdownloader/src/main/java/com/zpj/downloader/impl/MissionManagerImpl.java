package com.zpj.downloader.impl;

import android.support.annotation.NonNull;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.MissionManager;
import com.zpj.downloader.core.Mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 下载mission管理
 * @param <T>
 */
public class MissionManagerImpl<T extends Mission>
        implements MissionManager<T>, Downloader.DownloaderObserver<T> {

    private final Class<T> mClass;
    private final List<T> mMissions = new ArrayList<>();
    private Observer<T> mObserver;

    public MissionManagerImpl(@NonNull Class<T> clazz) {
        this.mClass = clazz;
    }

    @Override
    public List<T> getMissions() {
        return mMissions;
    }

    @Override
    public void register(@NonNull Observer<T> observer) {
        this.mObserver = observer;
    }

    @Override
    public void loadMissions() {
        ZDownloader.loadMissions(this.mClass, missions -> {
            if (this.mObserver == null) {
                return;
            }
            mMissions.clear();
            mMissions.addAll(missions);
            Collections.reverse(mMissions);

            this.mObserver.onMissionLoaded(this.mMissions);
            ZDownloader.addObserver(this.mClass, MissionManagerImpl.this);
        });
    }

    @Override
    public void onDestroy() {
        this.mObserver = null;
        ZDownloader.removeObserver(mClass, this);
        this.mMissions.clear();
    }


    @Override
    public void onMissionAdd(T mission) {
        mMissions.add(0, mission);
        if (this.mObserver != null) {
            this.mObserver.onMissionAdd(mission, 0);
        }
    }

    @Override
    public void onMissionDelete(T mission) {
        int index = mMissions.indexOf(mission);
        if (index > 0) {
            mMissions.remove(index);
            if (this.mObserver != null) {
                this.mObserver.onMissionDelete(mission, index);
            }
        }
    }

    @Override
    public void onMissionFinished(T mission) {
        if (this.mObserver != null) {
            this.mObserver.onMissionFinished(mission, this.mMissions.indexOf(mission));
        }
    }


}
