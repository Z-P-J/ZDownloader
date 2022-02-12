package com.zpj.http.core;

import android.text.TextUtils;

import java.io.InputStream;

public class HttpKeyVal implements IHttp.KeyVal {

    private String key;
    private String value;
    private InputStream stream;
    private String contentType;
    private IHttp.OnStreamWriteListener listener;

    public static HttpKeyVal create(String key, String value) {
        if (TextUtils.isEmpty(key)) { //  || TextUtils.isEmpty(value)
            return null;
        }
        if (value == null) {
            value = "";
        }
        return new HttpKeyVal().key(key).value(value);
    }

    public static HttpKeyVal create(String key, String filename, InputStream stream) {
        return create(key, filename, stream, null);
    }

    public static HttpKeyVal create(String key, String filename, InputStream stream, IHttp.OnStreamWriteListener listener) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(filename) || stream == null) {
            return null;
        }
        return new HttpKeyVal().key(key).value(filename).inputStream(stream).setListener(listener);
    }

    private HttpKeyVal() {}

    @Override
    public HttpKeyVal key(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String key() {
        return key;
    }

    public HttpKeyVal value(String value) {
        this.value = value;
        return this;
    }

    public String value() {
        return value;
    }

    public HttpKeyVal inputStream(InputStream inputStream) {
        this.stream = inputStream;
        return this;
    }

    public InputStream inputStream() {
        return stream;
    }

    public boolean hasInputStream() {
        return stream != null;
    }

    @Override
    public IHttp.KeyVal contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public IHttp.OnStreamWriteListener getListener() {
        return listener;
    }

    @Override
    public HttpKeyVal setListener(IHttp.OnStreamWriteListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

}
