package com.zpj.downloader.impl;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.core.http.HttpHeader;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.core.http.Response;
import com.zpj.downloader.core.model.Block;
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
public class MissionBlockTransfer<T extends Mission> implements Transfer<T> {

    public static final String TAG = "BlockTransfer";

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
            mission.getMissionInfo().setDownloaded(0);
            downloader.getRepository().updateMissionInfo(mission);
            block.setDownloaded(0);
            start = end = 0;
            headers.put(HttpHeader.RANGE, "bytes=0-");
        }
        Logger.d(TAG, "start=%s end=%s", start, end);

        Response response = null;
        try {
            response = downloader.getHttpFactory().request(mission, headers);
            if (!mission.isDownloading()) {
                return Result.paused();
            }
            Logger.d(TAG, "response=%s", response);
            int code = response.statusCode();
            if (code / 100 == 2) {
                try (BufferedInputStream is = new BufferedInputStream(response.bodyStream());
                     BufferedRandomAccessFile f = new BufferedRandomAccessFile(mission.getFilePath(), "rw")) {
                    f.seek(start);

                    int bufferSize = 512 * 1024; // mission.getConfig().getBufferSize()
                    byte[] buf = new byte[bufferSize];
                    int len;
                    long downloaded = block.getDownloaded();
                    while ((len = is.read(buf, 0, buf.length)) != -1) {
                        f.write(buf, 0, len);
                        downloaded += len;
//                        Logger.d(TAG, "downloaded=" + downloaded);
                        block.setDownloaded(downloaded);
                        downloader.getRepository().updateBlockDownloaded(block, downloaded);

                        if (!mission.isDownloading()) {
                            f.flush();
                            Logger.d(TAG, "return by not downloading!");
                            return Result.paused();
                        }
                    }


                    Logger.d(TAG, "total downloaded=%s", downloaded);

                    f.flush();
                }
                return Result.ok("block transfer success!");
            } else {
                return Result.error(response.statusMessage());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error("file not found!");
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
