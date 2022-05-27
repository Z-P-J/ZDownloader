package com.zpj.downloader.core.http;

import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.model.Config;
import com.zpj.downloader.utils.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Map;

public class UrlConnectionHttpFactory implements HttpFactory {

    private static final String TAG = "UrlConnectionHttpFactory";

    @Override
    public Response request(Mission mission, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(mission.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            wrapConnection(conn, mission, headers);

            Logger.d(TAG, "getRequestProperties=" + conn.getRequestProperties());

            conn.connect();

            return new UrlConnectionResponse(conn);
        } catch (SocketException e) {
            if (!mission.isBlockDownload() && headers.containsKey(HttpHeader.RANGE)) {
                if (conn != null) {
                    conn.disconnect();
                }
                e.printStackTrace();
                headers.remove(HttpHeader.RANGE);
                return request(mission, headers);
            }
            throw e;
        }
    }

    protected void wrapConnection(HttpURLConnection conn, Mission mission, Map<String, String> headers) {
        Config config = mission.getConfig();
        conn.setConnectTimeout(config.getConnectOutTime());
        conn.setReadTimeout(config.getReadOutTime());
//        conn.setRequestProperty("Referer", mission.getUrl());
        conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
//        conn.setRequestProperty(HttpHeader.ACCEPT_ENCODING, "identity");
//        conn.setRequestProperty(HttpHeader.PRAGMA, "no-cache");
//        conn.setRequestProperty(HttpHeader.CACHE_CONTROL, "no-cache");
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
    }

}
