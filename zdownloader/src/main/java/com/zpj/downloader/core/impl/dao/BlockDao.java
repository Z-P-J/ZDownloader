package com.zpj.downloader.core.impl.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zpj.downloader.core.Block;

import java.util.List;

@Dao
public interface BlockDao {

    @Insert
    void insert(Block... blocks);

    @Insert
    void insert(List<Block> blocks);

    @Update
    int update(Block... blocks);

    @Query(value = "SELECT * from mission_blocks WHERE mission_id = :missionId")
    List<Block> queryAll(String missionId);

    @Query(value = "SELECT * from mission_blocks WHERE mission_id = :missionId and status = 0")
    List<Block> queryUnfinishedBlocks(String missionId);

}
