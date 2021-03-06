package com.zpj.mydownloader.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.DownloadMission;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.utils.Utils;
import com.zpj.mydownloader.ui.fragment.ActionBottomFragment;
import com.zpj.mydownloader.ui.widget.ArrowDownloadButton;

import java.io.File;
import java.util.List;

/**
 * @author Z-P-J
 * */
public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.ViewHolder> {

	private final List<BaseMission<?>> list;

	private static final String STATUS_INIT = "初始化中...";
	
	private final Context mContext;

	private final int mLayout;
	
	MissionAdapter(Context context, List<BaseMission<?>> list) {
		mContext = context;
		this.list = list;
		
		mLayout = R.layout.mission_item_linear;
	}

	@NonNull
	@Override
	public MissionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(mContext).inflate(mLayout, parent, false));
	}

	@Override
	public void onViewRecycled(@NonNull MissionAdapter.ViewHolder h) {
		h.bindMission(null);
		super.onViewRecycled(h);
	}

	@Override
	public void onBindViewHolder(@NonNull MissionAdapter.ViewHolder h, @SuppressLint("RecyclerView") int pos) {
		BaseMission<?> mission = list.get(pos);

		h.bindMission(mission);
		
		h.updateProgress();
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	protected static class ViewHolder extends RecyclerView.ViewHolder implements DownloadMission.MissionListener {

		private final Context context;
		private BaseMission<?> mission;

		CardView cardView;
		TextView status;
		ImageView icon;
		TextView name;
		TextView size;
		View bkg;
		ArrowDownloadButton menu;
		
		private ViewHolder(View itemView) {
			super(itemView);
			context = itemView.getContext();
			cardView = itemView.findViewById(R.id.card_view);
//			progressBar = v.findViewById(R.id.progress_bar);
			status = itemView.findViewById(R.id.item_status);
			icon = itemView.findViewById(R.id.item_icon);
			name = itemView.findViewById(R.id.item_name);
			size = itemView.findViewById(R.id.item_size);
			bkg = itemView.findViewById(R.id.item_bkg);
			menu = itemView.findViewById(R.id.item_more);


			itemView.setOnClickListener(v -> {
				Toast.makeText(context, "点击了下载任务:" + mission.getTaskName(), Toast.LENGTH_SHORT).show();
			});

			itemView.setOnLongClickListener(v -> {
				new ActionBottomFragment()
						.setMission(mission)
						.show(context);
				return true;
			});

			menu.setOnClickListener(v -> {
				if (mission.isFinished()) {
					Toast.makeText(context, "打开文件", Toast.LENGTH_SHORT).show();
					mission.openFile();
				} else if (mission.canPause()) {
					Toast.makeText(context, "暂停下载", Toast.LENGTH_SHORT).show();
					mission.pause();
				} else if (mission.canStart()) {
					Toast.makeText(context, "恢复下载", Toast.LENGTH_SHORT).show();
					mission.start();
				}
			});
		}

		public void bindMission(BaseMission<?> mission) {
			if (this.mission != null) {
				this.mission.removeListener(this);
			}
			if (mission != null) {
				mission.addListener(this);
				icon.setImageResource(Utils.getFileTypeIconId(mission.getTaskName()));
				name.setText(mission.getTaskName());
				size.setText(mission.getFileSizeStr());
			}
			this.mission = mission;
		}

		public void updateProgress() {
			if (mission == null) {
				return;
			}

			if (mission.isFinished()) {
				status.setText("已完成");
				menu.setVisibility(View.GONE);
			} else {
				menu.setVisibility(View.VISIBLE);
				DownloadMission.ProgressInfo progressInfo = mission.getProgressInfo();
				if (progressInfo == null) {
					menu.setProgress(mission.getProgress());
					size.setText(mission.getFileSizeStr() + File.separator + mission.getDownloadedSizeStr() + "  " + mission.getSpeed());
					if (mission.isRunning()) {
						status.setText(mission.getProgressStr());
					} else {
						status.setText(mission.getStatus().toString());
					}
				} else {
					status.setText(progressInfo.getProgressStr());
					menu.setProgress(progressInfo.getProgress());
					size.setText(progressInfo.getFileSizeStr() + File.separator + progressInfo.getDownloadedSizeStr() + "  " + progressInfo.getSpeedStr());
				}
			}
		}

		@Override
		public void onInit() {
			status.setText("初始化...");
		}

		@Override
		public void onStart() {
			menu.resume();
		}

		@Override
		public void onPause() {
			menu.pause();
			status.setText("已暂停");
		}

		@Override
		public void onWaiting() {
			status.setText("等待中...");
		}

		@Override
		public void onRetry() {
			status.setText("重试中...");
		}

		@Override
		public void onProgress(DownloadMission.ProgressInfo update) {
			if (TextUtils.equals(name.getText().toString(), STATUS_INIT) && !TextUtils.isEmpty(mission.getTaskName())) {
				name.setText(mission.getTaskName());
			}
			updateProgress();
		}

		@Override
		public void onFinish() {
			if (mission != null) {
				size.setText(mission.getFileSizeStr());
				updateProgress();
			}
			status.setText("已完成");
		}

		@Override
		public void onError(Error e) {
			size.setText("");
			status.setText("出错了：" + e.getErrorMsg());
		}

		@Override
		public void onDelete() {

		}

		@Override
		public void onClear() {

		}


	}

}
