package com.zpj.qxdownloader.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LongSparseArray;

import com.google.gson.Gson;
import com.zpj.qxdownloader.config.MissionConfig;
import com.zpj.qxdownloader.constant.ErrorCode;
import com.zpj.qxdownloader.util.ThreadPoolFactory;
import com.zpj.qxdownloader.util.Utility;
import com.zpj.qxdownloader.util.notification.NotifyUtil;
import com.zpj.qxdownloader.util.notification.builder.ProgressBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Z-P-J
 */
public class DownloadMission {
	private static final String TAG = DownloadMission.class.getSimpleName();
	
	public interface MissionListener {
		HashMap<MissionListener, Handler> HANDLER_STORE = new HashMap<>();

		void onInit();
		void onStart();
		void onPause();
		void onWaiting();
		void onRetry();
		void onProgress(long done, long total);
		void onFinish();
		void onError(int errCode);
	}

	public enum MissionState {
		INITING,
		RUNNING,
		WAITING,
		PAUSE,
		FINISHED,
		ERROR
	}

	public String uuid = "";

	public String name = "";

	public String url = "";

	public String redictUrl = "";

	public String originUrl = "";

//	public String location = "";
//	public String cookie = "";
//	public String userAgent = "";

	public long createTime = 0;

	public int notifyId = 0;

	public long blocks = 0;
//	public int bufferSize = DefaultConstant.BUFFER_SIZE;
//	public int blockSize = DefaultConstant.BLOCK_SIZE;

	public long length = 0;

	public long done = 0;

	public int finishCount = 0;
	//单位毫秒
//	public int retryDelay = DefaultConstant.RETRY_DELAY;
//	public int connectOutTime = DefaultConstant.CONNECT_OUT_TIME;
//	public int readOutTime = DefaultConstant.READ_OUT_TIME;

	public List<Long> threadPositions = new ArrayList<>();

	public transient int a;
//	public final LongSparseBooleanArray longSparseBooleanArray = new LongSparseBooleanArray();

	public final LongSparseArray<Boolean> blockState = new LongSparseArray<>();

	public MissionState missionState = MissionState.INITING;

//	public boolean running = false;
//
//	public boolean waiting = false;
//
//	public boolean finished = false;

	public boolean fallback = false;

	public int errCode = -1;

	public long timestamp = 0;

	public boolean hasInit = false;

	public MissionConfig missionConfig = MissionConfig.with();

	public transient boolean recovered = false;

	public transient int currentRetryCount = missionConfig.getRetryCount();

	public transient int threadCount = missionConfig.getThreadPoolConfig().getCorePoolSize();
//	public transient int maximumPoolSize = missionConfig.getThreadPoolConfig().getMaximumPoolSize();
//	public transient int keepAliveTime = missionConfig.getThreadPoolConfig().getKeepAliveTime();
	
//	private transient ArrayList<WeakReference<MissionListener>> mListeners = new ArrayList<>();
//	private transient WeakReference<MissionListener> missionListener;
	private transient MissionListener missionListener;
	private transient boolean mWritingToFile = false;

	private transient int errorCount = 0;

	private transient ThreadPoolExecutor threadPoolExecutor;

//	private transient final ProgressBuilder progressBuilder = new ProgressBuilder();

	public void initNotification() {
//		progressBuilder.setId(getId());
//		progressBuilder.setSmallIcon(android.R.mipmap.sym_def_app_icon);
	}
	
	public boolean isBlockPreserved(long block) {
		Boolean state = blockState.get(block);
		return state != null && state;
//		return blockState.containsKey(block) ? blockState.get(block) : false;
	}
	
	public void preserveBlock(long block) {
		synchronized (blockState) {
			blockState.put(block, true);
		}
	}

//	public void setThreadPoolConfig(ThreadPoolConfig config) {
//		this.config = config;
////		threadCount = config.getCorePoolSize();
////		keepAliveTime = config.getKeepAliveTime();
////		maximumPoolSize = config.getMaximumPoolSize();
//	}

