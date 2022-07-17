package com.zpj.downloader.core.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zpj.downloader.core.model.MissionInfo;

import java.util.List;

@Dao
public interface MissionInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MissionInfo... infos);

    @Delete
    void delete(MissionInfo... infos);

    @Update
    void update(MissionInfo... infos);

    @Query("SELECT * FROM mission_infos ORDER BY create_time ASC")
    List<MissionInfo> queryInfos();

    @Query("SELECT * FROM mission_infos WHERE mission_id = :missionId")
    MissionInfo queryInfo(String missionId);

    @Query("SELECT * FROM mission_infos WHERE url = :url or origin_url == :url")
    MissionInfo queryInfoByUrl(String url);

    @Query("SELECT * FROM mission_infos WHERE name = :name")
    List<MissionInfo> queryInfoByName(String name);

//    @Query("UPDATE downloaded FROM mission_infos WHERE missionId = :missionId")
//    MissionInfo updateProgress(String missionId, long downloaded);

}
