# ZDownloader

----------------------------------
### 待完善后发布到jCenter或jitpack
----------------------------------

## 如何使用？
### 一. 简单使用
#### 1. 推荐在Activity中初始化
```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        ZDownloader.init(this); //初始化
    }

}            
```

#### 2. 创建下载并监听进度
```java
DownloadMission mission = DownloadMission.create("your download url", config);
mission..addListener(new DownloadMission.MissionListener() {
                    @Override
                    public void onInit() {
                        
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onWaiting() {

                    }

                    @Override
                    public void onProgress(long done, long total) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onError(int errCode) {

                    }
                }); 

mission.start(); // 开始下载任务
```

#### 3. 完全退出应用时
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    ZDownloader.onDestroy();
}
```

#### 4. 其它操作
```java
//暂停下载
mission.pause();

//恢复下载
mission.start();

//删除下载任务和下载文件
mission.delete();

//删除下载任务(不包含下载文件)
mission.clear();

//暂停所有下载任务
ZDownloader.pauseAll();

//恢复所有下载任务
ZDownloader.resumeAll();

//删除所有下载任务和下载文件
ZDownloader.deleteAll();

//删除所有下载任务(不包含下载文件)
ZDownloader.clearAll();

//打开下载完成的文件
mission.openFile(); // 使用初始化时的Context
mission.openFile(context);

//重命名文件
mission.renameTo(newName);
```

### 二. 高级使用
#### 1. 在Activity中初始化时进行全局设置
```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        //全局设置
        DownloaderOptions options = DownloaderOptions.with(this)
                .setNotificationInterceptor(new DownloadNotificationInterceptor()) // 设置通知拦截器
                .setDownloadPath("") //设置默认下载路径
                .setEnableNotification(true) //是否显示通知栏下载进度通知，默认为true
                .setBlockSize(1024 * 1024) //设置下载块大小，默认为1024 * 1024
                .setBufferSize(1024) //设置缓存大小
                //.setThreadCount(5) //已过时。设置线程大小，默认为3，要修改线程数量请使用setThreadPoolConfig
                .setRetryCount(10) //设置出错重试次数，默认为5
                .setRetryDelay(10000) //设置重试延迟，单位为ms
                .setUserAgent("") //设置UA，默认为系统自带UA
                .setCookie("") //设置全局下载任务cookie，默认为空
                .setConnectOutTime(10000) //设置连接超时，单位ms
                .setReadOutTime(10000) //设置读取请求内容超时，单位ms
                .setHeaders(new HashMap<>()) //设置请求头，若Map中含有key为cookie或user-agent的键值对，则会覆盖setCookie或setUserAgent的值
                .setProxy(Proxy.NO_PROXY) //设置代理
                //.setProxy("127.0.0.1", 80) //设置代理
                .setThreadPoolConfig(
                        ThreadPoolConfig.build()
                        .setCorePoolSize(5) //设置线程池线程数
                        .setMaximumPoolSize(36) //设置线程池最大线程数
                        .setKeepAliveTime(60) // 设置空闲线程存活时间
                        .setWorkQueue(new LinkedBlockingQueue<Runnable>()) //设置并发队列
                        .setHandler(new ThreadPoolExecutor.AbortPolicy()) //设置拒绝策略
                        .setThreadFactory(new ThreadFactory() { //设置创建新线程的线程工厂
                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r);
                            }
                        })
                );
        ZDownloader.init(options); //初始化
    }

}            
```

#### 1. 创建下载任务时单独设置下载配置
```java
//为每个下载任务进行设置，优先使用单独设置的参数
MissionOptions options = MissionOptions.with()
                .setDownloadPath("") //单独设置任务下载路径
                .setNotificationInterceptor(new DownloadNotificationInterceptor()) // 单独设置通知拦截器
                .setEnableNotification(true) //单独设置是否显示通知栏下载进度通知，默认为true
                .setBlockSize(1024 * 1024) //单独设置下载块大小，默认为1024 * 1024
                .setBufferSize(1024) //设置缓存大小
                //.setThreadCount(5) //单独设置线程大小，默认为3，已过时
                .setRetryCount(10) //设置出错重试次数，默认为5
                .setRetryDelay(10000) //设置重试延迟，单位为ms
                .setUserAgent("") //单独设置UA，默认为系统自带UA
                .setCookie(""); //单独设置cookie，默认为空
                .setConnectOutTime(10000) //设置连接超时，单位ms
                .setReadOutTime(10000) //设置读取请求内容超时，单位ms
                .setHeaders(new HashMap<>()) //设置请求头，若Map中含有key为cookie或user-agent的键值对，则会覆盖setCookie或setUserAgent的值
                .setProxy(Proxy.NO_PROXY) //设置代理
                //.setProxy("127.0.0.1", 80) //设置代理
                .setThreadPoolConfig(
                        ThreadPoolConfig.build()
                        .setCorePoolSize(5) //设置线程池线程数
                        .setMaximumPoolSize(36) //设置线程池最大线程数
                        .setKeepAliveTime(60) // 设置空闲线程存活时间
                        .setWorkQueue(new LinkedBlockingQueue<Runnable>()) //设置并发队列
                        .setHandler(new ThreadPoolExecutor.AbortPolicy()) //设置拒绝策略
                        .setThreadFactory(new ThreadFactory() { //设置创建新线程的线程工厂
                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r);
                            }
                        })
                );
DownloadMission mission = DownloadMission.create("your download url", config);
mission.start();
//下载进度监听同上
```


##  混淆规则（如果您的项目使用了代码混淆，请一定添加以下规则至项目的proguard-rules.pro文件中）
```
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.zpj.downloader.core.DownloadMission{*;}
-keep class com.zpj.downloader.core.DownloadMission* {
        *;
 }
-keep class com.zpj.downloader.config.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------
```
