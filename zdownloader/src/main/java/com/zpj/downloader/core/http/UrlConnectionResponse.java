package com.zpj.downloader.core.http;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    private final int mStatusCode;
    private final String mStatusMessage;
    private final String mContentType;
    private long mContentLength = -1;
    private final Map<String, String> mHeaders;

    public UrlConnectionResponse(@NonNull HttpURLConnection conn) throws IOException {
        this.mConnection = conn;

        mStatusCode = conn.getResponseCode();
        mStatusMessage = conn.getResponseMessage();
        mContentType = conn.getContentType();

        mHeaders = getHeaderMap(conn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mContentLength = conn.getContentLengthLong();
        } else {
            try {
                mContentLength = Long.parseLong(header(HttpHeader.CONTENT_LENGTH));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mContentLength < 0) {
            mContentLength = conn.getContentLength();
        }
    }

    @Override
    public String header(String name) {
        Map<String, String> headers = headers();
        if (headers == null) {
            return null;
        }
        return headers.get(name);
    }

    @Override
    public Map<String, String> headers() {
        return mHeaders;
    }

    @Override
    public int statusCode() {
        return mStatusCode;
    }

    @Override
    public String statusMessage() {
        return mStatusMessage;
    }

    @Override
    public String contentType() {
        return mContentType;
    }

    @Override
    public long contentLength() {
        return mContentLength;
    }

    @Override
    public InputStream bodyStream() throws IOException {
        InputStream bodyStream = mConnection.getErrorStream();
        if (bodyStream == null) {
            bodyStream = mConnection.getInputStream();
            if (hasContentEncoding("gzip")) {
                bodyStream = new GZIPInputStream(bodyStream);
            } else if (hasContentEncoding("deflate")) {
                bodyStream = new InflaterInputStream(bodyStream, new Inflater(true));
            }
        }
        return bodyStream;
    }

    private boolean hasContentEncoding(String value) {
        return value.equalsIgnoreCase(header(HttpHeader.CONTENT_ENCODING));
    }

    @Override
    public void close() {
        mConnection.disconnect();
    }

    private Map<String, String> getHeaderMap(HttpURLConnection conn) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String key : conn.getHeaderFields().keySet()) {
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            String value = conn.getHeaderField(key);
            headers.put(key, value);
        }
        return headers;
    }

    @Override
    public String toString() {
        return "UrlConnectionResponse{" +
                "statusCode=" + mStatusCode +
                ", statusMessage='" + mStatusMessage + '\'' +
                ", contentType='" + mContentType + '\'' +
                ", contentLength=" + mContentLength +
                ", headers=" + mHeaders +
                '}';
    }
}
