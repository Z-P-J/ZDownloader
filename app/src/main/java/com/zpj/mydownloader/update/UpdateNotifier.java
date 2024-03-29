package com.zpj.mydownloader.update;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Notifier;
import com.zpj.mydownloader.ui.MainActivity;
import com.zpj.notification.ZNotify;

/**
 * 实现Notifier接口，在onProgress、onFinished、onError方法中更新通知
 */
public class UpdateNotifier implements Notifier<Mission> {

    @Override
    public void onProgress(Context context, Mission mission, float progress, boolean isPause) {
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
    public void onFinished(Context context, Mission mission) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle(mission.getName())
                .setContentText("下载已完成，点击安装")
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onError(Context context, Mission mission, int errCode) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle(String.format("应用更新失败！code=%s, msg=%s", errCode, mission.getErrorMessage()))
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onCancel(Context context, Mission mission) {
        ZNotify.cancel(mission.getNotifyId());
    }

    @Override
    public void onCancelAll(Context context) {
        ZNotify.cancelAll();
    }

}
