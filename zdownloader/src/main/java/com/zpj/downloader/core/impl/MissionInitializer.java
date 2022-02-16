package com.zpj.downloader.core.impl;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.ErrorCode;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Initializer;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Result;
import com.zpj.http.ZHttp;
import com.zpj.http.core.HttpHeader;
import com.zpj.http.core.IHttp;
import com.zpj.http.utils.CharsetUtil;
import com.zpj.utils.FileUtils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 下载任务初始化
 * @author Z-P-J
 */
public class MissionInitializer<T extends Mission> implements Initializer<T> {

    private static final String TAG = "MissionInitializer";

    @Override
    public Result initMission(Downloader<T> downloader, T mission) {
        IHttp.Response response = null;
        try {

            Map<String, String> headers = new HashMap<>(mission.getConfig().getHeaders());
            headers.put(HttpHeader.RANGE, "bytes=0-");
            response = downloader.getHttpFactory().request(mission.getUrl(), headers);

            String contentType = response.contentType();
            String contentDisposition = response.header("Content-Disposition");
            String mimeType = null;
            int i = contentType.indexOf(";");
            if (i > 0) {
                mimeType = contentType.substring(0, i).trim();
            }
            String name = URLUtil.guessFileName(mission.getUrl(), contentDisposition, mimeType);
            mission.setName(name);

//            mission.setName(getFileNameFromResponse(response));

            Log.d("mission.name", "mission.name=" + mission.getName());
            int statusCode = response.statusCode();
            int code = statusCode / 100;
            if (code == 3) {
                // 重定向响应码3xx
                String redirectUrl = response.header(HttpHeader.LOCATION);
                Log.d(TAG, "redirectUrl=" + redirectUrl);
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
                Log.d("mission.length", "mission.length=" + mission.getLength());
                if (mission.getLength() <= 0) {
                    return Result.error(ErrorCode.ERROR_SERVER_UNSUPPORTED, Error.SERVER_UNSUPPORTED.getErrorMsg());
                } else if (mission.getLength() >= FileUtils.getAvailableSize()) {
                    return Result.error(ErrorCode.ERROR_NO_ENOUGH_SPACE, Error.NO_ENOUGH_SPACE.getErrorMsg());
                }
            } else {
                // 请求错误响应码4xx和5xx，请求码1xx出现的情况太少，暂时视为错误码。
                return Result.error(statusCode, response.statusMessage());
            }

            mission.setSupportSlice(statusCode == HttpURLConnection.HTTP_PARTIAL);

//            Log.d("mission.name", "mission.name444=" + mission.getName());
//            if (TextUtils.isEmpty(mission.getName())) {
//                Log.d("Initializer", "getMissionNameFromUrl--url=" + mission.getUrl());
//                mission.setName(generateFileNameFromUrl(mission, mission.getUrl()));
//            }

            Log.d("mission.name", "mission.name555=" + mission.getName());
            Log.d(TAG, "storage=" + FileUtils.getAvailableSize());

            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(-1, e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String getFileNameFromResponse(IHttp.Response resp) throws UnsupportedEncodingException {
        String contentDisposition = resp.header("Content-Disposition");
        String regex = "filename[^;\\n=]*=((['\"]).*?\\2|[^;\\n]*)";
        Matcher matcher = Pattern.compile(regex).matcher(contentDisposition);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (name.contains("\"")) {
                String[] splits = name.split("\"");
                if (splits.length == 2) {
                    String charset = splits[0];
                    charset = CharsetUtil.validateCharset(charset);
                    if (charset == null) {
                        charset = resp.charset();
                    }
                    return URLDecoder.decode(splits[1], charset);
                }
            }
        }
        return resp.config().url().getFile();
    }

    protected String generateFileNameFromUrl(Mission mission, String url) throws MalformedURLException {
        Log.d("getMissionNameFromUrl", "1");
        if (!TextUtils.isEmpty(url)) {

            URL u = new URL(url);
            String name = u.getFile();
            if (name != null) {
                return name;
            }

            int index = url.lastIndexOf("/");

            if (index > 0) {
                int end = url.lastIndexOf("?");
                if (end < index) {
                    end = url.length();
                }
                name = url.substring(index + 1, end);
                Log.d("getMissionNameFromUrl", "2");
                String originUrl = mission.getOriginUrl();
                if (!TextUtils.isEmpty(originUrl) && !TextUtils.equals(url, originUrl)) {
                    String originName = generateFileNameFromUrl(mission, originUrl);
                    Log.d("getMissionNameFromUrl", "3");
                    if (FileUtils.getFileType(originName) != FileUtils.FileType.UNKNOWN) {
                        Log.d("getMissionNameFromUrl", "4");
                        return originName;
                    }
                }

                if (FileUtils.getFileType(name) != FileUtils.FileType.UNKNOWN || name.contains(".")) {
                    Log.d("getMissionNameFromUrl", "5");
                    return name;
                } else {
                    Log.d("getMissionNameFromUrl", "6");
                    return name + ".ext";
                }
            }
        }
        Log.d("getMissionNameFromUrl", "7");
        return "Unknown.ext";
    }

}
