package com.zpj.http.core;

/**
 * @author Z-P-J
 * */
public class Configs {

    public static final int BUFFER_SIZE = 1024;

    public static final int MAX_BODY_SIZE = 0;

    public static final String USER_AGENT = System.getProperty("http.agent");

    public static final int RETRY_COUNT = 3;

    public static final int MAX_REDIRECTS = 20;

//    public static final CookieJar COOKIE_JAR = new HttpCookieJarImpl();

    // 单位毫秒
    public static final int RETRY_DELAY = 10 * 1000;

    public static final int CONNECT_OUT_TIME = 30000;
    public static final int READ_OUT_TIME = 30000;

    private Configs() {

    }

}
