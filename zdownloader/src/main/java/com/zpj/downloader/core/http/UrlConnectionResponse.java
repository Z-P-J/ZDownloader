package com.zpj.downloader.core.http;

import android.support.annotation.NonNull;

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

    @NonNull
    private final HttpURLConnection mConnection;

    private final int statusCode;
    private final String statusMessage;
    private final String contentType;
    private long contentLength = -1;
    private final Map<String, String> headers;

    public UrlConnectionResponse(@NonNull HttpURLConnection conn) throws IOException {
        this.mConnection = conn;

        statusCode = conn.getResponseCode();
        statusMessage = conn.getResponseMessage();
        contentType = conn.getContentType();

        try {
            contentLength = Long.parseLong(conn.getHeaderField(HttpHeader.CONTENT_LENGTH));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (contentLength < 0) {
            contentLength = conn.getContentLength();
        }

        headers = getHeaderMap(conn);
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
        return value.equalsIgnoreCase(headers().get(HttpHeader.CONTENT_ENCODING));
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

    @Override
    public String toString() {
        return "UrlConnectionResponse{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                ", headers=" + headers +
                '}';
    }
}
