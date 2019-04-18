# QXDownloader

----------------------------------
### 待完善后发布到jCenter或jitpack
----------------------------------

# 如何使用？
## 一. 简单使用
### 1. 在Application中初始化
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QianXun.init(this); //初始化
    }

}            
```

### 2. 下载并监听进度
```java
QianXun.download("your download url")
                .addListener(new DownloadMission.MissionListener() {
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
```

### 3. 退出应用时
```java
@Override
protected void onDestroy() {
    QianXun.unInit();
    super.onDestroy();
}
```

### 4. 其它操作
```java
//暂停下载
QianXun.pause(mission);

//恢复下载
QianXun.resume(mission);

//删除下载任务和下载文件
QianXun.delete(mission);

//删除下载任务(不包含下载文件)
QianXun.clear(mission);

//暂停所有下载任务
QianXun.pauseAll();

//恢复所有下载任务
QianXun.resumeAll();

//删除所有下载任务和下载文件
QianXun.deleteAll();

//删除所有下载任务(不包含下载文件)
QianXun.clearAll();
```

## 二. 高级使用
### 1. 在Application中初始化时进行全局设置
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //全局设置
        QianXunOptions options = QianXunOptions.with(this)
                .setDownloadPath("") //设置默认下载路径
                .setBlockSize(1024 * 1024) //设置下载块大小，默认为1024 * 1024
                .setBufferSize(1024) //设置缓存大小
                //.setThreadCount(5) //设置线程大小，默认为3, 已过时，要修改线程数量请使用setThreadPoolConfig
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
                )
                .setRetryCount(10) //设置出错重试次数，默认为5
                .setRetryDelay(10000) //设置重试延迟，单位为ms
                .setUserAgent("") //设置UA，默认为系统自带UA
                .setCookie("") //设置下载任务cookie，默认为空
                .setConnectOutTime(10000) //设置连接超时，单位ms
                .setReadOutTime(10000) //设置读取请求内容超时，单位ms
                .setHeaders(new HashMap<>()); //设置请求头，若Map中含有key为cookie或user-agent的键值对，则会覆盖setCookie或setUserAgent的值
        QianXun.init(options); //初始化
    }

}            
```

### 1. 下载时进行单独设置
```java
//为每个下载任务进行设置，优先使用单独设置的参数
MissionOptions options = MissionOptions.with()
                .setDownloadPath("") //单独设置任务下载路径
                .setBlockSize(1024 * 1024) //单独设置下载块大小，默认为1024 * 1024
                .setBufferSize(1024) //设置缓存大小
                //.setThreadCount(5) //单独设置线程大小，默认为3，已过时
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
                )
                .setRetryCount(10) //设置出错重试次数，默认为5
                .setRetryDelay(10000) //设置重试延迟，单位为ms
                .setUserAgent("") //单独设置UA，默认为系统自带UA
                .setCookie(""); //单独设置全局cookie，默认为空
                .setConnectOutTime(10000) //设置连接超时，单位ms
                .setReadOutTime(10000) //设置读取请求内容超时，单位ms
                .setHeaders(new HashMap<>()); //设置请求头，若Map中含有key为cookie或user-agent的键值对，则会覆盖setCookie或setUserAgent的值
QianXun.download("your download url", options);
//下载进度监听同上
```
