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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import dentex.youtube.downloader.R;
import dentex.youtube.downloader.YTD;
import dentex.youtube.downloader.utils.FetchUrl;
import dentex.youtube.downloader.utils.UpdateHelper;
import dentex.youtube.downloader.utils.Utils;

public class AutoUpgradeApkService extends Service {
	
	private String apkFilename;
	private static final String DEBUG_TAG = "AutoUpgradeApkService";
	private DownloadManager downloadManager;
	File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	private long enqueue;
	private Uri fileUri;
	private AsyncUpdate asyncAutoUpdate;
	public static String currentVersion;
	public static String matchedVersion;
	public static String matchedChangeLog;
	private String matchedMd5;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Utils.logger("d", "service created", DEBUG_TAG);
		registerReceiver(apkReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		currentVersion = UpdateHelper.findCurrentAppVersion(this);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected() && matchedVersion != "n.a.") {
			try {
				//init version and changelog
				matchedChangeLog = null;
				matchedVersion = null;
				
				asyncAutoUpdate = new AsyncUpdate();
				asyncAutoUpdate.execute(getString(R.string.apk_upgrade_sourceforge_link));
			} catch (NullPointerException e) {
				Log.e(DEBUG_TAG, "unable to retrieve update data.");
			}
		} else {
			Log.e(DEBUG_TAG, getString(R.string.no_net));
		}
	}
	
	@Override
	public void onDestroy() {
		Utils.logger("d", "service destroyed", DEBUG_TAG);
		unregisterReceiver(apkReceiver);
	}

	
	private class AsyncUpdate extends AsyncTask<String, Void, String[]> {
		
		/*protected void onPreExecute() {
			isAsyncTaskRunning = true;
		}*/

    	protected String[] doInBackground(String... urls) {
    		try {
    			Utils.logger("d", "doInBackground...", DEBUG_TAG);
    			
	    		FetchUrl fu = new FetchUrl(AutoUpgradeApkService.this);
				String content = fu.doFetch(urls[0]);
				
				if (!content.isEmpty()) {
					return UpdateHelper.doUpdateCheck(AutoUpgradeApkService.this, this, content);
				} else {
					return null;//"e";
				}
            } catch (Exception e) {
            	Log.e(DEBUG_TAG, "doInBackground: " + e.getMessage());
            	matchedVersion = "n.a.";
                return null;//"e";
            }
        }
        
        @Override
        protected void onPostExecute(String[] result) {
     
        	matchedVersion = result[1];
        	matchedChangeLog = result[2];
        	matchedMd5 = result[3];
	        
	        if (result[0].contentEquals(">")) {
		        Utils.logger("d", "version comparison: downloading latest version...", DEBUG_TAG);
		        
		        NotificationCompat.Builder builder =  new NotificationCompat.Builder(AutoUpgradeApkService.this);
            	
            	builder.setSmallIcon(R.drawable.ic_stat_ytd)
            	        .setContentTitle(getString(R.string.title_activity_share))
            	        .setContentText("v" + matchedVersion + " " + getString(R.string.new_v_download));
            	
            	NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            	notificationManager.notify(2, builder.build());
		        
		        callDownloadApk(matchedVersion);
	    	} else if (result[0].contentEquals("==")) {
	    		//PopUps.showPopUp(getString(R.string.information), getString(R.string.upgrade_latest_installed), "status", AutoUpgradeApk.this);
	    		Utils.logger("d", "version comparison: latest version is already installed!", DEBUG_TAG);
	    		stopSelf();
	    	} else if (result[0].contentEquals("<")) {
	    		// No need for a popup...
	    		Utils.logger("d", "version comparison: installed higher than the one online? ...this should not happen...", DEBUG_TAG);
	    		stopSelf();
	    	} else if (result[0].contentEquals("e")) {
	    		Utils.logger("d", "version comparison not tested", DEBUG_TAG);
	    		stopSelf();
	    	}
        }   
	}
	
	void callDownloadApk(String ver) {
		String apklink = getString(R.string.apk_download_sourceforge_link, ver);
		apkFilename = getString(R.string.apk_filename, ver);
	    Request request = new Request(Uri.parse(apklink));
	    fileUri = Uri.parse(dir.toURI() + apkFilename);
	    request.setDestinationUri(fileUri);
	    request.allowScanningByMediaScanner();
	    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
	    request.setTitle("YouTube Downloader v" + ver);
	    downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	    try {
	    	enqueue = downloadManager.enqueue(request);
	    } catch (IllegalArgumentException e) {
	    	Log.e(DEBUG_TAG, "callDownloadApk: " + e.getMessage());
	    } catch (NullPointerException ne) {
	    	Log.e(DEBUG_TAG, "callDownloadApk: " + ne.getMessage());
	    }
	}

	BroadcastReceiver apkReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(final Context context, final Intent intent) {
	        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2);
	        if (enqueue != -1 && id != -2 && id == enqueue) {
	            Query query = new Query();
	            query.setFilterById(id);
	            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	            Cursor c = dm.query(query);
	            if (c.moveToFirst()) {
	                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
	                int status = c.getInt(columnIndex);
	                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    	
                    	if (Utils.checkMD5(matchedMd5, new File(dir, apkFilename))) {
                    	
                    		NotificationCompat.Builder builder =  new NotificationCompat.Builder(context);
                        	
                        	builder.setSmallIcon(R.drawable.ic_stat_ytd)
                        	        .setContentTitle(getString(R.string.title_activity_share))
                        	        .setContentText("v" + matchedVersion + " " + getString(R.string.new_v_install));
                        	
                        	NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	
                            Intent intent1 = new Intent();
                            intent1.setAction(android.content.Intent.ACTION_VIEW);
                        	intent1.setDataAndType(fileUri, "application/vnd.android.package-archive");
                        	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent1, 0);
                        	builder.setContentIntent(contentIntent);
                        	
                        	notificationManager.notify(2, builder.build());
                    	} else {
                    		deleteBadDownload(context, intent);
                    	}
	                }
	            }
            }
	        stopSelf();
		}

		public void deleteBadDownload(final Context context, final Intent intent) {
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2);
			downloadManager.remove(id);
			Toast.makeText(context, "YTD: " + getString(R.string.failed_download), Toast.LENGTH_SHORT).show();
		}

	};
}
