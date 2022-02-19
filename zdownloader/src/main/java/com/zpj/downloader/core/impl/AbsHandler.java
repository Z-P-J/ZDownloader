package com.zpj.downloader.core.impl;

import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;

import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Handler;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.utils.Logger;

public class AbsHandler implements Handler {

    private static final String TAG = "AbsHandler";

    @NonNull
    private final Downloader<?> downloader;

    private HandlerThread handlerThread;
    private android.os.Handler handler;

    public AbsHandler(@NonNull Downloader<?> downloader) {
        this.downloader = downloader;
    }

    @Override
    public void start() {
        stop();
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new android.os.Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int event = msg.what;
                Mission mission = (Mission) msg.obj;
                Logger.d(TAG, "handleMessage event=" + event + " missionName=" + mission.getName() + " url=" + mission.getUrl() + " missionId=" + mission.getMissionInfo().getMissionId());
                handleEvent(mission, event);
            }
        };
    }

    @Override
    public void stop() {
        if (handler != null) {
            handler = null;
        }
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
    }

    @Override
    public void sendEvent(Mission mission, @Event int event) {
        if (handler != null) {
            Message.obtain(handler, event, mission).sendToTarget();
        }
    }

    @Override
    public void handleEvent(Mission mission, @Event int event) {

    }

}
