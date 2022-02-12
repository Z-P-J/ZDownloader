package com.zpj.downloader.core;

import java.util.List;

public interface BlockDivider<T extends Mission> {

    List<Block> divide(T mission);

    List<Block> load(T mission);

    void save(T mission, List<Block> blocks);

}
