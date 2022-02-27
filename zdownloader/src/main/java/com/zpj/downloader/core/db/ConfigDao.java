package com.zpj.downloader.core.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zpj.downloader.core.model.Config;

import java.util.List;

@Dao
public interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Config... configs);

    @Delete
    void delete(Config...configs);

    @Update
    void update(Config...configs);

    @Query("SELECT * FROM mission_configs")
    List<Config> queryConfigs();

    @Query("SELECT * FROM mission_configs WHERE mission_id = :missionId")
    Config queryConfig(String missionId);

}
