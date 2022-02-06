package com.zpj.downloader.core;

import java.io.File;
import java.util.List;

/**
 * dao
 */
public interface Dao<T extends Mission> {

    List<T> queryMissions(Downloader<T> downloader);

    boolean saveMission(T mission);

    boolean saveBlocks(List<Block> blocks);

    boolean queryBlocks(List<Block> blocks);

    boolean updateBlockDownloaded(Block block, long downloaded);

    boolean updateProgress(final T mission, long done);

    boolean updateStatus(final T mission, int status);

}
