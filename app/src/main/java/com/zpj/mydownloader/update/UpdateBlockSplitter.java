package com.zpj.mydownloader.update;

import com.zpj.downloader.core.BlockSplitter;
import com.zpj.downloader.core.model.Block;

import java.util.Collections;
import java.util.List;

public class UpdateBlockSplitter implements BlockSplitter<UpdateDownloadMission> {
    @Override
    public List<Block> divide(UpdateDownloadMission mission) {
        Block block = new Block(mission.getMissionId(), 0, mission.getLength());
        return Collections.singletonList(block);
    }

    @Override
    public List<Block> load(UpdateDownloadMission mission) {
        return null;
    }

    @Override
    public void save(UpdateDownloadMission mission, List<Block> blocks) {

    }
}
