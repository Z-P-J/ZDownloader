package com.zpj.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.ErrorCode;
import com.zpj.utils.FileUtils;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * 下载任务初始化器
 * @author Z-P-J
 */
public class MissionInitializer implements Runnable {

    private static final String TAG = "MissionInitializer";

    private final BaseMission<?> mission;

    public MissionInitializer(BaseMission<?> mission) {
        this.mission = mission;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            connection = HttpUrlConnectionFactory.getFileInfo(mission);
            if (TextUtils.isEmpty(mission.name)) {
                mission.name = getMissionNameFromResponse(connection);
            }
            Log.d("mission.name", "mission.name=" + mission.name);
            int statusCode = connection.getResponseCode();
            int code = statusCode / 100;
            if (code == 3) {
                // 重定向响应码3xx
                String redirectUrl = connection.getHeaderField("location");
                Log.d(TAG, "redirectUrl=" + redirectUrl);
                if (TextUtils.isEmpty(redirectUrl)) {
                    // 重定向链接为空，出错了
                    mission.errCode = statusCode;
                    mission.notifyError(new Error(connection.getResponseMessage()), false);
                    return;
                } else {
                    // 重定向，更新链接，重新发起请求
                    mission.url = redirectUrl;
                    run();
                    return;
                }
            } else if (code == 2) {
                // 成功响应码2xx
                String contentLength = connection.getHeaderField("Content-Length");
                if (contentLength != null) {
                    mission.length = Long.parseLong(contentLength);
                }
                Log.d("mission.length", "mission.length=" + mission.length);
                if (mission.length <= 0) {
                    mission.errCode = ErrorCode.ERROR_SERVER_UNSUPPORTED;
                    mission.notifyError(Error.SERVER_UNSUPPORTED, false);
                    return;
                } else if (mission.length >= FileUtils.getAvailableSize()) {
                    mission.errCode = ErrorCode.ERROR_NO_ENOUGH_SPACE;
                    mission.notifyError(Error.NO_ENOUGH_SPACE, false);
                    return;
                }
            } else {
                // 请求错误响应码4xx和5xx，请求码1xx出现的情况太少，暂时视为错误码。
                mission.errCode = statusCode;
                mission.notifyError(new Error(connection.getResponseMessage()), false);
                return;
            }

            mission.isBlockDownload = (statusCode == HttpURLConnection.HTTP_PARTIAL);

            Log.d("mission.name", "mission.name444=" + mission.name);
            if (TextUtils.isEmpty(mission.name)) {
                Log.d("Initializer", "getMissionNameFromUrl--url=" + mission.url);
                mission.name = mission.generateFileNameFromUrl(mission.url);
            }

            Log.d("mission.name", "mission.name555=" + mission.name);

            if (mission.isBlockDownload) {
                long blocks = mission.length / mission.getBlockSize();
                if (blocks * mission.getBlockSize() < mission.length) {
                    blocks += 1;
                }
                mission.blocks = blocks;
            } else {
                mission.blocks = 1;
            }
            Log.d(TAG, "blocks=" + mission.blocks);

            mission.queue.clear();
            for (long position = 0; position < mission.blocks; position++) {
                Log.d(TAG, "initQueue add position=" + position);
                mission.queue.add(position);
            }

            File loacation = new File(mission.getDownloadPath());
            if (!loacation.exists()) {
                loacation.mkdirs();
            }
            File file = new File(mission.getFilePath());
            if (!file.exists()) {
                file.createNewFile();
            }

            Log.d(TAG, "storage=" + FileUtils.getAvailableSize());
            mission.hasPrepared = true;

            DownloadManagerImpl.getInstance().writeMission(mission);
            mission.start();
        } catch (Exception e) {
            e.printStackTrace();
            mission.notifyError(new Error(e.getMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getMissionNameFromResponse(HttpURLConnection connection) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        Log.d("contentDisposition", "contentDisposition=" + contentDisposition);
        if (contentDisposition != null) {
            String[] dispositions = contentDisposition.split(";");
            for (String disposition : dispositions) {
                Log.d("disposition", "disposition=" + disposition);
                if (disposition.contains("filename=")) {
                    return disposition.replace("filename=", "").trim();
                }
            }
        }
        return "";
    }

}
