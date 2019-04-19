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

import android.os.AsyncTask;
import android.util.Log;

import com.zpj.qxdownloader.util.permission.PermissionUtil;
import com.zpj.qxdownloader.util.permission.checker.PermissionChecker;
import com.zpj.qxdownloader.util.permission.checker.StrictChecker;
import com.zpj.qxdownloader.util.permission.source.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by YanZhenjie on 2018/1/25.
 */
public class LRequest implements PermissionRequest {

    private static final PermissionChecker STRICT_CHECKER = new StrictChecker();

    private Source mSource;

    private Action<List<String>> mGranted;
    private Action<List<String>> mDenied;

    public LRequest(Source source) {
        this.mSource = source;
    }

//    @Override
//    public PermissionRequest onGranted(Action<List<String>> granted) {
//        this.mGranted = granted;
//        return this;
//    }

    @Override
    public PermissionRequest onDenied(Action<List<String>> denied) {
        this.mDenied = denied;
        return this;
    }

    @Override
    public void start() {
        Log.d("LRequest", "start");
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                return getDeniedPermissions(mSource);
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
    private static List<String> getDeniedPermissions(Source source) {
        if (!LRequest.STRICT_CHECKER.hasStoragePermission(source.getContext())) {
            return Arrays.asList(PermissionUtil.STORAGE);
        }
        return new ArrayList<>();
    }
}