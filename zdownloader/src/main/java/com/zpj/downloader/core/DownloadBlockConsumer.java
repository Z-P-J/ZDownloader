package com.zpj.downloader.core;

import android.util.Log;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.util.io.BufferedRandomAccessFile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

public class DownloadBlockConsumer implements ObservableOnSubscribe<DownloadBlock> {

    private static final String TAG = "DownloadConsumer";

    private static final int BUFFER_SIZE = 1024;

    private final DownloadMission mMission;
    private final ConcurrentLinkedQueue<DownloadBlock> queue;
    private final int maxSize;

    public DownloadBlockConsumer(DownloadMission mission, ConcurrentLinkedQueue<DownloadBlock> queue) {
        this.mMission = mission;
        this.queue = queue;
        this.maxSize = 2 * mission.getThreadCount();
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<DownloadBlock> emitter) throws Exception {
        String threadName = Thread.currentThread().getName();
        byte[] buf = new byte[BUFFER_SIZE];
        BufferedRandomAccessFile f;
        try {
            f = new BufferedRandomAccessFile(mMission.getFilePath(), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            synchronized (mMission) {
                mMission.notifyError(Error.FILE_NOT_FOUND, true);
                return;
            }
        }
        while (true) {
            long startTime = System.currentTimeMillis();
            DownloadBlock block = null;
//            synchronized (queue) {
//                if (queue.isEmpty()) {
//                    synchronized (mMission) {
//                        if (mMission.getAliveThreadCount() == 0) {
//                            break;
//                        }
//                    }
//                } else {
//                    block = queue.poll();
//                }
//                if (queue.size() < maxSize) {
//                    queue.notify();
//                }
//            }

            Log.e(TAG, "isEmpty=" + queue.isEmpty() + " getAliveThreadCount=" + mMission.getAliveThreadCount());
            if (queue.isEmpty()) {
                if (mMission.getAliveThreadCount() == 0) {
                    break;
                }
            } else {
                block = queue.poll();
            }
            if (queue.size() < maxSize || !mMission.isRunning()) {
                synchronized (queue) {
                    try {
                        queue.notifyAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (block != null) {
                long start = block.getStart();
                long end = block.getEnd();
                long position = block.getPosition();
                Log.d(TAG, threadName + " start=" + start + " end=" + end + " position=" + position);
                Log.d(TAG, threadName + " total=" + (end - start));
                int total = 0;
                try {
                    f.seek(start);
                    InputStream stream = block.getInputStream();
                    Log.d(TAG, threadName + " stream=" + stream);
                    BufferedInputStream ipt = new BufferedInputStream(stream);
                    while (start < end) { //  && mMission.isRunning()
                        final int len = ipt.read(buf, 0, BUFFER_SIZE);
                        if (len == -1) {
                            break;
                        } else {
                            start += len;
                            f.write(buf, 0, len);
                            total += len;
                            Log.d(TAG, threadName + " notifyProgress len=" + len);
                            mMission.notifyProgress(len);
                        }
                    }
                    Log.d(TAG, threadName + " start=" + start + " end=" + end + " total=" + total);
                    ipt.close();
                    f.flush();
                    mMission.onBlockFinished(position);
                    Log.d(TAG, threadName + " onBlockFinished position=" + position);
                } catch (Exception e) {
                    mMission.notifyProgress(-total);
                    mMission.onPositionDownloadFailed(position);
                }
                block.disconnect();
                Log.e(TAG, threadName + " DownloadBlockConsumer Finished Time=" + (System.currentTimeMillis() - startTime));
            }
        }
        try {
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (mMission.getErrCode() == -1 && mMission.isRunning() && (mMission.getDone() == mMission.getLength()) || mMission.isFallback()) {
//            mMission.notifyFinished();
//        }
        Log.e(TAG, "thread=" + Thread.currentThread().getName() + " onComplete");
        emitter.onComplete();
    }
}
