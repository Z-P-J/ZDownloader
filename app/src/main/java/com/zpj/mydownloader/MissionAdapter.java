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
import android.widget.Toast;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.DownloadManager;
import com.zpj.downloader.DownloadMission;
import com.zpj.mydownloader.widget.ActionBottomPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Z-P-J
 * */
public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.ViewHolder>
		implements DownloadManager.DownloadManagerListener {

	private final List<BaseMission<?>> list = new ArrayList<>();

	private static final String STATUS_INIT = "初始化中...";
	
	private final Context mContext;

	private final int mLayout;
	
	MissionAdapter(Context context, List<BaseMission<?>> list) {
		mContext = context;
		this.list.clear();
		this.list.addAll(list);
		ZDownloader.getDownloadManager()
				.addDownloadManagerListener(this);
		
		mLayout = R.layout.mission_item_linear; // R.layout.mission_item;
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
		BaseMission<?> mission = list.get(pos);
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
		return list.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private void updateProgress(ViewHolder h, DownloadMission.ProgressInfo progressInfo) {
		updateProgress(h, progressInfo, false);
	}
	
	private void updateProgress(ViewHolder h, DownloadMission.ProgressInfo progressInfo, boolean finished) {
		if (h.mission == null) {
			return;
		}

		if (finished) {
			h.status.setText("已完成");
		} else {
			if (progressInfo == null) {
				h.menu.setProgress(h.mission.getProgress());
				h.size.setText(h.mission.getFileSizeStr() + File.separator + h.mission.getDownloadedSizeStr() + "  " + h.mission.getSpeed());
				if (h.mission.isRunning()) {
					h.status.setText(h.mission.getProgressStr());
				} else {
					h.status.setText(h.mission.getStatus().toString());
				}
			} else {
				h.status.setText(progressInfo.getProgressStr());
				h.menu.setProgress(progressInfo.getProgress());
				h.size.setText(progressInfo.getFileSizeStr() + File.separator + progressInfo.getDownloadedSizeStr() + "  " + progressInfo.getSpeedStr());
			}
		}
	}

	@Override
	public void onMissionAdd(BaseMission<?> mission) {
		list.add(0, mission);
//		notifyDataSetChanged();
		notifyItemInserted(0);
	}

	@Override
	public void onMissionDelete(BaseMission<?> mission) {
		list.remove(mission);
		notifyDataSetChanged();
	}

	@Override
	public void onMissionFinished(BaseMission<?> mission) {

	}


	static class ViewHolder extends RecyclerView.ViewHolder {
		BaseMission<?> mission;
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
			mHolder.status.setText("已暂停");
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
		public void onProgress(DownloadMission.ProgressInfo update) {
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
			mHolder.status.setText("已完成");
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

}
