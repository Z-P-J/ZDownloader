package com.zpj.http.impl;

import com.zpj.http.core.HttpResponse;
import com.zpj.http.core.IHttp;

public class HttpResponseImpl extends HttpResponse {

    public HttpResponseImpl(IHttp.Connection connection) {
        super(connection);
    }

}
