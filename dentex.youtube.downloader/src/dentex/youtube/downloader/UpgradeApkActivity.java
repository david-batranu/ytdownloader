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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import dentex.youtube.downloader.utils.FetchUrl;
import dentex.youtube.downloader.utils.PopUps;
import dentex.youtube.downloader.utils.UpdateHelper;
import dentex.youtube.downloader.utils.Utils;

public class UpgradeApkActivity extends Activity {
	
	private ProgressBar progressBar2;
	private String currentVersion;
	private String apkFilename;
	private static final String DEBUG_TAG = "UpgradeApkActivity";
	private boolean buttonClickedOnce = false;
	private TextView tv;
	private TextView cl;
	private Button upgradeButton;
	private DownloadManager downloadManager;
	private File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	private long enqueue;
	private Uri fileUri;
	private AsyncUpdate asyncUpdate;
	private String matchedVersion;
	private String matchedChangeLog;
	private String matchedMd5;
	private boolean isAsyncTaskRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		// Theme init
    	Utils.themeInit(this);
    	
    	// Language init
    	Utils.langInit(this);
    	
		setContentView(R.layout.activity_upgrade_apk);
		
		try {
		    currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		    Utils.logger("d", "current version: " + currentVersion, DEBUG_TAG);
		} catch (NameNotFoundException e) {
		    Log.e(DEBUG_TAG, "version not read: " + e.getMessage());
		    currentVersion = "100";
		}
		
		upgradeButton = (Button) findViewById(R.id.upgrade_button);
		
		progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar2.setVisibility(View.GONE);
        
        tv = (TextView) findViewById(R.id.upgrade_upper_text);
        tv.setText(getString(R.string.upgrade_uppertext_init) + currentVersion);
        
        cl = (TextView) findViewById(R.id.upgrade_textview2);
        
