package com.zpj.http.core;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpDispatcher implements IHttp.HttpDispatcher {

    private static final class ExecuteHolder {
        private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    }

    private static final class Holder {
        private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    }

    public static void execute(Runnable runnable) {
        ExecuteHolder.EXECUTOR.execute(runnable);
    }

    public static void post(Runnable runnable) {
        Holder.HANDLER.post(runnable);
    }

    public static void postDelayed(Runnable r, long delayMillis) {
        Holder.HANDLER.postDelayed(r, delayMillis);
    }

    public static Future<?> submit(Runnable runnable) {
        return ExecuteHolder.EXECUTOR.submit(runnable);
    }

    @Override
    public void enqueue(final IHttp.Connection connection, final IHttp.Callback callback) {
        assert connection != null;
        assert callback != null;
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final IHttp.Response response = connection.execute();
                    post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callback.onResponse(connection, response);
                            } catch (IOException e) {
                                callback.onFailure(connection, e);
                            }
                        }
                    });
                } catch (final IOException e) {
                    onFailure(callback, connection, e);
                }
            }
        });
    }

    private void onFailure(final IHttp.Callback callback, final IHttp.Connection conn, final IOException e) {
        assert callback != null;
        assert conn != null;
        post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(conn, e);
            }
        });
    }


}
