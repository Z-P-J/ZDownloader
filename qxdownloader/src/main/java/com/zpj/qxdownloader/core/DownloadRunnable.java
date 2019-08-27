package com.zpj.qxdownloader.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.zpj.qxdownloader.config.ThreadPoolConfig;
import com.zpj.qxdownloader.constant.ErrorCode;
import com.zpj.qxdownloader.constant.ResponseCode;
import com.zpj.qxdownloader.util.ThreadPoolFactory;
import com.zpj.qxdownloader.util.io.BufferedRandomAccessFile;
import com.zpj.qxdownloader.util.permission.PermissionUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadRunnable implements Runnable {
    private static final String TAG = DownloadRunnable.class.getSimpleName();

    private static final int BUFFER_SIZE = 512;

    private final DownloadMission mMission;
    private int mId;

    private Handler mHandler;

    private final byte[] buf = new byte[BUFFER_SIZE];

    private BufferedRandomAccessFile f;

    DownloadRunnable(DownloadMission mission, int id) {
        mMission = mission;
        mId = id;
        try {
            f = new BufferedRandomAccessFile(mMission.getFilePath(), "rw");
        } catch (IOException e) {
            e.printStackTrace();
//			PermissionUtil.hasPermissions(DownloadManagerImpl.getInstance().getContext(), Permission.Group.STORAGE);
            if (PermissionUtil.checkStoragePermissions(DownloadManagerImpl.getInstance().getContext())) {
                notifyError(ErrorCode.ERROR_FILE_NOT_FOUND);
            } else {
                notifyError(ErrorCode.ERROR_WITHOUT_STORAGE_PERMISSIONS);
            }

            return;
        }

        HandlerThread thread = new HandlerThread("ServiceMessenger");
        thread.start();

        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int obj = (int) msg.obj;
                    synchronized (mMission) {
                        mMission.notifyProgress(obj);
                    }
                }
            }
        };
    }

    @Override
    public void run() {

//        try {
//            ThreadPoolExecutor writeExecutor = ThreadPoolFactory.newFixedThreadPool(ThreadPoolConfig.build().setCorePoolSize(1));
//            HttpURLConnection conn = HttpUrlConnectionFactory.getConnection(mMission);
//            if (conn.getResponseCode() != ResponseCode.RESPONSE_200 && conn.getResponseCode() != ResponseCode.RESPONSE_206) {
//                Log.d("DownRunFallback", "getResponseCode=" + conn.getResponseCode());
//                notifyError(ErrorCode.ERROR_SERVER_UNSUPPORTED);
//            } else {
//                f.seek(0);
//                BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
//                int total = 0;
//                int lastTotal = 0;
////                byte[] buf = new byte[2048];
//                mMission.length = 0;
//                notifyProgress(0);
//                while (mMission.isRunning()) {
//                    long readStartTime = System.currentTimeMillis();
//                    final int len = ipt.read(buf, 0, BUFFER_SIZE);
//                    long readFinishedTime = System.currentTimeMillis();
//                    Log.d(TAG, "readTime=" + (readFinishedTime - readStartTime));
//                    if (len == -1) {
//                        notifyProgress(0);
//                        Log.d(TAG, "len == -1");
//                        break;
//                    }
//                    total += len;
////                    writeExecutor.execute(new Runnable() {
////                        @Override
////                        public void run() {
////                            try {
////                                f.write(buf, 0, len);
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    });
//                    f.write(buf, 0, len);
//
//                    notifyProgress(total - lastTotal);
//                    lastTotal = total;
//                    mMission.length = total;
//
//
//                    if (Thread.currentThread().isInterrupted()) {
//                        Log.d(TAG, "isInterrupted");
//                        break;
//                    }
//                    Log.d(TAG, "writeTime=" + (System.currentTimeMillis() - readFinishedTime));
//                }
//                ipt.close();
//                conn.disconnect();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

		if (mMission.fallback) {
			try {
				ThreadPoolExecutor writeExecutor = ThreadPoolFactory.newFixedThreadPool(ThreadPoolConfig.build().setCorePoolSize(1));
				HttpURLConnection conn = HttpUrlConnectionFactory.getConnection(mMission);

				if (conn.getResponseCode() != ResponseCode.RESPONSE_200 && conn.getResponseCode() != ResponseCode.RESPONSE_206) {
					Log.d("DownRunFallback", "error:206");
					notifyError(ErrorCode.ERROR_SERVER_UNSUPPORTED);
				} else {
//					BufferedRandomAccessFile f = new BufferedRandomAccessFile(mMission.location + "/" + mMission.name, "rw");
					f.seek(0);
					BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());

					int total = 0;
					int lastTotal = 0;
					while (mMission.isRunning()) {
						long readStartTime = System.currentTimeMillis();
						final int len  = ipt.read(buf, 0, BUFFER_SIZE);
						long readFinishedTime = System.currentTimeMillis();
						Log.d(TAG, "readTime=" + (readFinishedTime - readStartTime));
						if (len == -1) {
							notifyProgress(0);
							break;
						}
						total += len;
//						writeExecutor.execute(new Runnable() {
//							@Override
//							public void run() {
//								try {
//									f.write(buf, 0, len);
//									f.flush();
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//							}
//						});
						f.write(buf, 0, len);
						f.flush();

//						if (total % (256 * 1024) == 0) {
//							notifyProgress(total - lastTotal);
//							lastTotal = total;
//						}
						notifyProgress(total - lastTotal);
						lastTotal = total;
						mMission.length = total;


						if (Thread.currentThread().isInterrupted()) {
							break;
						}
						Log.d(TAG, "writeTime=" + (System.currentTimeMillis() - readFinishedTime));
					}

//					f.close();
					ipt.close();
					conn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			boolean retry = mMission.recovered;
			long position = mMission.getPosition(mId);

			Log.d(TAG, mId + ":default pos " + position);
			Log.d(TAG, mId + ":recovered: " + mMission.recovered);

			mMission.errCode = -1;

			while (mMission.errCode == -1 && mMission.isRunning() && position < mMission.blocks) {

				Log.d("timetimetimetime" + mId, "----------------------------start-------------------------");
				long time0 = System.currentTimeMillis();

				if (Thread.currentThread().isInterrupted()) {
					mMission.pause();
					return;
				}

				Log.d(TAG, mId + ":retry is true. Resuming at " + position);

				// Wait for an unblocked position
				while (!retry && position < mMission.blocks && mMission.isBlockPreserved(position)) {

					Log.d(TAG, mId + ":position " + position + " preserved, passing");

					position++;
				}

				retry = false;

				if (position >= mMission.blocks) {
					break;
				}

				Log.d(TAG, mId + ":preserving position " + position);

				mMission.preserveBlock(position);
				mMission.setPosition(mId, position);

				long start = position * mMission.getBlockSize();
				long end = start + mMission.getBlockSize() - 1;

				if (start >= mMission.length) {
					continue;
				}

				if (end >= mMission.length) {
					end = mMission.length - 1;
				}

				HttpURLConnection conn;

				int total = 0;

				long time_0 = System.currentTimeMillis();
				Log.d("timetimetimetime" + mId, "timetime=" + (time_0 - time0));
				try {
//				OkHttpClient client = new OkHttpClient();
//				Request request = new Request.Builder()
//						.url(mMission.url)
//						.addHeader("User-Agent", "")
//						.addHeader("Cookie", "")
//						.addHeader("Accept", "*/*")
//						.addHeader("Referer","https://pan.baidu.com/disk/home")
//						.addHeader("Pragma", "no-cache")
//						.addHeader("Cache-Control", "no-cache")
//						.addHeader("Want-Digest", "SHA-512;q=1, SHA-256;q=1, SHA;q=0.1")
//						.addHeader("Range", "bytes=" + start + "-" + end)
//						.build();
//
//				Response res = client.newCall(request).execute();


					conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);

					Log.d(TAG, mId + ":" + conn.getRequestProperty("Range"));
					Log.d(TAG, mId + ":Content-Length=" + conn.getContentLength() + " Code:" + conn.getResponseCode());

					if (conn.getResponseCode() == ResponseCode.RESPONSE_302
							|| conn.getResponseCode() == ResponseCode.RESPONSE_301
							|| conn.getResponseCode() == ResponseCode.RESPONSE_300) {
						String redictUrl = conn.getHeaderField("location");
						Log.d(TAG, "redictUrl=" + redictUrl);
						mMission.url = redictUrl;
						mMission.redictUrl = redictUrl;
						conn.disconnect();
						conn = HttpUrlConnectionFactory.getConnection(mMission, start, end);
					}

					// A server may be ignoring the range requet
					if (conn.getResponseCode() != ResponseCode.RESPONSE_206) {
						mMission.errCode = ErrorCode.ERROR_SERVER_UNSUPPORTED;
						Log.d("DownRun", "error:206");
						notifyError(ErrorCode.ERROR_SERVER_UNSUPPORTED);

						Log.e(TAG, mId + ":Unsupported " + conn.getResponseCode());

						break;
					}

					long time_1 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "ttttttttt=" + (time_1 - time_0));

					f.seek(start);
//
					BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
//
					long time1 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "hhhhhhhhhh=" + (time1 - time_1));
					int i = 0;
					int tempTime = 0;
					int tempTime2 = 0;
					int tempTime3 = 0;
					int tempTime4 = 0;

					while (start < end && mMission.isRunning()) {
						i++;
						long time3 = System.currentTimeMillis();
						final int len = ipt.read(buf, 0, BUFFER_SIZE);
						long timet = System.currentTimeMillis();
						tempTime4 += (timet - time3);

						if (len == -1) {
							break;
						} else {
							start += len;
							total += len;
							long time_4 = System.currentTimeMillis();
							f.write(buf, 0, len);

							Log.d("len", "len=" + len);
							long time4 = System.currentTimeMillis();
							tempTime2 += (time4 - time_4);
//							notifyProgress(len, false);
							long time5 = System.currentTimeMillis();
							tempTime3 += (time5 - time4);
//							Log.d("timetimetimetime" + mId, "time2222 = " + (time5 - time4));
							tempTime += (time5 - time3);
						}
					}


					notifyProgress(total);

					Log.d("timetimetimetime" + mId, "tempTime=" + tempTime);
					Log.d("timetimetimetime" + mId, "tempTime2=" + tempTime2);
					Log.d("timetimetimetime" + mId, "tempTime3=" + tempTime3);
					Log.d("timetimetimetime" + mId, "tempTime4=" + tempTime4);
//					Log.d("timetimetimetime" + mId, "i=" + i);
					ipt.close();
					conn.disconnect();

					long time2 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "time3333=" + (time2 - time1));
					Log.d("timetimetimetime" + mId, "----------------------------finished-------------------------");
					Log.d(TAG, mId + ":position " + position + " finished, total length " + total);



					// TODO We should save progress for each thread
				} catch (Exception e) {
					// TODO Retry count limit & notify error
					retry = true;

					notifyProgress(-total);

					Log.d(TAG, mId + ":position " + position + " retrying");
				}
			}

		}

        Log.d(TAG, "thread " + mId + " exited main loop");

        if (mMission.errCode == -1 && mMission.isRunning()) {
            Log.d(TAG, "no error has happened, notifying");
            notifyFinished();
        }

        if (!mMission.isRunning()) {
            Log.d(TAG, "The mission has been paused. Passing.");
        }
        try {
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyProgress(final int len) {
        Log.d("notifyProgress", "len=" + len);
        Message msg = new Message();
        msg.obj = len;
        msg.what = 0;
        mHandler.sendMessage(msg);
    }

    private void notifyError(final int err) {
        synchronized (mMission) {
            mMission.pause();
            mMission.notifyError(err);
        }
    }

    private void notifyFinished() {
        synchronized (mMission) {
            mMission.notifyFinished();
        }
    }
}
