package com.zpj.downloader.core.impl;

import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.BlockDivider;
import com.zpj.downloader.core.Mission;

import java.util.ArrayList;
import java.util.List;

public class BaseBlockDivider<T extends Mission> implements BlockDivider<T> {
    @Override
    public List<Block> divide(T mission) {

        String missionId = mission.getMissionInfo().getMissionId();

        long start = 0;
        long end;

        long blockSize = DefaultConstant.BLOCK_SIZE;
        long count = mission.getLength() / blockSize;
        List<Block> blocks = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            end = i * blockSize;
            blocks.add(new Block(missionId, start, end));
            start = end;
        }

        if (start < mission.getLength()) {
            blocks.add(new Block(missionId, start, mission.getLength()));
        }

        return blocks;
    }

    @Override
    public List<Block> load(T mission) {
        return null;
    }

    @Override
    public void save(T mission, List<Block> blocks) {

    }
}
