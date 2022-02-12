package com.zpj.http;

import com.zpj.http.core.BaseConfig;
import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.HttpCookieJar;
import com.zpj.http.core.HttpDispatcher;
import com.zpj.http.core.IHttp;
import com.zpj.http.parser.HttpParserFactory;

public final class ZHttp {

    private static final class Holder {
        private static final HttpGlobalConfig HTTP_CONFIG = new HttpGlobalConfig();
    }

    private ZHttp() {}

    public static HttpGlobalConfig config() {
        return Holder.HTTP_CONFIG;
    }

    private static HttpConfig newConfig() {
        return new HttpConfig()
                .baseUrl(config().baseUrl())
                .debug(config().debug())
                .headers(config().headers())
                .bufferSize(config().bufferSize())
                .maxRedirectCount(config().maxRedirectCount())
                .ignoreHttpErrors(config().ignoreHttpErrors())
                .ignoreContentType(config().ignoreContentType())
                .postDataCharset(config().postDataCharset())
                .retryCount(config().retryCount())
                .cookies(config().cookies())
                .retryDelay(config().retryDelay())
                .connectTimeout(config().connectTimeout())
                .readTimeout(config().readTimeout())
                .allowAllSSL(config().allowAllSSL())
                .sslSocketFactory(config().sslSocketFactory())
                .proxy(config().proxy())
                .maxRedirectCount(config().maxRedirectCount())
                .onRedirect(config().getOnRedirectListener())
                .cookieJar(config().cookieJar())
                .httpFactory(config().httpFactory())
                .httpDispatcher(config().httpDispatcher());
    }

    public static HttpConfig connect(String url) {
        return newConfig().url(url);
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

        private final IHttp.ParserFactory parserFactory = new HttpParserFactory();

        private HttpGlobalConfig() {
            cookieJar(new HttpCookieJar());
            httpDispatcher(new HttpDispatcher());
        }

        public IHttp.ParserFactory parserFactory() {
            return parserFactory;
        }

        public void init() {
            httpFactory().initSSL(allowAllSSL());
        }

        public HttpGlobalConfig registerHttpParser(IHttp.Parser parser) {
            parserFactory.register(parser);
            return this;
        }

    }


}
