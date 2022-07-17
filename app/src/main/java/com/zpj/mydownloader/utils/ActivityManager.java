package com.zpj.mydownloader.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public class ActivityManager {

    private static WeakReference<Activity> sActivity;

    public static void init(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                setActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                setActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity == sActivity.get()) {
                    sActivity.clear();
                    sActivity = null;
                }
            }

            private void setActivity(Activity activity) {
                if (sActivity != null) {
                    sActivity.clear();
                }
                sActivity = new WeakReference<>(activity);
            }



        });
    }

    public static Activity getCurrentActivity() {
        return sActivity == null ? null : sActivity.get();
    }

}
