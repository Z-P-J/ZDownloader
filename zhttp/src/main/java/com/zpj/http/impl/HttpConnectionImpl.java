package com.zpj.http.impl;

import android.text.TextUtils;
import android.util.Log;

import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.HttpConnection;
import com.zpj.http.core.HttpHeader;
import com.zpj.http.core.IHttp;
import com.zpj.http.io.ConstrainableInputStream;
import com.zpj.http.utils.DataUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpConnectionImpl extends HttpConnection {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String DefaultUploadType = "application/octet-stream";

    private HttpURLConnection conn;

    private int statusCode;
    private String statusMessage;
    private String contentType;
    private long contentLength;
    protected Map<String, String> headers;


    public HttpConnectionImpl(IHttp.Request request) {
        super(request);
    }

    @Override
    public void enqueue() {

    }

    @Override
    public void disconnect() {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    @Override
    public void connect() throws IOException {
        String mimeBoundary = null;
        if (config().method().hasBody()) {
            mimeBoundary = setOutputContentType();
        }

        conn = createConnection(config());
        if (conn.getDoOutput()) {
            conn.setUseCaches(false);
            writePost2(conn, mimeBoundary);
        } else {
            conn.connect();
        }

        try {
            contentLength = Long.parseLong(conn.getHeaderField(HttpHeader.CONTENT_LENGTH));
        } catch (Exception ignore) {
            contentLength = conn.getContentLength();
        }
        Map<String, List<String>> map = conn.getHeaderFields();

        if (config().debug()) {
            Log.d("onExecute", "length=" + contentLength + " content-length=" + conn.getContentLength());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                Log.d("onExecute", "key=" + entry.getKey() + " value=" + entry.getValue());
            }
        }

        statusCode = conn.getResponseCode();
        statusMessage = conn.getResponseMessage();
        contentType = conn.getContentType();
        headers = getHeaderMap(conn);

    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public InputStream getBodyStream() throws IOException {
        long startTime = System.nanoTime();
        InputStream bodyStream;
        if (getContentLength() != 0 && config().method() != IHttp.Method.HEAD) { // -1 means unknown, chunked. sun throws an IO exception on 500 response with no content when trying to read body
            bodyStream = conn.getInputStream();
            if (hasContentEncoding("gzip")) {
                bodyStream = new GZIPInputStream(bodyStream);
            } else if (hasContentEncoding("deflate")) {
                bodyStream = new InflaterInputStream(bodyStream, new Inflater(true));
            }
        } else {
            bodyStream = conn.getErrorStream();
        }
        return ConstrainableInputStream
                .wrap(bodyStream, DataUtil.bufferSize, config().maxBodySize())
                .timeout(startTime, config().connectTimeout() + config().readTimeout());
    }

    private boolean hasContentEncoding(String value) {
        return value.equalsIgnoreCase(headers.get(HttpHeader.CONTENT_ENCODING));
    }

    private String setOutputContentType() {
        String bound = null;
        if (config().hasHeader(HttpHeader.CONTENT_TYPE)) {
            // no-op; don't add content type as already set (e.g. for requestBody())
            // todo - if content type already set, we could add charset

            // if user has set content type to multipart/form-data, auto add boundary.
            if (config().header(HttpHeader.CONTENT_TYPE).contains(MULTIPART_FORM_DATA) &&
                    !config().header(HttpHeader.CONTENT_TYPE).contains("boundary")) {
                bound = DataUtil.mimeBoundary();
                config().header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
            }

        } else if (config().needsMultipart()) {
            bound = DataUtil.mimeBoundary();
            config().header(HttpHeader.CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
        } else {
            config().header(HttpHeader.CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + config().postDataCharset());
        }
        return bound;
    }

    private Map<String, String> getHeaderMap(HttpURLConnection conn) {
        // the default sun impl of conn.getHeaderFields() returns header values out of order
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        int i = 0;
        while (true) {
            final String key = conn.getHeaderFieldKey(i);
            final String val = conn.getHeaderField(i);
            if (key == null && val == null)
                break;
            i++;
            if (key == null || val == null)
                continue; // skip http1.1 line

            headers.put(key, val);
        }
        return headers;
    }

    private long getTotalBytes(final String bound) throws IOException {
        String charset = config().postDataCharset();
        byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
        byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
        long total = 0;
        for (IHttp.KeyVal keyVal : config().data()) {
            total += boundaryBytes.length;
            String multipartHeader = ("Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"");
            if (keyVal.hasInputStream()) {
                multipartHeader += ("; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ");
                multipartHeader += (TextUtils.isEmpty(keyVal.contentType()) ? DefaultUploadType : keyVal.contentType());
                multipartHeader += "\r\n\r\n";
                total += multipartHeader.getBytes(charset).length;
                if (keyVal.inputStream() instanceof FileInputStream) {
                    total += ((FileInputStream) keyVal.inputStream()).getChannel().size();
                } else {
                    int available = keyVal.inputStream().available();
                    if (available < Integer.MAX_VALUE) {
                        total += available;
                    } else {
                        byte[] buf = new byte[512 * 1024];
                        int len;
                        while ((len = keyVal.inputStream().read(buf)) > 0) {
                            total += len;
                        }
                    }
                }
            } else {
                multipartHeader += ("\r\n\r\n" + keyVal.value());
                total += multipartHeader.getBytes(charset).length;
            }
            total += "\r\n".getBytes(charset).length;
        }
        total += trailerBytes.length;
        Log.d("HttpResponse", "total=" + total);
        return total;
    }

    private void writePost2(final HttpURLConnection conn, final String bound) throws IOException {
        final Collection<IHttp.KeyVal> data = config().data();
        String charset = config().postDataCharset();

//        final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream, req.postDataCharset()));

        OutputStream w;
        if (bound != null) {
            conn.setFixedLengthStreamingMode(getTotalBytes(bound));
            Log.d("HttpResponse", "setFixedLengthStreamingMode finished");
            w = conn.getOutputStream();
            // boundary will be set if we're in multipart mode
            byte[] boundaryBytes = ("--" + bound + "\r\n").getBytes(charset);
            byte[] trailerBytes = ("--" + bound + "--").getBytes(charset);
            for (IHttp.KeyVal keyVal : data) {
                w.write(boundaryBytes);
                String multipartHeader = "Content-Disposition: form-data; name=\"" + encodeMimeName(keyVal.key()) + "\"";
                if (keyVal.hasInputStream()) {

                    multipartHeader += "; filename=\"" + encodeMimeName(keyVal.value()) + "\"\r\nContent-Type: ";
                    multipartHeader += (TextUtils.isEmpty(keyVal.contentType()) ? DefaultUploadType : keyVal.contentType());
                    multipartHeader += "\r\n\r\n";
                    w.write(multipartHeader.getBytes(charset));

                    Log.d("HttpResponse", "crossStreams");
                    DataUtil.crossStreams(keyVal.inputStream(), w, keyVal.getListener());
                    w.flush();
                } else {
                    multipartHeader += ("\r\n\r\n" + keyVal.value());
                    w.write(multipartHeader.getBytes(charset));
                }
                w.write("\r\n".getBytes(charset));
            }
            w.write(trailerBytes);
        } else if (config().requestBody() != null) {
            w = conn.getOutputStream();
            // data will be in query string, we're sending a plaintext body
            w.write(config().requestBody().getBytes(charset));
        } else {
            w = conn.getOutputStream();
            // regular form data (application/x-www-form-urlencoded)
            boolean first = true;
            for (IHttp.KeyVal keyVal : data) {
                if (!first)
                    w.write("&".getBytes(charset));
                else
                    first = false;

                w.write(URLEncoder.encode(keyVal.key(), config().postDataCharset()).getBytes(charset));
                w.write("=".getBytes(charset));
                w.write(URLEncoder.encode(keyVal.value(), config().postDataCharset()).getBytes(charset));
            }
        }
        w.close();
    }

    private static String encodeMimeName(String val) {
        if (val == null)
            return null;
        return val.replaceAll("\"", "%22");
    }

    public HttpURLConnection createConnection(HttpConfig config) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) (
                config.proxy() == null ?
                        config.url().openConnection() :
                        config.url().openConnection(config.proxy())
        );

        conn.setRequestMethod(config.method().name());
        conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
        conn.setConnectTimeout(config.connectTimeout());
        conn.setReadTimeout(config.readTimeout()); // gets reduced after connection is made and status is read

//        if (req.sslSocketFactory() != null && conn instanceof HttpsURLConnection)
//            ((HttpsURLConnection) conn).setSSLSocketFactory(req.sslSocketFactory());

        if (conn instanceof HttpsURLConnection) {
            SSLSocketFactory socketFactory = config.sslSocketFactory();

            if (socketFactory != null) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);
            } else if (config.allowAllSSL()) {
                initUnSecureTSL(config);
                ((HttpsURLConnection) conn).setSSLSocketFactory(config.sslSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                });
            }
        }

        if (config.method().hasBody())
            conn.setDoOutput(true);

        if (config.cookieJar() != null) {
            Map<String, String> cookieMap = config.cookieJar().loadCookies(config.url());
            if (cookieMap != null) {
                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                    if (!config.hasCookie(entry.getKey())) {
                        config.cookie(entry.getKey(), entry.getValue());
                    }
                }
            }
        }


        if (config.cookies().size() > 0)
            conn.addRequestProperty(HttpHeader.COOKIE, config.cookieStr());
        String userAgent = config.userAgent();
        if (!TextUtils.isEmpty(userAgent)) {
            conn.addRequestProperty(HttpHeader.USER_AGENT, userAgent);
        }
//        for (Map.Entry<String, List<String>> header : req.multiHeaders().entrySet()) {
//            for (String value : header.getValue()) {
//                conn.addRequestProperty(header.getKey(), value);
//            }
//        }
        for (Map.Entry<String, String> header : config.headers().entrySet()) {
            conn.addRequestProperty(header.getKey(), header.getValue());
        }
        return conn;
    }

    private synchronized void initUnSecureTSL(HttpConfig config) throws IOException {
        if (config.sslSocketFactory() == null) {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                }

                public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                config.sslSocketFactory(sslContext.getSocketFactory());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException("Can't create unsecure trust manager");
            }
        }
    }

}
