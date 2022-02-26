package com.zpj.downloader.core.http;

import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.core.http.HttpFactory;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.http.Response;
import com.zpj.downloader.core.http.UrlConnectionResponse;
import com.zpj.downloader.core.impl.Config;
import com.zpj.downloader.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UrlConnectionHttpFactory implements HttpFactory {

    private static final String TAG = "UrlConnectionHttpFactory";

    @Override
    public Response request(Mission mission, Map<String, String> headers) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        wrapConnection(conn, mission, headers);

        Logger.d(TAG, "getRequestProperties=" + conn.getRequestProperties());

        conn.connect();

        return new UrlConnectionResponse(conn);
    }

    protected void wrapConnection(HttpURLConnection conn, Mission mission, Map<String, String> headers) {
        Config config = mission.getConfig();
        conn.setConnectTimeout(config.getConnectOutTime());
        conn.setReadTimeout(config.getReadOutTime());
        conn.setRequestProperty("Referer", mission.getUrl());
        conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
        conn.setRequestProperty(HttpHeader.ACCEPT_ENCODING, "identity");
        conn.setRequestProperty(HttpHeader.PRAGMA, "no-cache");
        conn.setRequestProperty(HttpHeader.CACHE_CONTROL, "no-cache");
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

}
