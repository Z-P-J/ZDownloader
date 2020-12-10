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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.downloader.util.FileUtil;
import com.zpj.mydownloader.widget.ActionBottomPopup;

import java.io.File;

/**
 * @author Z-P-J
 * */
public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.ViewHolder>
		implements DownloadManager.DownloadManagerListener {

	private static final int BACKGROUND_COLOR = Color.parseColor("#FF9800");
	private static final int FOREGROUND_COLOR = Color.parseColor("#EF6C00");

	private static final int DELTA_TIME_LIMIT = 1000;

	private static final String STATUS_INIT = "初始化中...";
	
	private final Context mContext;
	private final DownloadManager mManager;
//	private DownloadManagerService.DMBinder mBinder;

	private final int mLayout;
	private DownloadCallback downloadCallback;
	
	MissionAdapter(Context context, boolean isLinear) {
		mContext = context;
		mManager = ZDownloader.getDownloadManager();
		mManager.setDownloadManagerListener(this);
		
		mLayout = isLinear ? R.layout.mission_item_linear : R.layout.mission_item;
	}

	@NonNull
	@Override
	public MissionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(mContext).inflate(mLayout, parent, false));
	}

	@Override
	public void onViewRecycled(@NonNull MissionAdapter.ViewHolder h) {
		super.onViewRecycled(h);
		h.mission.removeListener(h.observer);
		h.mission = null;
		h.observer = null;
//		h.progress = null;
		h.position = -1;
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


//		h.progressBar.setProgress((int) mission.getProgress());


		h.icon.setImageResource(Utils.getFileTypeIconId(mission.getTaskName()));
		h.name.setText(mission.getTaskName());
		h.size.setText(mission.getFileSizeStr());
//		if (mission.isIniting()) {
//			h.name.setText(mission.getTaskName());
//		} else {
//			h.name.setText(mission.getTaskName());
//			h.size.setText(mission.getFileSizeStr());
//		}
		
		updateProgress(h, null);

		h.itemView.setOnClickListener(v -> {
			Toast.makeText(mContext, "点击了下载任务:" + mission.getTaskName(), Toast.LENGTH_SHORT).show();
		});

		h.itemView.setOnLongClickListener(v -> {
			new ActionBottomPopup(mContext, mission).show();
			return true;
		});

		h.menu.setOnClickListener(v -> {
			if (mission.isFinished()) {
				mission.openFile();
			} else if (mission.canPause()) {
				Toast.makeText(mContext, "暂停下载", Toast.LENGTH_SHORT).show();
				mission.pause();
			} else if (mission.canStart()) {
				Toast.makeText(mContext, "恢复下载", Toast.LENGTH_SHORT).show();
				mission.start();
			}
		});
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
		mManager.loadMissions();
		notifyDataSetChanged();
	}

	@Override
	public void onMissionAdd(DownloadMission mission) {
		notifyDataSetChanged();
	}

	@Override
	public void onMissionDelete(DownloadMission mission) {
		notifyDataSetChanged();
	}

	@Override
	public void onMissionFinished(DownloadMission mission) {

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
//		ProgressBar progressBar;
		MissionObserver observer;
		
		ViewHolder(View v) {
			super(v);
			cardView = v.findViewById(R.id.card_view);
//			progressBar = v.findViewById(R.id.progress_bar);
			status = v.findViewById(R.id.item_status);
			icon = v.findViewById(R.id.item_icon);
			name = v.findViewById(R.id.item_name);
			size = v.findViewById(R.id.item_size);
			bkg = v.findViewById(R.id.item_bkg);
			menu = v.findViewById(R.id.item_more);
		}
	}
	
	private static class MissionObserver implements DownloadMission.MissionListener {
		private final MissionAdapter mAdapter;
		private final ViewHolder mHolder;
		
		MissionObserver(MissionAdapter adapter, ViewHolder holder) {
			mAdapter = adapter;
			mHolder = holder;
		}

		@Override
		public void onInit() {
			mHolder.status.setText("初始化...");
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
			mHolder.status.setText("等待中...");
		}

		@Override
		public void onRetry() {
			mHolder.status.setText("重试中...");
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
		public void onError(Error e) {
			mHolder.size.setText("");
			mHolder.status.setText("出错了：" + e.getErrorMsg());
		}

		@Override
		public void onDelete() {

		}

		@Override
		public void onClear() {

		}
	}

	public interface DownloadCallback {

		void onEmpty();

		void onDownloadFinished();

		void onItemClicked(View view, ViewHolder holder, DownloadManager mManager);

		void onItemLongClicked(View view, ViewHolder holder, DownloadManager mManager);

	}

	void setMissionAdapterClickListener(DownloadCallback downloadCallback) {
		this.downloadCallback = downloadCallback;
	}
}
