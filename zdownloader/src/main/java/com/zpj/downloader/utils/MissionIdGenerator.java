// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.zpj.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.atomic.AtomicLong;


public class MissionIdGenerator {

    private static final class SingletonHolder {
        private static final MissionIdGenerator INSTANCE = new MissionIdGenerator();
    }

    private static final String SP_NAME = "z_downloader_mission_id_generator";
    public static final String KEY_MISSION_NEXT_ID = "KEY_MISSION_NEXT_ID";

    private final AtomicLong mIdCounter = new AtomicLong(0);

    private final SharedPreferences mSp;

    private MissionIdGenerator() {
        mSp = ContextProvider.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mIdCounter.set(mSp.getLong(KEY_MISSION_NEXT_ID, 0));
    }

    public static MissionIdGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public final long generateValidId() {
        long id = mIdCounter.getAndIncrement();
        incrementIdCounterTo(id + 1);
        return id;
    }

    private void incrementIdCounterTo(long id) {
        long diff = id - mIdCounter.get();
        if (diff < 0) return;
        mSp.edit().putLong(KEY_MISSION_NEXT_ID, mIdCounter.addAndGet(diff)).apply();
    }

}