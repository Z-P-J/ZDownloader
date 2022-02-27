package com.zpj.downloader.core;

import com.zpj.downloader.core.model.Block;

import java.util.List;

public interface BlockSplitter<T extends Mission> {

    List<Block> divide(T mission);

    List<Block> load(T mission);

    void save(T mission, List<Block> blocks);

}
