package com.zpj.mydownloader.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.core.Notifier;
import com.zpj.mydownloader.ui.MainActivity;
import com.zpj.notification.ZNotify;

/**
 * 实现INotificationInterceptor接口，在onProgress、onFinished、onError方法中更新通知
 */
public class DownloadNotificationInterceptor implements Notifier {

    @Override
    public void onProgress(Context context, BaseMission<?> mission, float progress, boolean isPause) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildProgressNotify()
                .setProgressAndFormat(progress, false, "")
                .setContentTitle((isPause ? "已暂停：" : "") + mission.getName())
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onFinished(Context context, BaseMission<?> mission) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle(mission.getName())
                .setContentText("下载已完成")
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onError(Context context, BaseMission<?> mission, int errCode) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle("下载出错" + errCode + ":" + mission.getName())
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onCancel(Context context, BaseMission<?> mission) {
        ZNotify.cancel(mission.getNotifyId());
    }

    @Override
    public void onCancelAll(Context context) {
        ZNotify.cancelAll();
    }

}
