package com.zpj.downloader;

import java.io.File;

/**
 * 下载任务序列化/反序列化接口
 */
public interface MissionSerializer {

    BaseMission<?> readMission(final File file, final Class<? extends BaseMission<?>> clazz);

    void writeMission(final BaseMission<?> mission);

}
