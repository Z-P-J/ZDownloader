package com.zpj.mydownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.qxdownloader.QXDownloader;
import com.zpj.qxdownloader.core.DownloadManager;
import com.zpj.qxdownloader.core.DownloadMission;
import com.zpj.qxdownloader.service.DownloadManagerService;
import com.zpj.qxdownloader.util.FileUtil;
import com.zpj.qxdownloader.util.Utility;

import java.util.Locale;

/**
 * @author Z-P-J
 * */
public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.ViewHolder> implements DownloadManager.DownloadManagerListener {

	private static final int BACKGROUND_COLOR = Color.parseColor("#FF9800");
	private static final int FOREGROUND_COLOR = Color.parseColor("#EF6C00");

	private static final int DELTA_TIME_LIMIT = 1000;

	private static final String STATUS_INIT = "初始化中...";
	
	private Context mContext;
	private LayoutInflater mInflater;
	private DownloadManager mManager;
//	private DownloadManagerService.DMBinder mBinder;

	private int mLayout;
	private DownloadCallback downloadCallback;
	
	MissionAdapter(Context context, boolean isLinear) {
		mContext = context;
		mManager = QXDownloader.getDownloadManager();
		mManager.setDownloadManagerListener(this);
//		mBinder = binder;
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mLayout = isLinear ? R.layout.mission_item_linear : R.layout.mission_item;
	}

	@NonNull
	@Override
	public MissionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		final ViewHolder h =  new ViewHolder(mInflater.inflate(mLayout, parent, false));

		h.menu.setOnClickListener(v -> {
			if (downloadCallback != null) {
				downloadCallback.onMoreClicked(v, h, null, mManager);
			}
		});

		h.itemView.setOnClickListener(v -> {
			if (downloadCallback != null) {
				downloadCallback.onItemClicked(v, h, null, mManager);
			}
		});

		h.itemView.setOnLongClickListener(v -> {
			if (downloadCallback != null) {
				downloadCallback.onItemLongClicked(v, h, null, mManager);
			}
			return true;
		});


		if (mManager != null && mManager.getCount() == 0) {
			if (downloadCallback != null) {
				downloadCallback.onEmpty();
			}
		}

		return h;
	}

	@Override
	public void onViewRecycled(@NonNull MissionAdapter.ViewHolder h) {
		super.onViewRecycled(h);
		h.mission.removeListener(h.observer);
		h.mission = null;
		h.observer = null;
//		h.progress = null;
		h.position = -1;
		h.lastTimeStamp = -1;
		h.lastDone = -1;
//		h.colorId = 0;
	}

	@Override
	public void onBindViewHolder(@NonNull MissionAdapter.ViewHolder h, @SuppressLint("RecyclerView") int pos) {
		DownloadMission mission = mManager.getMission(pos);
		if (h.observer != null) {
			mission.removeListener(h.observer);
		}
		h.observer = new MissionObserver(this, h);
		mission.addListener(h.observer);


		h.mission = mission;
		h.position = pos;

		
		h.progress = new ProgressDrawable(BACKGROUND_COLOR, FOREGROUND_COLOR);
		h.bkg.setBackground(h.progress);

		h.icon.setImageResource(FileUtil.getFileTypeIconId(mission.name));
		if (TextUtils.isEmpty(mission.name)) {
			h.name.setText(STATUS_INIT);
		} else {
			h.name.setText(mission.name);
			h.size.setText(Utility.formatSize(mission.length));
		}
		
		updateProgress(h);
	}

	@Override
	public int getItemCount() {
		return mManager.getCount();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private void updateProgress(ViewHolder h) {
		updateProgress(h, false);
	}
	
	private void updateProgress(ViewHolder h, boolean finished) {
		if (h.mission == null) {
			return;
		}
		
		long now = System.currentTimeMillis();
		
		if (h.lastTimeStamp == -1) {
			h.lastTimeStamp = now;
		}
		
		if (h.lastDone == -1) {
			h.lastDone = h.mission.done;
		}
		
		long deltaTime = now - h.lastTimeStamp;
		long deltaDone = h.mission.done - h.lastDone;
		
		if (deltaTime == 0 || deltaTime > DELTA_TIME_LIMIT || finished) {
			int errorCode = h.mission.errCode;
			if (errorCode > 0) {
				switch (errorCode) {
					case 1000:
						h.status.setText("存储空间不足！");
						break;
					case 233:
						h.status.setText("未知错误");
						break;
					default:
						h.status.setText("出错了:errorCode=" + errorCode);
						break;
				}
			} else {
				float progress = h.mission.getProgress();
				Log.d("progress", "progress=" + progress);
				h.menu.setProgress(progress);
				h.status.setText(String.format(Locale.CHINA, "%.2f%%", progress));
				h.progress.setProgress(progress / 100);
			}
		}
		
		if (deltaTime > DELTA_TIME_LIMIT && deltaDone > 0) {
			float speed = (float) deltaDone / deltaTime;
			String speedStr = Utility.formatSpeed(speed * 1000);
			String sizeStr = Utility.formatSize(h.mission.length);
			
			h.size.setText(sizeStr + " " + speedStr);
			
			h.lastTimeStamp = now;
			h.lastDone = h.mission.done;
		}
		if (finished) {
			Toast.makeText(mContext, "download finish", Toast.LENGTH_SHORT).show();
			downloadCallback.onDownloadFinished();
		}
	}

	public void refresh() {
//		((FilteredDownloadManagerWrapper)mManager).refreshMap();
		mManager.loadMissions();
		notifyDataSetChanged();
	}

	@Override
	public void onMissionAdd() {
//		mManager.loadMissions();
		notifyDataSetChanged();
	}

	@Override
	public void onMissionDelete() {
		notifyDataSetChanged();
	}


	static class ViewHolder extends RecyclerView.ViewHolder {
		DownloadMission mission;
		int position;

		CardView cardView;
		TextView status;
		ImageView icon;
		TextView name;
		TextView size;
		View bkg;
		ArrowDownloadButton menu;
		ProgressDrawable progress;
		MissionObserver observer;
		
		long lastTimeStamp = -1;
		long lastDone = -1;
//		int colorId = 0;
		
		ViewHolder(View v) {
			super(v);
			cardView = v.findViewById(R.id.card_view);
			status = v.findViewById(R.id.item_status);
			icon = v.findViewById(R.id.item_icon);
			name = v.findViewById(R.id.item_name);
			size = v.findViewById(R.id.item_size);
			bkg = v.findViewById(R.id.item_bkg);
			menu = v.findViewById(R.id.item_more);
		}
	}
	
	private static class MissionObserver implements DownloadMission.MissionListener {
		private MissionAdapter mAdapter;
		private ViewHolder mHolder;
		
		MissionObserver(MissionAdapter adapter, ViewHolder holder) {
			mAdapter = adapter;
			mHolder = holder;
		}

		@Override
		public void onInit() {

		}

		@Override
		public void onStart() {
			mHolder.menu.resume();
		}

		@Override
		public void onPause() {
			mHolder.menu.pause();
		}

		@Override
		public void onWaiting() {
			mHolder.status.setText("等待中。。。");
		}

		@Override
		public void onRetry() {
			mHolder.status.setText("重试中。。。");
		}

		@Override
		public void onProgress(long done, long total) {
			if (TextUtils.equals(mHolder.name.getText().toString(), STATUS_INIT) && !TextUtils.isEmpty(mHolder.mission.name)) {
				mHolder.name.setText(mHolder.mission.name);
			}
			mAdapter.updateProgress(mHolder);
		}

		@Override
		public void onFinish() {
			if (mHolder.mission != null) {
				mHolder.size.setText(Utility.formatSize(mHolder.mission.length));
				mAdapter.updateProgress(mHolder, true);
			}
		}

		@Override
		public void onError(int errCode) {
			mAdapter.updateProgress(mHolder);
		}
		
	}

	public interface DownloadCallback {

		/**
		 * the downloading tasks are all complete
		 * */
		void onEmpty();

		/**
		 * notify change
		 * */
		void onNotifyChange();

		/***
		 *download finished
		 */
		void onDownloadFinished();

		/**
		 * on view clicked
		 * @param holder the ViewHolder
		 * @param mBinder the DMBinder
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onItemClicked(View view, ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager);

		/**
		 * on view longCLicked
		 * @param holder the ViewHolder
		 * @param mBinder the DMBinder
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onItemLongClicked(View view, ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager);

		/**
		 * on more clicked
		 * @param holder the ViewHolder
		 * @param mBinder the DMBinder
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onMoreClicked(View view, ViewHolder holder, DownloadManagerService.DMBinder mBinder, DownloadManager mManager);
	}

	void setMissionAdapterClickListener(DownloadCallback downloadCallback) {
		this.downloadCallback = downloadCallback;
	}
}
