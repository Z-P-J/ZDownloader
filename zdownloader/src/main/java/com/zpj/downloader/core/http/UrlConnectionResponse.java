package com.zpj.downloader.core.http;

import android.support.annotation.NonNull;

import com.zpj.downloader.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class UrlConnectionResponse implements Response {

    private static final String TAG = "UrlConnectionResponse";

    public static final String CONTENT_LENGTH = "Content-Length";

    @NonNull
    private final HttpURLConnection mConnection;

    private final int statusCode;
    private final String statusMessage;
    private final String contentType;
    private long contentLength;
    private final Map<String, String> headers;

    public UrlConnectionResponse(@NonNull HttpURLConnection conn) throws IOException {
        this.mConnection = conn;

        statusCode = conn.getResponseCode();
        statusMessage = conn.getResponseMessage();
        contentType = conn.getContentType();

        Logger.d(TAG, "statusCode=" + statusCode + " statusMessage=" + statusMessage + " contentType=" + contentType);

        String len = conn.getHeaderField("content-length");
        try {
            contentLength = Long.parseLong(conn.getHeaderField(CONTENT_LENGTH));

            Logger.d(TAG, "contentLength=" + contentLength + " len=" + len + " getContentLength=" + conn.getContentLength());
            if (contentLength < 0) {
                contentLength = conn.getContentLength();
            }
        } catch (Exception ignore) {
            contentLength = conn.getContentLength();
        }
        Logger.d(TAG, "contentLength=" + contentLength + " len=" + len + " getContentLength=" + conn.getContentLength());


        headers = getHeaderMap(conn);
        Logger.d(TAG, "headers=" + headers);

    }

    @Override
    public boolean hasHeader(String name) {
        return headers().containsKey(name);
    }

    @Override
    public boolean hasHeaderWithValue(String name, String value) {
        return value.equalsIgnoreCase(header(name));
    }

    @Override
    public String header(String name) {
        return headers().get(name);
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String statusMessage() {
        return statusMessage;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public InputStream bodyStream() throws IOException {
        InputStream bodyStream;
        if (contentLength() != 0 && !"HEAD".equalsIgnoreCase(mConnection.getRequestMethod())) { // -1 means unknown, chunked. sun throws an IO exception on 500 response with no content when trying to read body
            bodyStream = mConnection.getInputStream();
            if (hasContentEncoding("gzip")) {
                bodyStream = new GZIPInputStream(bodyStream);
            } else if (hasContentEncoding("deflate")) {
                bodyStream = new InflaterInputStream(bodyStream, new Inflater(true));
            }
        } else {
            bodyStream = mConnection.getErrorStream();
        }
        return bodyStream;
    }

    private boolean hasContentEncoding(String value) {
        return value.equalsIgnoreCase(headers().get("Content-Encoding"));
    }

    @Override
    public void close() {
        mConnection.disconnect();
    }

    private Map<String, String> getHeaderMap(HttpURLConnection conn) {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        int i = 0;
        while (true) {
            final String key = conn.getHeaderFieldKey(i);
            final String val = conn.getHeaderField(i);
            if (key == null && val == null)
                break;
            i++;
            if (key == null || val == null)
                continue; // skip http1.1 line

            headers.put(key, val);
        }
        return headers;
    }

}
