package com.zpj.qxdownloader;

import android.content.Context;
import android.content.IntentFilter;

import com.zpj.qxdownloader.get.DownloadManager;
import com.zpj.qxdownloader.get.DownloadManagerImpl;
import com.zpj.qxdownloader.get.DownloadMission;
import com.zpj.qxdownloader.notification.NotifyUtil;
import com.zpj.qxdownloader.option.MissionOptions;
import com.zpj.qxdownloader.option.QianXunOptions;
import com.zpj.qxdownloader.util.NetworkChangeReceiver;

public class QianXun {

//    private static DownloadManager mManager;
//    private static DownloadManagerService.DMBinder mBinder;
//    private static ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName p1, IBinder binder) {
//            mBinder = (DownloadManagerService.DMBinder) binder;
//            mManager = mBinder.getDownloadManager();
//            if (registerListener != null) {
//                registerListener.onServiceConnected();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName p1) {
//
//        }
//    };
//
//    private static RegisterListener registerListener;

    private QianXun() {

    }

//    public interface RegisterListener {
//        void onServiceConnected();
//    }

//    public void setRegisterListener(RegisterListener registerListener) {
//        this.registerListener = registerListener;
//    }

    public static void init(Context context) {
//        register(context, null);
        NotifyUtil.init(context);
        DownloadManagerImpl.register(context, QianXunOptions.with(context));
    }

    public static void init(QianXunOptions options) {
//        register(context, null);
        Context context = options.getContext();
        NotifyUtil.init(context);
        DownloadManagerImpl.register(context, options);
    }

//    public static void register(Context context, RegisterListener registerListener) {
//        NotifyUtil.init(context);
//        QianXun.registerListener = registerListener;
//        if (mManager == null || mBinder == null) {
//            Intent intent = new Intent();
//            intent.setClass(context, DownloadManagerService.class);
//            context.startService(intent);
//            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        }
//    }

    public static void unInit() {
        DownloadManagerImpl.unRegister();
        NotifyUtil.cancelAll();
//        context.unbindService(mConnection);
//        Intent intent = new Intent();
//        intent.setClass(context, DownloadManagerService.class);
//        context.stopService(intent);
    }

    public static DownloadMission download(String url) {
//        哈哈.apk
        int res = DownloadManagerImpl.getInstance().startMission(url, "", 5);
        if (res == -1) {
//            Log.d("download", "文件已存在！！！");
            return null;
        }
        //        mBinder.onMissionAdded(downloadMission);
        return DownloadManagerImpl.getInstance().getMission(res);
    }

    public static DownloadMission download(String url, MissionOptions options) {
//        哈哈.apk
        int res = DownloadManagerImpl.getInstance().startMission(url, "", 5);
        if (res == -1) {
//            Log.d("download", "文件已存在！！！");
            return null;
        }
        //        mBinder.onMissionAdded(downloadMission);
        return DownloadManagerImpl.getInstance().getMission(res);
    }

    public static DownloadMission download(String url, int threadCount) {
//        哈哈.apk
        int res = DownloadManagerImpl.getInstance().startMission(url, "", threadCount);
        if (res == -1) {
//            Log.d("download", "文件已存在！！！");
            return null;
        }
        DownloadMission downloadMission = DownloadManagerImpl.getInstance().getMission(res);
//        mBinder.onMissionAdded(downloadMission);
        return downloadMission;
    }

    public static void pause(DownloadMission mission) {
//        mManager.pauseMission(mission.uuid);
        if (mission.running) {
            mission.pause();
        }
//        mBinder.onMissionRemoved(mission);
    }

    public static void resume(DownloadMission mission) {
//        mManager.resumeMission(mission.uuid);
        if (!mission.running) {
            mission.start();
        }
//        mBinder.onMissionAdded(mission);
    }

    public static void delete(DownloadMission mission) {
//        mManager.deleteMission(mission.uuid);
        mission.pause();
        mission.delete();
        DownloadManagerImpl.getInstance().getMissions().remove(mission);
//        mBinder.onMissionRemoved(mission);
    }

    public static void clear(DownloadMission mission) {
//        mManager.clearMission(mission.uuid);
        mission.pause();
        mission.deleteThisFromFile();
        DownloadManagerImpl.getInstance().getMissions().remove(mission);
//        mBinder.onMissionRemoved(mission);
    }

    public static void pauseAll() {
        DownloadManagerImpl.getInstance().pauseAllMissions();
    }

    public static void resumeAll() {
        DownloadManagerImpl.getInstance().resumeAllMissions();
    }

    public static void deleteAll() {
        DownloadManagerImpl.getInstance().deleteAllMissions();
    }

    public static void clearAll() {
        DownloadManagerImpl.getInstance().clearAllMissions();
    }

//    public static DownloadManagerService.DMBinder getBinder() {
//        return mBinder;
//    }

    public static DownloadManager getDownloadManager() {
        return DownloadManagerImpl.getInstance();
    }
}
