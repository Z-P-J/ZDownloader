//package com.zpj.downloader.core;
//
//import android.support.annotation.IntDef;
//
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//
///**
// * 下载器Handler
// * @author Z-P-J
// */
//public interface Handler {
//
//
//
//    @Retention(RetentionPolicy.SOURCE)
//    @IntDef({
//            Event.CREATE, Event.PREPARE, Event.WAIT, Event.DOWNLOAD, Event.PROGRESS,
//            Event.PAUSE, Event.ERROR, Event.RETRY, Event.COMPLETE, Event.DELETE, Event.CLEAR
//    })
//    public @interface Event {
//        int CREATE = 0;
//        int PREPARE = 1;
//        int WAIT = 2;
//        int DOWNLOAD = 3;
//        int PROGRESS = 4;
//        int PAUSE = 5;
//        int ERROR = 6;
//        int RETRY = 7;
//        int COMPLETE = 8;
//
//        int DELETE = 9;
//        int CLEAR = 10;
//    }
//
//    void start();
//
//    void stop();
//
//    void sendEvent(Mission mission, @Event int event);
//
//    void handleEvent(Mission mission, @Event int event);
//
//
//}
