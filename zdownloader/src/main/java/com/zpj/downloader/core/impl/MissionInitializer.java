package com.zpj.downloader.core.impl;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.ErrorCode;
import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Initializer;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.http.Response;
import com.zpj.downloader.utils.Logger;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载任务初始化
 * @author Z-P-J
 */
public class MissionInitializer<T extends Mission> implements Initializer<T> {

    private static final String TAG = "MissionInitializer";

    @Override
    public Result initMission(Downloader<T> downloader, T mission) {
        Response response = null;
        try {

            Map<String, String> headers = new HashMap<>(mission.getConfig().getHeaders());
            headers.put(HttpHeader.RANGE, "bytes=0-");
            response = downloader.getHttpFactory().request(mission, headers);
            Logger.d(TAG, "response=" + response);


            return handleResponse(downloader, mission, response);
        } catch (Exception e) {
            e.printStackTrace();
//            if (e instanceof SocketException) {
//
//                try {
//                    Map<String, String> headers = new HashMap<>(mission.getConfig().getHeaders());
//                    response = downloader.getHttpFactory().request(mission, headers);
//                    Logger.d(TAG, "response=" + response);
//                    handleResponse(downloader, mission, response);
//                } catch (Exception exception) {
//                    exception.printStackTrace();
//                    return Result.error(exception.getMessage());
//                }
//
//            }

            return Result.error(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private Result handleResponse(Downloader<T> downloader, T mission, Response response) {
        if (TextUtils.isEmpty(mission.getName())) {
            String contentType = response.contentType();
            String contentDisposition = response.header(HttpHeader.Content_Disposition);
            String mimeType = null;
            int i = contentType.indexOf(";");
            if (i > 0) {
                mimeType = contentType.substring(0, i).trim();
            }
            String name = URLUtil.guessFileName(mission.getUrl(), contentDisposition, mimeType);
            mission.setName(name);
        }


        Logger.d(TAG, "mission.name=" + mission.getName());

        if (!mission.isDownloading()) {
            return Result.paused();
        }

        int statusCode = response.statusCode();
        int code = statusCode / 100;
        if (code == 3) {
            // 重定向响应码3xx
            String redirectUrl = response.header(HttpHeader.LOCATION);
            Logger.d(TAG, "redirectUrl=" + redirectUrl);
            if (TextUtils.isEmpty(redirectUrl)) {
                // 重定向链接为空，出错了
                return Result.error(statusCode, response.statusMessage());
            } else {
                // 重定向，更新链接，重新发起请求
                mission.setUrl(redirectUrl);
                return initMission(downloader, mission);
            }
        } else if (code == 2) {
            // 成功响应码2xx
            mission.setLength(response.contentLength());
            Logger.d("mission.length", "mission.length=" + mission.getLength());
            if (mission.getLength() >= getAvailableSize()) {
                return Result.error(ErrorCode.ERROR_NO_ENOUGH_SPACE, Error.NO_ENOUGH_SPACE.getErrorMsg());
            }
        } else {
            // 请求错误响应码4xx和5xx，请求码1xx出现的情况太少，暂时视为错误码。
            return Result.error(statusCode, response.statusMessage());
        }

        mission.setBlockDownload(statusCode == HttpURLConnection.HTTP_PARTIAL);

        Logger.d("mission.name", "mission.name555=" + mission.getName());
        Logger.d(TAG, "storage=" + getAvailableSize());

        return Result.ok(statusCode, response.statusMessage());
    }

    private long getAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        if(sdcard.equals(state)) {
            //获得Sdcard上每个block的size
            long blockSize = statFs.getBlockSizeLong();
            //获取可供程序使用的Block数量
            long blockavailable = statFs.getAvailableBlocksLong();
            //计算标准大小使用：1024，当然使用1000也可以
            return blockSize * blockavailable;
        } else {
            return -1;
        }
    }


}
