package com.zpj.downloader.core.http;

import com.zpj.downloader.core.Mission;

import java.io.IOException;
import java.util.Map;

public interface HttpFactory {

    Response request(Mission mission, Map<String, String> headers) throws IOException;

}