		setupActionBar();
	}
	
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void upgradeButtonClick(View v) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.isConnected() && matchedVersion != "n.a.") {
			try {
				if (buttonClickedOnce == false) {
					buttonClickedOnce = true;
					
					//init version and changelog
					matchedChangeLog = "n.a.";
					matchedVersion = null;
					cl.setText("");
					
					asyncUpdate = new AsyncUpdate();
					asyncUpdate.execute(getString(R.string.apk_upgrade_sourceforge_link));
				} else {
					buttonClickedOnce = false;
					callDownloadApk(matchedVersion);
				    upgradeButton.setEnabled(false);
				}
			} catch (NullPointerException e) {
				PopUps.showPopUp(getString(R.string.error), getString(R.string.upgrade_network_error), "error", UpgradeApkActivity.this);
				Log.e(DEBUG_TAG, "unable to retrieve update data.");
				
			}
		} else {
			progressBar2.setVisibility(View.GONE);
			tv.setText(getString(R.string.no_net));
			upgradeButton.setEnabled(false);
			PopUps.showPopUp(getString(R.string.no_net), getString(R.string.no_net_dialog_msg), "error", this);
		}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        registerReceiver(apkReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Log.v(DEBUG_TAG, "_onStart");
    }
	
    @Override
    protected void onRestart() {
    	super.onRestart();
    	Log.v(DEBUG_TAG, "_onRestart");
    }

    @Override
    public void onPause() {
    	super.onPause();
    	Log.v(DEBUG_TAG, "_onPause");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    	unregisterReceiver(apkReceiver);
    	Log.v(DEBUG_TAG, "_onStop");
    	
    	if (isAsyncTaskRunning) {
    		asyncUpdate.cancel(true);
    		isAsyncTaskRunning = false;
    	}
    }
	
	private class AsyncUpdate extends AsyncTask<String, Void, String[]> {
		
		protected void onPreExecute() {
			upgradeButton.setEnabled(false);
			progressBar2.setVisibility(View.VISIBLE);
			tv.setText(R.string.upgrade_uppertext_searching);
			isAsyncTaskRunning = true;
		}

		protected String[] doInBackground(String... urls) {
    		try {
    			Utils.logger("d", "doInBackground...", DEBUG_TAG);
    			
	    		FetchUrl fu = new FetchUrl(UpgradeApkActivity.this);
				String content = fu.doFetch(urls[0]);
				
				if (!content.isEmpty()) {
					return UpdateHelper.doUpdateCheck(UpgradeApkActivity.this, this, content);
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
        	
        	progressBar2.setVisibility(View.GONE);
        	
        	matchedVersion = result[1];
        	matchedChangeLog = result[2];
        	matchedMd5 = result[3];
			
        	tv.setText(getString(R.string.upgrade_latest) + matchedVersion + getString(R.string.upgrade_installed) + currentVersion);
	        cl.setText(matchedChangeLog);
	        
	        if (result[0].contentEquals(">")) {
		        Utils.logger("d", "version comparison: downloading latest version...", DEBUG_TAG);
			    upgradeButton.setEnabled(true);
			    upgradeButton.setText(getString(R.string.upgrade_button_download));
	    	} else if (result[0].contentEquals("==")) {
	    		PopUps.showPopUp(getString(R.string.information), getString(R.string.upgrade_latest_installed), "status", UpgradeApkActivity.this);
	    		Utils.logger("d", "version comparison: latest version is already installed!", DEBUG_TAG);
	    		upgradeButton.setEnabled(false);
	    	} else if (result[0].contentEquals("<")) {
	    		// No need for a popup...
	    		Utils.logger("d", "version comparison: installed higher than the one online? ...this should not happen...", DEBUG_TAG);
	    		upgradeButton.setEnabled(false);
	    	} else if (result[0].contentEquals("e")) {
	    		Utils.logger("d", "version comparison not tested", DEBUG_TAG);
	    		upgradeButton.setEnabled(false);
	    	}
        }   
	}
	
	void callDownloadApk(String ver) {
		String apklink = getString(R.string.apk_download_sourceforge_link, ver);
		apkFilename = getString(R.string.apk_filename, ver);
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	    Request request = new Request(Uri.parse(apklink));
	    fileUri = Uri.parse(dir.toURI() + apkFilename);
	    request.setDestinationUri(fileUri);
	    request.allowScanningByMediaScanner();
	    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	    request.setTitle("YouTube Downloader v" + ver);
	    try {
	    	enqueue = downloadManager.enqueue(request);
	    } catch (IllegalArgumentException e) {
	    	Log.e(DEBUG_TAG, "callDownloadApk: " + e.getMessage());
	    	YTD.NoDownProvPopUp(this);
	    } catch (NullPointerException ne) {
	    	Log.e(DEBUG_TAG, "callDownloadApk: " + ne.getMessage());
	    	Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
	    }
	}

	BroadcastReceiver apkReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(final Context context, final Intent intent) {
	        final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2);
	        if (enqueue != -1 && id != -2 && id == enqueue) {
	            Query query = new Query();
	            query.setFilterById(id);
	            Cursor c = downloadManager.query(query);
	            if (c.moveToFirst()) {
	                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
	                int status = c.getInt(columnIndex);
	                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    	
                    	upgradeButton.setText(getString(R.string.upgrade_button_init));
                    	upgradeButton.setEnabled(true);
                    	
                    	if (Utils.checkMD5(matchedMd5, new File(dir, apkFilename))) {
                    	
	                        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(UpgradeApkActivity.this);
	                        helpBuilder.setIcon(Utils.selectThemedInfoIcon())
	                        	.setTitle(getString(R.string.information))
	                        	.setMessage(getString(R.string.upgraded_dialog_msg))
	                        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	
	                            public void onClick(DialogInterface dialog, int which) {
	
	                                Intent intent = new Intent();
	                                intent.setAction(android.content.Intent.ACTION_VIEW);
	                            	intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
	                            	startActivity(intent);
	                            }
	                        });
	
	                        helpBuilder.setNegativeButton(getString(R.string.upgraded_dialog_negative), new DialogInterface.OnClickListener() {
	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	// cancel
	                            }
	                        });
	
	                        AlertDialog helpDialog = helpBuilder.create();
	                        if (! ((Activity) context).isFinishing()) {
	                        	helpDialog.show();
	                        }
                        
                    	} else {
                    		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(UpgradeApkActivity.this);
	                        helpBuilder.setIcon(Utils.selectThemedInfoIcon())
	                        	.setTitle(getString(R.string.information))
	                        	.setMessage(getString(R.string.upgrade_bad_md5_dialog_msg))
	                        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                        	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	deleteBadDownload(id);
	                            	callDownloadApk(matchedVersion);
	                            	upgradeButton.setEnabled(false);
	                            }
	                        });
	
	                        helpBuilder.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	deleteBadDownload(id);
	                            	// cancel
	                            }
	                        });

	                        AlertDialog helpDialog = helpBuilder.create();
	                        if (! ((Activity) context).isFinishing()) {
	                        	helpDialog.show();
	                        }
                    	}
                    } else if (status == DownloadManager.STATUS_FAILED) {
                    	deleteBadDownload(id);
                    }
                }
            }
		}
	};
	
	private void deleteBadDownload (long id) {
		downloadManager.remove(id);
		Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
	}
}
