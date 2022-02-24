package com.zpj.downloader.core;

import android.content.Context;

/**
 * 通知拦截器接口
 * @author Z-P-J
 */
public interface Notifier<T> {

    void onProgress(Context applicationContext, T mission, float progress, boolean isPause);

    void onFinished(Context applicationContext, T mission);

    void onError(Context applicationContext, T mission, int errCode);

    void onCancel(Context applicationContext, T mission);

    void onCancelAll(Context applicationContext);

}
