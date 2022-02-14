package com.zpj.downloader.core.impl;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.zpj.downloader.ConflictPolicy;
import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.Notifier;
import com.zpj.downloader.impl.DefaultConflictPolicy;
import com.zpj.downloader.utils.MissionIdGenerator;
import com.zpj.downloader.utils.SerializableProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Z-P-J
 * */
@Keep
@Entity(
        tableName = "mission_configs",
        foreignKeys = @ForeignKey(entity = MissionInfo.class, parentColumns = {"mission_id"}, childColumns = {"mission_id"})
)
public class Config implements Serializable {

//    public static class SerializableConverter {
//
//        @TypeConverter
//        public String objToStr(SerializableProxy proxy) {
//            try {
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//                objectOutputStream.writeObject(proxy);
//                String string = byteArrayOutputStream.toString(StandardCharsets.ISO_8859_1.name());
//                objectOutputStream.close();
//                byteArrayOutputStream.close();
//                return string;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return "NULL";
//        }
//
//        @TypeConverter
//        public SerializableProxy strToObj(String str) throws IOException, ClassNotFoundException {
//            if (TextUtils.isEmpty(str) || "NULL".equalsIgnoreCase(str)) {
//                return null;
//            }
//            try {
//                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.ISO_8859_1));
//                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//                Object object = objectInputStream.readObject();
//                objectInputStream.close();
//                byteArrayInputStream.close();
//                return (SerializableProxy) object;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//    }

    @PrimaryKey
    @ColumnInfo(name = "mission_id")
    String missionId;

    /*
    * 下载线程数
    * */
    @ColumnInfo(name = "thread_count")
    int threadCount = DefaultConstant.THREAD_COUNT;

    /**
     * 下载路径
     * */
    @ColumnInfo(name = "download_path")
    String downloadPath = DefaultConstant.DOWNLOAD_PATH;

    /**
     * 下载缓冲大小
     * */
    @ColumnInfo(name = "thread_count")
    int bufferSize = DefaultConstant.BUFFER_SIZE;

    /**
     * 进度更新频率，默认1000ms更新一次（单位ms）
     * */
    @ColumnInfo(name = "progress_interval")
    long progressInterval = DefaultConstant.PROGRESS_INTERVAL;

    /**
     * 下载出错重试次数
     * */
    @ColumnInfo(name = "retry_count")
    int retryCount = DefaultConstant.RETRY_COUNT;

    /**
     * 下载出错重试延迟时间（单位ms）
     * */
    @ColumnInfo(name = "retry_delay_millis")
    int retryDelayMillis = DefaultConstant.RETRY_DELAY_MILLIS;

    /**
     * 下载连接超时
     * */
    @ColumnInfo(name = "connect_out_time")
    int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;

    /**
     * 下载链接读取超时
     * */
    @ColumnInfo(name = "read_out_time")
    int readOutTime = DefaultConstant.READ_OUT_TIME;

    /**
     * 是否允许在通知栏显示任务下载进度
     * */
    @ColumnInfo(name = "enable_notification")
    boolean enableNotification = true;

    final HashMap<String, String> headers = new HashMap<>();

    public Config() {

    }

    public Config(String missionId, ZDownloader.Builder builder) {
        this.missionId = missionId;
        this.setThreadCount(builder.getThreadCount());
        this.setBufferSize(builder.getBufferSize());
        this.setConnectOutTime(builder.getConnectOutTime());
        this.setDownloadPath(builder.getDownloadPath());
        this.setEnableNotification(builder.getEnableNotification());
        this.setHeaders(builder.getHeaders());
        this.setProgressInterval(builder.getProgressInterval());
        this.setReadOutTime(builder.getReadOutTime());
        this.setRetryCount(builder.getRetryCount());
        this.setRetryDelayMillis(builder.getRetryDelayMillis());
    }


    //-----------------------------------------------------------getter-------------------------------------------------------------


    public String getMissionId() {
        return missionId;
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

    public int getRetryCount() {
        return retryCount;
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean getEnableNotification() {
        return enableNotification;
    }




    //-----------------------------------------------------------------setter------------------------------------------------------


    public Config setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
        return this;
    }

    public Config setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public Config setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public Config setProgressInterval(long progressInterval) {
        this.progressInterval = progressInterval;
        return this;
    }

    public Config setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public Config setRetryDelayMillis(int retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
        return this;
    }

    public Config setConnectOutTime(int connectOutTime) {
        this.connectOutTime = connectOutTime;
        return this;
    }

    public Config setReadOutTime(int readOutTime) {
        this.readOutTime = readOutTime;
        return this;
    }

    public Config setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    public Config addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public Config setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
        return this;
    }
}