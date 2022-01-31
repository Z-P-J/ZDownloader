package com.zpj.http.core;

import android.util.Log;

import com.zpj.http.utils.UrlUtil;
import com.zpj.http.utils.Validate;

import java.io.IOException;
import java.net.MalformedURLException;

public abstract class HttpConnection implements IHttp.Connection {

    public static final int HTTP_TEMP_REDIR = 307;

    private final IHttp.Request request;

    private int numRedirects = 0;

    protected HttpConnection(IHttp.Request request) {
        this.request = request;
    }

    @Override
    public IHttp.Request request() {
        return request;
    }

    @Override
    public HttpConfig config() {
        return request().config();
    }

    @Override
    public void enqueue(IHttp.Callback callback) {
        Validate.notNull(callback, "Callback must not be null");
        config().httpDispatcher().enqueue(this, callback);
    }

    @Override
    public IHttp.Response execute() throws IOException {
        Validate.notNull(request(), "Request must not be null");
        Validate.notNull(config().url(), "URL must be specified to connect");
        String protocol = config().url().getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new MalformedURLException("Only http & https protocols supported");
        }
        final boolean methodHasBody = config().method().hasBody();
        if (!methodHasBody) {
            config().requestBody(null);
        }

        if (config().data().size() > 0 && (!methodHasBody)) {
            UrlUtil.serialiseRequestUrl(config());
        }

        try {

            connect();
            Log.d("HttpResponse", "execute connection=" + this);

            if (getHeaders().containsKey(HttpHeader.SET_COOKIE)) {
                config().cookie(getHeaders().get(HttpHeader.SET_COOKIE));
            }
            if (config().cookieJar() != null && config().cookies() != null) {
                config().cookieJar().saveCookies(config().url(), config().cookies());
            }

            if (getHeaders().containsKey(HttpHeader.LOCATION)) {
                String location = getHeaders().get(HttpHeader.LOCATION);
                if (++numRedirects > config().maxRedirectCount) {
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", config().url()));
                }
                // fix broken Location: http:/temp/AAG_New/en/index.php
                if (location.charAt(6) != '/' && location.startsWith("http:/")) {
                    location = location.substring(6);
                }
                if (config().getOnRedirectListener() == null
                        || config().getOnRedirectListener().onRedirect(numRedirects, location)) {
                    if (getStatusCode() != HTTP_TEMP_REDIR) {
                        // always redirect with a get. any data param from original req are dropped.
                        config().method(IHttp.Method.GET);
                        config().data().clear();
                        config().requestBody(null);
                        config().removeHeader(HttpHeader.CONTENT_TYPE);
                    }

                    config().url(UrlUtil.resolve(config().url(), location));

                    disconnect();

                    return execute();
                }
            }
            if ((getStatusCode() < 200 || getStatusCode() >= 400)
                    && !config().ignoreHttpErrors()) {
                throw new IOException("HTTP error fetching URL. statusCode=" + getStatusCode()
                        + " statusMessage=" + getStatusMessage() + " url=" + config().url());
            }
        } catch (IOException e) {
            // per Java's documentation, this is not necessary, and precludes keepalives. However in practice,
            // connection errors will not be released quickly enough and can cause a too many open files error.
            e.printStackTrace();

            disconnect();
            throw e;
        }
        return config().httpFactory().createResponse(this);
    }

    public abstract void connect() throws IOException;

}
