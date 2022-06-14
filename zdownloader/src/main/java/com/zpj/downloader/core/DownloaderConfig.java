package com.zpj.downloader.core;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.db.MissionDatabase;
import com.zpj.downloader.core.db.MissionDatabaseFactory;
import com.zpj.downloader.core.db.MissionRepository;
import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.http.UrlConnectionHttpFactory;
import com.zpj.downloader.impl.MissionBlockSplitter;
import com.zpj.downloader.impl.MissionBlockTransfer;
import com.zpj.downloader.impl.MissionDispatcher;
import com.zpj.downloader.impl.MissionExecutorFactory;
import com.zpj.downloader.impl.MissionInitializer;

public class DownloaderConfig<T extends Mission> {


    private final String mKey;
    private final Dispatcher<T> mDispatcher;
    private final Initializer<T> mInitializer;
    private final Notifier<? super T> mNotifier;
    private final Transfer<T> mTransfer;
    private final Repository<T> mRepository;
    private final BlockSplitter<T> mBlockSplitter;
    private final ExecutorFactory<T> mExecutorFactory;
//    private final MissionFactory<T> mMissionFactory;
    private final HttpFactory mHttpFactory;

    private DownloaderConfig(Builder<T> builder) {
        this.mKey = builder.mKey;
        this.mDispatcher = builder.mDispatcher;
        this.mInitializer = builder.mInitializer;
        this.mNotifier = builder.mNotifier;
        this.mTransfer = builder.mTransfer;
        this.mRepository = builder.mRepository;
        this.mBlockSplitter = builder.mBlockSplitter;
        this.mExecutorFactory = builder.mExecutorFactory;
        this.mHttpFactory = builder.mHttpFactory;
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

    public Notifier<? super T> getNotifier() {
        return mNotifier;
    }

    public Transfer<T> getTransfer() {
        return mTransfer;
    }

    public Repository<T> getRepository() {
        return mRepository;
    }

    public BlockSplitter<T> getBlockSplitter() {
        return mBlockSplitter;
    }

    public ExecutorFactory<T> getExecutorFactory() {
        return mExecutorFactory;
    }

    public HttpFactory getHttpFactory() {
        return mHttpFactory;
    }

    public static class Builder<T extends Mission> {

        private final String mKey;
        private Dispatcher<T> mDispatcher = new MissionDispatcher<>();
        private Initializer<T> mInitializer = new MissionInitializer<>();
        private Notifier<? super T> mNotifier;
        private Transfer<T> mTransfer = new MissionBlockTransfer<>();
        private Repository<T> mRepository;
        private BlockSplitter<T> mBlockSplitter = new MissionBlockSplitter<>();
        private ExecutorFactory<T> mExecutorFactory = new MissionExecutorFactory<>();
//        private final MissionFactory<T> mMissionFactory;
        private HttpFactory mHttpFactory = new UrlConnectionHttpFactory();

        public Builder(@NonNull final String key) {
            this.mKey = key;
            mRepository = new MissionRepository<>(new MissionDatabaseFactory() {
                @Override
                public MissionDatabase createDatabase() {
                    return MissionDatabase.get(key);
                }
            });
        }

        public Builder<T> setDispatcher(Dispatcher<T> dispatcher) {
            this.mDispatcher = dispatcher;
            return this;
        }

        public Builder<T> setInitializer(Initializer<T> initializer) {
            this.mInitializer = initializer;
            return this;
        }

        public Builder<T> setNotifier(Notifier<? super T> notifier) {
            this.mNotifier = notifier;
            return this;
        }

        public Builder<T> setTransfer(Transfer<T> transfer) {
            this.mTransfer = transfer;
            return this;
        }

        public Builder<T> setRepository(Repository<T> repository) {
            this.mRepository = repository;
            return this;
        }

        public Builder<T> setBlockSplitter(BlockSplitter<T> blockSplitter) {
            this.mBlockSplitter = blockSplitter;
            return this;
        }

        public Builder<T> setExecutorFactory(ExecutorFactory<T> executorFactory) {
            this.mExecutorFactory = executorFactory;
            return this;
        }

        public Builder<T> setHttpFactory(HttpFactory httpFactory) {
            this.mHttpFactory = httpFactory;
            return this;
        }

        public DownloaderConfig<T> build() {
            return new DownloaderConfig<>(this);
        }

    }

}
