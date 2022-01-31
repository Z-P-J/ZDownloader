package com.zpj.http.core;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConnectionFactory {

    private ConnectionFactory() {

    }

    public static HttpConfig createHttpRequest(String url) {
        return new HttpConfig().url(url);
    }

    public static HttpURLConnection createConnection(HttpConfig config) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) (
                config.proxy == null ?
                        config.url().openConnection() :
                        config.url().openConnection(config.proxy)
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

        if (config.cookieJar != null) {
            Map<String, String> cookieMap = config.cookieJar.loadCookies(config.url);
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
        String userAgent = config.userAgent;
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

    private static synchronized void initUnSecureTSL(HttpConfig config) throws IOException {
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
