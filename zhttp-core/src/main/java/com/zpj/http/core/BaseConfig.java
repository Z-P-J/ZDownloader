package com.zpj.http.core;

import android.text.TextUtils;

import com.zpj.http.constant.Defaults;
import com.zpj.http.utils.Validate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author Z-P-J
 * */
public abstract class BaseConfig<T extends BaseConfig<T>> {

    /**
     * baseUrl
     */
    URI baseUrl;

    /**
     * debug模式
     */
    boolean debug;

    /**
     * 下载缓冲大小
     * */
    int bufferSize = Defaults.BUFFER_SIZE;

    boolean ignoreHttpErrors = false;

    boolean ignoreContentType = false;

    String postDataCharset = Defaults.CHARSET;



    /**
     * 出错重试次数
     * */
    int retryCount = Defaults.RETRY_COUNT;

    /**
     * 出错重试延迟时间（单位ms）
     * */
    int retryDelay = Defaults.RETRY_DELAY;

    /**
     * 连接超时
     * */
    int connectTimeout = Defaults.CONNECT_OUT_TIME;

    /**
     * 读取超时
     * */
    int readTimeout = Defaults.READ_OUT_TIME;

    /**
     * 默认cookie值
     * */
    final Map<String, String> cookies = new HashMap<>();

    boolean allowAllSSL = false;

    final Map<String, String> headers = new HashMap<>();

    Proxy proxy;

    private SSLSocketFactory sslSocketFactory;

    int maxRedirectCount = Defaults.MAX_REDIRECTS;

    IHttp.OnRedirectListener onRedirectListener;

    private IHttp.CookieJar cookieJar;

    private IHttp.HttpDispatcher httpDispatcher;

    private IHttp.HttpEngine httpEngine;

    protected BaseConfig() {
        headers.put(HttpHeader.USER_AGENT, Defaults.USER_AGENT);
    }


    //-----------------------------------------------------------getter-------------------------------------------------------------


    public URI baseUrl() {
        return baseUrl;
    }

    public boolean debug() {
        return debug;
    }

    public int bufferSize() {
        return bufferSize;
    }

    public String postDataCharset() {
        return postDataCharset;
    }

    public boolean ignoreHttpErrors() {
        return ignoreHttpErrors;
    }

    public boolean ignoreContentType() {
        return ignoreContentType;
    }

    public String userAgent() {
        return headers.get(HttpHeader.USER_AGENT);
    }

    public int retryCount() {
        return retryCount;
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }

    public boolean hasCookie(String name) {
        return cookies.containsKey(name);
    }

    public String removeCookie(String name) {
        return cookies.remove(name);
    }

    public Map<String, String> cookies() {
        return cookies;
    }

