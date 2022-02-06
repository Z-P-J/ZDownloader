package com.zpj.downloader.core;

public class Block {

    private final long start;

    private final long end;

    private long downloaded;

    public Block(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public interface BlockObserver {

        void onProgress();

        void onError();

    }

}
