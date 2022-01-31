package com.zpj.downloader.core;

import android.content.Context;

import com.zpj.downloader.BaseMission;

/**
 * 通知拦截器接口
 * @author Z-P-J
 */
public interface Notifier<T> {

    void onProgress(Context context, T mission, float progress, boolean isPause);

    void onFinished(Context context, T mission);

    void onError(Context context, T mission, int errCode);

    void onCancel(Context context, T mission);

    void onCancelAll(Context context);

}
