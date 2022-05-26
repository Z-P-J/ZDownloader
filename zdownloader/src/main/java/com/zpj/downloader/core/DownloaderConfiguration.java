package com.zpj.downloader.core;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.impl.MissionBlockTransfer;
import com.zpj.downloader.core.impl.MissionDispatcher;
import com.zpj.downloader.core.impl.MissionInitializer;

public class DownloaderConfiguration<T extends Mission> {


    private final String mKey;
    private final Dispatcher<T> mDispatcher;
    private final Initializer<T> mInitializer;
    private final Notifier<T> mNotifier;
    private final Transfer<T> mTransfer;
    private final Repository<T> mRepository;

    private DownloaderConfiguration(Builder<T> builder) {
        this.mKey = builder.mKey;
        this.mDispatcher = builder.mDispatcher;
        this.mInitializer = builder.mInitializer;
        this.mNotifier = builder.mNotifier;
        this.mTransfer = builder.mTransfer;
        this.mRepository = builder.mRepository;
    }

    public String getKey() {
        return mKey;
    }

    public Dispatcher<T> getDispatcher() {
        return mDispatcher;
    }

    public Initializer<T> getInitializer() {
        return mInitializer;
    }

    public Notifier<T> getNotifier() {
        return mNotifier;
    }

    public Transfer<T> getTransfer() {
        return mTransfer;
    }

    public Repository<T> getRepository() {
        return mRepository;
    }

    public static class Builder<T extends Mission> {

        private final String mKey;
        private Dispatcher<T> mDispatcher = new MissionDispatcher<>();
        private Initializer<T> mInitializer = new MissionInitializer<>();
        private Notifier<T> mNotifier;
        private Transfer<T> mTransfer = new MissionBlockTransfer<>();
        private Repository<T> mRepository;

        public Builder(@NonNull String key) {
            this.mKey = key;
        }

        public Builder<T> setDispatcher(Dispatcher<T> mDispatcher) {
            this.mDispatcher = mDispatcher;
            return this;
        }

        public Builder<T> setInitializer(Initializer<T> mInitializer) {
            this.mInitializer = mInitializer;
            return this;
        }

        public Builder<T> setNotifier(Notifier<T> mNotifier) {
            this.mNotifier = mNotifier;
            return this;
        }

        public Builder<T> setTransfer(Transfer<T> mTransfer) {
            this.mTransfer = mTransfer;
            return this;
        }

        public Builder<T> setRepository(Repository<T> mRepository) {
            this.mRepository = mRepository;
            return this;
        }

        public DownloaderConfiguration<T> build() {
            return new DownloaderConfiguration<>(this);
        }

    }

}
