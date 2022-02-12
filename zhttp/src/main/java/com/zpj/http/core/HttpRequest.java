package com.zpj.http.core;

public abstract class HttpRequest implements IHttp.Request {

    protected final HttpConfig config;

    protected HttpRequest(HttpConfig config) {
        this.config = config;
    }

    @Override
    public HttpConfig config() {
        return config;
    }

    @Override
    public IHttp.Connection connect() {
        return config.httpFactory().createConnection(this);
    }

}
