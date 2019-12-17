package com.zpj.mydownloader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.downloader.jsoup.Jsoup;
import com.zpj.downloader.jsoup.connection.Connection;
import com.zpj.zdialog.ZAlertDialog;
import com.zpj.zdialog.ZBottomSheetDialog;
import com.zpj.zdialog.ZDialog;
import com.zpj.zdialog.base.IDialog;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

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
        ZBottomSheetDialog.with(this)
                .setContentView(R.layout.layout_dialog_share)
                .setOnViewCreateListener(new IDialog.OnViewCreateListener() {
                    @Override
                    public void onViewCreate(IDialog dialog, View view) {
                        LinearLayout openFile = view.findViewById(R.id.open_file);
                        LinearLayout pauseDownload = view.findViewById(R.id.pause_download);
                        LinearLayout resumeDownload = view.findViewById(R.id.resume_download);
                        LinearLayout deleteTask = view.findViewById(R.id.delete_task);
                        LinearLayout copyLink = view.findViewById(R.id.copy_link);

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
                            ZDownloader.openFile(mission);
                            dialog.dismiss();
                        });

                        pauseDownload.setOnClickListener(v -> {
                            ZDownloader.pause(mission);
                            holder.lastTimeStamp = -1;
                            holder.lastDone = -1;
                            dialog.dismiss();
                        });

                        resumeDownload.setOnClickListener(v -> {
                            ZDownloader.resume(mission);
                            dialog.dismiss();
                        });

                        deleteTask.setOnClickListener(v -> {
                            ZDownloader.delete(mission);
                            missionAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        });

                        copyLink.setOnClickListener(v -> {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("Label", mission.getUrl());
                            cm.setPrimaryClip(mClipData);
                            dialog.dismiss();
                        });
                    }
                })
                .show();
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
        ZAlertDialog.with(this)
                .setTitle("添加下载任务")
                .setContentView(R.layout.layout_dialog_download)
                .setGravity(Gravity.BOTTOM)
                .setScreenWidthP(1.0f)
                .setSwipable(false)
                .setOnViewCreateListener((dialog, view) -> {
                    final EditText text = view.findViewById(R.id.url);
                    final EditText name = view.findViewById(R.id.file_name);
                    final TextView tCount = view.findViewById(R.id.threads_count);
                    final SeekBar threads = view.findViewById(R.id.threads);

                    threads.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                            tCount.setText(String.valueOf(progress + 1));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar p1) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar p1) {

                        }

                    });

                    int def = 5;
                    threads.setProgress(def - 1);
                    tCount.setText(String.valueOf(def));

                    text.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {

                        }

                        @Override
                        public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {

                            String url = text.getText().toString().trim();

                            if (!TextUtils.isEmpty(url)) {
                                int index = url.lastIndexOf("/");

                                if (index > 0) {
                                    int end = url.lastIndexOf("?");

                                    if (end < index) {
                                        end = url.length();
                                    }

                                    name.setText(url.substring(index + 1, end));
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable txt) {

                        }
                    });
                })
                .setPositiveButton(dialog -> {
                    String url = ((EditText)(dialog.getView(R.id.url))).getText().toString();
                    if (TextUtils.isEmpty(url)) {
                        Toast.makeText(this, "链接为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ZDownloader.download(url);
                    dialog.dismiss();
                })
                .show();
    }

    private void showMenuDialog() {

    }

}
