# QXDownloader

### 如何使用

## 1. 在Application中初始化
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        QianXun.register(this);
    }

}            
```
