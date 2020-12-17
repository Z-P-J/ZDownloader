package com.zpj.downloader.util;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public final class RxHandler {

    private RxHandler() {
    }

    public static void post(Action action) {
        Observable.empty()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(action)
                .subscribe();
    }

    public static void post(Action action, long delayMillis) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(action)
                .subscribe();
    }

}
