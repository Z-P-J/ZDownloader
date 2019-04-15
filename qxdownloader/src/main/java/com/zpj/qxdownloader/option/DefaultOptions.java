package com.zpj.qxdownloader.option;

import android.os.Environment;

public class DefaultOptions {

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QianXun/";

    public static final int THREAD_COUNT = 3;

    public static final int BLOCK_SIZE = 1024 * 1024;

    public static final String USER_AGENT = System.getProperty("http.agent");

    public static final int RETRY_COUNT = 3;

}
