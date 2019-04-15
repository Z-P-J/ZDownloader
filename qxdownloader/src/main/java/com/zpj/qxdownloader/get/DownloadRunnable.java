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

//	private static final byte[] buf = new byte[1024];
	
	DownloadRunnable(DownloadMission mission, int id) {
		mMission = mission;
		mId = id;
	}
	
	@Override
	public void run() {
		boolean retry = mMission.recovered;
		long position = mMission.getPosition(mId);

		Log.d(TAG, mId + ":default pos " + position);
		Log.d(TAG, mId + ":recovered: " + mMission.recovered);

		mMission.errCode = -1;
		
		while (mMission.errCode == -1 && mMission.running && position < mMission.blocks) {
			
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


				final BufferedRandomAccessFile f = new BufferedRandomAccessFile(mMission.location + "/" + mMission.name, "rw");
				f.seek(start);
				BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
				final byte[] buf = new byte[1024];

				Log.d("available", "len=" + ipt.available());
				while (start < end && mMission.running) {
					final int len = ipt.read(buf, 0, 1024);

					if (len == -1) {
						break;
					} else {
						start += len;
						total += len;
//						new Thread(new Runnable() {
//							@Override
//							public void run() {
//								synchronized (buf) {
//									try {
//										f.write(buf, 0, len);
//									} catch (IOException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//						}).start();
						f.write(buf, 0, len);

						Log.d("len", "len=" + len);
						notifyProgress(len);
					}
				}

				ipt.close();
				f.close();
				conn.disconnect();

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
