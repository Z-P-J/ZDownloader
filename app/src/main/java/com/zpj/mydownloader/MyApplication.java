package com.zpj.mydownloader;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.zpj.qxdownloader.QianXun;
import com.zpj.qxdownloader.config.QianXunConfig;

import java.security.MessageDigest;
import java.util.Locale;

/**
 * @author Z-P-J
 * */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        QianXunConfig options = QianXunConfig.with(this)
                .setBlockSize(1024 * 1024)
//                .setThreadCount(5)
//                .setThreadPoolConfig(
//                        ThreadPoolConfig.build()
//                        .setCorePoolSize(5)
//                        .setMaximumPoolSize(36)
//                        .setKeepAliveTime(60)
//                        .setWorkQueue(new LinkedBlockingQueue<Runnable>())
//                        .setHandler(new ThreadPoolExecutor.AbortPolicy())
//                        .setThreadFactory(new ThreadFactory() {
//                            @Override
//                            public Thread newThread(Runnable r) {
//                                return new Thread(r);
//                            }
//                        })
//                )
                .setRetryCount(10)
//                .setUserAgent("")
                .setCookie("");
        QianXun.init(options);
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
