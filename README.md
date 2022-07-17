# ZDownloader (TODO 完善2.0 README)
Android多线程下载库，可以自定义下载任务，可扩展性强。


[The demo](https://github.com/Z-P-J/ZDownloader/tree/master/app)

 <div>
     <img src="./demo.gif" width="30%">
 </div>

## Install

#### Latest Version：2.0.0
```groovy
implementation 'com.github.Z-P-J:ZDownloader:latest_version'
```

## How To Use？
### 一. 简单使用
#### 1. 初始化
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册默认下载器或自定义下载器
        ZDownloader.register(DownloadMission.class, new MissionDownloader());
    }

}            
```

#### 2. 创建下载并监听进度
```java
public class Test {
    
    public void testDownload() {
        // 创建下载任务
        DownloadMission mission = new Mission.Builder(url, name.getText().toString())
                .build(DownloadMission.class);

        // 任务状态监听回调
        mission.addObserver(new Mission.Observer() {
            @Override
            public void onPrepare() { }

            @Override
            public void onStart() { }

            @Override
            public void onPaused() { }

            @Override
            public void onWaiting() { }

            @Override
            public void onProgress(Mission mission, float speed) { }

            @Override
            public void onFinished() { }

            @Override
            public void onError(int errorCode, String errorMessage) { }

            @Override
            public void onDelete() { }

            @Override
            public void onClear() { }
        });

        // 开始下载
        mission.start();
    }
    
}
```

#### 3. 下载任务管理（以Fragment为例）
```java
public class DownloadManagerFragment extends Fragment {

    /**
     * 创建DownloadMission类型的下载任务管理器
     */
    private final MissionManager<DownloadMission> mManager =
            new MissionManagerImpl<>(DownloadMission.class);

    /**
     * RecyclerView展示所有下载任务
     */
    private RecyclerView mRecyclerView;
    private MissionAdapter mMissionAdapter;
    
    ...省略其它代码...


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 注册MissionManager.Observer
        mManager.register(new MissionManager.Observer<DownloadMission>() {
            @Override
            public void onMissionLoaded(List<DownloadMission> missions) {
                // 刷新数据
                mMissionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onMissionAdd(DownloadMission mission, int position) {
                // 通知添加任务
                mMissionAdapter.notifyItemInserted(position);
            }

            @Override
            public void onMissionDelete(DownloadMission mission, int position) {
                // 通知移除任务
                mMissionAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onMissionFinished(DownloadMission mission, int position) {
                // 通知任务完成
                mMissionAdapter.notifyItemChanged(position);
            }
        });
        // 加载下载任务
        mManager.loadMissions();
    }

    @Override
    public void onDestroyView() {
        // 销毁MissionManager
        mManager.onDestroy();
        super.onDestroyView();
    }

}
```

#### 4. 其它操作
```java

// 开始下载
mission.start();

// 暂停下载
mission.pause();

// 删除下载任务和下载文件
mission.delete();

// 删除下载任务(不包含下载文件)
mission.clear();

// 重新开始下载任务
mission.restart();


// 暂停所有下载任务
ZDownloader.pauseAll();

// 恢复所有下载任务
ZDownloader.resumeAll();

// 删除所有下载任务和下载文件
ZDownloader.deleteAll();

// 删除所有下载任务(不包含下载文件)
ZDownloader.clearAll();

// 打开下载完成的文件
mission.openFile(); // 使用初始化时的Context
mission.openFile(context);

// 重命名文件
mission.renameTo(newName);
```

### 二. 高级使用
#### 1. 在Application中初始化时进行全局设置
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DownloaderConfig<DownloadMission> config = MissionDownloader.builder()
                // 设置状态栏通知回调
                .setNotifier(new DownloadNotifierImpl())
                // 设置任务冲突处理策略
                .setConflictPolicy(new DefaultConflictPolicy() {

                    @Override
                    public void onConflict(Mission mission, Callback callback) {
                        Activity activity = ActivityManager.getCurrentActivity();
                        if (activity == null) {
                            return;
                        }
                        ZDialog.alert()
                                .setTitle("任务已存在")
                                .setContent("下载任务已存在，是否继续下载？")
                                .setPositiveButton((fragment, which) -> callback.onResult(true))
                                .setNegativeButton((fragment, which) -> callback.onResult(false))
                                .show(activity);
                    }
                })
                .build();

        ZDownloader.register(DownloadMission.class, new MissionDownloader(config));
    }

}            
```

#### 2. 创建下载任务时单独设置下载配置
```java
		// 创建下载任务
        DownloadMission mission = new Mission.Builder(url, name.getText().toString())
                // 设置文件保存地址
                .setDownloadPath("custom download path")
                // 下载线程（分块下载有效）
                .setThreadCount(3)
                // 设置User-Agent
                .setUserAgent("custom user-agent")
                // 设置Cookie
                .setCookie("set cookies")
                // 添加请求头
                .addHeader(HttpHeader.REFERER, url)
                // 设置分块大小
                .setBlockSize(2 * 1024 * 1024)
                // 设置缓冲区大小
                .setBufferSize(64 * 1024)
                // 设置连接超时时间
                .setConnectOutTime(20000)
                // 设置读取超时时间
                .setReadOutTime(20000)
                // 设置进度回调频率，单位ms
                .setProgressInterval(2000)
                // 设置出错重试次数
                .setRetryCount(10)
                // 设置出错重试延迟
                .setRetryDelayMillis(10000)
                // 设置是否允许通知栏通知
                .setEnableNotification(true)
                // 创建DownloadMission类型的下载任务
                .build(DownloadMission.class);
```

#### 3. 通知拦截
```java
/**
 * 实现Notifier接口，在onProgress、onFinished、onError方法中更新通知
 */
public class DownloadNotifierImpl implements Notifier<Mission> {

    @Override
    public void onProgress(Context context, Mission mission, float progress, boolean isPause) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildProgressNotify()
                .setProgressAndFormat(progress, false, "")
                .setContentTitle((isPause ? "已暂停：" : "") + mission.getName())
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onFinished(Context context, Mission mission) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle(mission.getName())
                .setContentText("下载已完成")
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onError(Context context, Mission mission, int errCode) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ZNotify.with(context)
                .buildNotify()
                .setContentTitle("下载出错" + errCode + ":" + mission.getName())
                .setContentIntent(pendingIntent)
                .setId(mission.getNotifyId())
                .show();
    }

    @Override
    public void onCancel(Context context, Mission mission) {
        ZNotify.cancel(mission.getNotifyId());
    }

    @Override
    public void onCancelAll(Context context) {
        ZNotify.cancelAll();
    }

}

	DownloaderConfig<DownloadMission> config = MissionDownloader.builder()
		// 设置状态栏通知回调
		.setNotifier(new DownloadNotifierImpl())
		.build();

	ZDownloader.register(DownloadMission.class, new MissionDownloader(config));
```

#### 4. 自定义下载任务（TODO 完善README）