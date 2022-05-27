package com.zpj.downloader.core.db;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.Repository;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissionRepository<T extends Mission> implements Repository<T> {
    
    private volatile MissionDatabase database;
    
    private final MissionDatabaseFactory mFactory;

//    public MissionRepository(String key) {
//        this(MissiongetInstance(key));
//    }
//
//    public MissionRepository(@NonNull MissionDatabase database) {
//        this.database = database;
//    }
    
    public MissionRepository(@NonNull MissionDatabaseFactory factory) {
        this.mFactory = factory;
    }
    
    private MissionDatabase getDatabase() {
        if (database == null) {
            synchronized (mFactory) {
                if (database == null) {
                    database = mFactory.createDatabase();
                }
            }
        }
        return database;
    }

    public ConfigDao getConfigDao() {
        return getDatabase().configDao();
    }

    public MissionInfoDao getMissionDao() {
        return getDatabase().missionDao();
    }

    public BlockDao getBlockDao() {
        return getDatabase().blockDao();
    }

    @Override
    public List<T> queryMissions(Downloader<T> downloader) {
        List<MissionInfo> infoList = getMissionDao().queryInfos();
        List<T> list = new ArrayList<>();
        for (MissionInfo info : infoList) {
            Config config = getConfigDao().queryConfig(info.getMissionId());
            list.add(downloader.createMission(info, config));
        }
        return list;
    }

    @Override
    public boolean saveConfig(Config config) {
        getConfigDao().insert(config);
        return true;
    }

    @Override
    public boolean saveMissionInfo(T mission) {
        getMissionDao().insert(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean hasMission(T mission) {
        return getMissionDao().queryInfo(mission.getMissionId()) != null;
    }

    @Override
    public boolean updateMissionInfo(T mission) {
        getMissionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean saveBlocks(List<Block> blocks) {
        getBlockDao().insert(blocks);
        return true;
    }

    @Override
    public boolean saveBlocks(Block... blocks) {
        return saveBlocks(Arrays.asList(blocks));
    }

    @Override
    public boolean updateBlock(Block block) {
        getBlockDao().update(block);
        return true;
    }

    @Override
    public List<Block> queryBlocks(T mission) {
        return getBlockDao().queryAll(mission.getMissionId());
    }

    @Override
    public long queryDownloaded(T mission) {
        return getBlockDao().queryDownloaded(mission.getMissionId());
    }

    @Override
    public List<Block> queryShouldDownloadBlocks(T mission) {
        return getBlockDao().queryDownloadableBlocks(mission.getMissionId());
    }

    @Override
    public boolean updateBlockDownloaded(Block block, long downloaded) {
        getBlockDao().update(block);
        return true;
    }

    @Override
    public boolean updateProgress(T mission, long done) {
        getMissionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean updateStatus(T mission, int status) {
        getMissionDao().update(mission.getMissionInfo());
        return true;
    }

    @Override
    public boolean deleteMission(T mission) {
        getConfigDao().delete(mission.getConfig());
        getBlockDao().delete(mission.getMissionId());
        getMissionDao().delete(mission.getMissionInfo());

//        result |= configDao().delete(mission.getConfig());
        return true;
    }

    @Override
    public boolean deleteBlocks(T mission) {
        getBlockDao().delete(mission.getMissionId());
        return true;
    }
}
