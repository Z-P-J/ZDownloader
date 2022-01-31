package com.zpj.downloader.core.impl;

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

public class FileDownloader implements Downloader {
    @Override
    public Mission download(String url) {
        return null;
    }

    @Override
    public Mission download(String url, String name) {
        return null;
    }

    @Override
    public Config config() {
        return null;
    }

    @Override
    public void setDispatcher(Dispatcher dispatcher) {

    }

    @Override
    public Dispatcher getDispatcher() {
        return null;
    }

    @Override
    public void setHttpFactory(HttpFactory httpFactory) {

    }

    @Override
    public HttpFactory getHttpFactory() {
        return null;
    }

    @Override
    public void setInitializer(Initializer initializer) {

    }

    @Override
    public Initializer getInitializer() {
        return null;
    }

    @Override
    public void setNotifier(Notifier notifier) {

    }

    @Override
    public Notifier getNotifier() {
        return null;
    }

    @Override
    public void setSerializer(Serializer serializer) {

    }

    @Override
    public Serializer getSerializer() {
        return null;
    }

    @Override
    public void setTransfer(Transfer transfer) {

    }

    @Override
    public Transfer getTransfer() {
        return null;
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
}
