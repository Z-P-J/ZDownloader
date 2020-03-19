package com.zpj.mydownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.mydownloader.widget.ActionBottomPopup;
import com.zpj.mydownloader.widget.AddTaskPopup;

/**
 * @author Z-P-J
 * */
public class MainActivity extends AppCompatActivity implements MissionAdapter.DownloadCallback {

    private MissionAdapter missionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        missionAdapter = new MissionAdapter(MainActivity.this, true);
        missionAdapter.setMissionAdapterClickListener(MainActivity.this);
        recyclerView.setAdapter(missionAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showDownloadDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        ZDownloader.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onNotifyChange() {

    }

    @Override
    public void onDownloadFinished() {

    }

    @Override
    public void onItemClicked(View view, MissionAdapter.ViewHolder holder, DownloadManager mManager) {

    }

    @Override
    public void onItemLongClicked(View view, MissionAdapter.ViewHolder holder, DownloadManager mManager) {
        DownloadMission mission = mManager.getMission(holder.position);
        new ActionBottomPopup(this, mission).show();
//        ZBottomSheetDialog.with(this)
//                .setContentView(R.layout.layout_dialog_share)
//                .setOnViewCreateListener(new IDialog.OnViewCreateListener() {
//                    @Override
//                    public void onViewCreate(IDialog dialog, View view) {
//                        LinearLayout openFile = view.findViewById(R.id.open_file);
//                        LinearLayout pauseDownload = view.findViewById(R.id.pause_download);
//                        LinearLayout resumeDownload = view.findViewById(R.id.resume_download);
//                        LinearLayout deleteTask = view.findViewById(R.id.delete_task);
//                        LinearLayout copyLink = view.findViewById(R.id.copy_link);
//
//                        openFile.setVisibility(View.GONE);
//                        if (mission.isFinished()){
//                            openFile.setVisibility(View.VISIBLE);
//                            pauseDownload.setVisibility(View.GONE);
//                            resumeDownload.setVisibility(View.GONE);
//                        } else if (mission.isRunning()) {
//                            resumeDownload.setVisibility(View.GONE);
//                        } else if (mission.isWaiting()) {
//                            resumeDownload.setVisibility(View.GONE);
//                        } else if (mission.isError()) {
//                            pauseDownload.setVisibility(View.GONE);
//                        } else if (mission.isIniting()) {
//                            pauseDownload.setVisibility(View.GONE);
//                            resumeDownload.setVisibility(View.GONE);
//                        } else if (mission.isPause()) {
//                            pauseDownload.setVisibility(View.GONE);
//                        }
//
//                        openFile.setOnClickListener(v -> {
//                            ZDownloader.openFile(mission);
//                            dialog.dismiss();
//                        });
//
//                        pauseDownload.setOnClickListener(v -> {
//                            ZDownloader.pause(mission);
//                            holder.lastTimeStamp = -1;
//                            holder.lastDone = -1;
//                            dialog.dismiss();
//                        });
//
//                        resumeDownload.setOnClickListener(v -> {
//                            ZDownloader.resume(mission);
//                            dialog.dismiss();
//                        });
//
//                        deleteTask.setOnClickListener(v -> {
//                            ZDownloader.delete(mission);
//                            missionAdapter.notifyDataSetChanged();
//                            dialog.dismiss();
//                        });
//
//                        copyLink.setOnClickListener(v -> {
//                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                            ClipData mClipData = ClipData.newPlainText("Label", mission.getUrl());
//                            cm.setPrimaryClip(mClipData);
//                            dialog.dismiss();
//                        });
//                    }
//                })
//                .show();
    }

    @Override
    public void onMoreClicked(View view, MissionAdapter.ViewHolder holder, DownloadManager mManager) {
        DownloadMission mission = mManager.getMission(holder.position);
        if (mission.isRunning()) {
            Toast.makeText(this, "暂停下载", Toast.LENGTH_SHORT).show();
            mManager.pauseMission(holder.position);
//            mBinder.onMissionRemoved(mission);
            holder.lastTimeStamp = -1;
            holder.lastDone = -1;
        } else {
            Toast.makeText(this, "恢复下载", Toast.LENGTH_SHORT).show();
            mManager.resumeMission(holder.position);
//            mBinder.onMissionAdded(mission);
        }
    }

    private void showDownloadDialog() {
        new AddTaskPopup(this).show();
    }

    private void showMenuDialog() {

    }

}
