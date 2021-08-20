package com.zpj.mydownloader.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.DownloadManager;
import com.zpj.downloader.ZDownloader;
import com.zpj.fragmentation.SimpleFragment;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.adapter.MissionAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends SimpleFragment implements DownloadManager.DownloadManagerListener {

    private final List<BaseMission<?>> mMissions = new ArrayList<>();

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
    }

    @Override
    public void onDestroy() {
        ZDownloader.getDownloadManager().removeDownloadManagerListener(MainFragment.this);
        super.onDestroy();
    }

    public void loadTasks() {
        postOnEnterAnimationEnd(new Runnable() {
            @Override
            public void run() {
                ZDownloader.getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
                    @Override
                    public void onLoaded(List<BaseMission<?>> missions) {
                        mMissions.clear();
                        mMissions.addAll(missions);

                        mMissionAdapter = new MissionAdapter(context, mMissions);
                        mRecyclerView.setAdapter(mMissionAdapter);

                        ZDownloader.getDownloadManager()
                                .addDownloadManagerListener(MainFragment.this);
                    }
                });
            }
        });
    }

    @Override
    public void onMissionAdd(BaseMission<?> mission) {
        mMissions.add(0, mission);
        mMissionAdapter.notifyItemInserted(0);
    }

    @Override
    public void onMissionDelete(BaseMission<?> mission) {
        mMissions.remove(mission);
        mMissionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionFinished(BaseMission<?> mission) {

    }
}
