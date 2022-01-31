package com.zpj.downloader.core;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.impl.Config;

public interface Downloader<T extends Mission> {

    T download(String url);

    T download(String url, String name);

    Config config();










    void setDispatcher(Dispatcher<T> dispatcher);

    Dispatcher<T> getDispatcher();

    void setHttpFactory(HttpFactory httpFactory);

    HttpFactory getHttpFactory();

    void setInitializer(Initializer<T> initializer);

    Initializer<T> getInitializer();

    void setNotifier(Notifier<T> notifier);

    Notifier<T> getNotifier();

    void setSerializer(Serializer<T> serializer);

    Serializer<T> getSerializer();

    void setTransfer(Transfer transfer);

    Transfer getTransfer();

    void setThreadPool(ThreadPool threadPool);

    ThreadPool getThreadPool();

    void setUpdater(Updater updater);

    Updater getUpdater();









    void enqueue(T mission);

    void pause(T mission);

    void delete(T mission);

    void notifyError(T mission, final Error e);

    void notifyStatus(final T mission, final int status);

}
