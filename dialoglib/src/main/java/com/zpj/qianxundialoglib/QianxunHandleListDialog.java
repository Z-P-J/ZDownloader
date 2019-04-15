//package com.zpj.qianxundialoglib;
//
//import android.app.Dialog;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.qianxun.browser.R;
//import com.qianxun.browser.utils.DisplayUtil;
//import com.qianxun.browser.utils.UIHelper;
//import com.qianxun.browser.view.listener.OnItemClickListener;
//import com.qianxun.browser.view.qianxundialoglib.base.QianxunHandleListBaseDialog;
//import com.zpj.qianxundialoglib.base.QianxunHandleListBaseDialog;
//
//import java.util.ArrayList;
//
//import cn.bingoogolapple.baseadapter.BGAOnItemChildClickListener;
//import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
//import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;
//
///**
// * Created by Renny on 2018/1/10.
// */
//
//public class QianxunHandleListDialog extends QianxunHandleListBaseDialog {
//    private RecyclerView mRecyclerView;
//    private int LocationX = 0;
//    private int LocationY = 0;
//    ArrayList<String> titleList = new ArrayList<>();
//    OnItemClickListener mOnItemClickListener;
//
//    public static QianxunHandleListDialog getInstance(int locationX, int locationY, ArrayList<String> titleList) {
//        QianxunHandleListDialog listDialog = new QianxunHandleListDialog();
//        Bundle bundle = new Bundle();
//        bundle.putInt("intX", locationX);
//        bundle.putInt("intY", locationY);
//        bundle.putStringArrayList("titleList", titleList);
//        listDialog.setArguments(bundle);
//        return listDialog;
//    }
//
//    public void show(FragmentManager manager) {
//        show(manager, QianxunHandleListDialog.class.getSimpleName());
//    }
//
//    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
//        mOnItemClickListener = onItemClickListener;
//    }
//
//    @Override
//    protected void initParams(Bundle bundle) {
//        LocationX = bundle.getInt("intX");
//        LocationY = bundle.getInt("intY");
//        titleList = bundle.getStringArrayList("titleList");
//    }
//
//    @Override
//    protected void initPresenter() {
//        super.initPresenter();
//    }
//
//    @Override
//    protected void initDialogStyle(Dialog dialog, Window window) {
//        super.initDialogStyle(dialog, window);
//        WindowManager.LayoutParams lp = window.getAttributes();
//
//        if (LocationX > DisplayUtil.getWidth() / 2) {
//            window.setGravity(Gravity.END | Gravity.TOP);
//            lp.x = DisplayUtil.getWidth() - LocationX;
//        } else {
//            window.setGravity(Gravity.START | Gravity.TOP);
//            lp.x = LocationX;
//        }
//
//        int windowHeight = UIHelper.dip2px(50 * (titleList.size() + 1));
//        if (LocationY +  windowHeight > DisplayUtil.getHeight()) {
//            Toast.makeText(mContext,  windowHeight + " " +  LocationY + "  " + DisplayUtil.getHeight(), Toast.LENGTH_SHORT).show();
//            LocationY -= windowHeight;
//        }
//        lp.y = LocationY;
//        lp.width = UIHelper.dip2px(100);
//        lp.dimAmount = 0.0f;
//        lp.windowAnimations = R.style.dialogAnim;
//        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        window.setAttributes(lp);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.dialog_list;
//    }
//
//
//    public void bindView(View rootView, Bundle savedInstanceState) {
//        mRecyclerView = rootView.findViewById(R.id.popup_list);
//    }
//
//
//    public void afterViewBind(View rootView, Bundle savedInstanceState) {
//        listAdapter listAdapter = new listAdapter(mRecyclerView);
//        listAdapter.setOnItemChildClickListener(new BGAOnItemChildClickListener() {
//            @Override
//            public void onItemChildClick(ViewGroup parent, View childView, int position) {
//                itemClick(position);
//            }
//        });
//        listAdapter.setData(titleList);
//        mRecyclerView.setAdapter(listAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//
//    }
//
//    private void itemClick(int position) {
//        if (mOnItemClickListener != null) {
//            mOnItemClickListener.onItemClicked(position);
//        }
//        dismiss();
//    }
//
//
//    public static class listAdapter extends BGARecyclerViewAdapter<String> {
//        listAdapter(RecyclerView recyclerView) {
//            super(recyclerView, R.layout.item_popup_list);
//        }
//
//        @Override
//        public void setItemChildListener(final BGAViewHolderHelper helper, int viewType) {
//            helper.setItemChildClickListener(R.id.item_title);
//        }
//
//        @Override
//        public void fillData(BGAViewHolderHelper helper, int position, String model) {
//            TextView textView = helper.getView(R.id.item_title);
//            textView.setText(model);
//        }
//    }
//
//}
//
