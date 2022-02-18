package com.zpj.downloader.core.impl;

import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.core.HttpFactory;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.http.Response;
import com.zpj.downloader.core.http.UrlConnectionResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpFactoryImpl implements HttpFactory {

    @Override
    public Response request(Mission mission, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = openConnection(mission);

        conn.connect();

        return new UrlConnectionResponse(conn);
    }

    protected HttpURLConnection openConnection(Mission mission) throws IOException {
        URL url = new URL(mission.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        wrapConnection(conn, mission);
        return conn;
    }

    protected void wrapConnection(HttpURLConnection conn, Mission mission) {
        Config config = mission.getConfig();
        conn.setConnectTimeout(config.getConnectOutTime());
        conn.setReadTimeout(config.getReadOutTime());
        conn.setRequestProperty("Referer", mission.getUrl());
        Map<String, String> headers = config.getHeaders();
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

}
