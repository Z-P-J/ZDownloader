package com.zpj.mydownloader.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.MissionLoader;
import com.zpj.downloader.core.impl.DownloadMission;
import com.zpj.fragmentation.SimpleFragment;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.adapter.MissionAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainFragment extends SimpleFragment implements Downloader.DownloaderObserver<DownloadMission> {

    private final List<DownloadMission> mMissions = new ArrayList<>();

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
        ZDownloader.removeObserver(DownloadMission.class, MainFragment.this);
        super.onDestroy();
    }

    public void loadTasks() {
        postOnEnterAnimationEnd(new Runnable() {
            @Override
            public void run() {

                ZDownloader.loadMissions(DownloadMission.class, new MissionLoader<DownloadMission>() {
                    @Override
                    public void onLoad(List<DownloadMission> missions) {
                        mMissions.clear();
                        mMissions.addAll(missions);
                        Collections.reverse(mMissions);

                        mMissionAdapter = new MissionAdapter(context, mMissions);
                        mRecyclerView.setAdapter(mMissionAdapter);

                        ZDownloader.addObserver(DownloadMission.class, MainFragment.this);
                    }
                });

            }
        });
    }

    @Override
    public void onMissionAdd(DownloadMission mission) {
        mMissions.add(0, mission);
        mMissionAdapter.notifyItemInserted(0);
    }

    @Override
    public void onMissionDelete(DownloadMission mission) {
        mMissions.remove(mission);
        mMissionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionFinished(DownloadMission mission) {

    }
}
