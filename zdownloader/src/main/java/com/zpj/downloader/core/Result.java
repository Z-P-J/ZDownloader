package com.zpj.downloader.core;

public class Result {

    private final boolean isOk;
    private final int code;
    private final String message;

    private Result(boolean isOk, int code, String message) {
        this.isOk = isOk;
        this.code = code;
        this.message = message;
    }

    public boolean isOk() {
        return isOk;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static Result ok() {
        return new Result(true, -1, null);
    }

    public static Result ok(int code, String message) {
        return new Result(true, code, message);
    }

    public static Result error(int code, String message) {
        return new Result(false, code, message);
    }

}
