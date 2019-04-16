package com.zpj.qxdownloader.get;

import android.text.TextUtils;
import android.util.Log;

import com.zpj.qxdownloader.io.BufferedRandomAccessFile;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadRunnable implements Runnable
{
	private static final String TAG = DownloadRunnable.class.getSimpleName();
	
	private final DownloadMission mMission;
	private int mId;

	private final byte[] buf = new byte[8 * 1024];
	
	DownloadRunnable(DownloadMission mission, int id) {
		mMission = mission;
		mId = id;
	}
	
	@Override
	public void run() {

		if (mMission.fallback) {
			try {
//				URL url = null;
//				url = new URL(mMission.url);

//				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//				conn.setRequestProperty("Cookie", mMission.cookie);
//				conn.setRequestProperty("User-Agent", mMission.user_agent);
//			conn.setRequestProperty("Accept", "*/*");
//			conn.setRequestProperty("Referer","https://pan.baidu.com/disk/home");
//			conn.setRequestProperty("Want-Digest", "SHA-512;q=1, SHA-256;q=1, SHA;q=0.1");
//			conn.setRequestProperty("Pragma", "no-cache");
//			conn.setRequestProperty("Cache-Control", "no-cache");

				HttpURLConnection conn = getConnection(mMission.url, 0, mMission.length);

				if (conn.getResponseCode() != 200 && conn.getResponseCode() != 206) {
					Log.d("DownRunFallback", "error:206");
					notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
				} else {
					BufferedRandomAccessFile f = new BufferedRandomAccessFile(mMission.location + "/" + mMission.name, "rw");
					f.seek(0);
					BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());

					int len = 0;

					while ((len = ipt.read(buf, 0, 1024)) != -1 && mMission.running) {
						f.write(buf, 0, len);
						notifyProgress(len);

						if (Thread.currentThread().isInterrupted()) {
							break;
						}

					}

					f.close();
					ipt.close();
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

			while (mMission.errCode == -1 && mMission.running && position < mMission.blocks) {

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

				long start = position * mMission.block_size;
				long end = start + mMission.block_size - 1;

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


					conn = getConnection(mMission.url, start, end);

					Log.d(TAG, mId + ":" + conn.getRequestProperty("Range"));
					Log.d(TAG, mId + ":Content-Length=" + conn.getContentLength() + " Code:" + conn.getResponseCode());

					if (conn.getResponseCode() == 302 || conn.getResponseCode() == 301 || conn.getResponseCode() == 300) {
						String redictUrl = conn.getHeaderField("location");
						Log.d(TAG, "redictUrl=" + redictUrl);
						mMission.url = redictUrl;
						mMission.redictUrl = redictUrl;
						conn.disconnect();
						conn = getConnection(redictUrl, start, end);
					}

					// A server may be ignoring the range requet
					if (conn.getResponseCode() != 206) {
						mMission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED;
						Log.d("DownRun", "error:206");
						notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);

						Log.e(TAG, mId + ":Unsupported " + conn.getResponseCode());

						break;
					}


//				Connection.Response response = Jsoup.connect(mMission.url)
//						.method(Connection.Method.GET)
//						.proxy(Proxy.NO_PROXY)
//						.userAgent(UAHelper.getPCBaiduUA())
//						.header("Cookie", UserHelper.getBduss())
//						.header("Accept", "*/*")
//						.header("Referer","https://pan.baidu.com/disk/home")
//						.header("Pragma", "no-cache")
//						.header("Range", "bytes=" + start + "-" + end)
//						.header("Cache-Control", "no-cache")
//						.timeout(100000)
//						.ignoreContentType(true)
//						.ignoreHttpErrors(true)
//						.maxBodySize(0)
//						.execute();
//
//				// A server may be ignoring the range requet
//				if (response.statusCode() != 206) {
//					mMission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED;
//					Log.d("DownRun", "error:206");
//					notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
//					break;
//				}

//				IOHelper.write(new IORunnable(mMission.location + File.separator + mMission.name, conn.getInputStream(), start, end, DownloadRunnable.this));
//				notifyProgress(end - start);


					long time_1 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "ttttttttt=" + (time_1 - time_0));
					final BufferedRandomAccessFile f = new BufferedRandomAccessFile(mMission.location + "/" + mMission.name, "rw");
					f.seek(start);
					BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
//					final byte[] buf = new byte[2048];

					long time1 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "hhhhhhhhhh=" + (time1 - time_1));
					int i = 0;
					int tempTime = 0;
					int tempTime2 = 0;
					int tempTime3 = 0;
					while (start < end && mMission.running) {
						i++;
						long time3 = System.currentTimeMillis();
						final int len = ipt.read(buf, 0, 8 * 1024);

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
//							Log.d("timetimetimetime" + mId, "time1111 = " + (time4 - time3));
							notifyProgress(len);
							long time5 = System.currentTimeMillis();
							tempTime3 += (time5 - time4);
//							Log.d("timetimetimetime" + mId, "time2222 = " + (time5 - time4));
							tempTime += (time5 - time3);
						}
					}
					Log.d("timetimetimetime" + mId, "tempTime=" + tempTime);
					Log.d("timetimetimetime" + mId, "tempTime2=" + tempTime2);
					Log.d("timetimetimetime" + mId, "tempTime3=" + tempTime3);
					Log.d("timetimetimetime" + mId, "i=" + i);
					ipt.close();
					f.close();
//					conn.disconnect();

					long time2 = System.currentTimeMillis();
					Log.d("timetimetimetime" + mId, "time3333=" + (time2 - time1));
					Log.d("timetimetimetime" + mId, "----------------------------finished-------------------------");
					Log.d(TAG, mId + ":position " + position + " finished, total length " + total);


//				new Thread(new IORunnable(mMission.location + File.separator + mMission.name, conn.getInputStream(), start, end, DownloadRunnable.this)).start();
//				notifyProgress(end - start);



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

		if (mMission.errCode == -1 && mMission.running) {
			Log.d(TAG, "no error has happened, notifying");
			notifyFinished();
		}

		if (!mMission.running) {
			Log.d(TAG, "The mission has been paused. Passing.");
		}
	}

	private HttpURLConnection getConnection(String link, long start, long end) throws Exception {
		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		if (!TextUtils.isEmpty(mMission.cookie.trim())) {
			conn.setRequestProperty("Cookie", mMission.cookie);
		}
		conn.setRequestProperty("User-Agent", mMission.user_agent);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Referer",mMission.url);
		conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
		return conn;
	}
	
	public void notifyProgress(final long len) {
		synchronized (mMission) {
			mMission.notifyProgress(len);
		}
	}
	
	private void notifyError(final int err) {
		synchronized (mMission) {
			mMission.notifyError(err);
			mMission.pause();
		}
	}
	
	private void notifyFinished() {
		synchronized (mMission) {
			mMission.notifyFinished();
		}
	}
}
