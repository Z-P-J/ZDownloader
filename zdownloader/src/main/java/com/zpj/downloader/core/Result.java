package com.zpj.downloader.core;

public class Result {

    public static final int CANCEL_BY_PAUSED = 100;

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
        return new Result(true, 0, null);
    }

    public static Result ok(String message) {
        return ok(0, message);
    }

    public static Result ok(int code, String message) {
        return new Result(true, code, message);
    }

    public static Result error(int code, String message) {
        return new Result(false, code, message);
    }

    public static Result error(String message) {
        return error(-1, message);
    }

    public static Result error(int code) {
        return error(code, null);
    }

    public static Result paused() {
        return error(CANCEL_BY_PAUSED, "mission paused!");
    }

    @Override
    public String toString() {
        return "Result{" +
                "isOk=" + isOk +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
