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
import com.zpj.downloader.ProgressUpdater;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.impl.DownloadMission;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.fragment.ActionBottomFragment;
import com.zpj.mydownloader.ui.fragment.ActionBottomFragment2;
import com.zpj.mydownloader.ui.widget.ArrowDownloadButton;
import com.zpj.mydownloader.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * @author Z-P-J
 * */
public class MissionAdapter2 extends RecyclerView.Adapter<MissionAdapter2.ViewHolder> {

	private final List<DownloadMission> list;

	private static final String STATUS_INIT = "初始化中...";

	private final Context mContext;

	private final int mLayout;

	public MissionAdapter2(Context context, List<DownloadMission> list) {
		mContext = context;
		this.list = list;
		
		mLayout = R.layout.mission_item_linear;
	}

	@NonNull
	@Override
	public MissionAdapter2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(mContext).inflate(mLayout, parent, false));
	}

	@Override
	public void onViewRecycled(@NonNull MissionAdapter2.ViewHolder h) {
		h.bindMission(null);
		super.onViewRecycled(h);
	}

	@Override
	public void onBindViewHolder(@NonNull MissionAdapter2.ViewHolder h, @SuppressLint("RecyclerView") int pos) {
		DownloadMission mission = list.get(pos);

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


	protected static class ViewHolder extends RecyclerView.ViewHolder implements Mission.Observer {

		private final Context context;
		private DownloadMission mission;

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
				Toast.makeText(context, "点击了下载任务:" + mission.getName(), Toast.LENGTH_SHORT).show();
			});

			itemView.setOnLongClickListener(v -> {
				new ActionBottomFragment2()
						.setMission(mission)
						.show(context);
				return true;
			});

			menu.setOnClickListener(v -> {
				if (mission.isComplete()) {
					Toast.makeText(context, "TODO 打开文件", Toast.LENGTH_SHORT).show();
//					mission.openFile();
				} else if (mission.canPause()) {
					Toast.makeText(context, "暂停下载", Toast.LENGTH_SHORT).show();
					mission.pause();
				} else if (mission.canStart()) {
					Toast.makeText(context, "恢复下载", Toast.LENGTH_SHORT).show();
					mission.start();
				}
			});
		}

		public void bindMission(DownloadMission mission) {
			if (this.mission != null) {
				this.mission.removeObserver(this);
			}
			if (mission != null) {
				mission.addObserver(this);
				icon.setImageResource(Utils.getFileTypeIconId(mission.getName()));
				name.setText(mission.getName());
				size.setText(mission.getFileSizeStr());
			}
			this.mission = mission;
		}

		public void updateProgress() {
			if (mission == null) {
				return;
			}

			if (mission.isComplete()) {
				status.setText("已完成");
				menu.setVisibility(View.GONE);
			} else {
				menu.setVisibility(View.VISIBLE);
				if (mission.isDownloading()) {
					status.setText(mission.getProgressStr());
				} else {
					status.setText("status: " + mission.getStatus());
				}
				menu.setProgress(mission.getProgress());
				size.setText(mission.getFileSizeStr() + File.separator + mission.getDownloadedSizeStr() + "  " + mission.getSpeedStr());
			}
		}

		@Override
		public void onPrepare() {
			status.setText("初始化...");
		}

		@Override
		public void onStart() {
			menu.resume();
		}

		@Override
		public void onPaused() {
			menu.pause();
			status.setText("已暂停");
		}

		@Override
		public void onWaiting() {
			status.setText("等待中...");
		}

		@Override
		public void onRetrying() {
			status.setText("重试中...");
		}

		@Override
		public void onProgress(Mission mission, float speed) {
			if (TextUtils.equals(name.getText().toString(), STATUS_INIT) && !TextUtils.isEmpty(mission.getName())) {
				name.setText(mission.getName());
			}
			updateProgress();
		}

		@Override
		public void onFinished() {
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
