package com.zpj.downloader.core.impl;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Updater;
import com.zpj.downloader.utils.ThreadPool;

import java.util.List;

/**
 * 下载任务进度更新
 */
public class ProgressUpdater<T extends Mission> implements Updater {

    private static final String TAG = "ProgressUpdater";

    private static final int MSG_PROGRESS = 100;

    private final T mission;
    private HandlerThread handlerThread;
    private Handler handler;
    private volatile long lastDownloaded;
    private volatile long lastTime;

    public ProgressUpdater(final T mission) {
        this.mission = mission;
    }

    @Override
    public void start() {
        stop();
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_PROGRESS) {
                    if (mission.isFinished() || mission.getErrorCode() != -1 || !mission.isRunning()) {
                        stop();
                        return;
                    }
                    long progressInterval = mission.getConfig().getProgressInterval();
                    sendEmptyMessageDelayed(MSG_PROGRESS, progressInterval);

                    long currentTime = SystemClock.elapsedRealtime();

                    long downloaded = 0;

                    List<Block> blocks = ZDownloader.get((Class<T>) mission.getClass()).getDao().queryBlocks(mission);
                    for (Block block : blocks) {
                        downloaded += block.getDownloaded();
                    }

                    long delta = downloaded - lastDownloaded;
                    Log.d(TAG, "progressRunnable--delta=" + delta);

                    if (delta > 0) {
                        lastDownloaded = downloaded;
                        final float speed = delta * ((currentTime - lastTime) / 1000f);

                        ThreadPool.post(new Runnable() {
                            @Override
                            public void run() {
                                List<Mission.Observer> observers = mission.getObservers();
                                for (Mission.Observer observer : observers) {
                                    observer.onProgress(mission, speed);
                                }
                            }
                        });
                    }
                    lastTime = currentTime;
                }
            }
        };

        lastDownloaded = mission.getDownloaded();
        lastTime = SystemClock.elapsedRealtime();
        handler.sendEmptyMessage(MSG_PROGRESS);
    }

    public void stop() {
        if (handler != null) {
            handler.removeMessages(MSG_PROGRESS);
            handler = null;
        }
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
        lastDownloaded = -1;
    }

}
