package com.zpj.downloader.core.impl;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Dao;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbsDao<T extends Mission> implements Dao<T> {

    static String MISSION_INFO_FILE_SUFFIX_NAME = ".zpj";

    @Override
    public List<T> queryMissions(Downloader<T> downloader) {
        List<T> missions = new ArrayList<>();
        File f = downloader.getConfig().getTaskFolder();
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (final File sub : f.listFiles()) {
                    if (sub.isFile() && sub.exists()
                            && sub.getName().endsWith(MISSION_INFO_FILE_SUFFIX_NAME)) {
                        T mission = readMission(sub);
                        if (mission == null) {
                            continue;
                        }
                        missions.add(mission);
                    }
                }
            }
        } else {
            f.mkdirs();
        }
        return missions;
    }

    public abstract T readMission(File file);

    @Override
    public boolean saveMissionInfo(T mission) {
        return false;
    }

    @Override
    public boolean saveBlocks(List<Block> blocks) {
        return false;
    }

    @Override
    public boolean updateProgress(T mission, long done) {
        return false;
    }

}
