package com.zpj.downloader.core;

import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.Block;

import java.util.List;

/**
 * 下载任务数据持久化接口
 */
public interface Repository<T extends Mission> {

    List<T> queryMissions(Downloader<T> downloader);

    boolean saveConfig(Config config);

    boolean saveMissionInfo(T mission);

    boolean hasMission(T mission);

    boolean updateMissionInfo(T mission);

    boolean saveBlocks(List<Block> blocks);

    boolean saveBlocks(Block... blocks);

    boolean updateBlock(Block block);

    List<Block> queryBlocks(T mission);

    long queryDownloaded(T mission);

    List<Block> queryShouldDownloadBlocks(T mission);

    boolean updateBlockDownloaded(Block block, long downloaded);

    boolean updateProgress(final T mission, long done);

    boolean updateStatus(final T mission, int status);

    boolean deleteMission(final T mission);

    boolean deleteBlocks(final T mission);

}
