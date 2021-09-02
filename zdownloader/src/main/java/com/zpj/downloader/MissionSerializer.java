package com.zpj.downloader;

import java.io.File;

public interface MissionSerializer {

    BaseMission<?> readMission(final File file, final Class<? extends BaseMission<?>> clazz);

    void writeMission(final BaseMission<?> mission);

}