	public float getProgress() {
		if (missionState == MissionState.FINISHED) {
			return 100f;
		} else if (fallback) {
			return 0f;
		}
		float progress = (float) done / (float) length;
		return progress * 100f;
	}

	private int getId() {
		if (notifyId == 0) {
			notifyId = (int)(createTime / 10000) + (int) (createTime % 10000) * 100000;
		}
		return notifyId;
	}
	
	public void setPosition(int id, long position) {
		threadPositions.set(id, position);
	}
	
	public long getPosition(int id) {
		return threadPositions.get(id);
	}
	
	public synchronized void notifyProgress(long deltaLen) {
		if (missionState != MissionState.RUNNING) {
			return;
		}
		
		if (recovered) {
			recovered = false;
		}
		
		done += deltaLen;
		
		if (done > length) {
			done = length;
		}
		
		if (done != length) {
			Log.d(TAG, "已下载");
			writeThisToFile();
			threadPoolExecutor.submit(progressRunnable);



//			long time1 = System.currentTimeMillis();
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					progressBuilder
//							.setProgressAndFormat(getProgress(),false, "")
//							.setContentTitle("已下载：" + name)
//							.setPause(false)
//							.setId(getId())
//							.show();
//				}
//			}).start();

//			long time2 = System.currentTimeMillis();
//			Log.d("timetimetimetime0", "tempTime3=" + (time2 - time1));

//			if (missionListener != null) {
//				MissionListener.HANDLER_STORE.get(missionListener).post(progressRunnable);
//			}

//			Log.d("timetimetimetime0", "mListeners.size=" + mListeners.size());
//			for (WeakReference<MissionListener> ref: mListeners) {
//				final MissionListener listener = ref.get();
//				if (listener != null) {
//					MissionListener.HANDLER_STORE.get(listener).post(new Runnable() {
//						@Override
//						public void run() {
//							listener.onProgressUpdate(done, length);
//						}
//					});
//				}
//			}
		}
	}

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
//			if (!mWritingToFile) {
//				mWritingToFile = true;
////				executorService.submit(runnable);
//				doWriteThisToFile();
//				mWritingToFile = false;
//			}
			if (missionListener != null) {
				missionListener.onProgress(done, length);
			}

