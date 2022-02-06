package com.zpj.downloader.core;

import com.zpj.http.core.IHttp;

import java.io.IOException;
import java.util.Map;

public interface HttpFactory {

    IHttp.Response request(String url, Map<String, String> headers) throws IOException;

}
