package com.zpj.downloader;

import android.content.Context;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.impl.DefaultConflictPolicy;
import com.zpj.downloader.utils.SerializableProxy;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Z-P-J
 * */
@Keep
abstract class BaseConfig<T extends BaseConfig<T>> implements Serializable {

    /**
     * context
     * */
    private transient Context context;

    /**
     * 通知拦截器
     */
    transient Notifier notificationInterceptor;

    private transient ConflictPolicy policy;

    /*
    * 下载线程数
    * */
    int threadCount = DefaultConstant.THREAD_COUNT;

    /**
     * 下载路径
     * */
    String downloadPath = DefaultConstant.DOWNLOAD_PATH;

    /**
     * 下载缓冲大小
     * */
    int bufferSize = DefaultConstant.BUFFER_SIZE;

    /**
     * 进度更新频率，默认1000ms更新一次（单位ms）
     * */
    long progressInterval =DefaultConstant.PROGRESS_INTERVAL;

    /**
     * 下载块大小
     * */
    int blockSize = DefaultConstant.BLOCK_SIZE;

    /**
     * 默认UserAgent
     * */
    String userAgent = DefaultConstant.USER_AGENT;

    /**
     * 下载出错重试次数
     * */
    int retryCount = DefaultConstant.RETRY_COUNT;

    /**
     * 下载出错重试延迟时间（单位ms）
     * */
    int retryDelayMillis = DefaultConstant.RETRY_DELAY_MILLIS;

    /**
     * 下载连接超时
     * */
    int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;

    /**
     * 下载链接读取超时
     * */
    int readOutTime = DefaultConstant.READ_OUT_TIME;

    /**
     * 是否允许在通知栏显示任务下载进度
     * */
    boolean enableNotification = true;

    /**
     * 下载时传入的cookie额值
     * */
    String cookie = "";

    boolean allowAllSSL = true;

    final HashMap<String, String> headers = new HashMap<>();

    SerializableProxy proxy;


    //-----------------------------------------------------------getter-------------------------------------------------------------

    public Context getContext() {
        return context;
    }

    public Notifier getNotificationInterceptor() {
        return notificationInterceptor;
    }

    public ConflictPolicy getConflictPolicy() {
        if (policy == null) {
            policy = new DefaultConflictPolicy();
        }
        return policy;
    }

    public int getThreadCount() {
        if (threadCount < 1) {
            threadCount = 1;
        }
        return threadCount;
    }

    public String getDownloadPath() {
        if (TextUtils.isEmpty(downloadPath)) {
            return DefaultConstant.DOWNLOAD_PATH;
        }
        return downloadPath;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getProgressInterval() {
        return progressInterval;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getCookie() {
        return cookie == null ? "" : cookie;
    }

    public int getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public int getConnectOutTime() {
        return connectOutTime;
    }

    public int getReadOutTime() {
        return readOutTime;
    }

    public boolean isAllowAllSSL() {
        return allowAllSSL;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Proxy getProxy() {
        if (proxy == null) {
            return null;
        }
        return proxy.proxy();
    }

    public boolean getEnableNotification() {
        return enableNotification;
    }




    //-----------------------------------------------------------------setter------------------------------------------------------

    void setContext(Context context) {
        this.context = context;
    }

    public T setNotificationInterceptor(Notifier interceptor) {
        this.enableNotification = interceptor != null;
        this.notificationInterceptor = interceptor;
        return (T) this;
    }

    public T setConflictPolicy(ConflictPolicy policy) {
        this.policy = policy;
        return (T) this;
    }

    public T setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return (T) this;
    }

    public T setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return (T) this;
    }

    public T setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return (T) this;
    }

    public T setProgressInterval(long progressInterval) {
        this.progressInterval = progressInterval;
        return (T) this;
    }

    public T setBlockSize(int blockSize) {
        this.blockSize = blockSize;
        return (T) this;
    }

    public T setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return (T) this;
    }

    public T setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return (T) this;
    }

    public T setCookie(String cookie) {
        this.cookie = cookie;
        return (T) this;
    }

    public T setRetryDelayMillis(int retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
        return (T) this;
    }

    public T setConnectOutTime(int connectOutTime) {
        this.connectOutTime = connectOutTime;
        return (T) this;
    }

    public T setReadOutTime(int readOutTime) {
        this.readOutTime = readOutTime;
        return (T) this;
    }

    public T setAllowAllSSL(boolean allowAllSSL) {
        this.allowAllSSL = allowAllSSL;
        return (T) this;
    }

    public T setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return (T) this;
    }

    public T addHeader(String key, String value) {
        this.headers.put(key, value);
        return (T) this;
    }

    public T setProxy(SerializableProxy proxy) {
        this.proxy = proxy;
        return (T) this;
    }

    public T setProxy(Proxy proxy) {
        if (proxy == null) {
            return (T) this;
        }
        return setProxy(SerializableProxy.with(proxy));
    }

    public T setProxy(String host, int port) {
        return setProxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port)));
    }

    public T setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
        return (T) this;
    }
}