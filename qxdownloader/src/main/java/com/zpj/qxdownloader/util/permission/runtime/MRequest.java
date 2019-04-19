/*
 * Copyright © Zhenjie Yan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zpj.qxdownloader.util.permission.runtime;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zpj.qxdownloader.util.permission.PermissionUtil;
import com.zpj.qxdownloader.util.permission.bridge.BridgeRequest;
import com.zpj.qxdownloader.util.permission.bridge.RequestThread;
import com.zpj.qxdownloader.util.permission.checker.DoubleChecker;
import com.zpj.qxdownloader.util.permission.checker.PermissionChecker;
import com.zpj.qxdownloader.util.permission.checker.StandardChecker;
import com.zpj.qxdownloader.util.permission.source.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by YanZhenjie on 2016/9/9.
 */
public class MRequest implements PermissionRequest, RequestExecutor, BridgeRequest.Callback {

    private static final PermissionChecker STANDARD_CHECKER = new StandardChecker();
    private static final PermissionChecker DOUBLE_CHECKER = new DoubleChecker();

    private Source mSource;

    private Rationale<List<String>> mRationale = new Rationale<List<String>>() {
        @Override
        public void showRationale(Context context, List<String> data, RequestExecutor executor) {
            executor.execute();
        }
    };
    private Action<List<String>> mGranted;
    private Action<List<String>> mDenied;

    public MRequest(Source source) {
        this.mSource = source;
    }

    @Override
    public PermissionRequest onDenied(Action<List<String>> denied) {
        this.mDenied = denied;
        return this;
    }

    @Override
    public void start() {
//        List<String> deniedList = getDeniedPermissions(STANDARD_CHECKER, mSource);
//        Log.d("MRequest", "" + deniedList.toString());
        String[] mDeniedPermissions = getDeniedPermissions(STANDARD_CHECKER, mSource);
        if (mDeniedPermissions.length > 0) {
            List<String> rationaleList = getRationalePermissions(mSource);
            if (rationaleList.size() > 0) {
                mRationale.showRationale(mSource.getContext(), rationaleList, this);
            } else {
                execute();
            }
        } else {
            onCallback();
        }
    }

    @Override
    public void execute() {
        BridgeRequest request = new BridgeRequest(mSource);
        request.setCallback(this);
        new RequestThread(request).start();
    }

    @Override
    public void cancel() {
        onCallback();
    }

    @Override
    public void onCallback() {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                return Arrays.asList(getDeniedPermissions(DOUBLE_CHECKER, mSource));
            }

            @Override
            protected void onPostExecute(List<String> deniedList) {
                if (deniedList.isEmpty()) {
                    callbackSucceed();
                } else {
                    callbackFailed(deniedList);
                }
            }
        }.execute();
    }

    /**
     * Callback acceptance status.
     */
    private void callbackSucceed() {
        if (mGranted != null) {
            List<String> permissionList = asList(PermissionUtil.STORAGE);
            try {
                mGranted.onAction(permissionList);
            } catch (Exception e) {
                Log.e("PermissionUtil", "Please check the onGranted() method body for bugs.", e);
                if (mDenied != null) {
                    mDenied.onAction(permissionList);
                }
            }
        }
    }

    /**
     * Callback rejected state.
     */
    private void callbackFailed(List<String> deniedList) {
        if (mDenied != null) {
            mDenied.onAction(deniedList);
        }
    }

    /**
     * Get denied permissions.
     */
//    private static List<String> getDeniedPermissions(PermissionChecker checker, Source source) {
//        if (!checker.hasStoragePermission(source.getContext())) {
//            return Arrays.asList(PermissionUtil.STORAGE);
//        }
//        return new ArrayList<>();
//    }

    private static String[] getDeniedPermissions(PermissionChecker checker, Source source) {
        if (!checker.hasStoragePermission(source.getContext())) {
            return PermissionUtil.STORAGE;
        }
        return new String[0];
    }

    /**
     * Get permissions to show rationale.
     */
    private static List<String> getRationalePermissions(Source source) {
        List<String> rationaleList = new ArrayList<>(1);
        for (String permission : PermissionUtil.STORAGE) {
            boolean a = source.isShowRationalePermission(permission);
            Log.d("getRationalePermissions", "getRationalePermissions=" + a);
            if (a) {
                rationaleList.add(permission);
            }
        }
        return rationaleList;
    }
}