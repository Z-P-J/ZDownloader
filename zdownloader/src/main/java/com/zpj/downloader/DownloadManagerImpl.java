package com.zpj.downloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.zpj.downloader.impl.DownloadMission;
import com.zpj.downloader.utils.ExecutorUtils;
import com.zpj.downloader.utils.NetworkChangeReceiver;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Z-P-J
 */
public class DownloadManagerImpl implements DownloadManager, MissionSerializer {

    private static final String TAG = DownloadManagerImpl.class.getSimpleName();

    static String MISSION_INFO_FILE_SUFFIX_NAME = ".zpj";

    @SuppressLint("StaticFieldLeak")
    private static DownloadManagerImpl mManager;

    private final Context mContext;

    private final ArrayList<WeakReference<DownloadManagerListener>> mListeners = new ArrayList<>();

    private final ArrayList<WeakReference<OnLoadMissionListener<BaseMission<?>>>> onLoadMissionListeners = new ArrayList<>();

    private final DownloaderConfig options;

    private static final AtomicInteger downloadingCount = new AtomicInteger(0);

//    private final AtomicBoolean isLoaded = new AtomicBoolean(false);
//    private final AtomicBoolean isLoading = new AtomicBoolean(false);

    private volatile boolean isLoaded = false;
    private volatile boolean isLoading = false;

    private DownloadManagerImpl(Context context, DownloaderConfig options) {
        mContext = context;
        this.options = options;
    }

    public static DownloadManagerImpl getInstance() {
        if (mManager == null) {
            throw new RuntimeException("must register first!");
        }
        return mManager;
    }

    public static DownloadManagerImpl get() {
        if (mManager == null) {
            synchronized (DownloadManagerImpl.class) {
                if (mManager == null) {
                    return null;
                }
            }
        }
        return mManager;
    }

    public static void register(DownloaderConfig options, Class<? extends BaseMission<?>> clazz) {
        if (mManager == null) {
            synchronized (DownloadManagerImpl.class) {
                if (mManager == null) {
                    mManager = new DownloadManagerImpl(options.getContext(), options);
                    mManager.loadMissions(clazz);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    options.getContext().registerReceiver(NetworkChangeReceiver.getInstance(), intentFilter);
                }
            }
        }
    }

    public void onDestroy() {
        this.mListeners.clear();
        this.onLoadMissionListeners.clear();
        pauseAllMissions();
        getContext().unregisterReceiver(NetworkChangeReceiver.getInstance());
        INotificationInterceptor interceptor = getDownloaderConfig().getNotificationInterceptor();
        if (interceptor != null) {
            interceptor.onCancelAll(getContext());
        }
        for (BaseMission<?> mission : ALL_MISSIONS) {
            mission.onDestroy();
        }
        ALL_MISSIONS.clear();
        isLoading = true;
        isLoaded = true;
        mManager = null;
    }

    private static int getDownloadingCount() {
        return downloadingCount.get();
    }

    static void decreaseDownloadingCount() {
        downloadingCount.decrementAndGet();
        for (BaseMission<?> mission : get().ALL_MISSIONS) {
            if (!mission.isFinished() && mission.isWaiting()) {
                mission.start();
                break;
            }
        }
    }

    static void increaseDownloadingCount() {
        downloadingCount.incrementAndGet();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public DownloaderConfig getDownloaderConfig() {
        return options;
    }

    @Override
    public List<BaseMission<?>> getMissions() {
        Collections.sort(ALL_MISSIONS, new Comparator<BaseMission<?>>() {
            @Override
            public int compare(BaseMission<?> o1, BaseMission<?> o2) {
                return -(int) (o1.getCreateTime() - o2.getCreateTime());
            }
        });
        return ALL_MISSIONS;
    }

    @Override
    public void loadMissions() {
        loadMissions(DownloadMission.class);
    }

    @Override
    public void loadMissions(final Class<? extends BaseMission<?>> clazz) {
        if (isLoading) {
            return;
        }
        isLoaded = false;
        isLoading = true;

        ExecutorUtils.submitIO(new Runnable() {
            @Override
            public void run() {
                ALL_MISSIONS.clear();
                File f = getDownloaderConfig().getTaskFolder();
                if (f.exists() && f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        for (final File sub : f.listFiles()) {
                            if (sub.isFile() && sub.exists()
                                    && sub.getName().endsWith(MISSION_INFO_FILE_SUFFIX_NAME)) {
                                BaseMission<?> mission = readMission(sub, clazz);
                                if (mission == null) {
                                    continue;
                                }
                                mission.setContext(getContext());
                                mission.setNotificationInterceptor(mission.getNotificationInterceptor());
                                mission.firstCreate();
                                insertMission(mission);
                            }
                        }
                    }
                    Collections.sort(ALL_MISSIONS, new Comparator<BaseMission<?>>() {
                        @Override
                        public int compare(BaseMission<?> o1, BaseMission<?> o2) {
                            return -(int) (o1.getCreateTime() - o2.getCreateTime());
                        }
                    });
                } else {
                    f.mkdirs();
                }
                synchronized (onLoadMissionListeners) {
                    isLoaded = true;
                    for (int i = onLoadMissionListeners.size() - 1; i >= 0; i--) {
                        OnLoadMissionListener<BaseMission<?>> listener = onLoadMissionListeners.get(i).get();
                        if (listener != null) {
                            listener.onLoaded(ALL_MISSIONS);
                        }
                        onLoadMissionListeners.remove(i);
                    }
                }
                isLoading = false;
            }
        });
    }

