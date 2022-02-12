//package com.zpj.downloader.core.impl;
//
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.util.Log;
//
//import com.zpj.downloader.BaseMission;
//import com.zpj.downloader.DownloadManagerImpl;
//import com.zpj.downloader.core.Updater;
//
///**
// * 下载任务进度更新
// */
//public class ProgressUpdater implements Updater {
//
//    private static final String TAG = "ProgressUpdater";
//
//    private final BaseMission<?> mission;
//    private HandlerThread handlerThread;
//    private Handler handler;
//    private long lastDone;
//
//    ProgressUpdater(final BaseMission<?> mission) {
//        this.mission = mission;
//    }
//
//    @Override
//    public long getSize() {
//        return mission.getLength();
//    }
//
//    @Override
//    public long getDone() {
//        return mission.getDone();
//    }
//
//    @Override
//    public float getProgress() {
//        return mission.getProgress();
//    }
//
//    @Override
//    public float getSpeed() {
//        return mission.getSpeed();
//    }
//
//    @Override
//    public String getFileSizeStr() {
//        return mission.getFileSizeStr();
//    }
//
//    @Override
//    public String getDownloadedSizeStr() {
//        return mission.getDownloadedSizeStr();
//    }
//
//    @Override
//    public String getProgressStr() {
//        return mission.getProgressStr();
//    }
//
//    @Override
//    public String getSpeedStr() {
//        return mission.getSpeedStr();
//    }
//
//    @Override
//    public void start() {
//        stop();
//        lastDone = mission.done.get();
//        handlerThread = new HandlerThread(TAG);
//        handlerThread.start();
//        handler = new Handler(handlerThread.getLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 0) {
//                    Log.d(TAG, "progressRunnable--start isRunning=" + mission.isRunning()
//                            + " missionStatus=" + mission.missionStatus
//                            + " aliveThreadCount=" + mission.aliveThreadCount.get());
//                    if (mission.isFinished() || mission.errCode != -1 || mission.aliveThreadCount.get() < 1 || !mission.isRunning()) {
//                        mission.notifyStatus(mission.missionStatus);
//                        stop();
//                        return;
//                    }
//                    sendEmptyMessageDelayed(0, mission.getProgressInterval());
//                    long downloaded = mission.done.get();
//                    long delta = downloaded - lastDone;
//                    Log.d(TAG, "progressRunnable--delta=" + delta);
//                    mission.speedHistoryList.add(delta);
//                    if (delta > 0) {
//                        lastDone = downloaded;
//                        mission.speed = delta * (mission.getProgressInterval() / 1000f);
//                    }
////                    mission.writeMissionInfo();
//                    DownloadManagerImpl.getInstance().writeMission(mission);
//                    mission.notifyStatus(BaseMission.MissionStatus.RUNNING);
//                }
//            }
//        };
//        handler.sendEmptyMessage(0);
//    }
//
//    public void stop() {
//        if (handler != null) {
//            handler.removeMessages(0);
//            handler = null;
//        }
//        if (handlerThread != null) {
//            handlerThread.quit();
//            handlerThread = null;
//        }
//        lastDone = -1;
//    }
//
//}
