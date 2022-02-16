package com.zpj.downloader.core;

import com.zpj.downloader.core.impl.Config;

import java.io.File;
import java.util.List;

/**
 * dao
 */
public interface Dao<T extends Mission> {

    List<T> queryMissions(Downloader<T> downloader);

    boolean saveConfig(Config config);

    boolean saveMissionInfo(T mission);

    boolean hasMission(T mission);

    boolean updateMissionInfo(T mission);

    boolean saveBlocks(List<Block> blocks);

    boolean updateBlock(Block block);

    List<Block> queryBlocks(T mission);

    List<Block> queryUnfinishedBlocks(T mission);

    boolean updateBlockDownloaded(Block block, long downloaded);

    boolean updateProgress(final T mission, long done);

    boolean updateStatus(final T mission, int status);

    boolean deleteMission(final T mission);

}
