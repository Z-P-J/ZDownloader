package com.zpj.http.core;

import android.util.Log;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Z-P-J
 */
public class HttpCookieJar implements IHttp.CookieJar {

    private static final String TAG = "DefaultCookieJar";

    private final Map<String, Map<String, String>> cookiesMap = new HashMap<>();

    @Override
    public Map<String, String> loadCookies(URL url) {
        if (url == null) {
            return Collections.emptyMap();
        }
        synchronized (cookiesMap) {
            String host = url.getHost();
            Log.d(TAG, "loadCookies host=" + host + " url=" + url.toString());
            Map<String, String> cookieMap = cookiesMap.get(host);
            Log.d(TAG, "loadCookies cookieMap=" + cookieMap);
            if (cookieMap != null) {
                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                    Log.d(TAG, "loadCookies key=" + entry.getKey());
                    Log.d(TAG, "loadCookies value=" + entry.getValue());
                }
            }

            return cookieMap;
        }
    }

    @Override
    public void saveCookies(URL url, Map<String, String> cookieMap) {
        if (url == null || cookieMap == null) {
            return;
        }
        synchronized (cookiesMap) {
            String host = url.getHost();
            Log.d(TAG, "saveCookies host=" + host + " url=" + url.toString());
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                Log.d(TAG, "saveCookies key111=" + entry.getKey());
                Log.d(TAG, "saveCookies value111=" + entry.getValue());
            }
            Map<String, String> cookies = cookiesMap.get(host);
            Log.d(TAG, "loadCookies cookieMap=" + cookieMap + " cookies=" + cookies);
            if (cookies == null) {
                cookiesMap.put(host, cookieMap);
            } else if (cookies != cookieMap) {
                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                    cookies.put(entry.getKey(), entry.getValue());
                    Log.d(TAG, "saveCookies key222=" + entry.getKey());
                    Log.d(TAG, "saveCookies value222=" + entry.getValue());
                }
            }
        }
    }

//    public Map<String, Map<String, String>> getCookiesMap() {
//        if (cookiesMap == null) {
//            synchronized (DefaultCookieJar.class) {
//                if (cookiesMap == null) {
//                    cookiesMap = new HashMap<>();
//                }
//            }
//        }
//        return cookiesMap;
//    }
}
