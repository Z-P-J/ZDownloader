package com.zpj.downloader;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * 下载任务精度更新器
 */
public class ProgressUpdater {

    private static final String TAG = "ProgressUpdater";

    private final BaseMission<?> mission;
    private HandlerThread handlerThread;
    private Handler handler;
    private long lastDone;

    ProgressUpdater(final BaseMission<?> mission) {
        this.mission = mission;
    }

    public long getSize() {
        return mission.getLength();
    }

    public long getDone() {
        return mission.getDone();
    }

    public float getProgress() {
        return mission.getProgress();
    }

    public float getSpeed() {
        return mission.getSpeed();
    }

    public String getFileSizeStr() {
        return mission.getFileSizeStr();
    }

    public String getDownloadedSizeStr() {
        return mission.getDownloadedSizeStr();
    }

    public String getProgressStr() {
        return mission.getProgressStr();
    }

    public String getSpeedStr() {
        return mission.getSpeedStr();
    }

    void start() {
        stop();
        lastDone = mission.done.get();
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    Log.d(TAG, "progressRunnable--start isRunning=" + mission.isRunning()
                            + " missionStatus=" + mission.missionStatus
                            + " aliveThreadCount=" + mission.aliveThreadCount.get());
                    if (mission.isFinished() || mission.errCode != -1 || mission.aliveThreadCount.get() < 1 || !mission.isRunning()) {
                        mission.notifyStatus(mission.missionStatus);
                        stop();
                        return;
                    }
                    sendEmptyMessageDelayed(0, mission.getProgressInterval());
                    long downloaded = mission.done.get();
                    long delta = downloaded - lastDone;
                    Log.d(TAG, "progressRunnable--delta=" + delta);
                    mission.speedHistoryList.add(delta);
                    if (delta > 0) {
                        lastDone = downloaded;
                        mission.speed = delta * (mission.getProgressInterval() / 1000f);
                    }
//                    mission.writeMissionInfo();
                    DownloadManagerImpl.getInstance().writeMission(mission);
                    mission.notifyStatus(BaseMission.MissionStatus.RUNNING);
                }
            }
        };
        handler.sendEmptyMessage(0);
    }

    void stop() {
        if (handler != null) {
            handler.removeMessages(0);
            handler = null;
        }
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
        lastDone = -1;
    }

}
