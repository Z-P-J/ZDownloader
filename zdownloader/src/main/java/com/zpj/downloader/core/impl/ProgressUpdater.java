package com.zpj.downloader.core.impl;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Updater;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.ThreadPool;

import java.util.List;

/**
 * 下载任务进度更新
 */
public class ProgressUpdater<T extends Mission> implements Updater {

    private static final String TAG = "ProgressUpdater";

    private static final int MSG_PROGRESS = 100;

    private final T mission;
    private final Downloader<T> downloader;


    private HandlerThread handlerThread;
    private Handler handler;
    private volatile long lastDownloaded;
    private volatile long lastTime;

    public ProgressUpdater(final T mission) {
        this.mission = mission;
        downloader = ZDownloader.get(mission);
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
                    Logger.d(TAG, "handleMessage isComplete=" + mission.isComplete() + " errorCode=" + mission.getErrorCode() + " isDownloading=" + mission.isDownloading());
                    long progressInterval = mission.getConfig().getProgressInterval();
                    sendEmptyMessageDelayed(MSG_PROGRESS, progressInterval);



                    long downloaded = downloader.getRepository().queryDownloaded(mission);

                    long delta = downloaded - lastDownloaded;

                    long currentTime = SystemClock.elapsedRealtime();

                    Logger.d(TAG, "progressRunnable--downloaded=" + downloaded + " delta=" + delta);

                    if (delta > 0) {

                        final long speed = (long) (delta * ((currentTime - lastTime) / 1000f));
                        mission.getMissionInfo().downloaded = downloaded;
                        mission.getMissionInfo().setSpeed(speed);

                        downloader.getRepository().saveMissionInfo(mission);

                        if (mission.isComplete() || mission.isError()) {
                            stop();
                            return;
                        }

                        lastDownloaded = downloaded;

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
