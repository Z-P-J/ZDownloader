package com.zpj.downloader.core.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.DownloadManagerImpl;
import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.Serializer;
import com.zpj.downloader.impl.DefaultMissionSerializer;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.utils.ContextUtils;

import java.io.File;


/**
* @author Z-P-J
* */
public class DownloaderConfig extends Config<DownloaderConfig> {

    private static final String MISSIONS_PATH = "missions";

    private transient Class<? extends BaseMission<?>> clazz = DownloadMission.class;

    private transient String taskPath;

    private transient Serializer mSerializer;

    private int concurrentMissionCount = DefaultConstant.CONCURRENT_MISSION_COUNT;

    public DownloaderConfig() {
        super();
    }

    public DownloaderConfig(Config config) {
        super(config);
    }

    static DownloaderConfig with(Context context, Class<? extends BaseMission<?>> clazz) {
        DownloaderConfig options = new DownloaderConfig();
        if (clazz == null) {
            clazz = DownloadMission.class;
        }
        options.clazz = clazz;
        return options;
    }

    public DownloaderConfig setMissionSerializer(Serializer mSerializer) {
        this.mSerializer = mSerializer;
        return this;
    }

    public Serializer getMissionSerializer() {
        return mSerializer;
    }

    public int getConcurrentMissionCount() {
        return concurrentMissionCount;
    }

    public DownloaderConfig setConcurrentMissionCount(int concurrentMissionCount) {
        this.concurrentMissionCount = concurrentMissionCount;
        return this;
    }

    public String getTaskPath() {
        if (TextUtils.isEmpty(taskPath)) {
            File file = new File(ContextUtils.getApplicationContext().getFilesDir(), MISSIONS_PATH);
            taskPath = file.getAbsolutePath();
        } else {
            File file = new File(taskPath);
            if (file.isDirectory()) {
                return taskPath;
            } else {
                taskPath = null;
                return getTaskPath();
            }
        }
        return taskPath;
    }

    public File getTaskFolder() {
        return new File(getTaskPath());
//        if (TextUtils.isEmpty(taskPath)) {
//            File file = new File(getContext().getFilesDir(), MISSIONS_PATH);
//            taskPath = file.getAbsolutePath();
//            return file;
//        }
//        File file = new File(taskPath);
//        if (file.isDirectory()) {
//            return file;
//        }
//        taskPath = null;
//        return getTaskFolder();
    }

    public void init() {
        if (mSerializer == null) {
            mSerializer = new DefaultMissionSerializer();
        }
        File path = new File(ContextUtils.getApplicationContext().getFilesDir(), MISSIONS_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        taskPath = path.getAbsolutePath();
        File file = new File(getDownloadPath());
        if (!file.exists()) {
            file.mkdirs();
        }

        ContextUtils.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                DownloadManagerImpl.register(DownloaderConfig.this, clazz);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

//        DownloadManagerImpl.register(this, clazz);
    }

}
