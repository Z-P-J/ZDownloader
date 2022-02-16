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

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.impl.DownloadMission;
import com.zpj.downloader.impl.DefaultConflictPolicy;
import com.zpj.downloader.ZDownloader;
import com.zpj.fragmentation.dialog.IDialog;
import com.zpj.fragmentation.dialog.ZDialog;
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


            Mission mission = new Mission.Builder(url, name.getText().toString())
                    .build(DownloadMission.class);
            mission.start();


        }
    }
}
