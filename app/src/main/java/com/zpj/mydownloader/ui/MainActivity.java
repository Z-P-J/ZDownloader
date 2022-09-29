package com.zpj.mydownloader.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.zpj.fragmentation.SupportActivity;
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

}
