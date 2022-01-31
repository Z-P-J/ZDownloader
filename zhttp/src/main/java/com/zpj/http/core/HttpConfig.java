package com.zpj.http.core;

import android.text.TextUtils;

import com.zpj.http.utils.UrlUtil;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class HttpConfig extends BaseConfig<HttpConfig> {

    private final Collection<IHttp.KeyVal> data = new ArrayList<>();

    protected URL originalUrl;
    protected URL url;
    protected IHttp.Method method = IHttp.Method.GET;
    protected String body = null;

    public URL url() {
        return url;
    }

    public URL getOriginalUrl() {
        return originalUrl;
    }

    public HttpConfig url(String url) {
        try {
            url = url.trim();
            String tempUrl = url.toLowerCase();
            boolean isHttp = tempUrl.startsWith("http://") || tempUrl.startsWith("https://");
            if (!isHttp) {
                if (tempUrl.contains("://")) {
                    throw new MalformedURLException("Only http and https protocols supported!");
                }
                if (baseUrl == null) {
                    throw new MalformedURLException("You must set baseUrl firstly!");
                }
                if (!tempUrl.startsWith("/")) {
                    url = "/" + url;
                }
                url = baseUrl.resolve(url).toString();
            }
            this.url = new URL(UrlUtil.encodeUrl(url));
            if (this.originalUrl == null) {
                this.originalUrl = this.url;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

    public HttpConfig url(URL url) {
//        this.url = url;
//        if (this.originalUrl == null) {
//            this.originalUrl = this.url;
//        }
//        return this;
        return url(url.toString());
    }

    public IHttp.Method method() {
        return method;
    }

    public HttpConfig method(IHttp.Method method) {
        this.method = method;
        return this;
    }

    public HttpConfig range(String range) {
        this.headers.put(HttpHeader.RANGE, range);
        return this;
    }

    public HttpConfig range(long start) {
        this.headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-", start));
        return this;
    }

    public HttpConfig range(long start, long end) {
        this.headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-%d", start, end));
        return this;
    }

    public HttpConfig requestBody(String body) {
        this.body = body;
        return this;
    }

    public String requestBody() {
        return body;
    }

    public HttpConfig data(IHttp.KeyVal keyval) {
        if (keyval != null) {
            data.add(keyval);
        }
        return this;
    }

    public Collection<IHttp.KeyVal> data() {
        return data;
    }

    public HttpConfig data(String key, String value) {
        return data(HttpKeyVal.create(key, value));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream) {
        return data(HttpKeyVal.create(key, filename, inputStream));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream, IHttp.OnStreamWriteListener listener) {
        return data(HttpKeyVal.create(key, filename, inputStream, listener));
    }

    public HttpConfig data(String key, String filename, InputStream inputStream, String contentType) {
        return data(HttpKeyVal.create(key, filename, inputStream).contentType(contentType));
    }

    public HttpConfig data(Map<String, String> data) {
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                data(HttpKeyVal.create(entry.getKey(), entry.getValue()));
            }
        }
        return this;
    }

    public HttpConfig data(Collection<IHttp.KeyVal> data) {
        if (data != null) {
            for (IHttp.KeyVal entry : data) {
                data(entry);
            }
        }
        return this;
    }

    public IHttp.KeyVal data(String key) {
        if (!TextUtils.isEmpty(key)) {
            for (IHttp.KeyVal keyVal : data()) {
                if (keyVal.key().equals(key))
                    return keyVal;
            }
        }
        return null;
    }

    public boolean needsMultipart() {
        // multipart mode, for files. add the header if we see something with an inputstream, and return a non-null boundary
        for (IHttp.KeyVal keyVal : data()) {
            if (keyVal.hasInputStream())
                return true;
        }
        return false;
    }

    public IHttp.Request request() {
        return httpFactory().createRequest(this);
    }

    public IHttp.Connection connection() {
        return httpFactory().createConnection(request());
    }

    public IHttp.Response execute() throws Exception {
        return connection().execute();
    }

}
