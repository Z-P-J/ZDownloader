package com.zpj.mydownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.downloader.util.FileUtil;

import java.io.File;

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
		mManager = ZDownloader.getDownloadManager();
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
				downloadCallback.onMoreClicked(v, h, mManager);
			}
		});

		h.itemView.setOnClickListener(v -> {
			if (downloadCallback != null) {
				downloadCallback.onItemClicked(v, h, mManager);
			}
		});

		h.itemView.setOnLongClickListener(v -> {
			if (downloadCallback != null) {
				downloadCallback.onItemLongClicked(v, h, mManager);
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

		h.icon.setImageResource(FileUtil.getFileTypeIconId(mission.getTaskName()));
		if (TextUtils.isEmpty(mission.getTaskName())) {
			h.name.setText(STATUS_INIT);
		} else {
			h.name.setText(mission.getTaskName());
			h.size.setText(mission.getFileSizeStr());
		}
		
		updateProgress(h, null);
	}

	@Override
	public int getItemCount() {
		return mManager.getCount();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private void updateProgress(ViewHolder h, DownloadMission.UpdateInfo updateInfo) {
		updateProgress(h, updateInfo, false);
	}
	
	private void updateProgress(ViewHolder h, DownloadMission.UpdateInfo updateInfo, boolean finished) {
		if (h.mission == null) {
			return;
		}

		if (finished) {
			downloadCallback.onDownloadFinished();
		} else {
			if (updateInfo == null) {
				h.menu.setProgress(h.mission.getProgress());
				h.size.setText(h.mission.getFileSizeStr() + File.separator + h.mission.getDownloadedSizeStr() + "  " + h.mission.getSpeed());
			} else {
				h.menu.setProgress(updateInfo.getProgress());
				h.size.setText(updateInfo.getFileSizeStr() + File.separator + updateInfo.getDownloadedSizeStr() + "  " + updateInfo.getSpeedStr());
			}
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

	@Override
	public void onMissionFinished() {

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
		public void onProgress(DownloadMission.UpdateInfo update) {
			if (TextUtils.equals(mHolder.name.getText().toString(), STATUS_INIT) && !TextUtils.isEmpty(mHolder.mission.getTaskName())) {
				mHolder.name.setText(mHolder.mission.getTaskName());
			}
			mAdapter.updateProgress(mHolder, update);
		}

		@Override
		public void onFinish() {
			if (mHolder.mission != null) {
				mHolder.size.setText(mHolder.mission.getFileSizeStr());
				mAdapter.updateProgress(mHolder, null, true);
			}
		}

		@Override
		public void onError(int errCode) {
			switch (errCode) {
				case 1000:
					mHolder.status.setText("存储空间不足！");
					break;
				case 233:
					mHolder.status.setText("未知错误");
					break;
				default:
					mHolder.status.setText("出错了:errorCode=" + errCode);
					break;
			}
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
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onItemClicked(View view, ViewHolder holder, DownloadManager mManager);

		/**
		 * on view longCLicked
		 * @param holder the ViewHolder
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onItemLongClicked(View view, ViewHolder holder, DownloadManager mManager);

		/**
		 * on more clicked
		 * @param holder the ViewHolder
		 * @param mManager  the DownloadManager
		 * @param view the click view
		 * */
		void onMoreClicked(View view, ViewHolder holder, DownloadManager mManager);
	}

	void setMissionAdapterClickListener(DownloadCallback downloadCallback) {
		this.downloadCallback = downloadCallback;
	}
}
