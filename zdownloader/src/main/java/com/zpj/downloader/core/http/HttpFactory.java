package com.zpj.downloader.core.http;

import com.zpj.downloader.core.Mission;

import java.io.IOException;
import java.util.Map;

/**
 * 网络请求工厂类
 * @author Z-P-J
 */
public interface HttpFactory {

    /**
     * 发起请求
     * @param mission 任务
     * @param headers 请求头
     * @return 网络响应 {@link Response}
     * @throws IOException IO异常
     */
    Response request(Mission mission, Map<String, String> headers) throws IOException;

}
