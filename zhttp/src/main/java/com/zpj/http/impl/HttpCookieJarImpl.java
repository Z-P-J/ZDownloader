package com.zpj.http.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zpj.http.core.IHttp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpCookieJarImpl implements IHttp.CookieJar {

    private static final String TAG = "DefaultCookieJar";

    private final Map<String, Map<String, String>> cookiesMap = new HashMap<>();

    @Nullable
    @Override
    public Map<String, String> loadCookies(@NonNull URL url) {
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
    public void saveCookies(@NonNull URL url, @NonNull Map<String, String> cookieMap) {
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
