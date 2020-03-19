package com.zpj.downloader.core;

import android.text.TextUtils;
import android.util.Log;

import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.ErrorCode;
import com.zpj.downloader.constant.ResponseCode;
import com.zpj.downloader.util.io.BufferedRandomAccessFile;
import com.zpj.downloader.util.permission.PermissionUtil;
import com.zpj.http.ZHttp;
import com.zpj.http.core.Connection;
import com.zpj.http.core.IHttp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public class DownloadRunnable implements Runnable {
    private final String TAG;

    //    private static final int BUFFER_SIZE = 512;
    private final int BUFFER_SIZE;

    private final DownloadMission mMission;
    private int mId;

    //    private final byte[] buf = new byte[BUFFER_SIZE];
    private final byte[] buf;

    private BufferedRandomAccessFile f;

    DownloadRunnable(DownloadMission mission, int id) {
        BUFFER_SIZE = mission.getMissionConfig().getBufferSize();
        buf = new byte[BUFFER_SIZE];
        mMission = mission;
        TAG = "DownloadRunnable-" + id;
        mId = id;
        try {
            f = new BufferedRandomAccessFile(mMission.getFilePath(), "rw");
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof FileNotFoundException) {
                notifyError(Error.FILE_NOT_FOUND);
            } else {
                if (PermissionUtil.checkStoragePermissions(DownloadManagerImpl.getInstance().getContext())) {
                    notifyError(new Error(e.getMessage()));
                } else {
                    notifyError(Error.WITHOUT_STORAGE_PERMISSIONS);
                }
            }
        }
    }

    @Override
    public void run() {
        if (mMission.isFallback()) {
            try {
                HttpURLConnection conn = HttpUrlConnectionFactory.getConnection(mMission);
                if (conn.getResponseCode() / 100 != 2) {
                    Log.d("DownRunFallback", "error:206");
                    notifyError(Error.SERVER_UNSUPPORTED);
                    return;
                } else {
                    f.seek(0);
                    BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());

                    int total = 0;
//					int lastTotal = 0;
                    while (mMission.isRunning()) {
                        long readStartTime = System.currentTimeMillis();
                        final int len = ipt.read(buf, 0, BUFFER_SIZE);
                        long readFinishedTime = System.currentTimeMillis();
                        Log.d(TAG, "readTime=" + (readFinishedTime - readStartTime));
                        if (len == -1) {
                            notifyProgress(0);
                            break;
                        }
                        total += len;
                        f.write(buf, 0, len);
                        f.flush();
                        notifyProgress(len);
//						notifyProgress(total - lastTotal);
//						lastTotal = total;
                        mMission.setLength(total);


                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        Log.d(TAG, "writeTime=" + (System.currentTimeMillis() - readFinishedTime));
                    }

//					f.close();
                    ipt.close();
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
//				notifyError(ErrorCode.ERROR_CONNECTION_TIMED_OUT);
                notifyError(new Error(e.getMessage()));
                return;
            }
        } else {
            mMission.setErrCode(-1);

            Log.d(TAG, "isRunning=" + mMission.isRunning());
            Log.d(TAG, "blocks=" + mMission.getBlocks());
            while (mMission.getErrCode() == -1 && mMission.isRunning()) {

                Log.d(TAG, "----------------------------start-------------------------");
                long time0 = System.currentTimeMillis();

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                long position = mMission.getPosition();
                Log.d(TAG, "position=" + position + " blocks=" + mMission.getBlocks());
                if (position < 0 || position > mMission.getBlocks()) {
                    break;
                }

//				Log.d(TAG, ":preserving position " + position);

                long start = position * mMission.getBlockSize();
                if (start >= mMission.getLength()) {
                    continue;
                }

                long end = start + mMission.getBlockSize() - 1;
                if (end >= mMission.getLength()) {
                    end = mMission.getLength() - 1;
                }

//				HttpURLConnection conn;

                int total = 0;

                long time_0 = System.currentTimeMillis();
                Log.d(TAG, "prepare time=" + (time_0 - time0));
                try {

                    Connection.Response response = ZHttp.get(mMission.getUrl())
                            .range("bytes=" + start + "-" + end)
                            .timeout(mMission.getConnectOutTime())
                            .cookie(mMission.getCookie())
                            .userAgent(mMission.getUserAgent())
                            .referer(mMission.getUrl())
                            .headers(mMission.getHeaders())
                            .validateTLSCertificates(true)
                            .ignoreContentType(true)
                            .syncExecute();
                    Log.d(TAG, "Content-Length=" + response.header("Content-Length")
                            + " Code:" + response.statusCode());

                    if (response.statusCode() != HttpURLConnection.HTTP_PARTIAL) {
                        mMission.onPositionDownloadFailed(position);
                        notifyError(Error.getHttpError(response.statusCode()));
                        Log.d(TAG, "DownRun Unsupported " + response.statusCode());
                        return;
                    }

                    long time_1 = System.currentTimeMillis();
                    Log.d(TAG, "connect time=" + (time_1 - time_0));

                    f.seek(start);
                    BufferedInputStream ipt = response.bodyStream();

                    long time1 = System.currentTimeMillis();
                    Log.d(TAG, "create BufferedInputStream time=" + (time1 - time_1));


//					conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);
//
//					Log.d(TAG, "Content-Length=" + conn.getContentLength()
//							+ " Code:" + conn.getResponseCode()
//							+ " range=" + conn.getRequestProperty("Range"));
//
//					if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
//							|| conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
//							|| conn.getResponseCode() == HttpURLConnection.HTTP_MULT_CHOICE) {
//						String redictUrl = conn.getHeaderField("location");
//						Log.d(TAG, "redictUrl=" + redictUrl);
//						mMission.setUrl(redictUrl);
//						mMission.setRedirectUrl(redictUrl);
//						conn.disconnect();
//						conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);
//					}
//
//					// A server may be ignoring the range requet
//					if (conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
//						mMission.onPositionDownloadFailed(position);
//						notifyError(Error.getHttpError(conn.getResponseCode()));
//						Log.d(TAG, "DownRun Unsupported " + conn.getResponseCode());
//						return;
//					}
//
//					long time_1 = System.currentTimeMillis();
//					Log.d(TAG, "connect time=" + (time_1 - time_0));
//
//					f.seek(start);
//					BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
//					long time1 = System.currentTimeMillis();
//					Log.d(TAG, "create BufferedInputStream time=" + (time1 - time_1));
                    int i = 0;
                    int writeTime = 0;
                    int notifyTime = 0;
                    int readTime = 0;

                    while (start < end && mMission.isRunning()) {
                        i++;
                        long time3 = System.currentTimeMillis();
                        final int len = ipt.read(buf, 0, BUFFER_SIZE);
                        long timet = System.currentTimeMillis();
                        readTime += (timet - time3);

                        if (len == -1) {
                            break;
                        } else {
                            start += len;
                            total += len;
                            long time_4 = System.currentTimeMillis();
                            f.write(buf, 0, len);
                            long time4 = System.currentTimeMillis();
                            writeTime += (time4 - time_4);

                            notifyProgress(len);
                            notifyTime += (System.currentTimeMillis() - time4);
                        }
                    }

                    Log.d(TAG, "io writeTime=" + writeTime);
                    Log.d(TAG, "io notifyTime=" + notifyTime);
                    Log.d(TAG, "io readTime=" + readTime);
                    ipt.close();
//					conn.disconnect();

                    long time2 = System.currentTimeMillis();
                    Log.d(TAG, "write time=" + (time2 - time1));
                    mMission.preserveBlock(position);
                    Log.d(TAG, "position " + position + " finished, total length " + total);
                    // TODO We should save progress for each thread
                } catch (IOException e) {

                    notifyProgress(-total);
                    mMission.onPositionDownloadFailed(position);

                    Log.d(TAG, mId + ":position " + position + " retrying");
                }
                Log.d(TAG, "finished time=" + (System.currentTimeMillis() - time0));
                Log.d(TAG, "----------------------------finished-------------------------");
            }
        }

        Log.d(TAG, "thread " + mId + " exited main loop");
        Log.d(TAG, "mMission.getDone()=" + mMission.getDone());
        Log.d(TAG, "mMission.getLength()=" + mMission.getLength());
        if (mMission.getErrCode() == -1 && mMission.isRunning() && (mMission.getDone() == mMission.getLength() || mMission.isFallback())) {
            Log.d(TAG, "no error has happened, notifying");
            notifyFinished();
        }

//        if (!mMission.isRunning()) {
//            Log.d(TAG, "The mission has been paused. Passing.");
//        }
        try {
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyProgress(final int len) {
//        Log.d(TAG, "notifyProgress len=" + len);
        synchronized (mMission) {
            mMission.notifyProgress(len);
        }
    }

//    private void notifyError(final int err) {
//        synchronized (mMission) {
//            mMission.notifyError(err, true);
//        }
//    }

    private void notifyError(final Error e) {
        synchronized (mMission) {
            mMission.notifyError(e, true);
        }
    }

    private void notifyFinished() {
        synchronized (mMission) {
            mMission.notifyFinished();
        }
    }
}
