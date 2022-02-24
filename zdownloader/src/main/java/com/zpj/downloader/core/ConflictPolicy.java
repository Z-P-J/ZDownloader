package com.zpj.downloader.core;

import com.zpj.downloader.core.Mission;

public interface ConflictPolicy {

    /**
     * 判断新下载任务是否冲突
     * @param mission 当前创建的下载任务
     * @param conflictMission 已存在的冲突任务
     * @return
     */
    boolean isConflict(Mission mission, Mission conflictMission);

    /**
     * 对冲突任务进行处理
     * @param mission 下载任务
     * @param callback 回调是否接受冲突该下载任务
     */
    void onConflict(Mission mission, Callback callback);

    interface Callback {
        void onResult(boolean accept);
    }

}
