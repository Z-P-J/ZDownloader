package com.zpj.downloader;

import android.text.TextUtils;

import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.utils.ssl.SSLContextUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

class HttpUrlConnectionFactory {


    static HttpURLConnection getConnection(BaseMission<?> mission, long start, long end) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        wrapConnection(conn, mission);
        conn.setRequestProperty(HttpHeader.RANGE, "bytes=" + start + "-" + end);
        return conn;
    }

    static HttpURLConnection getFileInfo(BaseMission<?> mission) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        wrapConnection(conn, mission);
        conn.setRequestProperty(HttpHeader.RANGE, "bytes=0-");
        return conn;
    }

    static HttpURLConnection getConnection(BaseMission<?> mission) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        wrapConnection(conn, mission);
        return conn;
    }

    static HttpURLConnection getConnection(BaseMission<?> mission, long position) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        wrapConnection(conn, mission);
        if (!mission.fallback) {
            long start = position * mission.getBlockSize();
            long end = start + mission.getBlockSize() - 1;

            if (start >= mission.getLength()) {
                return null;
            }

            if (end >= mission.getLength()) {
                end = mission.getLength() - 1;
            }
            conn.setRequestProperty(HttpHeader.RANGE, "bytes=" + start + "-" + end);
        }
        return conn;
    }

    private static void wrapConnection(HttpURLConnection conn, BaseMission<?> mission) {
        if (conn instanceof HttpsURLConnection) {
            SSLContext sslContext =
                    SSLContextUtil.getSSLContext(DownloadManagerImpl.getInstance().getContext(), SSLContextUtil.CA_ALIAS, SSLContextUtil.CA_PATH);
            if (sslContext == null) {
                sslContext = SSLContextUtil.getDefaultSLLContext();
            }
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            ((HttpsURLConnection) conn).setSSLSocketFactory(ssf);
            ((HttpsURLConnection) conn).setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
        }
        conn.setConnectTimeout(mission.getConnectOutTime());
        conn.setReadTimeout(mission.getReadOutTime());
        if (!TextUtils.isEmpty(mission.getCookie().trim())) {
            conn.setRequestProperty(HttpHeader.COOKIE, mission.getCookie());
        }
        conn.setRequestProperty(HttpHeader.USER_AGENT, mission.getUserAgent());
        conn.setRequestProperty(HttpHeader.REFERER, mission.getUrl());
        conn.setConnectTimeout(mission.getConnectOutTime());
        conn.setReadTimeout(mission.getReadOutTime());
        Map<String, String> headers = mission.getHeaders();
        if (!headers.isEmpty()) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

}
