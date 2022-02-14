package com.zpj.downloader.core.impl.dao;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Dao;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.MissionInfo;

import java.util.ArrayList;
import java.util.List;

public class RoomDao<T extends Mission> implements Dao<T> {

    @NonNull
    private final MissionDatabase database;

    public RoomDao(@NonNull MissionDatabase database) {
        this.database = database;
    }

    @Override
    public List<T> queryMissions(Downloader<T> downloader) {
        List<MissionInfo> infoList = database.missionDao().queryInfos();
        List<T> list = new ArrayList<>();
        for (MissionInfo info : infoList) {
            Config config = database.configDao().queryConfig(info.getMissionId());
            list.add(downloader.create(info, config));
        }
        return list;
    }

    @Override
    public boolean saveConfig(Config config) {
        return database.configDao().insert(config);
    }

    @Override
    public boolean saveMissionInfo(T mission) {
        return database.missionDao().insert(mission.getMissionInfo());
    }

    @Override
    public boolean updateMissionInfo(T mission) {
        return database.missionDao().update(mission.getMissionInfo());
    }

    @Override
    public boolean saveBlocks(List<Block> blocks) {
        return database.blockDao().insert(blocks);
    }

    @Override
    public boolean updateBlock(Block block) {
        return database.blockDao().update(block);
    }

    @Override
    public List<Block> queryBlocks(T mission) {
        return database.blockDao().queryAll(mission.getMissionInfo().getMissionId());
    }

    @Override
    public List<Block> queryUnfinishedBlocks(T mission) {
        return database.blockDao().queryUnfinishedBlocks(mission.getMissionInfo().getMissionId());
    }

    @Override
    public boolean updateBlockDownloaded(Block block, long downloaded) {
        return database.blockDao().update(block);
    }

    @Override
    public boolean updateProgress(T mission, long done) {
        return database.missionDao().update(mission.getMissionInfo());
    }

    @Override
    public boolean updateStatus(T mission, int status) {
        return database.missionDao().update(mission.getMissionInfo());
    }

    @Override
    public boolean deleteMission(T mission) {
        boolean result = true;
        result |= database.missionDao().delete(mission.getMissionInfo());
//        result |= database.configDao().delete(mission.getConfig());
        return result;
    }
}
