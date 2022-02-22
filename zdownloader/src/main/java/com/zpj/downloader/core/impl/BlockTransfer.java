package com.zpj.downloader.core.impl;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.constant.HttpHeader;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.core.http.Response;
import com.zpj.downloader.utils.Logger;
import com.zpj.downloader.utils.io.BufferedRandomAccessFile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 下载任务传输器
 * @param <T> mission
 * @author Z-P-J
 */
public class BlockTransfer<T extends Mission> implements Transfer<T> {

    public static final String TAG = "AbsTransfer";

    @WorkerThread
    @Override
    public Result transfer(T mission, @NonNull Block block) {
        Downloader<T> downloader = ZDownloader.get(mission);

        long start;
        long end;

        Map<String, String> headers = new HashMap<>(mission.getConfig().getHeaders());
        if (mission.isBlockDownload()) {
            start = block.getStart() + block.getDownloaded();
            end = block.getEnd();
            headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-%d", start, end));
        } else {
            start = end = 0;
            headers.put(HttpHeader.RANGE, "bytes=0-");
        }
        Logger.d(TAG, "start=" + start + " end=" + end);

        Logger.d(TAG, "headers=" + headers);

        Response response = null;
        try {
            response = downloader.getHttpFactory().request(mission, headers);
            if (!mission.isDownloading()) {
                return Result.paused();
            }
            int code = response.statusCode();
            Logger.d(TAG, "code=" + code);
            if (code / 100 == 2) {
                try (BufferedInputStream is = new BufferedInputStream(response.bodyStream());
                     BufferedRandomAccessFile f = new BufferedRandomAccessFile(mission.getFilePath(), "rw")) {
                    f.seek(start);

                    int bufferSize = 512 * 1024; // mission.getConfig().getBufferSize()
                    byte[] buf = new byte[bufferSize];
                    int len;
                    int downloaded = 0;
                    while ((len = is.read(buf, 0, buf.length)) != -1) {
                        f.write(buf, 0, len);
                        downloaded += len;
                        Logger.d(TAG, "downloaded=" + downloaded);
                        block.setDownloaded(downloaded);
                        downloader.getDao().updateBlockDownloaded(block, downloaded);

                        if (!mission.isDownloading()) {
                            f.flush();
                            Logger.d(TAG, "return by not downloading!");
                            return Result.paused();
                        }
                    }


                    Logger.d(TAG, "total downloaded=" + downloaded);

                    f.flush();
                }
                return Result.ok("block transfer success!");
            } else {
                return Result.error(Error.SERVER_UNSUPPORTED.getErrorMsg());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(Error.FILE_NOT_FOUND.getErrorMsg());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
