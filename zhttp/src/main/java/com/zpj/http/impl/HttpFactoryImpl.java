package com.zpj.http.impl;

import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.IHttp;

public class HttpFactoryImpl implements IHttp.HttpFactory {
    @Override
    public IHttp.Request createRequest(HttpConfig config) {
        return new HttpRequestImpl(config);
    }

    @Override
    public IHttp.Connection createConnection(IHttp.Request request) {
        return new HttpConnectionImpl(request);
    }

    @Override
    public IHttp.Response createResponse(IHttp.Connection connection) {
        return new HttpResponseImpl(connection);
    }

}
