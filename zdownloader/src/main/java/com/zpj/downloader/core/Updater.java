package com.zpj.downloader.core;

public interface Updater {

    public long getSize();

    public long getDone();

    public float getProgress();

    public float getSpeed();

    public String getFileSizeStr();

    public String getDownloadedSizeStr();

    public String getProgressStr();

    public String getSpeedStr();

    void start();

    void stop();

}
