package com.zpj.downloader.core;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;

import com.zpj.downloader.core.impl.MissionInfo;

@Keep
@Entity(
        tableName = "mission_blocks",
        foreignKeys = @ForeignKey(entity = MissionInfo.class, parentColumns = {"mission_id"}, childColumns = {"mission_id"}),
        indices = {@Index(value = "mission_id")}
)
public class Block {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "mission_id")
    private String missionId;

    private long start;

    private long end;

    private long downloaded;

    private int status;

    public Block(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public interface BlockObserver {

        void onProgress();

        void onError();

    }

}