			NotifyUtil.with(getContext())
					.buildProgressNotify()
					.setProgressAndFormat(getProgress(),false, "")
					.setContentTitle(name)
					.setId(getId())
					.show();
//			if (!mWritingToFile) {
//				mWritingToFile = true;
////				executorService.submit(runnable);
//				doWriteThisToFile();
//				mWritingToFile = false;
//			}
		}
	};
	
	public synchronized void notifyFinished() {
		if (errCode > 0) {
			return;
		}
		
		finishCount++;
		
		if (finishCount == threadCount) {
			onFinish();
		}
	}

	private Runnable finishedRunnable = new Runnable() {
		@Override
		public void run() {
			NotifyUtil.with(getContext())
					.buildNotify()
					.setContentTitle(name)
					.setContentText("下载已完成")
					.setId(getId())
					.show();
		}
	};
	
	private void onFinish() {
		if (errCode > 0) {
			return;
		}

		Log.d(TAG, "onFinish");

//		waiting = false;
//		running = false;
//		finished = true;
		missionState = MissionState.FINISHED;
		
//		deleteThisFromFile();
		writeThisToFile();

		if (missionListener != null) {
			MissionListener.HANDLER_STORE.get(missionListener).post(new Runnable() {
				@Override
				public void run() {
					missionListener.onFinish();
				}
			});
		}

		DownloadManagerImpl.decreaseDownloadingCount();

		threadPoolExecutor.submit(finishedRunnable);

//		for (WeakReference<MissionListener> ref : mListeners) {
//			final MissionListener listener = ref.get();
//			if (listener != null) {
//				MissionListener.HANDLER_STORE.get(listener).post(new Runnable() {
//					@Override
//					public void run() {
//						listener.onFinish();
//					}
//				});
//			}
//		}
	}

	private synchronized void onRetry() {
		if (missionListener != null) {
			MissionListener.HANDLER_STORE.get(missionListener).post(new Runnable() {
				@Override
				public void run() {
					missionListener.onRetry();
				}
			});
		}
	}

	synchronized void notifyError(int err) {

		if (!(err == ErrorCode.ERROR_NOT_HAVE_STORAGE_PERMISSIONS || err == ErrorCode.ERROR_FILE_NOT_FOUND)) {
			errorCount++;
			if (errorCount == threadCount) {
				currentRetryCount--;
				if (currentRetryCount >= 0) {
					pause();
					onRetry();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							start();
						}
					}, missionConfig.getRetryDelay());
					return;
				}
			}
		}

		missionState = MissionState.ERROR;

		currentRetryCount = missionConfig.getRetryCount();

		errCode = err;

		Log.d("eeeeeeeeeeeeeeeeeeee", "error:" + errCode);

		writeThisToFile();

		if (missionListener != null) {
			MissionListener.HANDLER_STORE.get(missionListener).post(new Runnable() {
				@Override
				public void run() {
					missionListener.onError(errCode);
				}
			});
		}

		DownloadManagerImpl.decreaseDownloadingCount();

		NotifyUtil.cancel(getId());
		NotifyUtil.with(getContext())
				.buildNotify()
				.setContentTitle("下载出错" + errCode + ":" + name)
				.setId(getId())
				.show();
	}

	public void waiting() {
		pause();
		notifyWaiting();
	}

	private void notifyWaiting() {
//		waiting = true;
		missionState = MissionState.WAITING;
	}
	
	public synchronized void addListener(MissionListener listener) {
		Handler handler = new Handler(Looper.getMainLooper());
		MissionListener.HANDLER_STORE.put(listener, handler);
//		mListeners.add(new WeakReference<>(listener));
		missionListener = listener;
	}
	
	public synchronized void removeListener(MissionListener listener) {
//		for (Iterator<WeakReference<MissionListener>> iterator = mListeners.iterator();
//             iterator.hasNext(); ) {
//			WeakReference<MissionListener> weakRef = iterator.next();
//			if (listener!=null && listener == weakRef.get())
//			{
//				iterator.remove();
//			}
//		}
		missionListener = null;
	}
	
	public void start() {
		errorCount = 0;
		if (!isRunning() && !isFinished()) {
			initCurrentRetryCount();
			if (DownloadManagerImpl.getInstance().shouldMissionWaiting()) {
				waiting();
				return;
			}

			DownloadManagerImpl.increaseDownloadingCount();

//			waiting = false;
//			running = true;
			missionState = MissionState.RUNNING;

//			ExecutorService executorService;
			if (!fallback) {
//				executorService = Executors.newFixedThreadPool(threadCount);
//				for (int i = 0; i < threadCount; i++) {
//					if (threadPositions.size() <= i && !recovered) {
//						threadPositions.add((long) i);
//					}
//					executorService.submit(new DownloadRunnable(this, i));
////					new Thread(new DownloadRunnable(this, i)).start();
//				}
			} else {
				// In fallback mode, resuming is not supported.
				missionConfig.getThreadPoolConfig().setCorePoolSize(1);
				threadCount = 1;
				done = 0;
				blocks = 0;
//				executorService = Executors.newFixedThreadPool(1);
//				executorService.submit(new DownloadRunnableFallback(this));
			}

			if (threadPoolExecutor == null || threadPoolExecutor.getCorePoolSize() != 2 * threadCount) {
				threadPoolExecutor = ThreadPoolFactory.newFixedThreadPool(missionConfig.getThreadPoolConfig());
			}
			for (int i = 0; i < threadCount; i++) {
				if (threadPositions.size() <= i && !recovered) {
					threadPositions.add((long) i);
				}
				threadPoolExecutor.submit(new DownloadRunnable(this, i));
			}

			writeThisToFile();

			if (missionListener != null) {
				MissionListener.HANDLER_STORE.get(missionListener).post(new Runnable() {
					@Override
					public void run() {
						missionListener.onStart();
					}
				});
			}

//			for (WeakReference<MissionListener> ref: mListeners) {
//				final MissionListener listener = ref.get();
//				if (listener != null) {
//					MissionListener.HANDLER_STORE.get(listener).post(new Runnable() {
//						@Override
//						public void run() {
//							listener.onStart();
//						}
//					});
//				}
//			}
		}
	}

	private Runnable pauseRunnable = new Runnable() {
		@Override
		public void run() {
			NotifyUtil.with(getContext())
					.buildProgressNotify()
					.setProgressAndFormat(getProgress(),false, "")
					.setId(getId())
					.setContentTitle("已暂停：" + name)
					.show();
		}
	};
	
	public void pause() {
		initCurrentRetryCount();
		if (isRunning() || isWaiting()) {
//			running = false;
			missionState = MissionState.PAUSE;
			recovered = true;

			writeThisToFile();

			Log.d(TAG, "已暂停");

			if (missionListener != null) {
				MissionListener.HANDLER_STORE.get(missionListener).post(new Runnable() {
					@Override
					public void run() {
						missionListener.onPause();
					}
				});
			}

			if (missionState != MissionState.WAITING) {
				DownloadManagerImpl.decreaseDownloadingCount();
			}

			NotifyUtil.cancel(getId());
			threadPoolExecutor.submit(pauseRunnable);
		}
	}
	
	public void delete() {
		deleteThisFromFile();
		new File(missionConfig.getDownloadPath() + File.separator + name).delete();
	}

	private Runnable writeRunnable = new Runnable() {
		@Override
		public void run() {
			doWriteThisToFile();
			mWritingToFile = false;
		}
	};
	
	public void writeThisToFile() {
		if (!mWritingToFile) {
			mWritingToFile = true;
			if (threadPoolExecutor == null) {
				threadPoolExecutor = ThreadPoolFactory.newFixedThreadPool(missionConfig.getThreadPoolConfig());
			}
			threadPoolExecutor.submit(writeRunnable);
		}
	}
	
	private void doWriteThisToFile() {
		synchronized (blockState) {
			Utility.writeToFile(DownloadManagerImpl.TASK_PATH + File.separator + uuid + ".zpj", new Gson().toJson(this));
		}
	}
	
	public void deleteThisFromFile() {
		new File(DownloadManagerImpl.TASK_PATH + File.separator + uuid + ".zpj").delete();
	}

	private void initCurrentRetryCount() {
		if (currentRetryCount != missionConfig.getRetryCount()) {
			currentRetryCount = missionConfig.getRetryCount();
		}
	}

	private Context getContext() {
		return DownloadManagerImpl.getInstance().getContext();
	}

	public String getDownloadPath() {
		return missionConfig.getDownloadPath();
	}

	public String getUserAgent() {
		return missionConfig.getUserAgent();
	}

	public String getCookie() {
		return missionConfig.getCookie();
	}

	public int getBlockSize() {
		return missionConfig.getBlockSize();
	}

	public int getConnectOutTime() {
		return missionConfig.getConnectOutTime();
	}

	public int getReadOutTime() {
		return missionConfig.getReadOutTime();
	}

	Map<String, String> getHeaders() {
		return missionConfig.getHeaders();
	}

	public boolean isIniting() {
		return missionState == MissionState.INITING;
	}

	public boolean isRunning() {
		return missionState == MissionState.RUNNING;
	}

	public boolean isWaiting() {
		return missionState == MissionState.WAITING;
	}

	public boolean isPause() {
		return missionState == MissionState.PAUSE;
	}

	public boolean isFinished() {
		return missionState == MissionState.FINISHED;
	}

	public boolean isError() {
		return missionState == MissionState.ERROR;
	}

}
