package com.zpj.downloader.core.impl;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Dispatcher;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.HttpFactory;
import com.zpj.downloader.core.Initializer;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.core.Serializer;
import com.zpj.downloader.core.ThreadPool;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.core.Updater;

public abstract class AbsDownloader<T extends Mission> implements Downloader<T> {

//    private static final class InstanceHolder {
//        private static final AbsDownloader INSTANCE = new AbsDownloader();
//    }
//
//    public static AbsDownloader getInstance() {
//        return InstanceHolder.INSTANCE;
//    }

    private Dispatcher<T> mDispatcher;
    private Initializer<T> mInitializer;
    private Serializer<T> mSerializer;
    private Notifier<T> mNotifier;
    private Transfer mTransfer;

    @Override
    public Config config() {
        return new DownloaderConfig();
    }

//    @Override
//    public T download(String url) {
//        return download(url, null);
//    }
//
//    @Override
//    public T download(String url, String name) {
//        Config config = new Config(config());
//
//        AbsMission mission = new AbsMission(config);
//        mission.url = url;
//        mission.originUrl = url;
//        mission.name = name;
//        mission.uuid = UUID.randomUUID().toString();
//        mission.createTime = System.currentTimeMillis();
//        return mission;
//    }

    @Override
    public void setDispatcher(Dispatcher<T> dispatcher) {
        this.mDispatcher = dispatcher;
    }

    @Override
    public Dispatcher<T> getDispatcher() {
        return mDispatcher;
    }

    @Override
    public void setHttpFactory(HttpFactory httpFactory) {

    }

    @Override
    public HttpFactory getHttpFactory() {
        return null;
    }

    @Override
    public void setInitializer(Initializer<T> initializer) {
        this.mInitializer = initializer;
    }

    @Override
    public Initializer<T> getInitializer() {
        return mInitializer;
    }

    @Override
    public void setNotifier(Notifier<T> notifier) {
        this.mNotifier = notifier;
    }

    @Override
    public Notifier<T> getNotifier() {
        return mNotifier;
    }

    @Override
    public void setSerializer(Serializer<T> serializer) {
        this.mSerializer = serializer;
    }

    @Override
    public Serializer<T> getSerializer() {
        return mSerializer;
    }

    @Override
    public void setTransfer(Transfer transfer) {
        this.mTransfer = transfer;
    }

    @Override
    public Transfer getTransfer() {
        return mTransfer;
    }

    @Override
    public void setThreadPool(ThreadPool threadPool) {

    }

    @Override
    public ThreadPool getThreadPool() {
        return null;
    }

    @Override
    public void setUpdater(Updater updater) {

    }

    @Override
    public Updater getUpdater() {
        return null;
    }








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
                // TODO notify

                break;
            case Mission.Status.PREPARING:
                // TODO
                getInitializer().initMission(this, mission);
                break;
//            case Mission.Status.START:
//                // TODO notify
//                break;
            case Mission.Status.PROGRESSING:
                // TODO notify
                break;
            case Mission.Status.PAUSED:
                // TODO notify
                break;
            case Mission.Status.ERROR:
                // TODO notify
                break;
            case Mission.Status.RETRYING:
                // TODO notify
                break;
//            case Mission.Status.CLEAR:
//                break;
//            case Mission.Status.DELETE:
//                break;
            case Mission.Status.FINISHED:
                // TODO notify
                break;
            default:
                break;
        }
    }

}
