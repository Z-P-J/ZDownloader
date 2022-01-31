package com.zpj.downloader.core.impl;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Mission;

import java.util.concurrent.LinkedBlockingQueue;

public class AbsDispatcher<T extends Mission> implements Dispatcher<T> {


    private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();

    @Override
    public void enqueue(T mission) {

    }

    @Override
    public void pause(T mission) {

    }

    @Override
    public void delete(T mission) {

    }

    @Override
    public void notifyError(T mission, Error e) {

    }

    @Override
    public void notifyStatus(T mission, int status) {
        switch (status) {
            case Mission.Status.NEW:
                enqueue(mission);
                break;
            case Mission.Status.WAITING:
                // TODO
                break;
            case Mission.Status.PREPARING:
                // TODO

                break;
            case Mission.Status.START:
                break;
            case Mission.Status.PROGRESSING:
                break;
            case Mission.Status.PAUSED:
                break;
            case Mission.Status.ERROR:
                break;
            case Mission.Status.RETRYING:
                break;
//            case Mission.Status.CLEAR:
//                break;
//            case Mission.Status.DELETE:
//                break;
            case Mission.Status.FINISHED:
                break;
            default:
                break;
        }
    }

}
