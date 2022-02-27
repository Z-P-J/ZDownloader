package com.zpj.mydownloader.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.impl.DownloadMission;
import com.zpj.fragmentation.dialog.base.BottomDragDialogFragment;
import com.zpj.mydownloader.R;

public class AddTaskFragment extends BottomDragDialogFragment<AddTaskFragment> implements View.OnClickListener {

    private EditText text;
    private EditText name;
    private EditText etBufferSize;

    @Override
    protected int getContentLayoutId() {
        return R.layout.popup_add_task;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);

        text = findViewById(R.id.url);
        etBufferSize = findViewById(R.id.et_buffer);
        name = findViewById(R.id.file_name);
        final TextView tCount = findViewById(R.id.threads_count);
        final SeekBar threads = findViewById(R.id.threads);

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

        findViewById(R.id.bt_ok).setOnClickListener(this);

        findViewById(R.id.bt_cancel).setOnClickListener(this);

        etBufferSize.setText("1");
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.bt_ok) {
            String url = text.getText().toString();
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(getContext(), "链接为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String bufferSize = etBufferSize.getText().toString();
            int buffer;
            if (TextUtils.isDigitsOnly(bufferSize)) {
                buffer = Integer.parseInt(bufferSize) * 1024;
            } else {
                buffer = 1024;
            }

//            ZDownloader.download(url, name.getText().toString())
//                    .setBufferSize(buffer)
//                    .setConflictPolicy(new DefaultConflictPolicy() {
//                        @Override
//                        public void onConflict(BaseMission<?> mission, Callback callback) {
//                            ZDialog.alert()
//                                    .setTitle("任务已存在")
//                                    .setContent("下载任务已存在，是否继续下载？")
//                                    .setPositiveButton(new IDialog.OnButtonClickListener<ZDialog.AlertDialogImpl>() {
//                                        @Override
//                                        public void onClick(ZDialog.AlertDialogImpl fragment, int which) {
//                                            callback.onResult(true);
//                                        }
//                                    })
//                                    .setNegativeButton(new IDialog.OnButtonClickListener<ZDialog.AlertDialogImpl>() {
//                                        @Override
//                                        public void onClick(ZDialog.AlertDialogImpl fragment, int which) {
//                                            callback.onResult(false);
//                                        }
//                                    })
//                                    .show(context);
//                        }
//                    })
//                    .start();


            // 创建下载任务
            DownloadMission mission = new Mission.Builder(url, name.getText().toString())
                    // 设置文件保存地址
                    .setDownloadPath("custom download path")
                    // 下载线程（分块下载有效）
                    .setThreadCount(3)
                    // 设置User-Agent
                    .setUserAgent("custom user-agent")
                    // 设置Cookie
                    .setCookie("set cookies")
                    // 添加请求头
                    .addHeader(HttpHeader.REFERER, url)
                    // 设置分块大小
                    .setBlockSize(2 * 1024 * 1024)
                    // 设置缓冲区大小
                    .setBufferSize(64 * 1024)
                    // 设置连接超时时间
                    .setConnectOutTime(20000)
                    // 设置读取超时时间
                    .setReadOutTime(20000)
                    // 设置进度回调频率，单位ms
                    .setProgressInterval(2000)
                    // 设置出错重试次数
                    .setRetryCount(10)
                    // 设置出错重试延迟
                    .setRetryDelayMillis(10000)
                    // 设置是否允许通知栏通知
                    .setEnableNotification(true)
                    // 创建DownloadMission类型的下载任务
                    .build(DownloadMission.class);

            // 任务状态监听回调
            mission.addObserver(new Mission.Observer() {
                @Override
                public void onPrepare() {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onPaused() {

                }

                @Override
                public void onWaiting() {

                }

                @Override
                public void onProgress(Mission mission, float speed) {

                }

                @Override
                public void onFinished() {

                }

                @Override
                public void onError(Error e) {

                }

                @Override
                public void onDelete() {

                }

                @Override
                public void onClear() {

                }
            });

            // 开始下载
            mission.start();


        }
    }
}
