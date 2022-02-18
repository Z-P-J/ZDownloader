package com.zpj.downloader.core.impl.dao;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Dao;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.core.impl.MissionInfo;

import java.util.ArrayList;
import java.util.Arrays;
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
        database.configDao().insert(config);
        return true;
    }

    @Override
    public boolean saveMissionInfo(T mission) {
        database.missionDao().insert(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean hasMission(T mission) {
        return database.missionDao().queryInfo(mission.getMissionInfo().getMissionId()) != null;
    }

    @Override
    public boolean updateMissionInfo(T mission) {
        database.missionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean saveBlocks(List<Block> blocks) {
        database.blockDao().insert(blocks);
        return true;
    }

    @Override
    public boolean saveBlocks(Block... blocks) {
        return saveBlocks(Arrays.asList(blocks));
    }

    @Override
    public boolean updateBlock(Block block) {
        database.blockDao().update(block);
        return true;
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
        database.blockDao().update(block);
        return true;
    }

    @Override
    public boolean updateProgress(T mission, long done) {
        database.missionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean updateStatus(T mission, int status) {
        database.missionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean deleteMission(T mission) {
        database.missionDao().delete(mission.getMissionInfo());
//        result |= database.configDao().delete(mission.getConfig());
        return true;
    }
}
