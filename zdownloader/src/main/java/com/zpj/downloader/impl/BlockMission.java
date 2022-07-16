package com.zpj.downloader.impl;

import com.zpj.downloader.core.ChildMission;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.core.model.MissionInfo;

import java.io.File;
import java.util.List;

public class BlockMission implements ChildMission {
    @Override
    public Mission getParentMission() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void waiting() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void delete() {

    }

    @Override
    public void clear() {

    }

    @Override
    public void addObserver(Observer listener) {

    }

    @Override
    public boolean hasObserver(Observer listener) {
        return false;
    }

    @Override
    public void removeObserver(Observer listener) {

    }

    @Override
    public void removeAllObserver() {

    }

    @Override
    public List<Observer> getObservers() {
        return null;
    }

    @Override
    public boolean isPreparing() {
        return false;
    }

    @Override
    public boolean isDownloading() {
        return false;
    }

    @Override
    public boolean isWaiting() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canStart() {
        return false;
    }

    @Override
    public String getMissionId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getOriginUrl() {
        return null;
    }

    @Override
    public long getCreateTime() {
        return 0;
    }

    @Override
    public long getFinishTime() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public long getDownloaded() {
        return 0;
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public int getErrorCode() {
        return 0;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public boolean isBlockDownload() {
        return false;
    }

    @Override
    public boolean hasInit() {
        return false;
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public String getFileSuffix() {
        return null;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public String getProgressStr() {
        return null;
    }

    @Override
    public String getFileSizeStr() {
        return null;
    }

    @Override
    public String getDownloadedSizeStr() {
        return null;
    }

    @Override
    public long getSpeed() {
        return 0;
    }

    @Override
    public String getSpeedStr() {
        return null;
    }

    @Override
    public int getNotifyId() {
        return 0;
    }

    @Override
    public boolean isSupportSlice() {
        return false;
    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public MissionInfo getMissionInfo() {
        return null;
    }

    @Override
    public void setStatus(int status) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setUrl(String url) {

    }

    @Override
    public void setOriginUrl(String originUrl) {

    }

    @Override
    public void setLength(long length) {

    }

    @Override
    public void setErrorCode(int errCode) {

    }

    @Override
    public void setErrorMessage(String msg) {

    }

    @Override
    public void setBlockDownload(boolean support) {

    }
}
