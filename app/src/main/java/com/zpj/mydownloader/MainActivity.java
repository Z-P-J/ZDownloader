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

import com.zpj.qianxundialoglib.IDialog;
import com.zpj.qianxundialoglib.QianxunDialog;
import com.zpj.qxdownloader.QXDownloader;
import com.zpj.qxdownloader.core.DownloadManager;
import com.zpj.qxdownloader.core.DownloadMission;
import com.zpj.qxdownloader.jsoup.Jsoup;
import com.zpj.qxdownloader.jsoup.connection.Connection;
import com.zpj.qxdownloader.service.DownloadManagerService;

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
        QXDownloader.unInit();
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
    public void onItemClicked(View view, MissionAdapter.ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager) {

    }

    @Override
    public void onItemLongClicked(View view, MissionAdapter.ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager) {
        DownloadMission mission = mManager.getMission(holder.position);
        QianxunDialog.with(this)
                //设置dialog布局
                .setDialogView(R.layout.layout_dialog_share)
                //设置动画 默认没有动画
                .setAnimStyle(R.style.slide_anim_style)
                //设置屏幕宽度比例 0.0f-1.0f
                .setScreenWidthP(1.0f)
                //设置Gravity
                .setGravity(Gravity.BOTTOM)
                //设置背景透明度 0.0f-1.0f 1.0f完全不透明
                .setWindowBackgroundP(0.1f)
                //设置是否屏蔽物理返回键 true不屏蔽  false屏蔽
                .setDialogCancelable(true)
                //设置dialog外点击是否可以让dialog消失
                .setCancelableOutSide(true)
                .setBuildChildListener((dialog, view1, layoutRes) -> {
                    LinearLayout openFile = view1.findViewById(R.id.open_file);
                    LinearLayout pauseDownload = view1.findViewById(R.id.pause_download);
                    LinearLayout resumeDownload = view1.findViewById(R.id.resume_download);
                    LinearLayout deleteTask = view1.findViewById(R.id.delete_task);
                    LinearLayout copyLink = view1.findViewById(R.id.copy_link);

//                    switch (mission.missionState) {
//                        case ERROR:
//                            break;
//                        case PAUSE:
//                            break;
//                        case INITING:
//                            break;
//                        case RUNNING:
//                            break;
//                        case WAITING:
//                            break;
//                        case FINISHED:
//                            break;
//                        default:
//                            break;
//                    }

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
                        QXDownloader.openFile(mission);
                        dialog.dismiss();
                    });

                    pauseDownload.setOnClickListener(v -> {
                        QXDownloader.pause(mission);
                        holder.lastTimeStamp = -1;
                        holder.lastDone = -1;
                        dialog.dismiss();
                    });

                    resumeDownload.setOnClickListener(v -> {
                        QXDownloader.resume(mission);
                        dialog.dismiss();
                    });

                    deleteTask.setOnClickListener(v -> {
                        QXDownloader.delete(mission);
                        missionAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    });

                    copyLink.setOnClickListener(v -> {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", mission.url);
                        cm.setPrimaryClip(mClipData);
                        dialog.dismiss();
                    });
                })
                .show();
    }

    @Override
    public void onMoreClicked(View view, MissionAdapter.ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager) {
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
        QianxunDialog.with(this)
                //设置dialog布局
                .setDialogView(R.layout.layout_dialog_download)
                //设置动画 默认没有动画
                .setAnimStyle(R.style.slide_anim_style)
                //设置屏幕宽度比例 0.0f-1.0f
                .setScreenWidthP(1.0f)
                //设置Gravity
                .setGravity(Gravity.BOTTOM)
                //设置背景透明度 0.0f-1.0f 1.0f完全不透明
                .setWindowBackgroundP(0.1f)
                //设置是否屏蔽物理返回键 true不屏蔽  false屏蔽
                .setDialogCancelable(true)
                //设置dialog外点击是否可以让dialog消失
                .setCancelableOutSide(true)
                .setBuildChildListener(new IDialog.OnBuildListener() {
                    //设置子View
                    @Override
                    public void onBuildChildView(final IDialog dialog, View view, int layoutRes) {
                        final EditText text = view.findViewById(R.id.url);
                        final EditText name = view.findViewById(R.id.file_name);
                        final TextView tCount = view.findViewById(R.id.threads_count);
                        final SeekBar threads = view.findViewById(R.id.threads);
                        final Button fetch = view.findViewById(R.id.fetch_name);

                        final Button ok = view.findViewById(R.id.download_ok);
                        final Button cancel = view.findViewById(R.id.download_cancel);


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

                        fetch.setOnClickListener(v -> new NameFetcherTask().execute(text, name));


                        cancel.setOnClickListener(v -> dialog.dismiss());

                        ok.setOnClickListener(v -> {
                            //todo
                            QXDownloader.download(text.getText().toString());
                            dialog.dismiss();
                        });
                    }
                }).show();
    }

    private void showMenuDialog() {

    }

    private static class NameFetcherTask extends AsyncTask<View, Void, Object[]> {

        private static final String TAG = "NameFetcherTask";

        private static final String UA = System.getProperty("http.agent");

        @Override
        protected Object[] doInBackground(View[] params) {
            try {

                String link = ((EditText) params[0]).getText().toString();
                Log.d(TAG, "link=" + link);
                Connection.Response response = Jsoup.connect(link)
                        .followRedirects(false)
                        .method(Connection.Method.HEAD)
//                        .proxy(Proxy.NO_PROXY)
                        .userAgent(UA)
//                        .header("Cookie", mission.cookie)
//                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "identity")
						.header("Referer", link)
                        .header("Range", "bytes=0-")
                        .timeout(20000)
                        .ignoreContentType(true)
//                        .ignoreHttpErrors(true)
//                        .validateTLSCertificates(false)
                        .maxBodySize(0)
                        .execute();
                print(response);

                response = Jsoup.connect(link)
                        .method(Connection.Method.GET)
                        .followRedirects(false)
//                        .proxy(Proxy.NO_PROXY)
                        .userAgent(UA)
//                        .header("Cookie", mission.cookie)
//                        .header("Accept", "*/*")
//                        .header("Access-Control-Expose-Headers", "Content-Disposition")
//						.header("Referer","")
//                        .header("Pragma", "no-cache")
                        .header("Accept-Encoding", "identity")
                        .header("Referer", link)
                        .header("Range", "bytes=0-255")
//                        .header("Cache-Control", "no-cache")
                        .timeout(10000)
                        .ignoreContentType(true)
//                        .ignoreHttpErrors(true)
//                        .validateTLSCertificates(false)
                        .maxBodySize(0)
                        .execute();
                print(response);


                response = Jsoup.connect(link)
                        .method(Connection.Method.GET)
                        .followRedirects(false)
                        .proxy(Proxy.NO_PROXY)
                        .userAgent(UA)
//                            .header("Cookie", mission.cookie)
//                        .header("Accept", "*/*")
//                        .header("Access-Control-Expose-Headers", "Content-Disposition")
//							.header("Referer","")
//                        .header("Pragma", "no-cache")
                        .header("Accept-Encoding", "identity")
                        .header("Referer", link)
                        .header("Range", "bytes=1-255")
//                        .header("Cache-Control", "no-cache")
                        .timeout(10000)
                        .ignoreContentType(true)
//                        .ignoreHttpErrors(true)
                        .maxBodySize(0)
                        .execute();
                print(response);














                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("HEAD");
//                conn.addRequestProperty("Range", "bytes=0-");
                conn.addRequestProperty("Access-Control-Expose-Headers", "Content-Disposition");

                Log.d("contentDisposition", "  " + response.statusCode());

                String contentDisposition = response.header("Content-Disposition");
                Log.d("contentDisposition", "contentDisposition=" + contentDisposition);
                if (contentDisposition == null) {
                    return new Object[]{params[1], conn.getURL().toString()};
                }
                String[] dispositions = contentDisposition.split(";");
                for (String disposition : dispositions) {
                    Log.d("disposition", "disposition=" + disposition);
                    if (disposition.contains("filename=")) {
                        return new Object[]{params[1], disposition.replace("filename=", "")};
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object[] result)	{
            super.onPostExecute(result);

            if (result != null) {
                ((EditText) result[0]).setText(result[1].toString());
            }
        }

        private String getMissionNameFromResponse(Connection.Response response) {
            String contentDisposition = response.header("Content-Disposition");
            Log.d("contentDisposition", "contentDisposition=" + contentDisposition);
            if (contentDisposition != null) {
                String[] dispositions = contentDisposition.split(";");
                for (String disposition : dispositions) {
                    Log.d("disposition", "disposition=" + disposition);
                    if (disposition.contains("filename=")) {
                        return disposition.replace("filename=", "");
                    }
                }
            }
            return "";
        }

        private void print(Connection.Response response) {
            Log.d(TAG, "\n------------------start------------------");
            Log.d(TAG, "response.headers()=" + response.headers());
            Log.d(TAG, "statusCode＝" + response.statusCode());
            Log.d(TAG, "mission.length=" +Long.parseLong(response.header("Content-Length")));
            Log.d(TAG, "mission.name111=" + getMissionNameFromResponse(response));
            Log.d(TAG, "-------------------finished-----------------\n");
        }
    }
}
