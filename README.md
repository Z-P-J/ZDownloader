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
                    public void onProgressUpdate(long done, long total) {

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
                .setThreadCount(5) //设置线程大小，默认为3
                .setRetryCount(10) //设置出错重试次数，默认为5
                .setUserAgent("") //设置UA，默认为系统自带UA
                .setCookie(""); //设置下载任务cookie，默认为空
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
                .setThreadCount(5) //单独设置线程大小，默认为3
                .setRetryCount(10) //单独设置出错重试次数，默认为5
                .setUserAgent("") //单独设置UA，默认为系统自带UA
                .setCookie(""); //单独设置全局cookie，默认为空
QianXun.download("your download url", options1);           
```
