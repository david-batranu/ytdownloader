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

package dentex.youtube.downloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;


import dentex.youtube.downloader.queue.QueueThread;
import dentex.youtube.downloader.queue.QueueThreadListener;
import dentex.youtube.downloader.service.AutoUpgradeApkService;
import dentex.youtube.downloader.utils.PopUps;
import dentex.youtube.downloader.utils.Utils;

public class YTD extends Application implements QueueThreadListener{
	
	static String DEBUG_TAG = "YTD";
	public static Context ctx;

	// *** development configurations *** // TODO					// for release set to:
	// ================================== // ===========			// ==================
	public static String BugsenseApiKey = "00000000";				// actual api key
	public static boolean SHOW_ITAGS_AND_NO_SIZE_FOR_DUBUG = false;	// false
	
	public static int SIG_HASH = -1892118308;   					// final string
	//public static int SIG_HASH = -118685648;  					// dev test: desktop
	//public static int SIG_HASH = 1922021506;  					// dev test: laptop
	// **********************************
	
	public static final String JSON_FILENAME = "dashboard.json";
	public static final String JSON_FILENAME_NO_EXT = "dashboard";
	public static final String JSON_FILENAME_EXT_ONLY = ".json";
	public static final String JSON_FOLDER = "json";
	public static File JSON_FILE = null;
	
	public static final String JSON_DATA_ID = "id";
	public static final String JSON_DATA_YTID = "ytid";
	public static final String JSON_DATA_POS = "pos";
	public static final String JSON_DATA_TYPE = "type";
	public static final String JSON_DATA_TYPE_V = "VIDEO";
	public static final String JSON_DATA_TYPE_V_O = "VIDEO-ONLY";
	public static final String JSON_DATA_TYPE_V_M = "VIDEO-MUX";
	public static final String JSON_DATA_TYPE_A_E = "AUDIO-EXTR";
	public static final String JSON_DATA_TYPE_A_M = "AUDIO-MP3";
	public static final String JSON_DATA_TYPE_A_O = "AUDIO-ONLY";
	public static final String JSON_DATA_STATUS = "status";
	public static final String JSON_DATA_STATUS_COMPLETED = "COMPLETED";
	public static final String JSON_DATA_STATUS_IN_PROGRESS = "IN_PROGRESS";
	public static final String JSON_DATA_STATUS_FAILED = "FAILED";
	public static final String JSON_DATA_STATUS_PAUSED = "PAUSED";
	public static final String JSON_DATA_STATUS_IMPORTED = "IMPORTED";
	public static final String JSON_DATA_STATUS_QUEUED = "QUEUED";
	public static final String JSON_DATA_PATH = "path";
	public static final String JSON_DATA_FILENAME = "filename";
	public static final String JSON_DATA_BASENAME = "basename";
	public static final String JSON_DATA_AUDIO_EXT = "audio_ext";
	public static final String JSON_DATA_SIZE = "size";
	
	public static final String ffmpegBinName = "ffmpeg";
	public static String FFMPEG_CURRENT_V = "_v2.1";
	
	public static String ARMv7a_NEON = "armv7a-neon";
	public static String ARMv7a = "armv7a";
	public static String ARMv5te = "armv5te";
	public static String UNSUPPORTED_CPU = "UNSUPPORTED";
	
	public static int _AUDIO_EXTR = 0;
	public static int _THUMB_EXTR = 1;
	
	public static SharedPreferences settings;
	public static SharedPreferences videoinfo;
	
	public static boolean isAnyAsyncInProgress = false;
	
	public static String PREFS_NAME = "dentex.youtube.downloader_preferences";
	public static String VIDEOINFO_NAME = "dentex.youtube.downloader_videoinfo";
	
	public static String pt1;
	public static String pt2;
	public static NotificationManager mNotificationManager;
	public static NotificationCompat.Builder mBuilder;
	public static List<Long> sequence = new ArrayList<Long>();
	
	//public static String USER_AGENT_FIREFOX = "Mozilla/5.0 (X11; Linux i686; rv:10.0) Gecko/20100101 Firefox/10.0";
	public static String USER_AGENT_FIREFOX = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20100101 Firefox/24.0";
	public static File dir_Downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	public static File dir_DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	public static File dir_Movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
	
	public static Map<Long, Integer> mFFmpegPercentMap = new HashMap<Long, Integer>();

