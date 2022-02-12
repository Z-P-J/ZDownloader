package com.zpj.downloader.core.impl;

import com.zpj.downloader.core.HttpFactory;
import com.zpj.http.ZHttp;
import com.zpj.http.core.IHttp;

import java.io.IOException;
import java.util.Map;

public class HttpFactoryImpl implements HttpFactory {

    @Override
    public IHttp.Response request(String url, Map<String, String> headers) throws IOException {
        return ZHttp.get(url).headers(headers)
                .maxRedirectCount(10)
                .execute();
    }

}
