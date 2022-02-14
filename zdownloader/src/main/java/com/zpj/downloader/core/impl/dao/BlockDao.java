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
    boolean insert(Block... blocks);

    @Insert
    boolean insert(List<Block> blocks);

    @Update
    boolean update(Block... blocks);

    @Query(value = "SELECT * from mission_blocks WHERE mission_id = :")
    List<Block> queryAll(String missionId);

    @Query(value = "SELECT * from mission_blocks WHERE mission_id = :missionId and status <> 0")
    List<Block> queryUnfinishedBlocks(String missionId);

}