	public static final String THUMBS_FOLDER = "thumbs";
	public static double reduceFactor;
	
	public static QueueThread queueThread;
	public static Handler handler;
	
	public static Drawable slMenuOrigBkg;
	
	public static final String VIEW_ALL_STRING = "";
	public static final int VIEW_ALL = -1;
	
	public static final int AO_FILTER = 10;
	public static final int VO_FILTER = 9;
	public static final int _3D_FILTER = 8;
	
	public static final int SD_FILTER = 7;
	public static final int MD_FILTER = 6;
	public static final int LD_FILTER = 5;
	public static final int HD_FILTER = 4;

	public static final int _3GP_FILTER = 3;
	public static final int FLV_FILTER = 2;
	public static final int WEBM_FILTER = 1;
	public static final int MP4_FILTER = 0;
	
	public static Integer[] iMp4 = { 18, 22, 37, 38, 59, 78, 82, 83, 84, 133, 134, 135, 136, 137, 138, 160, 264 };
	public static Integer[] iWebm = { 43, 44, 45, 46, 100, 101, 102, 242, 243, 244, 245, 246, 247, 248 };
	public static Integer[] iFlv = { 5, 6, 34, 35 };
	public static Integer[] i3gp = { 17, 36 };
	
	public static Integer[] iHd = { 22, 37, 38, 45, 46, 84, 102, 136, 137, 138, 247, 248, 264 };
	public static Integer[] iLd = { 35, 44, 59, 85, 135, 244, 245, 246 };
	public static Integer[] iMd = { 18, 34, 43, 78, 82, 100, 101, 134, 243 };
	public static Integer[] iSd = { 5, 6, 17, 36, 83, 133 };
	
	public static Integer[] i3d = { 82, 83, 84, 85, 100, 101, 102 };
	
	public static Integer[] iVo = { 133, 134, 135, 136, 137, 138, 160, 242, 243, 244, 245, 246, 247, 248, 264 };
	public static Integer[] iAo = { 139, 140, 141, 171, 172 };
	
	public static List<Integer> iVoList = Arrays.asList(iVo);
	public static List<Integer> iAoList = Arrays.asList(iAo);
	
	@Override
	public void onCreate() {
		Log.d(DEBUG_TAG, "onCreate");

		settings = getSharedPreferences(PREFS_NAME, 0);
		videoinfo = getSharedPreferences(VIDEOINFO_NAME, 0);
		
		
		queueThread = new QueueThread(this);
        queueThread.start();
        
        // Create the Handler. It will implicitly bind to the Looper
        // that is internally created for this thread (since it is the UI thread)
        handler = new Handler();
		
		//findProcessUid();
		
		ctx = getApplicationContext(); 
		JSON_FILE = new File(ctx.getDir(JSON_FOLDER, 0), JSON_FILENAME);
		
		detectSysDefLang();
		
		detectFirstLaunch();
		
		mBuilder =  new NotificationCompat.Builder(ctx);
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		/*Log.i(DEBUG_TAG, 
				"\n --------------- " +
				Utils.getCpuInfo() + 
				"\n --------------- "
				+ "\nNeon support: " + Utils.neonCpu() + 
				"\n --------------- ");*/
		
		super.onCreate();
	}

	/*private void findProcessUid() {
		uid = android.os.Process.myUid();
		Log.d(DEBUG_TAG, "YTD's UID: " + uid);
	}*/
	
	private void detectFirstLaunch() {
		if (settings.getBoolean("first_launch", true)) {
			Log.i(DEBUG_TAG, "First launch for YTD!");
			settings.edit().putBoolean("first_launch", false).apply();
			reduceFactor = detectScreenDensity();
			
			JSON_FILE.delete();
			videoinfo.edit().clear().apply();
		} else {
			reduceFactor = Double.parseDouble(settings.getString("REDUCE_FACTOR", "1"));
			Log.d(DEBUG_TAG, "Retrieved a REDUCE_FACTOR of " + reduceFactor + " from prefs");
		}
	}
	
	private void detectSysDefLang() {
		String storedDefLang = settings.getString("DEF_LANG", "");
    	if (storedDefLang.isEmpty() && storedDefLang != null) {	
    		Locale defLocale = Locale.getDefault();
    		String defLang = defLocale.getLanguage();
    		Log.d(DEBUG_TAG, "Storing default system lang: " + defLang);
    		settings.edit().putString("DEF_LANG", defLang).commit();
    	}
	}

