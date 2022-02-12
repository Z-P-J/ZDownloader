package com.zpj.downloader.core;

import com.zpj.http.core.IHttp;

import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

public interface Request {

    Response execute() throws Exception;



    //-----------------------------------------------------------getter-------------------------------------------------------------

    public URL url();

    public URL originalUrl();

    public void url(String url);

    public void url(URL url);

    public IHttp.Method method();

    public void method(IHttp.Method method);

    public void range(String range);

    public void range(long start);

    public void range(long start, long end);

    public void requestBody(String body);

    public String requestBody();

    public void data(IHttp.KeyVal keyval);

    public Collection<IHttp.KeyVal> data();

    public void data(String key, String value);

    public void data(String key, String filename, InputStream inputStream);

    public void data(String key, String filename, InputStream inputStream, IHttp.OnStreamWriteListener listener);

    public void data(String key, String filename, InputStream inputStream, String contentType);

    public void data(Map<String, String> data);

    public void data(Collection<IHttp.KeyVal> data);

    public IHttp.KeyVal data(String key);

    public boolean needsMultipart();


    public URI baseUrl();

    public boolean debug();

    public int bufferSize();

    public long maxBodySize();

    public String postDataCharset();

    public boolean ignoreHttpErrors();

    public boolean ignoreContentType();

    public String userAgent();

    public int retryCount();

    public String getCookie(String name);

    public boolean hasCookie(String name);

    public String removeCookie(String name);

    public Map<String, String> cookies();

    public String cookieStr();

    public int retryDelay();

    public int connectTimeout();

    public int readTimeout();

    public boolean allowAllSSL();

    public Map<String, String> headers();

    public String removeHeader(String key);

    public boolean hasHeader(String name);

    public boolean hasHeaderWithValue(String name, String value);

    public String header(String name);

    public Proxy proxy();

    public SSLSocketFactory sslSocketFactory();

    public int maxRedirectCount();

    public IHttp.OnRedirectListener getOnRedirectListener();

    public IHttp.CookieJar cookieJar();

    //-----------------------------------------------------------------setter------------------------------------------------------


    void baseUrl(String baseUrl);

    void baseUrl(URI baseUrl);

    void debug(boolean debug);

    void referer(String referer);

    void contentType(String contentType);

    void acceptLanguage(String acceptLanguage);

    void host(String host);

    void accept(String accept);

    void acceptEncoding(String acceptEncoding);

    void connectionMethod(String connection);

    void acceptCharset(String acceptCharset);

    void bufferSize(int bufferSize);

    void maxBodySize(long maxBodySize);

    public void postDataCharset(String charset);

    void ignoreHttpErrors(boolean ignoreHttpErrors);

    void ignoreContentType(boolean ignoreContentType);

    void userAgent(String userAgent);

    void retryCount(int retryCount);

    void cookie(String name, String value);

    void cookie(String cookie);

    void cookies(Map<String, String> cookies);

    void retryDelay(int retryDelay);

    void connectTimeout(int connectTimeout);

    void readTimeout(int readTimeout);

    void allowAllSSL(boolean allowAllSSL);

    void headers(Map<String, String> headers);

    void header(String key, String value);

    void proxy(Proxy proxy);

    void proxy(String host, int port);

    void sslSocketFactory(SSLSocketFactory sslSocketFactory);

    void maxRedirectCount(int maxRedirectCount);

    void onRedirect(IHttp.OnRedirectListener onRedirectListener);

    void cookieJar(IHttp.CookieJar cookieJar);
    
}
