package com.zpj.downloader.core.impl.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zpj.downloader.core.impl.Config;

import java.util.List;

@Dao
public interface ConfigDao {

    @Insert
    public boolean insert(Config...configs);

    @Delete
    boolean delete(Config...configs);

    @Update
    boolean update(Config...configs);

    @Query("SELECT * FROM mission_configs")
    List<Config> queryConfigs();

    @Query("SELECT * FROM mission_configs WHERE mission_id = :missionId")
    Config queryConfig(String missionId);

}
