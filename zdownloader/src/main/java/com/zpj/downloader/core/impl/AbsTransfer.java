package com.zpj.downloader.core.impl;

import android.support.annotation.WorkerThread;

import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.Block;
import com.zpj.downloader.core.Downloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.Result;
import com.zpj.downloader.core.Transfer;
import com.zpj.downloader.utils.io.BufferedRandomAccessFile;
import com.zpj.http.core.HttpHeader;
import com.zpj.http.core.IHttp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AbsTransfer<T extends Mission> implements Transfer<T> {

    public static final String TAG = "AbsTransfer";

    @WorkerThread
    @Override
    public Result transfer(T mission, Block block) {
        Downloader<T> downloader = ZDownloader.get(mission);

        long start;
        long end;
        if (block == null) {
            start = end = 0;
        } else {
            start = block.getStart() + block.getDownloaded();
            end = block.getEnd();
        }


        Map<String, String> headers = new HashMap<>(mission.getConfig().getHeaders());
        if (mission.isBlockDownload()) {
            headers.put(HttpHeader.RANGE, String.format(Locale.ENGLISH, "bytes=%d-%d", start, end));
        } else {
            headers.put(HttpHeader.RANGE, "bytes=0-");
        }

        IHttp.Response response = null;
        try {
            response = downloader.getHttpFactory().request(mission.getUrl(), headers);
            int code = response.statusCode();
            if (code / 100 == 2) {
                try (BufferedInputStream is = new BufferedInputStream(response.bodyStream());
                     BufferedRandomAccessFile f = new BufferedRandomAccessFile(mission.getFilePath(), "rw")) {
                    f.seek(start);

                    int bufferSize = mission.getConfig().getBufferSize();
                    byte[] buf = new byte[bufferSize];
                    int len;
                    int downloaded = 0;
                    while ((len = is.read(buf, 0, buf.length)) != -1) {
                        f.write(buf, 0, len);
                        downloaded += len;
                        block.setDownloaded(downloaded);
                        downloader.getDao().updateBlockDownloaded(block, downloaded);
                    }

                    f.flush();
                }
            } else {
                return Result.error(-1, Error.SERVER_UNSUPPORTED.getErrorMsg());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(-1, Error.FILE_NOT_FOUND.getErrorMsg());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(-1, e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return Result.ok(0, "block transfer success!");
    }

}
