package com.zpj.mydownloader.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.config.MissionConfig;
import com.zpj.mydownloader.R;
import com.zpj.popup.core.BottomPopupView;

public class AddTaskPopup extends BottomPopupView implements View.OnClickListener {

    private EditText text;
    private EditText etBufferSize;

    public AddTaskPopup(Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_add_task;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        text = findViewById(R.id.url);
        etBufferSize = findViewById(R.id.et_buffer);
        final EditText name = findViewById(R.id.file_name);
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
            MissionConfig config = MissionConfig.with();
            String bufferSize = etBufferSize.getText().toString();
            if (TextUtils.isDigitsOnly(bufferSize)) {
                config.setBufferSize(Integer.parseInt(bufferSize) * 1024);
            } else {
                config.setBufferSize(1024);
            }
            ZDownloader.download(url, config);
        }
    }
}
