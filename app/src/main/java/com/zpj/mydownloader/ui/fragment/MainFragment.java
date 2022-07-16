package com.zpj.mydownloader.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.downloader.core.MissionManager;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionManagerImpl;
import com.zpj.fragmentation.SimpleFragment;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.adapter.MissionAdapter;

import java.util.List;

public class MainFragment extends SimpleFragment
        implements MissionManager.Observer<DownloadMission> {

    private final MissionManager<DownloadMission> mManager =
            new MissionManagerImpl<>(DownloadMission.class);

    private RecyclerView mRecyclerView;
    private MissionAdapter mMissionAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mMissionAdapter = new MissionAdapter(context, mManager.getMissions());
        mRecyclerView.setAdapter(mMissionAdapter);
    }

    @Override
    public void onDestroyView() {
        mManager.onDestroy();
        super.onDestroyView();
    }

    public void loadTasks() {
        mManager.register(this);
        postOnEnterAnimationEnd(mManager::loadMissions);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onMissionLoaded(List<DownloadMission> missions) {
        if (mMissionAdapter != null) {
            mMissionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMissionAdd(DownloadMission mission, int position) {
        mMissionAdapter.notifyItemInserted(position);
    }

    @Override
    public void onMissionDelete(DownloadMission mission, int position) {
        mMissionAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onMissionFinished(DownloadMission mission, int position) {
        mMissionAdapter.notifyItemChanged(position);
    }

}
