package com.zpj.downloader.core;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * 下载mission管理
 * @param <T> 下载任务类型
 * @author Z-P-J
 */
public interface MissionManager<T extends Mission> {

    /**
     * 获取所有下载任务
     * @return
     */
    List<T> getMissions();

    /**
     * 注册监听
     * @param observer
     */
    void register(@NonNull Observer<T> observer);

    /**
     * 加载所有下载任务
     */
    void loadMissions();

    /**
     * 销毁管理器
     */
    void destroy();

    /**
     * 下载任务监听
     * @param <T> 下载任务类型
     */
    interface Observer<T extends Mission> {

        void onMissionLoaded(List<T> missions);

        void onMissionAdd(T mission, int position);

        void onMissionDelete(T mission, int position);

        void onMissionFinished(T mission, int position);
    }

}