    public String cookieStr() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            if (!first)
                sb.append("; ");
            else
                first = false;
            sb.append(cookie.getKey()).append('=').append(cookie.getValue());
        }
        return sb.toString();
    }

    public int retryDelay() {
        return retryDelay;
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public boolean allowAllSSL() {
        return allowAllSSL;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String removeHeader(String key) {
        return headers.remove(key);
    }

    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    public boolean hasHeaderWithValue(String name, String value) {
        return value.equalsIgnoreCase(header(name));
    }

    public String header(String name) {
        return headers.get(name);
    }

    public Proxy proxy() {
        return proxy;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public int maxRedirectCount() {
        return maxRedirectCount;
    }

    public IHttp.OnRedirectListener getOnRedirectListener() {
        return onRedirectListener;
    }

    public IHttp.CookieJar cookieJar() {
        return cookieJar;
    }

    public IHttp.HttpDispatcher httpDispatcher() {
        if (httpDispatcher == null) {
            httpDispatcher = new HttpDispatcher();
        }
        return httpDispatcher;
    }

    public IHttp.HttpEngine httpFactory() {
        Validate.notNull(httpEngine, "HttpFactory must not be null");
        return httpEngine;
    }

    //-----------------------------------------------------------------setter------------------------------------------------------


    public T baseUrl(String baseUrl) {
        try {
            this.baseUrl = new URI(baseUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            this.baseUrl = null;
        }
        return (T) this;
    }

//    public T baseUrl(URL baseUrl) {
//        try {
//            this.baseUrl = baseUrl.toURI();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        return (T) this;
//    }
//
    public T baseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
        return (T) this;
    }

    public T debug(boolean debug) {
        this.debug = debug;
        return (T) this;
    }

    public T userAgent(String userAgent) {
        if (!TextUtils.isEmpty(userAgent)) {
            headers.put(HttpHeader.USER_AGENT, userAgent);
        }
        return (T) this;
    }

    public T referer(String referer) {
        if (!TextUtils.isEmpty(referer)) {
            headers.put(HttpHeader.REFERER, referer);
        }
        return (T) this;
    }

    public T contentType(String contentType) {
        if (!TextUtils.isEmpty(contentType)) {
            headers.put(HttpHeader.CONTENT_TYPE, contentType);
        }
        return (T) this;
    }

    public T acceptLanguage(String acceptLanguage) {
        if (!TextUtils.isEmpty(acceptLanguage)) {
            headers.put(HttpHeader.ACCEPT_LANGUAGE, acceptLanguage);
        }
        return (T) this;
    }

    public T host(String host) {
        if (!TextUtils.isEmpty(host)) {
            headers.put(HttpHeader.HOST, host);
        }
        return (T) this;
    }

    public T accept(String accept) {
        if (!TextUtils.isEmpty(accept)) {
            headers.put(HttpHeader.ACCEPT, accept);
        }
        return (T) this;
    }

    public T acceptEncoding(String acceptEncoding) {
        if (!TextUtils.isEmpty(acceptEncoding)) {
            headers.put(HttpHeader.ACCEPT_ENCODING, acceptEncoding);
        }
        return (T) this;
    }

    public T connectionMethod(String connection) {
        if (!TextUtils.isEmpty(connection)) {
            headers.put(HttpHeader.CONNECTION, connection);
        }
        return (T) this;
    }

    public T acceptCharset(String acceptCharset) {
        if (!TextUtils.isEmpty(acceptCharset)) {
            headers.put(HttpHeader.ACCEPT_CHARSET, acceptCharset);
        }
        return (T) this;
    }

    public T bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return (T) this;
    }

    public T postDataCharset(String postDataCharset) {
        this.postDataCharset = postDataCharset;
        return (T) this;
    }

    public T ignoreHttpErrors(boolean ignoreHttpErrors) {
        this.ignoreHttpErrors = ignoreHttpErrors;
        return (T) this;
    }

    public T ignoreContentType(boolean ignoreContentType) {
        this.ignoreContentType = ignoreContentType;
        return (T) this;
    }

    public T retryCount(int retryCount) {
        this.retryCount = retryCount;
        return (T) this;
    }

    public T cookie(String name, String value) {
        if (!TextUtils.isEmpty(name)) {
            cookies.put(name, value);
        }
        return (T) this;
    }

    public T cookie(String cookie) {
        if (!TextUtils.isEmpty(cookie)) {
            String[] arr = cookie.split(";");
            for (String str : arr) {
                int index = str.indexOf("=");
                if (index < 0) {
                    continue;
                }
                String cookieName = str.substring(0, index).trim();
                if (cookieName.isEmpty()) {
                    continue;
                }
                String cookieVal = str.substring(index + 1).trim();
                cookie(cookieName, cookieVal);
            }
        }
        return (T) this;
    }

    public T cookies(Map<String, String> cookies) {
        if (cookies != null) {
            this.cookies.putAll(cookies);
        }
        return (T) this;
    }

    public T retryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
        return (T) this;
    }

    public T connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return (T) this;
    }

    public T readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return (T) this;
    }

    public T allowAllSSL(boolean allowAllSSL) {
        this.allowAllSSL = allowAllSSL;
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return (T) this;
    }

    public T header(String key, String value) {
        this.headers.put(key, value);
        return (T) this;
    }

    public T proxy(Proxy proxy) {
        this.proxy = proxy;
        return (T) this;
    }

    public T proxy(String host, int port) {
        this.proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
        return (T) this;
    }

    public T sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return (T) this;
    }

    public T maxRedirectCount(int maxRedirectCount) {
        if (maxRedirectCount > 0) {
            this.maxRedirectCount = maxRedirectCount;
        }
        return (T) this;
    }

    public T onRedirect(IHttp.OnRedirectListener onRedirectListener) {
        this.onRedirectListener = onRedirectListener;
        return (T) this;
    }

    public T cookieJar(IHttp.CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return (T) this;
    }

    public T httpDispatcher(IHttp.HttpDispatcher httpDispatcher) {
        this.httpDispatcher = httpDispatcher;
        return (T) this;
    }

    public T httpFactory(IHttp.HttpEngine httpEngine) {
        this.httpEngine = httpEngine;
        return (T) this;
    }
}