package com.zpj.http.core;

import android.text.TextUtils;

import com.zpj.http.impl.HttpCookieJarImpl;
import com.zpj.http.impl.HttpFactoryImpl;
import com.zpj.http.utils.DataUtil;
import com.zpj.http.utils.StringUtil;
import com.zpj.http.utils.TokenQueue;

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
    int bufferSize = Configs.BUFFER_SIZE;

    long maxBodySize = Configs.MAX_BODY_SIZE;

    boolean ignoreHttpErrors = false;

    boolean ignoreContentType = false;

    String postDataCharset = DataUtil.defaultCharset;


    /**
     * 默认UserAgent
     * */
    String userAgent = Configs.USER_AGENT;

    /**
     * 出错重试次数
     * */
    int retryCount = Configs.RETRY_COUNT;

    /**
     * 出错重试延迟时间（单位ms）
     * */
    int retryDelay = Configs.RETRY_DELAY;

    /**
     * 连接超时
     * */
    int connectTimeout = Configs.CONNECT_OUT_TIME;

    /**
     * 读取超时
     * */
    int readTimeout = Configs.READ_OUT_TIME;

    /**
     * 默认cookie值
     * */
    final Map<String, String> cookies = new HashMap<>();

    boolean allowAllSSL = false;

    final Map<String, String> headers = new HashMap<>();

    Proxy proxy;

    private SSLSocketFactory sslSocketFactory;

    int maxRedirectCount = Configs.MAX_REDIRECTS;

    IHttp.OnRedirectListener onRedirectListener;

    IHttp.CookieJar cookieJar;

    IHttp.HttpFactory httpFactory;


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

    public long maxBodySize() {
        return maxBodySize;
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
        return userAgent;
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
        StringBuilder sb = StringUtil.borrowBuilder();
        boolean first = true;
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            if (!first)
                sb.append("; ");
            else
                first = false;
            sb.append(cookie.getKey()).append('=').append(cookie.getValue());
            // todo: spec says only ascii, no escaping / encoding defined. validate on set? or escape somehow here?
        }
        return StringUtil.releaseBuilder(sb);
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
        if (cookieJar == null) {
            cookieJar = new HttpCookieJarImpl();
        }
        return cookieJar;
    }

    public IHttp.HttpFactory httpFactory() {
        if (httpFactory == null) {
            httpFactory = new HttpFactoryImpl();
        }
        return httpFactory;
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

    public T maxBodySize(long maxBodySize) {
        this.maxBodySize = maxBodySize;
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

    public T userAgent(String userAgent) {
        this.userAgent = userAgent;
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
            TokenQueue cd = new TokenQueue(cookie);
            while (!cd.isEmpty()) {
                String cookieName = cd.chompTo("=").trim();
                String cookieVal = cd.consumeTo(";").trim();
                cd.chompTo(";");
                cookie(cookieName, cookieVal);
            }
        }
        return (T) this;
    }

    public T cookies(Map<String, String> cookies) {
        this.cookies.clear();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            this.cookies.put(entry.getKey(), entry.getValue());
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

    public T setHttpFactory(IHttp.HttpFactory httpFactory) {
        this.httpFactory = httpFactory;
        return (T) this;
    }
}