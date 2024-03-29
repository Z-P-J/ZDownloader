package com.zpj.downloader.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Response {

    String header(String name);

    Map<String, String> headers();

    int statusCode();

    String statusMessage();

    String contentType();

    long contentLength();

    InputStream bodyStream() throws IOException;

    void close();

}
