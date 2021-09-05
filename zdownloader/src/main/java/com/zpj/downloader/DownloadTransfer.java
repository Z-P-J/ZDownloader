package com.zpj.downloader;

import android.util.Log;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.utils.io.BufferedRandomAccessFile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * 下载任务网络传输器
 * @author Z-P-J
 */
abstract class DownloadTransfer implements Runnable {

    private static final String TAG = "DownloadProducer";

    private final BaseMission<?> mMission;
    private final int blockSize;

    DownloadTransfer(BaseMission<?> mission) {
        this.mMission = mission;
        this.blockSize = mission.getBlockSize();
    }

    @Override
    public void run() {
        byte[] buf = new byte[mMission.bufferSize];
        BufferedRandomAccessFile f = null;
        Error error = null;
        try {
            f = new BufferedRandomAccessFile(mMission.getFilePath(), "rw");
            if (mMission.isBlockDownload()) {
                while (true) {
                    long startTime = System.currentTimeMillis();
                    if (!mMission.isRunning()) {
                        break;
                    }

                    long position = mMission.getNextPosition();
                    if (position < 0 || position >= mMission.getBlocks()) {
                        break;
                    }

                    long start = position * blockSize;
                    long end = start + blockSize - 1;

                    if (start >= mMission.getLength()) {
                        continue;
                    }

                    if (end >= mMission.getLength()) {
                        end = mMission.getLength() - 1;
                    }

                    HttpURLConnection conn = null;

                    int total = 0;
                    try {
                        conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);
                        if (conn.getResponseCode() / 100 == 3) {
                            String redictUrl = conn.getHeaderField("location");
                            Log.d(TAG, "redictUrl=" + redictUrl);
                            mMission.setUrl(redictUrl);
                            conn.disconnect();
                            conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);
                        }

                        if (conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                            mMission.onPositionDownloadFailed(position);
                            error = new Error(conn.getResponseMessage());
                            break;
                        }

                        f.seek(start);
                        InputStream stream = conn.getInputStream();
                        BufferedInputStream ipt = new BufferedInputStream(stream);
                        while (start < end) { //  && mMission.isRunning()
                            final int len = ipt.read(buf, 0, buf.length);
                            if (len == -1) {
                                break;
                            } else {
                                start += len;
                                f.write(buf, 0, len);
                                total += len;
                                mMission.notifyDownloaded(len);
                            }
                        }
                        ipt.close();
                        f.flush();
                        mMission.onBlockFinished(position);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mMission.notifyDownloaded(-total);
                        mMission.onPositionDownloadFailed(position);
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                    Log.d(TAG, "DownloadBlockProducer Finished Time=" + (System.currentTimeMillis() - startTime));
                }
            } else {
                HttpURLConnection conn = HttpUrlConnectionFactory.getConnection(mMission);
                if (conn.getResponseCode() / 100 == 2) {
                    f.seek(0);
                    BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());

                    long total = 0;
                    while (mMission.isRunning()) {
                        long readStartTime = System.currentTimeMillis();
                        final int len  = ipt.read(buf, 0, buf.length);
                        long readFinishedTime = System.currentTimeMillis();
//                        Log.d(TAG, "readTime=" + (readFinishedTime - readStartTime));
                        if (len == -1) {
                            mMission.notifyDownloaded(0);
                            break;
                        }
                        total += len;
                        f.write(buf, 0, len);
//						f.flush();
                        mMission.notifyDownloaded(len);
//						notifyProgress(total - lastTotal);
//						lastTotal = total;
                        mMission.setLength(total);


                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                    ipt.close();
                } else {
                    Log.d("DownRunFallback", "error:206");
                    error = Error.SERVER_UNSUPPORTED;
                }
                conn.disconnect();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            error = Error.FILE_NOT_FOUND;
        } catch (IOException e) {
            e.printStackTrace();
            error = new Error(e.getMessage());
        } finally {
            try {
                if (f != null) {
                    f.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG, "thread=" + Thread.currentThread().getName() + " onComplete");
        onFinished(this, error);
    }

    public abstract void onFinished(DownloadTransfer transfer, Error error);

}

