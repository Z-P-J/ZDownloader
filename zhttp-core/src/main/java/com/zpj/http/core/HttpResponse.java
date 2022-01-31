package com.zpj.http.core;

import android.util.Log;

import com.zpj.http.ZHttp;
import com.zpj.http.constant.Defaults;
import com.zpj.http.parser.StringParser;
import com.zpj.http.utils.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

public abstract class HttpResponse implements IHttp.Response {

    protected final IHttp.Connection connection;
    protected final HttpConfig config;

    private final String charset;

    protected HttpResponse(IHttp.Connection connection) {
        this.connection = connection;
        this.config = connection.config();
        this.charset = CharsetUtil.getCharsetFromContentType(contentType());
    }

    @Override
    public HttpConfig config() {
        return config;
    }

    @Override
    public InputStream bodyStream() throws IOException {
        return connection.getBodyStream();
    }

    @Override
    public <T> T parse(IHttp.Parser parser) throws IOException {
        if (parser == null) {
            return null;
        }
        return (T) parser.parse(this, null);
    }

    @Override
    public <T> T parse(Type type) throws IOException {
        IHttp.Parser parser = ZHttp.config().parserFactory().create(this, type);
        if (parser == null) {
            return null;
        }
        return (T) parser.parse(this, type);
    }

    @Override
    public <T> T parse(Class<T> clazz) throws IOException {
        Log.d("Test", "test contentType=" + contentType());
        IHttp.Parser parser = ZHttp.config().parserFactory().create(this, clazz);
        if (parser == null) {
            return null;
        }
        return clazz.cast(parser.parse(this, clazz));
    }

    @Override
    public String bodyString() throws IOException {
        return new StringParser().parse(this, String.class);
    }

    @Override
    public void close() {
        connection.disconnect();
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
        return connection.getHeaders();
    }

    @Override
    public Map<String, String> cookies() {
        return config.cookies;
    }

    @Override
    public String cookieStr() {
        return config.cookieStr();
    }

    @Override
    public String cookie(String key) {
        return config.getCookie(key);
    }

    @Override
    public IHttp.Method method() {
        return this.config.method();
    }

    @Override
    public int statusCode() {
        return connection.getStatusCode();
    }

    @Override
    public String statusMessage() {
        return connection.getStatusMessage();
    }

    @Override
    public String charset() {
        if (charset == null) {
            return Defaults.CHARSET;
        }
        return charset;
    }

    @Override
    public String contentType() {
        return connection.getContentType();
    }

    @Override
    public long contentLength() {
        return connection.getContentLength();
    }

}
