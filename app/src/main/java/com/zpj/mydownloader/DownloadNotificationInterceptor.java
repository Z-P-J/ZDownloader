package com.zpj.mydownloader;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.DownloadMission;
import com.zpj.downloader.INotificationInterceptor;
import com.zpj.notification.ZNotify;

public class DownloadNotificationInterceptor implements INotificationInterceptor {

    @Override
    public void onProgress(Context context, BaseMission<?> mission, float progress, boolean isPause) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildProgressNotify()
                .setProgressAndFormat(progress, false, "")
                .setContentTitle((isPause ? "已暂停：" : "") + mission.getTaskName())
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
                .setContentTitle(mission.getTaskName())
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
                .setContentTitle("下载出错" + errCode + ":" + mission.getTaskName())
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
