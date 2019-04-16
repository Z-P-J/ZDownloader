package com.zpj.qxdownloader.get;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public interface DownloadManager
{
	int BLOCK_SIZE = 1024 * 1024;

	List<DownloadMission> mMissions = new ArrayList<>();

	interface DownloadManagerListener {
		void onMissionAdd();
	}
	
	int startMission(String url, String name, int threads);
	int startMission(String url, String name, int threads, String cookie, String user_agent);

	void resumeMission(int id);
	void resumeMission(String uuid);
	void resumeAllMissions();

	void pauseMission(int id);
	void pauseMission(String uuid);
	void pauseAllMissions();

	void deleteMission(int id);
	void deleteMission(String uuid);
	void deleteAllMissions();

	void clearMission(int i);
	void clearMission(String uuid);
	void clearAllMissions();

	DownloadMission getMission(int id);
	DownloadMission getMission(String uuid);

	int getCount();

	Context getContext();

//	String getDownloadPath();
//
//	void setDownloadPath(String downloadPath);

	void loadMissions();

	void setDownloadManagerListener(DownloadManagerListener downloadManagerListener);

	List<DownloadMission> getMissions();
}
