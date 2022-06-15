package com.zpj.downloader.core;

import com.zpj.downloader.core.model.Block;

public interface MissionExecutor<T extends Mission> {

//    void execute(T mission);

    void execute(BaseDownloader.BlockTask<T> block);

    void shutdown();

}
