package com.zpj.downloader.impl;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.MissionSerializer;
import com.zpj.downloader.utils.io.UnsafeObjectInputStream;
import com.zpj.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DefaultMissionSerializer implements MissionSerializer {


    @Override
    public BaseMission<?> readMission(final File file, final Class<? extends BaseMission<?>> clazz) {
        BufferedInputStream fileIn = null;
        ObjectInputStream in = null;
        BaseMission<?> mission = null;
        try {
            fileIn = new BufferedInputStream(new FileInputStream(file));
            in = new UnsafeObjectInputStream(fileIn);
            mission = clazz.cast(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeIO(in, fileIn);
        }
        return mission;
    }

    @Override
    public void writeMission(final BaseMission<?> mission) {
        synchronized (DefaultMissionSerializer.class) {
            BufferedOutputStream fileOut = null;
            ObjectOutputStream out = null;
            try {
                fileOut = new BufferedOutputStream(new FileOutputStream(mission.getMissionInfoFilePath()));
                out = new ObjectOutputStream(fileOut);
                out.writeObject(mission);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtils.closeIO(out, fileOut);
            }
        }
    }


}
