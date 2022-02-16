package com.zpj.downloader.core.impl;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.zpj.downloader.core.Mission;

@Keep
@Entity(tableName = "mission_infos")
public class MissionInfo {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "mission_id")
    protected String missionId;

    protected volatile String uuid = "";
    protected volatile String name = "";
    protected volatile String url = "";
    @ColumnInfo(name = "origin_url")
    protected volatile String originUrl = "";
    @ColumnInfo(name = "create_time")
    protected volatile long createTime = 0;
    @ColumnInfo(name = "finish_time")
    protected volatile long finishTime = 0;
    protected volatile long length = 0;
    protected volatile long downloaded = 0;
    @ColumnInfo(name = "status")
    @Mission.Status
    protected volatile int missionStatus = Mission.Status.NEW;
    @ColumnInfo(name = "block_download")
    protected volatile boolean isBlockDownload = false;
    @ColumnInfo(name = "error_code")
    protected volatile int errorCode = -1;
    @ColumnInfo(name = "error_message")
    protected volatile String errorMessage;
    @ColumnInfo(name = "prepared")
    protected volatile boolean isPrepared;

    public MissionInfo(@NonNull String missionId, String url, String name) {
        this.missionId = missionId;
        this.url = url;
        this.name = name;
    }

    public String getMissionId() {
        return missionId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getLength() {
        return length;
    }

    public long getDownloaded() {
        return downloaded;
    }

    @Mission.Status
    public int getMissionStatus() {
        return missionStatus;
    }

    public boolean isBlockDownload() {
        return isBlockDownload;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setMissionId(@NonNull String missionId) {
        this.missionId = missionId;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public void setMissionStatus(int missionStatus) {
        this.missionStatus = missionStatus;
    }

    public void setBlockDownload(boolean blockDownload) {
        isBlockDownload = blockDownload;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setPrepared(boolean prepared) {
        isPrepared = prepared;
    }
}
