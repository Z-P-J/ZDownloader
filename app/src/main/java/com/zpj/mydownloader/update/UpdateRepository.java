package com.zpj.mydownloader.update;

import android.content.Context;

import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Repository;
import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.model.Config;
import com.zpj.utils.PrefsHelper;

import java.util.List;

public class UpdateRepository implements Repository<UpdateDownloadMission> {

    private static final String SP_UPDATE = "sp_update_";

    private final PrefsHelper mSpHelper;

    public UpdateRepository(Context context) {
        this.mSpHelper = PrefsHelper.with(SP_UPDATE + context.getPackageName());
    }

    @Override
    public List<UpdateDownloadMission> queryMissions(Downloader<UpdateDownloadMission> downloader) {
        return null;
    }

    @Override
    public boolean saveConfig(Config config) {
        return false;
    }

    @Override
    public boolean saveMissionInfo(UpdateDownloadMission mission) {
        return false;
    }

    @Override
    public boolean hasMission(UpdateDownloadMission mission) {
        return false;
    }

    @Override
    public boolean updateMissionInfo(UpdateDownloadMission mission) {
        return false;
    }

    @Override
    public boolean saveBlocks(List<Block> blocks) {
        return false;
    }

    @Override
    public boolean saveBlocks(Block... blocks) {
        return false;
    }

    @Override
    public boolean updateBlock(Block block) {
        return false;
    }

    @Override
    public List<Block> queryBlocks(UpdateDownloadMission mission) {
        return null;
    }

    @Override
    public long queryDownloaded(UpdateDownloadMission mission) {
        return 0;
    }

    @Override
    public List<Block> queryShouldDownloadBlocks(UpdateDownloadMission mission) {
        return null;
    }

    @Override
    public boolean updateBlockDownloaded(Block block, long downloaded) {
        return false;
    }

    @Override
    public boolean updateProgress(UpdateDownloadMission mission, long done) {
        return false;
    }

    @Override
    public boolean updateStatus(UpdateDownloadMission mission, int status) {
        return false;
    }

    @Override
    public boolean deleteMission(UpdateDownloadMission mission) {
        return false;
    }

    @Override
    public boolean deleteBlocks(UpdateDownloadMission mission) {
        return false;
    }
}
