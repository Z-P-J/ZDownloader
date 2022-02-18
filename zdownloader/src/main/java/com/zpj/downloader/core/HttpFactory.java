package com.zpj.downloader.core;

import com.zpj.downloader.core.http.Response;

import java.io.IOException;
import java.util.Map;

public interface HttpFactory {

    Response request(Mission mission, Map<String, String> headers) throws IOException;

}
