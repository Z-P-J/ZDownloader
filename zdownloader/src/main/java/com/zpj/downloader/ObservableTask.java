package com.zpj.downloader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ObservableTask<T> implements Callable<T> {

    private Future<T> future;

    interface OnErrorListener {
        void onError(Throwable throwable);
    }

    interface OnSuccessListener<T> {
        void onSuccess(T data);
    }

    interface OnCompleteListener {
        void onComplete();
    }

    private OnSuccessListener<T> onSuccessListener;
    private OnErrorListener onErrorListener;
    private OnCompleteListener onCompleteListener;

    public ObservableTask<T> onSuccess(OnSuccessListener<T> onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public ObservableTask<T> onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public ObservableTask<T> onComplete(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

    @Override
    public final T call() throws Exception {
        try {
            T result = run();
            if (onSuccessListener != null) {
                onSuccessListener.onSuccess(result);
            }
            return result;
        } catch (Exception e) {
            if (onErrorListener != null) {
                onErrorListener.onError(e);
            }
            return null;
        } finally {
            if (onCompleteListener != null) {
                onCompleteListener.onComplete();
            }
        }
    }

    private void checkExecuted() {
        if (future == null) {
            throw new RuntimeException("You must execute this task first!");
        }
    }

    public void execute() {
        if (future != null) {
            return;
        }
        future = Executors.newFixedThreadPool(1).submit(this);
    }

    public void execute(ExecutorService executorService) {
        if (future != null) {
            return;
        }
        future = executorService.submit(this);
    }

    public boolean isCancelled() {
        checkExecuted();
        return future.isCancelled();
    }

    public boolean isDone() {
        checkExecuted();
        return future.isDone();
    }

    boolean cancel(boolean mayInterruptIfRunning) {
        checkExecuted();
        return future.cancel(mayInterruptIfRunning);
    }

    public T get() {
        checkExecuted();
        try {
            if (future.isDone()) {
                return future.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract T run() throws Exception;


}