    @Override
    public void loadMissions(OnLoadMissionListener<BaseMission<?>> listener) {
        synchronized (onLoadMissionListeners) {
            if (isLoaded) {
                listener.onLoaded(ALL_MISSIONS);
            } else {
                this.onLoadMissionListeners.add(new WeakReference<>(listener));
            }
        }
    }

    @Override
    public void addDownloadManagerListener(DownloadManagerListener downloadManagerListener) {
        this.mListeners.add(new WeakReference<>(downloadManagerListener));
    }

    @Override
    public void removeDownloadManagerListener(DownloadManagerListener downloadManagerListener) {
        for (WeakReference<DownloadManagerListener> reference : this.mListeners) {
            DownloadManagerListener listener = reference.get();
            if (listener == downloadManagerListener) {
                this.mListeners.remove(reference);
                return;
            }
        }
    }

    @Override
    public void pauseAllMissions() {
        for (BaseMission<?> downloadMission : ALL_MISSIONS) {
            downloadMission.pause();
        }
    }

    @Override
    public void deleteAllMissions() {
        for (BaseMission<?> mission : ALL_MISSIONS) {
            mission.delete();
        }
        ALL_MISSIONS.clear();
        onMissionDelete(null);
    }

    @Override
    public void clearAllMissions() {
        for (BaseMission<?> mission : ALL_MISSIONS) {
            mission.clear();
        }
        ALL_MISSIONS.clear();
        onMissionDelete(null);
    }

    @Override
    public BaseMission<?> getMission(int i) {
        return ALL_MISSIONS.get(i);
    }

    @Override
    public BaseMission<?> getMission(String uuid) {
        for (BaseMission<?> mission : ALL_MISSIONS) {
            if (TextUtils.equals(mission.getUuid(), uuid)) {
                return mission;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return ALL_MISSIONS.size();
    }

    @Override
    public int insertMission(BaseMission<?> mission) {
        if (ALL_MISSIONS.contains(mission)) {
            return ALL_MISSIONS.indexOf(mission);
        }
        ALL_MISSIONS.add(0, mission);
        onMissionAdd(mission);
//		return ALL_MISSIONS.size() - 1;
        return 0;
    }

    @Override
    public boolean shouldMissionWaiting() {
        return DownloadManagerImpl.getDownloadingCount() >= getDownloaderConfig().getConcurrentMissionCount();
    }


    static void onMissionAdd(BaseMission<?> mission) {
        Iterator<WeakReference<DownloadManagerListener>> iterator = DownloadManagerImpl.get().mListeners.iterator();
        while (iterator.hasNext()) {
            DownloadManagerListener listener = iterator.next().get();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.onMissionAdd(mission);
            }
        }
    }

    static void onMissionClear(BaseMission<?> mission) {
        ALL_MISSIONS.remove(mission);
    }

    static void onMissionDelete(BaseMission<?> mission) {
        onMissionClear(mission);
        Iterator<WeakReference<DownloadManagerListener>> iterator = DownloadManagerImpl.get().mListeners.iterator();
        while (iterator.hasNext()) {
            DownloadManagerListener listener = iterator.next().get();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.onMissionDelete(mission);
            }
        }
    }

    static void onMissionFinished(BaseMission<?> mission) {
        Iterator<WeakReference<DownloadManagerListener>> iterator = DownloadManagerImpl.get().mListeners.iterator();
        while (iterator.hasNext()) {
            DownloadManagerListener listener = iterator.next().get();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.onMissionFinished(mission);
            }
        }
    }


    public static List<? extends BaseMission<?>> getAllMissions() {
        return DownloadManagerImpl.getInstance().getMissions();
    }

    @Override
    public BaseMission<?> readMission(File file, Class<? extends BaseMission<?>> clazz) {
        return options.getMissionSerializer().readMission(file, clazz);
    }

    @Override
    public void writeMission(BaseMission<?> mission) {
        options.getMissionSerializer().writeMission(mission);
    }
}
