package com.zpj.downloader.constant;

import android.support.annotation.NonNull;

import com.zpj.downloader.core.Result;

public class  Error {
    
    public static final Error FILE_NOT_FOUND = new Error("文件不存在");
    public static final Error NO_ENOUGH_SPACE = new Error("存储空间不足");
    public static final Error WITHOUT_STORAGE_PERMISSIONS = new Error("无读写权限");
    public static final Error IO = new Error("未知IO错误");
    public static final Error SERVER_UNSUPPORTED = new Error("服务器不支持");
    public static final Error CONNECTION_TIME_OUT = new Error("连接超时");
    public static final Error UNKNOWN = new Error("未知错误");

    private String errorMsg;

    public Error(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @NonNull
    @Override
    public String toString() {
        return errorMsg;
    }

}
