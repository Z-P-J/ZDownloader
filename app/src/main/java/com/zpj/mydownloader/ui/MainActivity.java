package com.zpj.mydownloader.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.lxj.xpermission.PermissionConstants;
import com.lxj.xpermission.XPermission;
import com.zpj.fragmentation.SupportActivity;
import com.zpj.fragmentation.dialog.ZDialog;
import com.zpj.mydownloader.R;
import com.zpj.mydownloader.ui.fragment.AddTaskFragment;
import com.zpj.mydownloader.ui.fragment.MainFragment;

/**
 * @author Z-P-J
 */
public class MainActivity extends SupportActivity {

    private MainFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = findFragment(MainFragment.class);
        if (fragment == null) {
            fragment = new MainFragment();
            loadRootFragment(R.id._fl_container, fragment);
        }

        showRequestPermissionPopup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            new AddTaskFragment().show(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showRequestPermissionPopup() {
        if (hasStoragePermissions(getApplicationContext())) {
            requestPermission();
        } else {
            ZDialog.alert()
                    .setTitle("权限申请")
                    .setContent("本软件需要读写手机存储的权限用于文件的下载与查看，是否申请该权限？")
                    .setPositiveButton("去申请", (fragment, which) -> requestPermission())
                    .setNegativeButton("拒绝", (fragment, which) -> ActivityCompat.finishAfterTransition(MainActivity.this))
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
        XPermission.create(getApplicationContext(), PermissionConstants.STORAGE)
                .callback(new XPermission.SimpleCallback() {
                    @Override
                    public void onGranted() {

                        if (fragment != null) {
                            fragment.loadTasks();
                        }


                    }

                    @Override
                    public void onDenied() {
                        showRequestPermissionPopup();
                    }
                }).request();
    }

}
