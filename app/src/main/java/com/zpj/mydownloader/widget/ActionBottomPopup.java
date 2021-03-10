package com.zpj.mydownloader.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.DownloadMission;
import com.zpj.mydownloader.R;
import com.zpj.popup.core.BottomPopupView;

public class ActionBottomPopup extends BottomPopupView {

    private final BaseMission<?> mission;

    public ActionBottomPopup(@NonNull Context context, BaseMission<?> mission) {
        super(context);
        this.mission = mission;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.layout_dialog_share;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        LinearLayout openFile = findViewById(R.id.open_file);
        LinearLayout pauseDownload = findViewById(R.id.pause_download);
        LinearLayout resumeDownload = findViewById(R.id.resume_download);
        LinearLayout deleteTask = findViewById(R.id.delete_task);
        LinearLayout copyLink = findViewById(R.id.copy_link);

        openFile.setVisibility(View.GONE);
        if (mission.isFinished()){
            openFile.setVisibility(View.VISIBLE);
            pauseDownload.setVisibility(View.GONE);
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isRunning()) {
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isWaiting()) {
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isError()) {
            pauseDownload.setVisibility(View.GONE);
        } else if (mission.isIniting()) {
            pauseDownload.setVisibility(View.GONE);
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isPause()) {
            pauseDownload.setVisibility(View.GONE);
        }

        openFile.setOnClickListener(v -> {
            mission.openFile();
            dismiss();
        });

        pauseDownload.setOnClickListener(v -> {
            mission.pause();
            dismiss();
        });

        resumeDownload.setOnClickListener(v -> {
            mission.start();
            dismiss();
        });

        deleteTask.setOnClickListener(v -> {
            mission.delete();
            dismiss();
        });

        copyLink.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", mission.getUrl());
            cm.setPrimaryClip(mClipData);
            dismiss();
        });

    }

}
