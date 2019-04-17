package com.zpj.qxdownloader.option;

import android.os.Environment;

/**
 * @author Z-P-J
 * */
public class DefaultOptions {

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QianXun/";

    public static final int THREAD_COUNT = 3;

    public static final int BLOCK_SIZE = 1024 * 1024;

    public static final String USER_AGENT = System.getProperty("http.agent");

    public static final int RETRY_COUNT = 3;

    // 单位毫秒
    public static final int RETRY_DELAY = 10 * 1000;

    public static final int TONG_SHI = 3;

    public static final String KEY_DOWNLOAD_PATH = "download_path";

    public static final String KEY_THREAD_COUNT = "thread_count";

    public static final String KEY_BLOCK_SIZE = "block_size";

    public static final String KEY_USER_AGENT = "user_agent";

    public static final String KEY_RETRY_COUNT = "retry_count";

    public static final String KEY_RETRY_DELAY = "retry_delay";

    public static final String KEY_TONG_SHI = "tong_shi";

    private DefaultOptions() {

    }

}
