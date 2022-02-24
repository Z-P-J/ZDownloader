// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.zpj.downloader.utils;

import com.zpj.utils.PrefsHelper;

import java.util.concurrent.atomic.AtomicLong;


public class MissionIdGenerator {

    public static final String PREF_NEXT_ID = "MISSION_NEXT_ID";

    private static final Object INSTANCE_LOCK = new Object();
    private static MissionIdGenerator sInstance;

    private final AtomicLong mIdCounter = new AtomicLong(0);

    public static MissionIdGenerator getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) sInstance = new MissionIdGenerator();
        }
        return sInstance;
    }

    public final long generateValidId() {
        long id = mIdCounter.getAndIncrement();
        incrementIdCounterTo(id + 1);
        return id;
    }

    private void incrementIdCounterTo(long id) {
        long diff = id - mIdCounter.get();
        if (diff < 0) return;

        mIdCounter.addAndGet(diff);
        PrefsHelper.with().putLong(PREF_NEXT_ID, mIdCounter.get());
    }

    private MissionIdGenerator() {
        mIdCounter.set(PrefsHelper.with().getLong(PREF_NEXT_ID, 0));
    }

}