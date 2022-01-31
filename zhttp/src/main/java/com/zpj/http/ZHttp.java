package com.zpj.http;

import com.zpj.http.core.BaseConfig;
import com.zpj.http.core.ConnectionFactory;
import com.zpj.http.impl.HttpCookieJarImpl;
import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.IHttp;
import com.zpj.http.ssl.HTTPSTrustManager;

public class ZHttp {

    private static HttpGlobalConfig HTTP_CONFIG;

    private ZHttp() {}

    public static HttpGlobalConfig config() {
        if (HTTP_CONFIG == null) {
            synchronized (HttpGlobalConfig.class) {
                if (HTTP_CONFIG == null) {
                    HTTP_CONFIG = new HttpGlobalConfig();
                }
            }
        }
        return HTTP_CONFIG;
    }

    public static HttpConfig connect(String url) {
        return ConnectionFactory.createHttpRequest(url);
    }

    public static HttpConfig get(String url) {
        return connect(url).method(IHttp.Method.GET);
    }

    public static HttpConfig post(String url) {
        return connect(url).method(IHttp.Method.POST);
    }

    public static HttpConfig head(String url) {
        return connect(url).method(IHttp.Method.HEAD);
    }

    public static HttpConfig put(String url) {
        return connect(url).method(IHttp.Method.PUT);
    }

    public static HttpConfig delete(String url) {
        return connect(url).method(IHttp.Method.DELETE);
    }

    public static HttpConfig patch(String url) {
        return connect(url).method(IHttp.Method.PATCH);
    }

    public static HttpConfig options(String url) {
        return connect(url).method(IHttp.Method.OPTIONS);
    }

    public static HttpConfig trace(String url) {
        return connect(url).method(IHttp.Method.TRACE);
    }


    public static class HttpGlobalConfig extends BaseConfig<HttpGlobalConfig> {

        private HttpGlobalConfig() {
            cookieJar(new HttpCookieJarImpl());
        }

        public void init() {
            if (allowAllSSL()) {
                HTTPSTrustManager.allowAllSSL();
            }
        }

//        @Override
//        public CookieJar cookieJar() {
//            CookieJar cookieJar = super.cookieJar();
//            if (cookieJar == null) {
//                cookieJar = new DefaultCookieJar();
//                cookieJar(cookieJar);
//            }
//            return cookieJar;
//        }
    }


}
