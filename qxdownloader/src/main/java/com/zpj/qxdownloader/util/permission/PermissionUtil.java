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
package com.zpj.qxdownloader.util.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.zpj.qxdownloader.util.permission.runtime.Action;
import com.zpj.qxdownloader.util.permission.runtime.LRequest;
import com.zpj.qxdownloader.util.permission.runtime.MRequest;
import com.zpj.qxdownloader.util.permission.runtime.PermissionRequest;
import com.zpj.qxdownloader.util.permission.source.ActivitySource;
import com.zpj.qxdownloader.util.permission.source.ContextSource;
import com.zpj.qxdownloader.util.permission.source.Source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * 本工具类基于开源项目AndPermission
 * 项目地址https://github.com/yanzhenjie/AndPermission
 * @author Zhenjie Yan
 * Created by Zhenjie Yan on 2016/9/9.
 * Modify by Z-P-J
 */
public class PermissionUtil {

    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final String[] STORAGE = new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};

//    /**
//     * With context.
//     *
//     * @param context {@link Context}.
//     *
//     * @return {@link Option}.
//     */
//    public static Option with(Context context) {
//        return new Boot(getContextSource(context));
//    }

    public static void grandStoragePermission(Context context) {
        Log.d("check", "" + checkStoragePermissions(context));
        if (!checkStoragePermissions(context)) {
            Source mSource = getContextSource(context);
//            if (sAppPermissions == null) sAppPermissions = getManifestPermissions(mSource.getContext());
            PermissionRequest request;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request = new MRequest(mSource);
            } else {
                request = new LRequest(mSource);
            }

            request.onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    throw  new RuntimeException("must grant Permission");
                }
            }).start();
        }
    }

    private static Source getContextSource(Context context) {
        if (context instanceof Activity) {
            return new ActivitySource((Activity)context);
        } else if (context instanceof ContextWrapper) {
            return getContextSource(((ContextWrapper)context).getBaseContext());
        }
        return new ContextSource(context);
    }

    /**
     * Get a list of permissions in the manifest.
     */
//    private static List<String> getManifestPermissions(Context context) {
//        try {
//            PackageInfo packageInfo = context.getPackageManager()
//                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] permissions = packageInfo.requestedPermissions;
//            if (permissions == null || permissions.length == 0) {
//                throw new IllegalStateException("You did not register any permissions in the manifest.xml.");
//            }
//            return Collections.unmodifiableList(Arrays.asList(permissions));
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new AssertionError("Package name cannot be found.");
//        }
//    }

    public static boolean checkStoragePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private PermissionUtil() {
    }
}