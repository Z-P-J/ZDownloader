package com.zpj.mydownloader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lxj.xpermission.PermissionConstants;
import com.lxj.xpermission.XPermission;
import com.zpj.downloader.BaseMission;
import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.DownloadManager;
import com.zpj.downloader.DownloadMission;
import com.zpj.mydownloader.widget.ActionBottomPopup;
import com.zpj.mydownloader.widget.AddTaskPopup;
import com.zpj.popup.ZPopup;

import java.util.List;

/**
 * @author Z-P-J
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showRequestPermissionPopup();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showDownloadDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZDownloader.onDestroy();
    }

    private void showRequestPermissionPopup() {
        if (hasStoragePermissions(getApplicationContext())) {
            requestPermission();
        } else {
            ZPopup.alert(this)
                    .setTitle("权限申请")
                    .setContent("本软件需要读写手机存储的权限用于文件的下载与查看，是否申请该权限？")
                    .setConfirmButton("去申请", this::requestPermission)
                    .setCancelButton("拒绝", () -> ActivityCompat.finishAfterTransition(MainActivity.this))
                    .show();
        }
    }

    private boolean hasStoragePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        XPermission.create(getApplicationContext(), PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {


                        ZDownloader.getAllMissions(new DownloadManager.OnLoadMissionListener<BaseMission<?>>() {
                            @Override
                            public void onLoaded(List<BaseMission<?>> missions) {
                                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                MissionAdapter missionAdapter = new MissionAdapter(MainActivity.this, missions);
                                recyclerView.setAdapter(missionAdapter);
                            }
                        });
                    }

                    @Override
                    public void onDenied() {
                        showRequestPermissionPopup();
                    }
                }).request();
    }

    private void showDownloadDialog() {
        new AddTaskPopup(this).show();
    }

    private void showMenuDialog() {

    }

}
