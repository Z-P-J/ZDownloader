package com.zpj.downloader.core.impl.dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.zpj.downloader.core.impl.Config;
import com.zpj.utils.ContextUtils;

@Database(entities = {Config.class}, version = 1)
public abstract class MissionDatabase extends RoomDatabase {

    private static MissionDatabase INSTANCE;

    public abstract ConfigDao configDao();

    public abstract MissionInfoDao missionDao();

    public abstract BlockDao blockDao();

    public static MissionDatabase getInstance(String dbName) {
        if (INSTANCE == null) {
            synchronized (MissionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ContextUtils.getApplicationContext(), MissionDatabase.class, dbName)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