	private double detectScreenDensity() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
	    ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

	    double rf = 1;
		int density = displayMetrics.densityDpi;
		
		switch (density) {
	    case DisplayMetrics.DENSITY_HIGH: 
	    	rf = 1.44;
	    	break;
	    case DisplayMetrics.DENSITY_MEDIUM:
	    	rf = 2;
	    	break;
	    case DisplayMetrics.DENSITY_LOW:
	    	rf = 3;
	    }
	    Log.d(DEBUG_TAG, "DispalyDensity: " + density + " - storing a REDUCE_FACTOR of " + rf + " into prefs");
	    settings.edit().putString("REDUCE_FACTOR", String.valueOf(rf)).apply();;
	    return rf;
	}
	
	public static void updateInit(Activity act, boolean intoSettings, Preference up) {
		int prefSig = settings.getInt("APP_SIGNATURE", 0);
		Utils.logger("d", "prefSig: " + prefSig, DEBUG_TAG);
		
		if (prefSig == 0 ) {
			int currentHash = Utils.getSigHash(act);
			if (currentHash == SIG_HASH) {
				Utils.logger("d", "Found YTD signature: update check possile", DEBUG_TAG);
				if (intoSettings) up.setEnabled(true);
				
				if (settings.getBoolean("autoupdate", false)) {
					Utils.logger("i", "autoupdate enabled", DEBUG_TAG);
					autoUpdate();
				}
	    	} else {
	    		Utils.logger("d", "Found different signature: " + currentHash + " (F-Droid?). Update check cancelled.", DEBUG_TAG);
	    		if (intoSettings) {
	    			up.setEnabled(false);
	    			up.setSummary(R.string.update_disabled_summary);
	    		}
	    	}
			SharedPreferences.Editor editor = settings.edit();
	    	editor.putInt("APP_SIGNATURE", currentHash);
	    	if (editor.commit()) Utils.logger("d", "saving sig pref...", DEBUG_TAG);
		} else {
			if (prefSig == SIG_HASH) {
				Utils.logger("d", "YTD signature in PREFS: update check possile", DEBUG_TAG);
				if (intoSettings) up.setEnabled(true);
				
				if (settings.getBoolean("autoupdate", false)) {
					Utils.logger("i", "autoupdate enabled", DEBUG_TAG);
					autoUpdate();
				}
			} else {
				Utils.logger("d", "diffrent YTD signature in prefs (F-Droid?). Update check cancelled.", DEBUG_TAG);
				if (intoSettings) up.setEnabled(false);
			}
		}
	}
	
	public static void autoUpdate() {
        long storedTime = settings.getLong("time", 0);	// for release
        //long storedTime = 10000; 						// dev test: forces auto update
        
        boolean shouldCheckForUpdate = !DateUtils.isToday(storedTime);
        Utils.logger("i", "shouldCheckForUpdate: " + shouldCheckForUpdate, DEBUG_TAG);
        if (shouldCheckForUpdate) {
        	Intent intent = new Intent(ctx, AutoUpgradeApkService.class);
	        ctx.startService(intent);
        }
        
        long time = System.currentTimeMillis();
        if (settings.edit().putLong("time", time).commit()) Utils.logger("i", "time written in prefs", DEBUG_TAG);
	}

	public static void NoDownProvPopUp(Activity act) {
		PopUps.showPopUp(act.getString(R.string.error), act.getString(R.string.no_downloads_sys_app), "error", act);
	}
	
    public static void NotificationHelper(Context ctx) {
    	mBuilder =  new NotificationCompat.Builder(ctx); // to reset its DEFAULTS
    	
    	pt1 = ctx.getString(R.string.notification_downloading_pt1);
    	pt2 = ctx.getString(R.string.notification_downloading_pt2);
    	
    	Intent notificationIntent = new Intent(ctx, DashboardActivity.class);
    	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	
    	PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    	
    	mBuilder.setSmallIcon(R.drawable.ic_stat_ytd)
			.setOngoing(true)
			.setContentTitle(ctx.getString(R.string.app_name))
			.setContentText(pt1 + " " + sequence.size() + " " + pt2)
			.setContentIntent(contentIntent);
    	
    	mNotificationManager.notify(1, mBuilder.build());
	}
    
    public static void removeIdUpdateNotification(long id) {
    	try {
	    	if (id != 0) {
				if (sequence.remove(id)) {
					Utils.logger("d", "ID " + id + " REMOVED from Notification", DEBUG_TAG);
				} else {
					Utils.logger("d", "ID " + id + " Already REMOVED from Notification", DEBUG_TAG);
				}
			} else {
				Utils.logger("w", "ID  not found!", DEBUG_TAG);
			}
	    	
	    	Utils.setNotificationDefaults(mBuilder);

			if (sequence.size() > 0) {
				mBuilder.setContentText(pt1 + " " + sequence.size() + " " + pt2)
						.setOngoing(true);
				mNotificationManager.notify(1, mBuilder.build());
			} else {
				mBuilder.setContentText(ctx.getString(R.string.notification_no_downloads))
						.setOngoing(false);
				mNotificationManager.notify(1, mBuilder.build());
				Utils.logger("d", "No downloads in progress.", DEBUG_TAG);
			}
		} catch (NullPointerException e) {
			Log.e(DEBUG_TAG, "NPE at removeIdUpdateNotification: " + e.getMessage());
		}
	}

    @Override
	public void handleQueueThreadUpdate() {
    	
		handler.post(new Runnable() {
			@Override
			public void run() {
				int total = queueThread.getTotalQueued();
				int completed = queueThread.getTotalCompleted();

				Utils.logger("i", String.format("Auto audio extractions completed: "
						+ "%d of %d", completed, total), DEBUG_TAG);
				
				if (completed == total) {
					queueThread.resetQueue();
					queueThread.pushNotificationText(ctx, ctx.getString(R.string.auto_audio_extr_completed), false);
				} else {
					queueThread.pushNotificationText(ctx, ctx.getString(R.string.auto_audio_extr_progress, completed, total), true);
				}
			}
		});
	}

	public static CharSequence getListFilterConstraint(int c) {
		//0
		List<Integer> iMp4List = Arrays.asList(iMp4);
		//1
		List<Integer> iWebmList = Arrays.asList(iWebm);
		//2
		List<Integer> iFlvList = Arrays.asList(iFlv);
		//3
		List<Integer> i3gpList = Arrays.asList(i3gp);
		
		//4
		List<Integer> iHdList = Arrays.asList(iHd);
		//5
		List<Integer> iLdList = Arrays.asList(iLd);
		//6
		List<Integer> iMdList = Arrays.asList(iMd);
		//7
		List<Integer> iSdList = Arrays.asList(iSd);
		
		//8
		List<Integer> i3dList = Arrays.asList(i3d);
		
		//9
		List<Integer> iVoList = Arrays.asList(iVo);
		//10
		List<Integer> iAoList = Arrays.asList(iAo);
		
		SparseArray<List<Integer>> filtersMap = new SparseArray<List<Integer>>();
		
		filtersMap.put(MP4_FILTER, iMp4List);
		filtersMap.put(WEBM_FILTER, iWebmList);
		filtersMap.put(FLV_FILTER, iFlvList);
		filtersMap.put(_3GP_FILTER, i3gpList);
		filtersMap.put(HD_FILTER, iHdList);
		filtersMap.put(LD_FILTER, iLdList);
		filtersMap.put(MD_FILTER, iMdList);
		filtersMap.put(SD_FILTER, iSdList);
		filtersMap.put(_3D_FILTER, i3dList);
		filtersMap.put(VO_FILTER, iVoList);
		filtersMap.put(AO_FILTER, iAoList);
		
		if (c == -1) return VIEW_ALL_STRING;
		
		CharSequence constraint = null;
		List<Integer> selectedMap = filtersMap.get(c);
		
		for (int i = 0; i < selectedMap.size(); i++) {
			if (constraint == null) { 
				constraint = String.valueOf(selectedMap.get(i));
			} else {
				constraint = constraint + "/" + selectedMap.get(i);
			}
		}
		//Utils.logger("i", "ListFilterConstraint: " + constraint, DEBUG_TAG);
		return constraint;
	}
	
	public static CharSequence getMultipleListFilterConstraints(int[] c) {
		CharSequence constraint = null;
		for (int i = 0 ; i < c.length; i++) {
			if (constraint == null) { 
				constraint = getListFilterConstraint(c[i]);
			} else {
				constraint = constraint + "/" + getListFilterConstraint(c[i]);
			}
		}
		return constraint;
	}
}
