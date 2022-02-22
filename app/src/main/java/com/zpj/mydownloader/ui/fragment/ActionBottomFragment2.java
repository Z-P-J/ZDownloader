package com.zpj.mydownloader.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.core.Mission;
import com.zpj.fragmentation.dialog.base.BottomDragDialogFragment;
import com.zpj.mydownloader.R;

public class ActionBottomFragment2 extends BottomDragDialogFragment<ActionBottomFragment2> {

    private Mission mission;

    public ActionBottomFragment2 setMission(Mission mission) {
        this.mission = mission;
        return this;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.layout_dialog_share;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);
        LinearLayout openFile = findViewById(R.id.open_file);
        LinearLayout pauseDownload = findViewById(R.id.pause_download);
        LinearLayout resumeDownload = findViewById(R.id.resume_download);
        LinearLayout deleteTask = findViewById(R.id.delete_task);
        LinearLayout copyLink = findViewById(R.id.copy_link);

        openFile.setVisibility(View.GONE);
        if (mission.isComplete()){
            openFile.setVisibility(View.VISIBLE);
            pauseDownload.setVisibility(View.GONE);
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isDownloading()) {
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isWaiting()) {
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isError()) {
            pauseDownload.setVisibility(View.GONE);
        } else if (mission.isPreparing()) {
            pauseDownload.setVisibility(View.GONE);
            resumeDownload.setVisibility(View.GONE);
        } else if (mission.isPaused()) {
            pauseDownload.setVisibility(View.GONE);
        }

        openFile.setOnClickListener(v -> {
//            mission.openFile();
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