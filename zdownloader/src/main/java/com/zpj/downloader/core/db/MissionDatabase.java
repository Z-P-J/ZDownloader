package com.zpj.downloader.core.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.zpj.downloader.core.model.Block;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;
import com.zpj.utils.ContextUtils;

@Database(entities = {Config.class, MissionInfo.class, Block.class}, version = 2, exportSchema = false)
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
                            .addMigrations(MIGRATION_1)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE mission_infos ADD COLUMN speed INTEGER NOT NULL DEFAULT 0");
        }
    };

}
