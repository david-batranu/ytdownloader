/***
 	Copyright (c) 2012-2013 Samuele Rini
 	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program. If not, see http://www.gnu.org/licenses
	
	***
	
	https://github.com/dentex/ytdownloader/
    https://sourceforge.net/projects/ytdownloader/
	
	***
	
	Different Licenses and Credits where noted in code comments.
*/


package dentex.youtube.downloader.service;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import dentex.youtube.downloader.R;
import dentex.youtube.downloader.SettingsActivity;
import dentex.youtube.downloader.YTD;
import dentex.youtube.downloader.utils.Observer;
import dentex.youtube.downloader.utils.Utils;

public class FfmpegDownloadService extends Service {
	
	private static final String DEBUG_TAG = "FfmpegDownloadService";
	public static Context nContext;
	private String cpuVers;
	private String sdCardAppDir;
	private String link;
	public static String DIR;
	private DownloadManager dm;
	private long enqueue;
	private Observer.YtdFileObserver ffmpegBinObserver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Utils.logger("d", "service created", DEBUG_TAG);
		nContext = getBaseContext();	
		registerReceiver(ffmpegReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	public static Context getContext() {
        return nContext;
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		cpuVers = intent.getStringExtra("CPU");
		Utils.logger("d", "CPU version: " + cpuVers, DEBUG_TAG);
		
		link = intent.getStringExtra("LINK");
		Utils.logger("d", "FFmpeg download link: " + link, DEBUG_TAG);
		
		sdCardAppDir = intent.getStringExtra("DIR");
		DIR = sdCardAppDir;
		
		downloadFfmpeg();
		
		super.onStartCommand(intent, flags, startId);
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Utils.logger("d", "service destroyed", DEBUG_TAG);
		unregisterReceiver(ffmpegReceiver);
	}
	
	private void downloadFfmpeg() {
        Request request = new Request(Uri.parse(link));
        request.setDestinationInExternalFilesDir(nContext, null, YTD.ffmpegBinName + YTD.FFMPEG_CURRENT_V);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(getString(R.string.ffmpeg_download_notification));
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
        	enqueue = dm.enqueue(request);
        } catch (IllegalArgumentException e) {
	    	Log.e(DEBUG_TAG, "downloadFfmpeg: " + e.getMessage());
	    	Toast.makeText(this,  this.getString(R.string.no_downloads_sys_app), Toast.LENGTH_SHORT).show();
	    } catch (SecurityException se) {
	    	request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, YTD.ffmpegBinName);
	    	enqueue = dm.enqueue(request);
	    	DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	    } catch (NullPointerException ne) {
	    	Log.e(DEBUG_TAG, "callDownloadApk: " + ne.getMessage());
	    	Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
	    }
        
		ffmpegBinObserver = new Observer.YtdFileObserver(DIR);
        ffmpegBinObserver.startWatching();
	}

	BroadcastReceiver ffmpegReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Utils.logger("d", "ffmpegReceiver: onReceive CALLED", DEBUG_TAG);
    		long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
    		
    		if (enqueue != -1 && id != -2 && id == enqueue) {
	    		Query query = new Query();
				query.setFilterById(id);
				Cursor c = dm.query(query);
				if (c.moveToFirst()) {
				
					int statusIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
					int reasonIndex = c.getColumnIndex(DownloadManager.COLUMN_REASON);
					int status = c.getInt(statusIndex);
					int reason = c.getInt(reasonIndex);

					switch (status) {
					
					case DownloadManager.STATUS_SUCCESSFUL:
	    		
						File src = new File(DIR, YTD.ffmpegBinName + YTD.FFMPEG_CURRENT_V);
						File dst = new File(nContext.getDir("bin", 0), YTD.ffmpegBinName);
						
						String md5 = null;
						if (cpuVers.equals(YTD.ARMv7a_NEON)) 	md5 = "ed3ac5269496b2625d26471cf57f15d0";
						if (cpuVers.equals(YTD.ARMv7a)) 		md5 = "deadfb4746e4cdf47aa2bcf7b55d718b";
						if (cpuVers.equals(YTD.ARMv5te)) 		md5 = "ecd4372e667feb2b2971dbe361a3fe40";
						
						if (Utils.checkMD5(md5, src)) {
							SettingsActivity.SettingsFragment.copyFfmpegToAppDataDir(context, src, dst);
						} else {
							SettingsActivity.SettingsFragment.touchAdvPref(true, false);
							deleteBadDownload(id);
						}
						break;
						
					case DownloadManager.STATUS_FAILED:
						Log.e(DEBUG_TAG, YTD.ffmpegBinName + " download FAILED (status " + status + ") Reason: " + reason);
						Toast.makeText(nContext,  YTD.ffmpegBinName + ": " + getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
						
						SettingsActivity.SettingsFragment.touchAdvPref(true, false);
						deleteBadDownload(id);
						break;
						
					default:
						Utils.logger("w", YTD.ffmpegBinName + " download completed with status " + status, DEBUG_TAG);
					}
				}
    		}
    		ffmpegBinObserver.stopWatching();
    		stopSelf();
		}
	};
	
	private void deleteBadDownload (long id) {
		dm.remove(id);
		Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
	}
}
