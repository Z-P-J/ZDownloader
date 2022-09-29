package com.zpj.mydownloader.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lxj.xpermission.PermissionConstants;
import com.lxj.xpermission.XPermission;
import com.zpj.downloader.core.MissionManager;
import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.impl.MissionManagerImpl;
import com.zpj.fragmentation.SimpleFragment;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.MainActivity;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showRequestPermissionPopup();
    }

    @Override
    public void onDestroyView() {
        mManager.destroy();
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

    private void showRequestPermissionPopup() {
        if (hasStoragePermissions(getContext())) {
            requestPermission();
        } else {
            ZDialog.alert()
                    .setTitle("权限申请")
                    .setContent("本软件需要读写手机存储的权限用于文件的下载与查看，是否申请该权限？")
                    .setPositiveButton("去申请", (fragment, which) -> requestPermission())
                    .setNegativeButton("拒绝", (fragment, which) -> ActivityCompat.finishAfterTransition(_mActivity))
                    .show(this);
        }
    }

    private boolean hasStoragePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        XPermission.create(context.getApplicationContext(), PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        loadTasks();
                    }

                    @Override
                    public void onDenied() {
                        showRequestPermissionPopup();
                    }
                }).request();
    }

}
