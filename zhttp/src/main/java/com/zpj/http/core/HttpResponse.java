package com.zpj.http.core;

import com.zpj.http.utils.DataUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class HttpResponse implements IHttp.Response {

    protected final IHttp.Connection connection;
    protected final HttpConfig config;

    private final String charset;

    private InputStream bodyStream;

    protected HttpResponse(IHttp.Connection connection) {
        this.connection = connection;
        this.config = connection.config();
        this.charset = DataUtil.getCharsetFromContentType(contentType());
    }

    @Override
    public HttpConfig config() {
        return config;
    }

//    @Override
//    public String body() {
//        prepareByteData();
//        // charset gets set from header on execute, and from meta-equiv on parse. parse may not have happened yet
//        String body;
//        if (charset == null)
//            body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
//        else
//            body = Charset.forName(charset).decode(byteData).toString();
//        ((Buffer) byteData).rewind(); // cast to avoid covariant return type change in jdk9
//        return body;
//    }

    @Override
    public InputStream bodyStream() throws IOException {
        return connection.getBodyStream();
    }

    @Override
    public <T> T parse(IHttp.Parser<T> parser) throws IOException {
        return parser.parse(this);
    }

    @Override
    public void close() {
        connection.disconnect();
        if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException e) {
                // no-op
            } finally {
                bodyStream = null;
            }
        }
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
            return DataUtil.defaultCharset;
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
