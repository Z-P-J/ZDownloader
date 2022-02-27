package com.zpj.downloader.core.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zpj.downloader.constant.DefaultConstant;
import com.zpj.downloader.core.Mission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Z-P-J
 * */
@Keep
@Entity(
        tableName = "mission_configs",
        foreignKeys = @ForeignKey(entity = MissionInfo.class, parentColumns = {"mission_id"}, childColumns = {"mission_id"})
)
@TypeConverters(Config.MapConverter.class)
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

    public static class MapConverter {

        @TypeConverter
        public String objToStr(Map<String, String> headers){
            try {
                JSONObject object = new JSONObject();
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    object.put(entry.getKey(), entry.getValue());
                }
                return object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "{}";
        }

        @TypeConverter
        public Map<String, String> strToObj(String str) {
            Map<String, String> headers = new HashMap<>();
            if (TextUtils.isEmpty(str) || "NULL".equalsIgnoreCase(str)) {
                return headers;
            }

            try {
                JSONObject jsonObject = new JSONObject(str);
                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    headers.put(key, jsonObject.getString(key));
                }
                return headers;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return headers;
        }

    }

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "mission_id")
    private String missionId;

    /*
    * 下载线程数
    * */
    @ColumnInfo(name = "thread_count")
    private int threadCount = DefaultConstant.THREAD_COUNT;

    /**
     * 下载路径
     * */
    @ColumnInfo(name = "download_path")
    private String downloadPath = DefaultConstant.DOWNLOAD_PATH;

    /**
     * 下载缓冲大小
     * */
    @ColumnInfo(name = "buffer_size")
    private int bufferSize = DefaultConstant.BUFFER_SIZE;

    /**
     * 进度更新频率，默认1000ms更新一次（单位ms）
     * */
    @ColumnInfo(name = "progress_interval")
    private long progressInterval = DefaultConstant.PROGRESS_INTERVAL;

    /**
     * 下载出错重试次数
     * */
    @ColumnInfo(name = "retry_count")
    private int retryCount = DefaultConstant.RETRY_COUNT;

    /**
     * 下载出错重试延迟时间（单位ms）
     * */
    @ColumnInfo(name = "retry_delay_millis")
    private int retryDelayMillis = DefaultConstant.RETRY_DELAY_MILLIS;

    /**
     * 下载连接超时
     * */
    @ColumnInfo(name = "connect_out_time")
    private int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;

    /**
     * 下载链接读取超时
     * */
    @ColumnInfo(name = "read_out_time")
    private int readOutTime = DefaultConstant.READ_OUT_TIME;

    /**
     * 是否允许在通知栏显示任务下载进度
     * */
    @ColumnInfo(name = "enable_notification")
    private boolean enableNotification = true;

    private final Map<String, String> headers = new HashMap<>();

    public Config(@NonNull String missionId) {
        this.missionId = missionId;
    }

    public Config(@NonNull String missionId, Mission.Builder builder) {
        this(missionId);
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

//    public Map<String, String> getHeaders() {
//        return headers;
//    }


    public Map<String, String> getHeaders() {
        return headers;
    }




    //-----------------------------------------------------------------setter------------------------------------------------------


    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setProgressInterval(long progressInterval) {
        this.progressInterval = progressInterval;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setRetryDelayMillis(int retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
    }

    public void setConnectOutTime(int connectOutTime) {
        this.connectOutTime = connectOutTime;
    }

    public void setReadOutTime(int readOutTime) {
        this.readOutTime = readOutTime;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
    }


    public void setMissionId(@NonNull String missionId) {
        this.missionId = missionId;
    }

    public boolean isEnableNotification() {
        return enableNotification;
    }

    @Override
    public String toString() {
        return "Config{" +
                "missionId='" + missionId + '\'' +
                ", threadCount=" + threadCount +
                ", downloadPath='" + downloadPath + '\'' +
                ", bufferSize=" + bufferSize +
                ", progressInterval=" + progressInterval +
                ", retryCount=" + retryCount +
                ", retryDelayMillis=" + retryDelayMillis +
                ", connectOutTime=" + connectOutTime +
                ", readOutTime=" + readOutTime +
                ", enableNotification=" + enableNotification +
                ", headers=" + headers +
                '}';
    }
}