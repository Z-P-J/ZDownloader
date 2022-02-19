//package com.zpj.downloader.core;
//
//import com.zpj.http.core.IHttp;
//
//import java.io.BufferedInputStream;
//import java.util.Map;
//
//public interface Response {
//
//    boolean hasHeader(String name);
//
//    boolean hasHeaderWithValue(String name, String value);
//
//    String header(String name);
//
//    Map<String, String> headers();
//
//    Map<String, String> cookies();
//
//    String cookieStr();
//
//    String cookie(String key);
//
//    IHttp.Method method();
//
//    int statusCode();
//
//    String statusMessage();
//
//    String charset();
//
//    String contentType();
//
//    long contentLength();
//
//    String body();
//
//    /**
//     * Get the body of the response as an array of bytes.
//     * @return body bytes
//     */
//    byte[] bodyAsBytes();
//
//    /**
//     * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
//     * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
//     * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
//     * @return the response body input stream
//     */
//    BufferedInputStream bodyStream();
//
//    void close();
//
//    void disconnect();
//
//    void closeIO();
//
//}
