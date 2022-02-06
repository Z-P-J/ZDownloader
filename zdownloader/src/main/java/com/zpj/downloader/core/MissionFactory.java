package com.zpj.downloader.core;

import com.zpj.downloader.core.impl.Config;

public interface MissionFactory<T> {

    T create(String url, String name, Config config);

}
