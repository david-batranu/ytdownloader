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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.matsuhiro.android.connect.NetworkUtils;
import com.matsuhiro.android.download.DownloadTask;
import com.matsuhiro.android.download.DownloadTaskListener;
import com.matsuhiro.android.download.Maps;

import dentex.youtube.downloader.menu.AboutActivity;
import dentex.youtube.downloader.menu.DonateActivity;
import dentex.youtube.downloader.menu.TutorialsActivity;
import dentex.youtube.downloader.queue.FFmpegExtractAudioTask;
import dentex.youtube.downloader.utils.FetchUrl;
import dentex.youtube.downloader.utils.JsonHelper;
import dentex.youtube.downloader.utils.PopUps;
import dentex.youtube.downloader.utils.RhinoRunner;
import dentex.youtube.downloader.utils.Utils;

public class ShareActivity extends Activity {
	
	private static final String _VO_WEBM_480P = "VO - WebM - 480p";
	private static final String _VO_MP4_1080P_HBR = "VO - MP4 - 1080p (HBR)";
	private static final String _VO_WEBM_1080P = "VO - WebM - 1080p";
	private static final String _VO_WEBM_720P = "VO - WebM - 720p";
	private static final String _VO_WEBM_360P = "VO - WebM - 360p";
	private static final String _VO_WEBM_240P = "VO - WebM - 240p";
	private static final String _AO_OGG_HI_Q = "AO - OGG - Hi-Q";
	private static final String _AO_OGG_MED_Q = "AO - OGG - Med-Q";
	private static final String _VO_MP4_144P = "VO - MP4 - 144p";
	private static final String _AO_M4A_HI_Q = "AO - M4A - Hi-Q";
	private static final String _AO_M4A_MED_Q = "AO - M4A - Med-Q";
	private static final String _AO_M4A_LOW_Q = "AO - M4A - Low-Q";
	private static final String _VO_MP4_ORIGINAL = "VO - MP4 - Original";
	private static final String _VO_MP4_1080P = "VO - MP4 - 1080p";
	private static final String _VO_MP4_720P = "VO - MP4 - 720p";
	private static final String _VO_MP4_480P = "VO - MP4 - 480p";
	private static final String _VO_MP4_360P = "VO - MP4 - 360p";
	private static final String _VO_MP4_240P = "VO - MP4 - 240p";
	private static final String _WEBM_720P_3D = "WebM - 720p (3D)";
	private static final String _WEBM_360P_3D = "WebM - 360p (3D)";
	private static final String _MP4_520P_3D = "MP4 - 520p (3D)";
	private static final String _MP4_720P_3D = "MP4 - 720p (3D)";
	private static final String _MP4_240P_3D = "MP4 - 240p (3D)";
	private static final String _MP4_360P_3D = "MP4 - 360p (3D)";
	private static final String _WEBM_1080P = "WebM - 1080p";
	private static final String _WEBM_720P = "WebM - 720p";
	private static final String _WEBM_480P = "WebM - 480p";
	private static final String _WEBM_360P = "WebM - 360p";
	private static final String _MP4_ORIGINAL = "MP4 - Original";
	private static final String _MP4_1080P = "MP4 - 1080p";
	private static final String _3GP_240P = "3GP - 240p";
	private static final String _FLV_480P = "FLV - 480p";
	private static final String _FLV_360P = "FLV - 360p";
	private static final String _MP4_720P = "MP4 - 720p";
	private static final String _MP4_270P_360P = "MP4 - 270p/360p";
	private static final String _3GP_144P = "3GP - 144p";
	private static final String _FLV_270P = "FLV - 270p";
	private static final String _FLV_240P = "FLV - 240p";
	private static final String _MP4_480P = "MP4 - 480p";
	private static final String _MP4_360P = "MP4 - 360p";
	private static final String _UNKNOWN = "Unknown";
	
	private ProgressBar progressBar1;
	private ProgressBar progressBarD;
	private ProgressBar progressBarL;
	private static final String DEBUG_TAG = "ShareActivity";
	
	private TextView tv;
	private TextView noVideoInfo;
	private ListView lv;
	private static ShareActivityAdapter aA;
	private static List<String> links = new ArrayList<String>();
	private static List<String> codecs = new ArrayList<String>();
	private static List<String> qualities = new ArrayList<String>();
	private static List<String> sizes = new ArrayList<String>();
	private static List<String> itagsText = new ArrayList<String>();
	private static List<Integer> itags = new ArrayList<Integer>();
	private static List<ShareActivityListItem> listEntries = new ArrayList<ShareActivityListItem>();
	private String titleRaw;
	private String basename;
	private int pos;
	private File path;
	private String validatedLink;
	private String filenameComplete = "";
	public static Uri videoUri;
	private TextView userFilename;
	private boolean sshInfoCheckboxEnabled;
	private boolean generalInfoCheckboxEnabled;
	private boolean fileRenameEnabled;
	private File chooserFolder;
	private AsyncDownload asyncDownload;
	private AsyncSizesFiller asyncSizesFiller;
	private boolean isAsyncDownloadRunning = false;
	private boolean isAsyncSizesFillerRunning = false;
	private Bitmap img;
	private ImageView imgView;
	private String videoId;
	public static Activity sShare;
	private String[] decryptionArray = null;
	private String jslink;
	private String decryptionFunction;
	private DownloadTaskListener dtl;
	private boolean autoModeEnabled = false;
	private boolean restartModeEnabled = false;
	private String extraId;
	private boolean autoFFmpegTaskAlreadySent = false;
	private String basenameTagged;
	//private String dashUrl = "";
	//private String dashStartUrl;
	private SlidingMenu slMenu;
	private static CharSequence constraint;
	
//	private int aoIndex;
//	private File muxedVideo;
//	private String muxedFileName;
//	private NotificationCompat.Builder mBuilder;
//	private NotificationManager mNotificationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sShare = ShareActivity.this;
		
//		aoIndex = -1;
		
		// Theme init
		Utils.themeInit(this);
		
		setContentView(R.layout.activity_share);
		
		if (aA != null) aA.clear();

		links.clear();
		codecs.clear();
		qualities.clear();
		sizes.clear();
		itagsText.clear();
		itags.clear();
		listEntries.clear();
		
		String theme = YTD.settings.getString("choose_theme", "D");
		
		int or = this.getResources().getConfiguration().orientation;
    	boolean isLandscape = (or == 2) ? true : false;
		
		// configure the SlidingMenu
		slMenu = new SlidingMenu(this);
		slMenu.setMode(SlidingMenu.LEFT);
		slMenu.setShadowWidthRes(R.dimen.shadow_width);
		slMenu.setShadowDrawable(R.drawable.shadow);
		if (isLandscape) {
			slMenu.setBehindWidthRes(R.dimen.slidingmenu_width_landscape);
			slMenu.showMenu();
		} else {
			slMenu.setBehindWidthRes(R.dimen.slidingmenu_width_portrait);
			slMenu.showContent();
		}
		slMenu.setFadeDegree(0.35f);
		slMenu.setHapticFeedbackEnabled(true);
		slMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		slMenu.setMenu(R.layout.menu_frame);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//showSizesInVideoList = YTD.settings.getBoolean("show_size_list", false);

		// Language init
		Utils.langInit(this);
		
		// loading views from the layout xml
		tv = (TextView) findViewById(R.id.textView1);
		noVideoInfo = (TextView) findViewById(R.id.share_activity_info);
		
		progressBarD = (ProgressBar) findViewById(R.id.progressBarD);
		progressBarL = (ProgressBar) findViewById(R.id.progressBarL);
		
		if (theme.equals("D")) {
			progressBar1 = progressBarD;
			progressBarL.setVisibility(View.GONE);
		} else {
			progressBar1 = progressBarL;
			progressBarD.setVisibility(View.GONE);
		}

		imgView = (ImageView)findViewById(R.id.imgview);
		
		lv = (ListView) findViewById(R.id.list);

		// YTD update initialization
		YTD.updateInit(this, false, null);
		
		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				try {
					handleSendText(intent, action);
					Utils.logger("d", "handling ACTION_SEND", DEBUG_TAG);
				} catch (IOException e) {
					Log.e(DEBUG_TAG, "Error: " + e.getMessage(), e);
				}
			}
		}
		
		if (Intent.ACTION_VIEW.equals(action)) {
			if (intent.hasCategory("AUTO")) {
				autoModeEnabled = true;
				extraId = intent.getStringExtra("id");
				pos = intent.getIntExtra("position", 0);
				filenameComplete = intent.getStringExtra("filename");
				basenameTagged = Utils.getFileNameWithoutExt(filenameComplete);
				
				Utils.logger("i", "Auto Mode Enabled:"
						+ "\n -> id: " + extraId
						+ "\n -> position: " + pos
						+ "\n -> filename: " + filenameComplete, DEBUG_TAG);
			} else if (intent.hasCategory("RESTART")) {
				restartModeEnabled = true; 
				extraId = intent.getStringExtra("id");
				
				Utils.logger("i", "Restart Mode Enabled:"
						+ "\n -> id: " + extraId, DEBUG_TAG);
			}
			try {
				handleSendText(intent, action);
				Utils.logger("d", "handling ACTION_VIEW", DEBUG_TAG);
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "Error: " + e.getMessage(), e);
			}
		}
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
 
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	//Utils.logger("i", "...landscape", DEBUG_TAG);
    		slMenu.setBehindWidthRes(R.dimen.slidingmenu_width_landscape);
    		slMenu.showMenu();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Utils.logger("i", "...portrait", DEBUG_TAG);
            slMenu.setBehindWidthRes(R.dimen.slidingmenu_width_portrait);
            slMenu.showContent();
        }
    }

	public static Context getContext() {
		return sShare;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_share, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (!autoModeEnabled) {
			switch(item.getItemId()){
			case android.R.id.home:
				slMenu.toggle();
				return true;
			case R.id.menu_donate:
				startActivity(new Intent(this, DonateActivity.class));
				return true;
			case R.id.menu_settings:
				Intent sIntent = new Intent(this, SettingsActivity.class);
				sIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(sIntent);
				return true;
			case R.id.menu_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.menu_dashboard:
				launchDashboardActivity();
				return true;
			case R.id.menu_tutorials:
				startActivity(new Intent(this, TutorialsActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchDashboardActivity() {
		Intent dashboardIntent = new Intent(this, DashboardActivity.class);
		dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(dashboardIntent);
	}
	
	/*@Override
	protected void onStart() {
		super.onStart();
		Utils.logger("v", "_onStart", DEBUG_TAG);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Utils.logger("v", "_onRestart");
	}

	@Override
	public void onPause() {
		super.onPause();
		Utils.logger("v", "_onPause");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Utils.logger("v", "_onStop", DEBUG_TAG);
	}*/
	
	@Override
	public void onBackPressed() {
		Utils.logger("v", "_onBackPressed", DEBUG_TAG);
		if (slMenu.isMenuShowing()) {
			slMenu.showContent(true);
		} else {
			super.onBackPressed();
			// To cancel the AsyncDownload AsyncSizesFiller tasks only on 
			// back button pressed (not when switching to other activities)
			if (isAsyncDownloadRunning) {
				Utils.logger("v", "canceling asyncDownload", DEBUG_TAG);
				asyncDownload.cancel(true);
			}
			if (isAsyncSizesFillerRunning) {
				Utils.logger("v", "canceling asyncSizesFiller", DEBUG_TAG);
				asyncSizesFiller.cancel(true);
			}
		}
	}

	void handleSendText(Intent intent, String action) throws IOException {

		/*ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {*/
		if (NetworkUtils.isNetworkAvailable(sShare)) {
			String sharedText = null;
			if (action.equals(Intent.ACTION_SEND)) {
				sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			} else if (action.equals(Intent.ACTION_VIEW)) {
				sharedText = intent.getDataString();
			}
			
			if (sharedText != null) {
				if (linkValidator(sharedText) == "bad_link") {
					badOrNullLinkAlert();
				} else if (sharedText != null) {
					showGeneralInfoTutorial();
					asyncDownload = new AsyncDownload();
					asyncDownload.execute(validatedLink);
				}
			} else {
				badOrNullLinkAlert();
			}
		} else {
			progressBar1.setVisibility(View.GONE);
			tv.setVisibility(View.GONE);
			noVideoInfo.setText(getString(R.string.no_net));
			noVideoInfo.setVisibility(View.VISIBLE);
			PopUps.showPopUp(getString(R.string.no_net), getString(R.string.no_net_dialog_msg), "error", this);
			
			showRetryButton();
		}
	}

	private void showRetryButton() {
		Button retry = (Button) findViewById(R.id.share_activity_retry_button);
		retry.setVisibility(View.VISIBLE);
		retry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.reload(ShareActivity.this);					
			}
		});
	}

	public void badOrNullLinkAlert() {
		progressBar1.setVisibility(View.GONE);
		PopUps.showPopUp(getString(R.string.error), getString(R.string.bad_link_dialog_msg), "error", this);
		tv.setVisibility(View.GONE);
		noVideoInfo.setText(getString(R.string.bad_link));
		noVideoInfo.setVisibility(View.VISIBLE);
	}
	
	private void showGeneralInfoTutorial() {
		generalInfoCheckboxEnabled = YTD.settings.getBoolean("general_info", true);
		if (generalInfoCheckboxEnabled == true) {
			AlertDialog.Builder adb = new AlertDialog.Builder(ShareActivity.this);
			LayoutInflater adbInflater = LayoutInflater.from(ShareActivity.this);
			View generalInfo = adbInflater.inflate(R.layout.dialog_general_info, null);
			final CheckBox showAgainCb = (CheckBox) generalInfo.findViewById(R.id.showAgain1);
			showAgainCb.setChecked(true);
			adb.setView(generalInfo);
			adb.setTitle(getString(R.string.tutorial_title));			
			adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (!showAgainCb.isChecked()) {
						YTD.settings.edit().putBoolean("general_info", false).commit();
						sshInfoCheckboxEnabled = YTD.settings.getBoolean("general_info", true);
						Utils.logger("v", "generalInfoCheckboxEnabled: " + generalInfoCheckboxEnabled, DEBUG_TAG);
					}
				}
			});

			Utils.secureShowDialog(sShare, adb);
		}
	}
	
	private String linkValidator(String sharedText) {
		Pattern pattern = Pattern.compile("(http://|https://).*(v=.{11}).*");
		Matcher matcher = pattern.matcher(sharedText);
		if (matcher.find()) {
			//validatedLink = matcher.group(1) + "www.youtube.com/watch?" + matcher.group(2);
			validatedLink = "http://www.youtube.com/watch?" + matcher.group(2);
			videoId = matcher.group(2).replace("v=", "");
			return validatedLink;
		}
		return "bad_link";
	}
	
	public void assignPath() {
		boolean Location = YTD.settings.getBoolean("swap_location", false);
		
		if (Location == false) {
			String location = YTD.settings.getString("standard_location", "Downloads");
			Utils.logger("d", "location: " + location, DEBUG_TAG);
			
			if (location.equals("DCIM") == true) {
				path = YTD.dir_DCIM;
			}
			if (location.equals("Movies") == true) {
				path = YTD.dir_Movies;
			} 
			if (location.equals("Downloads") == true) {
				path = YTD.dir_Downloads;
			}
			
		} else {
			String cs = YTD.settings.getString("CHOOSER_FOLDER", "");
			chooserFolder = new File(cs);
			if (chooserFolder.exists()) {
				Utils.logger("d", "chooserFolder: " + chooserFolder, DEBUG_TAG);
				path = chooserFolder;
			} else {
				path = YTD.dir_Downloads;
				Utils.logger("w", "chooserFolder not found, falling back to Download path", DEBUG_TAG);
			}
		}
		
		if (!path.exists()) {
			if (new File(path.getAbsolutePath()).mkdirs()) {
				Utils.logger("w", "destination path not found, creating it now", DEBUG_TAG);
			} else {
				Log.e(DEBUG_TAG, "Something really bad happened with the download destination...");
			}
			
		}
			
		Utils.logger("d", "path: " + path, DEBUG_TAG);
	}

	private class AsyncDownload extends AsyncTask<String, Integer, String> {

		protected void onPreExecute() {
			isAsyncDownloadRunning = true;
			tv.setText(R.string.loading);
			progressBar1.setIndeterminate(true);
			progressBar1.setVisibility(View.VISIBLE);
		}
		
		protected String doInBackground(String... urls) {
			try {
				Utils.logger("d", "doInBackground...", DEBUG_TAG);
				assignBitmapToVideoListThumbnail(generateThumbUrls());

				FetchUrl fu = new FetchUrl(sShare);
				String content = fu.doFetch(urls[0]);
				
				if (!content.isEmpty()) {
					return urlBlockMatchAndDecode(content);
				} else {
					return "e";
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "downloadUrl: " + e.getMessage());
				return "e";
			} 
		}
		
		public void doProgress(int value){
			publishProgress(value);
		}
		
		protected void onProgressUpdate(Integer... values) {
			progressBar1.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {

			progressBar1.setVisibility(View.GONE);
			
			isAsyncDownloadRunning = false;
			
			if (YTD.settings.getBoolean("show_thumb", false) && 
					!((result == null || result.equals("e")) ||
					  (result != null && result.equals("login_required")) ||
					  (result != null && result.equals("rtmpe")) ) ) {
				imgView.setImageBitmap(img);
			}
			
			if (result == null || result.equals("e") && !autoModeEnabled) {
				noVideosMsgs("error", getString(R.string.invalid_url));
				showRetryButton();
			}
			
			if (result != null && result.equals("login_required") && !autoModeEnabled) {
				noVideosMsgs("status", getString(R.string.login_required));
			}
			
			if (result != null && result.equals("rtmpe")) {
				listEntries.clear();
				noVideosMsgs("status", getString(R.string.encrypted_streams));
			}
			
			aA = new ShareActivityAdapter(listEntries, ShareActivity.this);
			//aA = new ArrayAdapter<String>(sShare, android.R.layout.simple_list_item_1, listEntries);
			
			
			if (autoModeEnabled) {
				assignPath();
				
				try {
					callDownloadManager();
				} catch (IndexOutOfBoundsException e) {
					Toast.makeText(ShareActivity.this, getString(R.string.video_list_error_toast), Toast.LENGTH_SHORT).show();
					launchDashboardActivity();
				}
				
				finish();
			} else {				
				setupStoredFilters();
				//listEntriesBuilder();
				lv.setAdapter(aA);

				if (!YTD.SHOW_ITAGS_AND_NO_SIZE_FOR_DUBUG) {
					asyncSizesFiller = new AsyncSizesFiller();
					asyncSizesFiller.execute(links.toArray(new String[0]));
				}
			}

			tv.setText(titleRaw);
			
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					//Utils.logger("i", "Selected link: " + links.get(pos), DEBUG_TAG);
					assignPath();

					// get the filtered position
					ShareActivityListItem item = aA.getItem(position);
					int currItag = item.getItag();
					pos = itags.indexOf(currItag);
					
					Utils.logger("i", "click @ position: " + pos, DEBUG_TAG);

					//pos = 45;		// to test IndexOutOfBound Exception...
					
					basenameTagged = composeFilenameWithOutExt();
					filenameComplete = composeFilenameWithExt();
					
					AlertDialog.Builder helpBuilder = new AlertDialog.Builder(ShareActivity.this);
					
					helpBuilder.setIcon(Utils.selectThemedInfoIcon());
					helpBuilder.setTitle(getString(R.string.list_click_dialog_title));

					boolean showSize = false;
					try {
						if (sizes.get(pos).equals("")) {
							helpBuilder.setMessage(titleRaw + "\n" +
									getString(R.string.quality) + " " + itagsText.get(pos));
						} else {
							helpBuilder.setMessage(titleRaw +  "\n" +
									getString(R.string.quality) + " " + itagsText.get(pos) +
									getString(R.string.size) + " " + sizes.get(pos).replace(" - ", ""));
						}
						
					} catch (IndexOutOfBoundsException e) {
						Toast.makeText(ShareActivity.this, getString(R.string.video_list_error_toast), Toast.LENGTH_SHORT).show();
					}
					
					helpBuilder.setPositiveButton(getString(R.string.list_click_download_local), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							try {
								fileRenameEnabled = YTD.settings.getBoolean("enable_rename", false);
								if (fileRenameEnabled == true) {
									AlertDialog.Builder adb = new AlertDialog.Builder(ShareActivity.this);
									LayoutInflater adbInflater = LayoutInflater.from(ShareActivity.this);
									View inputFilename = adbInflater.inflate(R.layout.dialog_input_filename, null);
									userFilename = (TextView) inputFilename.findViewById(R.id.input_filename);
									
									// ====================================
									//userFilename.setText(basenameTagged);
									//                        ^
									// edit "tagged" basename |  (A)
									//    ###  OR  ###
									// edit "clean"  basename |  (B)
									//                        v
									userFilename.setText(basename);
									// ====================================
									
									adb.setView(inputFilename);
									adb.setTitle(getString(R.string.rename_dialog_title));
									adb.setMessage(getString(R.string.rename_dialog_msg));
									
									adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											
											//basenameTagged = userFilename.getText().toString();	// (A)
											basename = userFilename.getText().toString();
											basenameTagged = composeFilenameWithOutExt();			// (B)
											
											filenameComplete = composeFilenameWithExt();
											
											callDownloadManager();
										}
									});
									
									adb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											// cancel
										}
									});
									
									Utils.secureShowDialog(sShare, adb);
								} else {
									callDownloadManager();
								}
							} catch (IndexOutOfBoundsException e) {
								Toast.makeText(ShareActivity.this, getString(R.string.video_list_error_toast), Toast.LENGTH_SHORT).show();
							}
						}
					});
					
					// show central button for SSH send if enabled in prefs
					if (!YTD.settings.getBoolean("ssh_to_longpress_menu", false)) {
						helpBuilder.setNeutralButton(getString(R.string.list_click_download_ssh), new DialogInterface.OnClickListener() {
	
							public void onClick(DialogInterface dialog, int which) {
								sendViaSsh();
							}
						});
					}

					helpBuilder.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							//Toast.makeText(ShareActivity.this, "Download canceled...", Toast.LENGTH_SHORT).show();
						}
					});
					
					if (!showSize) {
						Utils.secureShowDialog(sShare, helpBuilder);
					}
				}
			});
			
			lv.setLongClickable(true);
			lv.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
					pos = position;
					
					basenameTagged = composeFilenameWithOutExt();
					filenameComplete = composeFilenameWithExt();
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ShareActivity.this);
					if (!YTD.settings.getBoolean("ssh_to_longpress_menu", false)) {
						builder.setTitle(R.string.long_click_title).setItems(R.array.long_click_entries, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0: // copy
										copy();
										break;
									case 1: // share
										share();
								}
							}
						});
					} else {
						builder.setTitle(R.string.long_click_title).setItems(R.array.long_click_entries2, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0: // copy
										copy();
										break;
									case 1: // share
										share();
										break;
									case 2: // SSH
										sendViaSsh();
								}
							}
						});
					}

					Utils.secureShowDialog(sShare, builder);
					return true;
				}
			});
		}

		private void noVideosMsgs(String type, String cause) {
			PopUps.showPopUp(getString(R.string.no_video_available), cause, type, ShareActivity.this);
			tv.setVisibility(View.GONE);
			noVideoInfo.setVisibility(View.VISIBLE);
		}

		private void share() {
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, filenameComplete);
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, links.get(pos));
			startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_link_via)));
		}

		private void copy() {
			ClipData cmd = ClipData.newPlainText("simple text", links.get(pos));
			ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			cb.setPrimaryClip(cmd);
			Toast.makeText(ShareActivity.this, getString(R.string.link_copied), Toast.LENGTH_SHORT).show();
		}
		
		private String composeFilenameWithOutExt() {
			String suffix = itagsText.get(pos)
					.replace("MP4 - ", "")
					.replace("WebM - ", "")
					.replace("FLV - ", "")
					.replace("3GP - ", "")
					.replace("M4A - ", "")
					.replace("OGG - ", "")
					.replace("/", "-")
					.replace(" - ", "_");
			
			String comp = basename + "_" + suffix;
			
			Utils.logger("d", "videoFilename with no EXT: " + comp, DEBUG_TAG);
			return comp;
		}
		
		private String composeFilenameWithExt() {
			
			String comp = basenameTagged + "." + codecs.get(pos);
			
			Utils.logger("d", "COMPLETE videoFilename: " + comp, DEBUG_TAG);
			return comp;
		}

		private void callConnectBot() {
			Context context = getApplicationContext();
			PackageManager pm = context.getPackageManager();
			
			final String connectBotFlavour = YTD.settings.getString("connectbot_flavour", "org.connectbot");
			
			String connectBotFlavourPlain = "ConnectBot";
			if (connectBotFlavour.equals("sk.vx.connectbot")) connectBotFlavourPlain = "VX " + connectBotFlavourPlain;
			if (connectBotFlavour.equals("org.woltage.irssiconnectbot")) connectBotFlavourPlain = "Irssi " + connectBotFlavourPlain;
			
			Intent appStartIntent = pm.getLaunchIntentForPackage(connectBotFlavour);
			if (null != appStartIntent) {
				Utils.logger("d", "appStartIntent: " + appStartIntent, DEBUG_TAG);
				context.startActivity(appStartIntent);
			} else {
				AlertDialog.Builder cb = new AlertDialog.Builder(ShareActivity.this);
				cb.setTitle(getString(R.string.callConnectBot_dialog_title, connectBotFlavourPlain));
				cb.setMessage(getString(R.string.callConnectBot_dialog_msg));
				cb.setIcon(Utils.selectThemedAlertIcon());
				cb.setPositiveButton(getString(R.string.callConnectBot_dialog_positive), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW); 
						intent.setData(Uri.parse("market://details?id=" + connectBotFlavour));
						try {
							startActivity(intent);
						} catch (ActivityNotFoundException exception){
							PopUps.showPopUp(getString(R.string.no_market), getString(R.string.no_net_dialog_msg), "error", ShareActivity.this);
						}
					}
				});
				cb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// cancel
					}
				});
				
				Utils.secureShowDialog(sShare, cb);
			}
		}

		private void sendViaSsh() {
			try {
				String wgetCmd;
				
				Boolean shortSshCmdEnabled = YTD.settings.getBoolean("enable_connectbot_short_cmd", false);
				if (shortSshCmdEnabled) {
					wgetCmd = "wget -e \"convert-links=off\" --keep-session-cookies --save-cookies /dev/null --no-check-certificate \'" + 
							links.get(pos) + "\' -O " + filenameComplete;
				} else {
					wgetCmd = "REQ=`wget -q -e \"convert-links=off\" --keep-session-cookies --save-cookies /dev/null --no-check-certificate \'" + 
							validatedLink + "\' -O-` && urlblock=`echo $REQ | grep -oE \'url_encoded_fmt_stream_map\": \".*\' | sed -e \'s/\", \".*//\'" + 
							" -e \'s/url_encoded_fmt_stream_map\": \"//\'` && urlarray=( `echo $urlblock | sed \'s/,/\\n\\n/g\'` ) && N=" + pos + 
							" && block=`echo \"${urlarray[$N]}\" | sed -e \'s/%3A/:/g\' -e \'s/%2F/\\//g\' -e \'s/%3F/\\?/g\' -e \'s/%3D/\\=/g\'" + 
							" -e \'s/%252C/%2C/g\' -e \'s/%26/\\&/g\' -e \'s/%253A/\\:/g\' -e \'s/\", \"/\"-\"/\' -e \'s/sig=/signature=/\'" + 
							" -e \'s/x-flv/flv/\' -e \'s/\\\\\\u0026/\\&/g\'` && url=`echo $block | grep -oE \'http://.*\' | sed -e \'s/&type=.*//\'" + 
							" -e \'s/&signature=.*//\' -e \'s/&quality=.*//\' -e \'s/&fallback_host=.*//\'` && sig=`echo $block | " +
							"grep -oE \'signature=.{81}\'` && downloadurl=`echo $url\\&$sig | sed \'s/&itag=[0-9][0-9]&signature/\\&signature/\'` && " +
							"wget -e \"convert-links=off\" --keep-session-cookies --save-cookies /dev/null --tries=5 --timeout=45 --no-check-certificate " +
							"\"$downloadurl\" -O " + filenameComplete;
				}
				
				Utils.logger("d", "wgetCmd: " + wgetCmd, DEBUG_TAG);
				
				ClipData cmd = ClipData.newPlainText("simple text", wgetCmd);
				ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				cb.setPrimaryClip(cmd);
				
				sshInfoCheckboxEnabled = YTD.settings.getBoolean("ssh_info", true);
				if (sshInfoCheckboxEnabled == true) {
					AlertDialog.Builder adb = new AlertDialog.Builder(ShareActivity.this);
					LayoutInflater adbInflater = LayoutInflater.from(ShareActivity.this);
					View showAgain = adbInflater.inflate(R.layout.dialog_inflatable_checkbox, null);
					final CheckBox showAgainCb = (CheckBox) showAgain.findViewById(R.id.infl_cb);
					showAgainCb.setChecked(true);
					showAgainCb.setText(getString(R.string.show_again_checkbox));
					adb.setView(showAgain);
					adb.setTitle(getString(R.string.ssh_info_tutorial_title));
					adb.setMessage(getString(R.string.ssh_info_tutorial_msg));
					adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (!showAgainCb.isChecked()) {
								YTD.settings.edit().putBoolean("ssh_info", false).apply();
								Utils.logger("d", "sshInfoCheckboxEnabled: " + false, DEBUG_TAG);
							}
							callConnectBot(); 
						}
					});

					Utils.secureShowDialog(sShare, adb);
				} else {
					callConnectBot();
				}
			} catch (IndexOutOfBoundsException e) {
				Toast.makeText(ShareActivity.this, getString(R.string.video_list_error_toast), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void setupStoredFilters() {
		int storedFilterInt = YTD.settings.getInt("list_filter", YTD.VIEW_ALL);
		assignConstraint(YTD.getListFilterConstraint(storedFilterInt));
		
		int storedView = YTD.settings.getInt("view_filter", R.id.ALL);
		resetAllBkg();
		if (storedView != R.id.ALL) {
			View sv = findViewById(storedView);
			if (sv != null) sv.setBackgroundResource(R.drawable.grad_bg_sel);
		}
		
		setupFilters();
	}

	private void setupFilters() {
		
		final View mp4 = findViewById(R.id.MP4);
		final View webm = findViewById(R.id.WEBM);
		final View flv = findViewById(R.id.FLV);
		final View _3gp = findViewById(R.id._3GP);
		final View hd = findViewById(R.id.HD);
		final View ld = findViewById(R.id.LD);
		final View md = findViewById(R.id.MD);
		final View sd = findViewById(R.id.SD);
		final View _3d = findViewById(R.id._3D);
		final View vo = findViewById(R.id.VO);
		final View ao = findViewById(R.id.AO);
		final View all = findViewById(R.id.ALL);
		
		YTD.slMenuOrigBkg = findViewById(R.id.list).getBackground();
		
		mp4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "MP4 filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.MP4_FILTER);
			}
		});
		
		webm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "WEBM filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.WEBM_FILTER);
			}
		});
		
		flv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "FLV filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.FLV_FILTER);
			}
		});
		
		
		_3gp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "3GP filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD._3GP_FILTER);
			}
		});
		
		hd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "HD filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.HD_FILTER);
			}
		});
		
		ld.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "LD filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.LD_FILTER);
			}
		});
		
		md.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "MD filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.MD_FILTER);
			}
		});
		
		sd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "SD filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.SD_FILTER);
			}
		});
		
		_3d.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "3D filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD._3D_FILTER);
			}
		});
		
		vo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "VO filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.VO_FILTER);
			}
		});
		
		ao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "AO filter clicked", DEBUG_TAG);
				reactToViewClick(v, YTD.AO_FILTER);
			}
		});
		
		all.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.logger("d", "ALL filter clicked", DEBUG_TAG);
				resetAllBkg();
				YTD.settings.edit().putInt("list_filter", YTD.VIEW_ALL).apply();
				YTD.settings.edit().putInt("view_filter", R.id.ALL).apply();
				assignConstraint(YTD.getListFilterConstraint(YTD.VIEW_ALL));
			}
		});
	}
	
	private void reactToViewClick(View v, int filterInt) {
		resetAllBkg();
		v.setBackgroundResource(R.drawable.grad_bg_sel);
		assignConstraint(YTD.getListFilterConstraint(filterInt));
		YTD.settings.edit().putInt("list_filter", filterInt).commit();
		YTD.settings.edit().putInt("view_filter", v.getId()).commit();
	}

	@SuppressWarnings("deprecation")
	private void resetAllBkg() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.all_filters);
		int childCount = ll.getChildCount();
		for (int i = 0; i < childCount; i++) {
			
			final View childAt = ll.getChildAt(i);
			if (childAt instanceof TextView)
				childAt.setBackgroundDrawable(YTD.slMenuOrigBkg);
		}
	}
	
	// TODO DM
	private void callDownloadManager() {
		
		dtl = new DownloadTaskListener() {
			
			@Override
			public void preDownload(DownloadTask task) {
				long ID = task.getDownloadId();
				String pathOfVideo = task.getAbsolutePath();
				String nameOfVideo = task.getFileName();
				String jsonDataType = task.getType();
				String aExt = task.getAudioExt();
				Utils.logger("d", "__preDownload on ID: " + ID, DEBUG_TAG);
				
				Maps.mNetworkSpeedMap.put(ID, (long) 0);
				
				JsonHelper.addEntryToJsonFile(
						String.valueOf(ID), 
						jsonDataType, 
						videoId,
						pos, 
						YTD.JSON_DATA_STATUS_IN_PROGRESS, 
						pathOfVideo, 
						nameOfVideo, 
						//basenameTagged, //(A)
						basename,  //(B) 
						aExt, 
						"-", 
						false);
				
				DashboardActivity.refreshlist();
				
				writeThumbToDisk();
				
				if (!autoModeEnabled) YTD.sequence.add(ID);
				
				YTD.NotificationHelper(sShare);
			}
			
			@Override
			public void updateProcess(DownloadTask task) {				
				// nothing to do
			}
			
			@Override
			public void finishDownload(DownloadTask task) {
				long ID = task.getDownloadId();
				String nameOfVideo = task.getFileName();
				String pathOfVideo = task.getAbsolutePath();
				String jsonDataType = task.getType();
				String aExt = task.getAudioExt();
				Utils.logger("d", "__finishDownload on ID: " + ID, DEBUG_TAG);
				
				Utils.scanMedia(getApplicationContext(), 
						new String[] { path.getAbsolutePath() + File.separator + nameOfVideo }, 
						new String[] {"video/*"});
				
				String size;
				try {
					long downloadTotalSize = task.getTotalSize(); //Maps.mTotalSizeMap.get(ID);
					size = String.valueOf(Utils.MakeSizeHumanReadable(downloadTotalSize, false));
				} catch (NullPointerException e) {
					Utils.logger("w", "NPE getting finished download size for ID: " + ID, DEBUG_TAG);
					size = "-";
				}
				
				JsonHelper.addEntryToJsonFile( 
						String.valueOf(ID), 
						jsonDataType, 
						videoId, 
						pos, 
						YTD.JSON_DATA_STATUS_COMPLETED, 
						pathOfVideo, 
						nameOfVideo, 
						//basenameTagged, //(A)
						basename,  //(B) 
						aExt, 
						size, 
						false);
				
				DashboardActivity.refreshlist();
				
				YTD.removeIdUpdateNotification(ID);
				
				YTD.videoinfo.edit().remove(String.valueOf(ID) + "_link").commit();
				
				Maps.removeFromAllMaps(ID);
				
				//TODO Auto FFmpeg task
				if (YTD.settings.getBoolean("ffmpeg_auto_cb", false) && 
						!autoFFmpegTaskAlreadySent && 
						!jsonDataType.equals(YTD.JSON_DATA_TYPE_V_O) && 
						!jsonDataType.equals(YTD.JSON_DATA_TYPE_A_O)) {
					Utils.logger("d", "autoFfmpeg enabled: enqueing task for id: " + ID, DEBUG_TAG);
					
					autoFFmpegTaskAlreadySent = true;
					
					String[] bitrateData = null;
					String brType = null;
					String brValue = null;
					
					String audioFileName;
					
					String extrType = YTD.settings.getString("audio_extraction_type", "conv");
					if (extrType.equals("conv")) {
						bitrateData = Utils.retrieveBitrateValuesFromPref(sShare);
						audioFileName = basename + "_" + bitrateData[0] + "-" + bitrateData[1] + ".mp3";
						brType = bitrateData[0];
						brValue = bitrateData[1];
					} else {
						audioFileName = basename + aExt;
						
					}
					
					String type = (brValue == null) ? YTD.JSON_DATA_TYPE_A_M : YTD.JSON_DATA_TYPE_A_E;
					File audioFile = new File(path.getAbsolutePath(), audioFileName);
					
					if (!audioFile.exists()) { 
						File videoFileToConvert = new File(path.getAbsolutePath(), filenameComplete);
						
						long newId = System.currentTimeMillis();
						
						YTD.queueThread.enqueueTask(new FFmpegExtractAudioTask(
								sShare, newId, 
								videoFileToConvert, audioFile, 
								brType, brValue, 
								String.valueOf(ID), 
								videoId, 
								pos), 0);
						
						JsonHelper.addEntryToJsonFile(
								String.valueOf(newId), 
								type, 
								videoId, 
								pos,
								YTD.JSON_DATA_STATUS_QUEUED,
								path.getAbsolutePath(), 
								audioFileName, 
								Utils.getFileNameWithoutExt(audioFileName), 
								"", 
								"-", 
								false);
						
						DashboardActivity.refreshlist();
					}
				} else {
					Utils.logger("v", "Auto FFmpeg task for ID " + ID
							+ " not enabled OR already sent for this video", DEBUG_TAG);
				}
			}
			
			@Override
			public void errorDownload(DownloadTask task, Throwable error) {
				long ID = task.getDownloadId();
				String nameOfVideo = task.getFileName();
				String pathOfVideo = task.getAbsolutePath();
				String jsonDataType = task.getType();
				String aExt = task.getAudioExt();
				
				Utils.logger("w", "__errorDownload on ID: " + ID, DEBUG_TAG);
				
				Toast.makeText(sShare,  nameOfVideo + ": " + getString(R.string.download_failed), 
						Toast.LENGTH_SHORT).show();
				
				String status = YTD.JSON_DATA_STATUS_PAUSED;
				String size = "-";
				
				if (error != null && error.getMessage() != null) {
					Pattern httpPattern = Pattern.compile("http error code: (400|403|404|405|410|411)");
					Matcher httpMatcher = httpPattern.matcher(error.getMessage());
					if (httpMatcher.find()) {
						status = YTD.JSON_DATA_STATUS_FAILED;
						Utils.logger("w", httpMatcher.group(1) + " Client Error for ID: " + ID, DEBUG_TAG);
					}
				}

				try {
					Long bytes_downloaded = Maps.mDownloadSizeMap.get(ID);
					Long bytes_total = Maps.mTotalSizeMap.get(ID);
					String progress = String.valueOf(Maps.mDownloadPercentMap.get(ID));
					String readableBytesDownloaded = Utils.MakeSizeHumanReadable(bytes_downloaded, false);
					String readableBytesTotal = Utils.MakeSizeHumanReadable(bytes_total, false);
					String progressRatio = readableBytesDownloaded + "/" + readableBytesTotal;
					size = progressRatio + " (" + progress + "%)";
				} catch (NullPointerException e) {
					Utils.logger("w", "errorDownload: NPE @ DM Maps", DEBUG_TAG);
				}
				
				JsonHelper.addEntryToJsonFile( 
						String.valueOf(ID), 
						jsonDataType, 
						videoId, 
						pos, 
						status, 
						pathOfVideo, 
						nameOfVideo, 
						//basenameTagged, //(A)
						basename,  //(B) 
						aExt, 
						size, 
						false);
				
				DashboardActivity.refreshlist();
				
				YTD.removeIdUpdateNotification(ID);
			}
		};
		
		//TODO DM
		File dest = new File(path, filenameComplete);
		File destTemp = new File(path, filenameComplete + DownloadTask.TEMP_SUFFIX);
		String previousJson = JsonHelper.readJsonDashboardFile();
		
		String aExt = findAudioCodec();
		String jsonDataType;
		if (YTD.iVoList.contains(itags.get(pos))) {
			jsonDataType = YTD.JSON_DATA_TYPE_V_O;
		} else if (YTD.iAoList.contains(itags.get(pos))) {
			jsonDataType = YTD.JSON_DATA_TYPE_A_O;
		} else {
			jsonDataType = YTD.JSON_DATA_TYPE_V;
		}
		
		boolean blockDashboardLaunch = false;
		
		if (dest.exists() || (destTemp.exists() && previousJson.contains(dest.getName())) && !autoModeEnabled && !restartModeEnabled) {
			blockDashboardLaunch = true;
			PopUps.showPopUp(getString(R.string.long_press_warning_title), 
					getString(R.string.file_already_added), "status", ShareActivity.this);
		} else {
			long id = 0;
			if (autoModeEnabled || restartModeEnabled) {
				id = Long.parseLong(extraId);
			} else {
				id = System.currentTimeMillis();
			}
			
			try {
				DownloadTask dt = new DownloadTask(this, id, links.get(pos), 
						filenameComplete, path.getAbsolutePath(), 
						aExt, jsonDataType, 
						dtl, false);
				
				YTD.videoinfo.edit().putString(String.valueOf(id) + "_link", links.get(pos)).apply();

				Maps.dtMap.put(id, dt);
				dt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} catch (MalformedURLException e) {
				Log.e(DEBUG_TAG, "unable to start Download Manager -> " + e.getMessage());
			}
		}
		
		if (autoModeEnabled && !blockDashboardLaunch) {
			launchDashboardActivity();
		}
	}
	
	private String findAudioCodec() {
		String aExt = null;
		
		if (codecs.get(pos).equals("webm")) aExt = ".ogg";
		if (codecs.get(pos).equals("mp4")) aExt = ".aac";
		if (codecs.get(pos).equals("flv") && qualities.get(pos).equals("small")) aExt = ".mp3";
		if (codecs.get(pos).equals("flv") && qualities.get(pos).equals("medium")) aExt = ".aac";
		if (codecs.get(pos).equals("flv") && qualities.get(pos).equals("large")) aExt = ".aac";
		if (codecs.get(pos).equals("3gp")) aExt = ".aac";
		
		if (codecs.get(pos).equals("m4a")) aExt = ".aac";
		if (codecs.get(pos).equals("ogg")) aExt = ".ogg";
		return aExt;
	}
	
	private String findMatchGroupOne(String text, String regEx) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) return matcher.group(1);
		return "";
	}

	private String urlBlockMatchAndDecode(String content) {
		
		// log entire YouTube requests
		//File req = new File(YTD.dir_Downloads, "YTD_yt_req.txt");
		//Utils.appendStringToFile(req, content);
		
		if (content.equals("e")) return "e";
		
		if (asyncDownload.isCancelled()) {
			Utils.logger("d", "asyncDownload cancelled @ urlBlockMatchAndDecode begin", DEBUG_TAG);
			return "Cancelled!";
		}
		
		Pattern rtmpePattern = Pattern.compile("rtmpe=yes|conn=rtmpe");
		Matcher rtmpeMatcher = rtmpePattern.matcher(content);
		if (rtmpeMatcher.find()) {
			return "rtmpe";
		}
		
		Pattern loginPattern = Pattern.compile("restrictions:age");
		Matcher loginMatcher = loginPattern.matcher(content);
		if (loginMatcher.find()) {
			return "login_required";
		}
		
		findVideoFilenameBase(content);
		findJs(content);
		
		String ueStreams = findMatchGroupOne(content, "url_encoded_fmt_stream_map\\\": \\\"(.*?)\\\"");
		String asStreams = findMatchGroupOne(content, "adaptive_fmts\\\": \\\"(.*?)\\\"");
		
		findItags(ueStreams + "," + asStreams);
		
		boolean asEnabled = YTD.settings.getBoolean("enable_adaptive", false);
		if (asEnabled || autoModeEnabled) {
			return splitStreamsGroups(ueStreams + "," + asStreams, content);
		} else {
			return splitStreamsGroups(ueStreams, content);
		}
	}
	
	private void findItags(String streams) {
		Pattern blockPattern = Pattern.compile(",");
		Matcher blockMatcher = blockPattern.matcher(streams);
		if (blockMatcher.find() && !asyncDownload.isCancelled()) {
			String[] blocks = streams.split(blockPattern.toString());
			int count = blocks.length-1;
			int i = 0;
			while (i < count && !asyncDownload.isCancelled()) {
				itagMatcher(blocks[i], i, false);
				i++;
			}
			
			int[] log = new int[itags.size()];
			for (int j = 0; j < itags.size(); j++) log[j] = itags.get(j);
			Utils.logger("v", "itags matched: " + Arrays.toString(log), DEBUG_TAG);
		}
	}

	private String splitStreamsGroups(String streams, String content) {
		Pattern blockPattern = Pattern.compile(",");
		Matcher blockMatcher = blockPattern.matcher(streams);
		if (blockMatcher.find() && !asyncDownload.isCancelled()) {
			String[] blocks = streams.split(blockPattern.toString());
			int count = blocks.length-1;
			
			if (count == 0) return "e";
			
			progressBar1.setIndeterminate(false);
			decryptionArray = null;
			int i = 0;
			Utils.logger("d", "*** decoded streams ***", DEBUG_TAG);
			while (i < count && !asyncDownload.isCancelled()) {
				try {
					blocks[i] = URLDecoder.decode(blocks[i], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Log.e(DEBUG_TAG, "UnsupportedEncodingException @ splitStreamsGroups: " + e.getMessage());
				}
				
				asyncDownload.doProgress((int) (i * 100 / count));
				
				Utils.logger("v", "index " + i + ", block: " + blocks[i], DEBUG_TAG);
				
				codecMatcher(blocks[i], i);
				qualityMatcher(blocks[i], i);
				itagMatcher(blocks[i], i, true);
				linkComposer(blocks[i], i);
				
				i++;
			}
			//findDashUrl(content);
		} else {
			Utils.logger("d", "asyncDownload cancelled @ 'matchUrlEncodedStreams' match", DEBUG_TAG);
		}
		return "ok";
	}
	
	private class AsyncSizesFiller extends AsyncTask<String, String, Void> {

		protected void onPreExecute() {
			isAsyncSizesFillerRunning = true;
			Utils.logger("d", "*** sizes ***", DEBUG_TAG);
		}

		@Override
		protected Void doInBackground(String... urls) {
			for (int i = 0; i < urls.length; i++) {
				if (!this.isCancelled()) {
					String size = getVideoFileSize(urls[i]);
					if (size.equals("-")) {
						Utils.logger("d", "trying getVideoFileSize 2nd time", DEBUG_TAG);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							Log.e(DEBUG_TAG, "InterruptedException: " + e.getMessage());
						}
						size = getVideoFileSize(urls[i]);
						if (size.equals("-")) {
							Utils.logger("w", "trying getVideoFileSize 3rd (last) time", DEBUG_TAG);
							try {
								Thread.sleep(250);
							} catch (InterruptedException e) {
								Log.e(DEBUG_TAG, "InterruptedException: " + e.getMessage());
							}
							size = getVideoFileSize(urls[i]);
						}
					}
					//Utils.logger("d", "index: " + i + ", size: " + size, DEBUG_TAG);
					Utils.logger("d", "size: " + size, DEBUG_TAG);

					publishProgress(String.valueOf(i), size);
				}
			}
			return null;
		}
		
		protected void onProgressUpdate(String... i) {
			try {
				Integer index = Integer.valueOf(i[0]);
				String newValue = i[1];
				
				sizes.remove(index);
				sizes.add(index, " - " + newValue);
				
				listEntries.clear();
				listEntriesBuilder();

				aA.notifyDataSetChanged();
			} catch (IndexOutOfBoundsException e) {
				Toast.makeText(ShareActivity.this, getString(R.string.video_list_error_toast), Toast.LENGTH_SHORT).show();
				Log.e(DEBUG_TAG, "IndexOutOfBoundsException@AsyncSizesFiller#onProgressUpdate:\n" + e.getMessage());
				this.cancel(true);
			}
		}

		@Override
		protected void onPostExecute(Void unused) {
			Utils.logger("v", "AsyncSizesFiller # onPostExecute", DEBUG_TAG);
			isAsyncSizesFillerRunning = false;
		}
	}

	private void findVideoFilenameBase(String content) {
		String title = findMatchGroupOne(content, "<title>(.*?)</title>");
		if (!title.isEmpty()) {
			titleRaw = title.replaceAll("\\s*-\\s*YouTube", "");
			titleRaw = android.text.Html.fromHtml(titleRaw).toString();
			basename = titleRaw.replaceAll("\\W", "_");
		} else {
			basename = "Youtube_Video";
		}
		Utils.logger("d", "findVideoFilenameBase: " + basename, DEBUG_TAG);
	}
	
	public static void assignConstraint(CharSequence pConstraint) {
		constraint = pConstraint;
		listEntries.clear();
		listEntriesBuilder();
		aA.notifyDataSetChanged();
	}
	
	public static void listEntriesBuilder() {
		for (int i = 0; i < itagsText.size(); i++) {
			if (constraint == null || TextUtils.isEmpty(constraint)) {
				try {
					listEntries.add(new ShareActivityListItem(itagsText.get(i) + sizes.get(i), itags.get(i)));
				} catch (NoSuchElementException e) {
					listEntries.add(new ShareActivityListItem("//", -1));
				} catch (IndexOutOfBoundsException e) {
					listEntries.add(new ShareActivityListItem("--", -1));
				}
			} else {
				String[] constraintItags = Pattern.compile("/", Pattern.LITERAL).split(constraint);
				for (int j = 0; j < constraintItags.length; j++) {
					if (itags.get(i) == Integer.parseInt(constraintItags[j])) {
						Utils.logger("i", "matched itag -> " + constraintItags[j], DEBUG_TAG);
						try {
							listEntries.add(new ShareActivityListItem(itagsText.get(i) + sizes.get(i), itags.get(i)));
						} catch (NoSuchElementException e) {
							listEntries.add(new ShareActivityListItem("//", -1));
						} catch (IndexOutOfBoundsException e) {
							listEntries.add(new ShareActivityListItem("--", -1));
						}
					}
				}
			}
		}
	}

	private void linkComposer(String block, int i) {
		Pattern urlPattern = Pattern.compile("url=(.+?)\\\\u0026");
		Matcher urlMatcher = urlPattern.matcher(block);
		String url = null;
		if (urlMatcher.find()) {
			url = urlMatcher.group(1);
		} else {
			Pattern urlPattern2 = Pattern.compile("url=(.+?)$");
			Matcher urlMatcher2 = urlPattern2.matcher(block);
			if (urlMatcher2.find()) {
				url = urlMatcher2.group(1);
			} else {
				Log.e(DEBUG_TAG, "index: " + i + "url: " + url);
			}
		}

		String sig = null;
		Pattern sigPattern = Pattern.compile("sig=(.+?)\\\\u0026");
		Matcher sigMatcher = sigPattern.matcher(block);
		if (sigMatcher.find()) {
			sig = "signature=" + sigMatcher.group(1);
			Utils.logger("d", "index: " + i + ", non-ecrypted signature found on step 1", DEBUG_TAG);
		} else {
			Pattern sigPattern2 = Pattern.compile("sig=(.+?)$");
			Matcher sigMatcher2 = sigPattern2.matcher(block);
			if (sigMatcher2.find()) {
				sig = "signature=" + sigMatcher2.group(1);
				Utils.logger("d", "index: " + i + ", non-ecrypted signature found on step 2", DEBUG_TAG);
			} else {
				Pattern sigPattern3 = Pattern.compile("sig=([[0-9][A-Z]]{39,40}\\.[[0-9][A-Z]]{39,40})");
				Matcher sigMatcher3 = sigPattern3.matcher(block);
				if (sigMatcher3.find()) {
					sig = "signature=" + sigMatcher3.group(1);
					Utils.logger("d", "index: " + i + ", non-ecrypted signature found on step 3", DEBUG_TAG);
				} else {
					Pattern sigPattern4 = Pattern.compile("^s=(.+?)\\\\u0026");
					Matcher sigMatcher4 = sigPattern4.matcher(block);
					if (sigMatcher4.find()) {
						Utils.logger("d", "index: " + i + ", encrypted signature found on step 1; length is " + sigMatcher4.group(1).length(), DEBUG_TAG);
						sig = "signature=" + decryptExpSig(sigMatcher4.group(1));
					} else {
						Pattern sigPattern5 = Pattern.compile("\\\\u0026s=(.+?)\\\\u0026");
						Matcher sigMatcher5 = sigPattern5.matcher(block);
						if (sigMatcher5.find()) {
							Utils.logger("d", "index: " + i + ", encrypted signature found on step 2; length is " + sigMatcher5.group(1).length(), DEBUG_TAG);
							sig = "signature=" + decryptExpSig(sigMatcher5.group(1));
						} else {
							Pattern sigPattern6 = Pattern.compile("\\\\u0026s=(.+?)$");
							Matcher sigMatcher6 = sigPattern6.matcher(block);
							if (sigMatcher6.find()) {
								Utils.logger("d", "index: " + i + ", encrypted signature found on step 3; length is " + sigMatcher6.group(1).length(), DEBUG_TAG);
								sig = "signature=" + decryptExpSig(sigMatcher6.group(1));
							} else {
								Utils.logger("w", "index: " + i + ", sig: " + sig, DEBUG_TAG);
							}
						}
					}
				}
			}
		}

		Utils.logger("v", "index " + i + ", url: " + url, DEBUG_TAG);
		Utils.logger("v", "index " + i + ", sig: " + sig, DEBUG_TAG);
		
		String composedLink = url + "&" + sig;

		links.add(composedLink);
		//Utils.logger("i", composedLink);
		
		sizes.add("");
	}
	
	private String decryptExpSig(String sig) {
		FetchUrl fu = new FetchUrl(sShare);
		
		if (decryptionArray == null) {
			String jsCode = null;
			
			if (!jslink.equals("e")) jsCode = fu.doFetch(jslink);
			
			String findSignatureCode = 
					"function isInteger(n) {" +
					"	return (typeof n==='number' && n%1==0);" +
					"}" +

					"function findSignatureCode(sourceCode) {" +
					"	var functionNameMatches=sourceCode.match(/\\.signature\\s*=\\s*(\\w+)\\(\\w+\\)/);" +
					"	var functionName=(functionNameMatches)?functionNameMatches[1]:null;" +
					"	" +
					"	var regCode=new RegExp('function '+functionName+" +
					"			'\\\\s*\\\\(\\\\w+\\\\)\\\\s*{\\\\w+=\\\\w+\\\\.split\\\\(\"\"\\\\);(.+);return \\\\w+\\\\.join');" +
					"	var functionCodeMatches=sourceCode.match(regCode);" +
					"	var functionCode=(functionCodeMatches)?functionCodeMatches[1]:null;" +
					"	" +
					"	var regSlice=new RegExp('slice\\\\s*\\\\(\\\\s*(.+)\\\\s*\\\\)');" +
					"	var regSwap=new RegExp('\\\\w+\\\\s*\\\\(\\\\s*\\\\w+\\\\s*,\\\\s*([0-9]+)\\\\s*\\\\)');" +
					"	var regInline=new RegExp('\\\\w+\\\\[0\\\\]\\\\s*=\\\\s*\\\\w+\\\\[([0-9]+)\\\\s*%\\\\s*\\\\w+\\\\.length\\\\]');" +
					"	var functionCodePieces = functionCode.split(';');" +
					"	var decodeArray=[], signatureLength=81;" +
					"	for (var i=0; i<functionCodePieces.length; i++) {" +
					"		functionCodePieces[i]=functionCodePieces[i].trim();" +
					"		if (functionCodePieces[i].length==0) {" +
					"		} else if (functionCodePieces[i].indexOf('slice') >= 0) {" +
					"			var sliceMatches=functionCodePieces[i].match(regSlice);" +
					"			var slice=(sliceMatches)?sliceMatches[1]:null;" +
					"			slice=parseInt(slice, 10);" +
					"			if (isInteger(slice)){ " +
					"				decodeArray.push(-slice);" +
					"				signatureLength+=slice;" +
					"			} " +
					"		} else if (functionCodePieces[i].indexOf('reverse') >= 0) {" +
					"			decodeArray.push(0);" +
					"		} else if (functionCodePieces[i].indexOf('[0]') >= 0) {" +
					"			if (i+2<functionCodePieces.length && " +
					" 				functionCodePieces[i+1].indexOf('.length') >= 0 &&" +					
					"				functionCodePieces[i+1].indexOf('[0]') >= 0) {" +
					"				var inlineMatches=functionCodePieces[i+1].match(regInline);" +
					"				var inline=(inlineMatches)?inlineMatches[1]:null;" +
					"				inline=parseInt(inline, 10);" +
					"				decodeArray.push(inline);" +
					"				i+=2;" +
					"			} " +
					"		} else if (functionCodePieces[i].indexOf(',') >= 0) {" +
					"			var swapMatches=functionCodePieces[i].match(regSwap);" +
					"			var swap=(swapMatches)?swapMatches[1]:null;" +
					"			swap=parseInt(swap, 10);" +
					"			if (isInteger(swap)){" +
					"				decodeArray.push(swap);" +
					"			} " +
					"		}" +
					"	}" +
					"	return decodeArray;" +
					"}";
			
			decryptionArray = RhinoRunner.obtainDecryptionArray(jsCode, findSignatureCode);
			
			if (decryptionArray[0].equals("e")) decryptionArray = downloadHardCodedArray();
			
			decryptionFunction = "function decryptSignature(a){ a=a.split(\"\"); ";
			
			for (int i = 0; i < decryptionArray.length; i++) {
				int rule = Integer.parseInt(decryptionArray[i]);
				if (rule == 0) decryptionFunction = decryptionFunction + "a=a.reverse(); ";
				if (rule < 0) decryptionFunction = decryptionFunction + "a=a.slice("+ -rule +"); ";
				if (rule > 0) decryptionFunction = decryptionFunction + "a=swap(a,"+ rule +"); ";
			}
			decryptionFunction = decryptionFunction + "return a.join(\"\")} function swap(a,b){ var c=a[0]; a[0]=a[b%a.length]; a[b]=c; return a };";
			
			Utils.logger("i", "decryptionArray: " + Arrays.toString(decryptionArray), DEBUG_TAG);
			Utils.logger("i", "decryptionFunction: " + decryptionFunction, DEBUG_TAG);
		}
		
		String signature = RhinoRunner.decipher(sig, decryptionFunction);
		
		return signature;
	}

	private String[] downloadHardCodedArray() {
		Utils.logger("w", "downloading hard-coded decryption array", DEBUG_TAG);
		FetchUrl fu = new FetchUrl(sShare);
		String arrayLink =  "http://sourceforge.net/projects/ytdownloader/files/utils/array/download";
		String as = fu.doFetch(arrayLink);
		String[] arr = Pattern.compile(",", Pattern.LITERAL).split(as.replaceAll("\\n", ""));
		return arr;
	}
	
	private void findJs(String content) {
		String jslinkRaw = findMatchGroupOne(content, "\"js\":\\s*\"([^\"]+)\"");
		if (!jslinkRaw.isEmpty()) {
			if (!(jslinkRaw.indexOf("//") == 0)) {
				Utils.logger("w", "adding 'http:' to jslinkRaw", DEBUG_TAG);
				jslinkRaw = "http:" + jslinkRaw;
			}
			jslink = jslinkRaw.replaceAll("\\\\", "");
		} else {
			jslink = "e";
		}
		Utils.logger("v", "jslink: " + jslink, DEBUG_TAG);
	}
	
	/*@SuppressLint("DefaultLocale")
	private void findDashUrl(String content) {
		Utils.logger("d", "*** dash signated streams ***", DEBUG_TAG);
		String[] dashElements;
		
		String dashManifest = findMatchGroupOne(content, "\"dashmpd\":\\s*\"([^\"]+)\"");
		//Utils.logger("i", "dashManifest: " + dashManifest, DEBUG_TAG);
		if (!dashManifest.isEmpty()) {
			String dashParams = findMatchGroupOne(dashManifest, "youtube.com\\\\\\/api\\\\\\/manifest\\\\\\/dash\\\\\\/(.+)");
			//Utils.logger("i", "dashParams: " + dashParams, DEBUG_TAG);
			if (!dashParams.isEmpty()) {
				dashElements = dashParams.split("\\\\\\/");
				for (int i=0; i < dashElements.length; i+=2) {
					if (i>0) dashUrl = dashUrl + "&";
					if (dashElements[i].equals("s")) {
						Utils.logger("i", "ecrypted signature found into dash manifest", DEBUG_TAG);
						dashUrl = dashUrl + ("signature=" + decryptExpSig(dashElements[i+1]));
					} else if (dashElements[i].equals("sig")) {
						dashUrl = dashUrl + ("signature=" + dashElements[i+1]);
					} else {
						dashUrl = dashUrl + (dashElements[i] + '=' + dashElements[i+1]);
					}
				}
				//Utils.logger("i", "dashUrl (partial): " + dashUrl, DEBUG_TAG);
				if (!links.get(0).isEmpty()) {
					dashStartUrl = findMatchGroupOne(links.get(0), "(http.*?videoplayback\\?)");
					//Utils.logger("i", "dashStartURL: " + dashStartUrl, DEBUG_TAG);
				}
				if (!dashStartUrl.isEmpty()) {
					dashUrl = dashStartUrl + dashUrl;
				} else {
					dashUrl = "";
				}
				
				if (!dashUrl.isEmpty()) {
					if (dashUrl.toLowerCase().indexOf("ratebypass") == -1) {
						dashUrl = dashUrl + "&ratebypass=yes";
					}
				
					if (itags.contains(135))
						
						addDashUrlEntries(3, dashUrl, "flv", "large", "35"); 
					
					if (itags.contains(137) || itags.contains(264)) 
						addDashUrlEntries(0, dashUrl, "mp4", "hd1080", "37");
					
					if (itags.contains(138)) 
						addDashUrlEntries(0, dashUrl, "mp4", "highres", "38");
					
					if (itags.contains(248)) 
						addDashUrlEntries(2, dashUrl, "webm", "hd1080", "46");
				}
			}
		}
	}
	
	private void addDashUrlEntries(int i, String link, String codec, String quality, String itag) {
		links.add(i, link + "&itag=" + itag);
		codecs.add(i, codec);
		qualities.add(i, quality);
		sizes.add(i, "");
		String itagText = findItag(itag);
		
		if (YTD.SHOW_ITAGS_AND_NO_SIZE_FOR_DUBUG) {
			itagsText.add(i, "[" + itag + "d]_" + itagText);
		} else {
			itagsText.add(i, itagText);
		}
		
		itags.add(i, Integer.parseInt(itag));
		
		Utils.logger("d", "inserted at index: " + i + ", codec: " + codec, DEBUG_TAG);
		Utils.logger("d", "inserted at index: " + i + ", quality: " + quality, DEBUG_TAG);
		Utils.logger("d", "inserted at index: " + i + ", itag: " + itag + " (" + itagText + ")", DEBUG_TAG);
		Utils.logger("v", "inserted at index: " + i + ", url: " + link + "&itag=" + itag, DEBUG_TAG);
	}*/

	private String getVideoFileSize(String link) {
		try {
			final URL url = new URL(link);
			URLConnection ucon = url.openConnection();
			ucon.connect();
			int file_size = ucon.getContentLength();
			return Utils.MakeSizeHumanReadable(file_size, false);
		} catch(IOException e) {
			return "-";
		}
	}

	private void codecMatcher(String current, int i) {
		Pattern codecPattern = Pattern.compile("(webm|mp4|flv|3gp)");
		Matcher codecMatcher = codecPattern.matcher(current);
		if (codecMatcher.find()) {
			codecs.add(codecMatcher.group());
		} else {
			codecs.add("video");
		}
		Utils.logger("d", "index: " + i + ", Codec: " + codecs.get(i), DEBUG_TAG);
	}

	private void qualityMatcher(String current, int i) {
		Pattern qualityPattern = Pattern.compile("(highres|hd1080|hd720|large|medium|small)");
		Matcher qualityMatcher = qualityPattern.matcher(current);
		if (qualityMatcher.find()) {
			qualities.add(qualityMatcher.group().replace("highres", "Original"));
		} else {
			qualities.add("-");
		}
		Utils.logger("d", "index: " + i + ", Quality: " + qualities.get(i), DEBUG_TAG);
	}
	
	private void itagMatcher(String current, int i, boolean isItagsTextRun) {
		String res = "-";
		String itag = findMatchGroupOne(current, "itag=([0-9]{1,3})\\\\u0026");
		if (itag.isEmpty()) {
			itag = findMatchGroupOne(current, "itag=([0-9]{1,3})$");
		}
		
		if (isItagsTextRun) {
			res = findItag(itag);
			Utils.logger("d", "index: " + i + ", itag: " + itag + " (" + res + ")", DEBUG_TAG);
			
			if (YTD.SHOW_ITAGS_AND_NO_SIZE_FOR_DUBUG) {
				itagsText.add("[" + itag + "]_" + res);
			} else {
				itagsText.add(res);
			}
			
			if (itag.equals("139") || itag.equals("140") || itag.equals("141")) {
				codecs.remove(i);
				codecs.add(i, "m4a");
			}
			
			if (itag.equals("171") || itag.equals("172")) {
				codecs.remove(i);
				codecs.add(i, "ogg");
			}
		} else {
			itags.add(Integer.parseInt(itag));
		}
	}

	private String findItag(String itag) {
		String res = "-";
		if (itag != null) {
			try {
				switch (Integer.parseInt(itag)) {
				// *** url encoded streams ***
				case 5:
					res = _FLV_240P;
					break;
				case 6:
					res = _FLV_270P;
					break;
				case 17:
					res = _3GP_144P;
					break;
				case 18:
					res = _MP4_270P_360P;
					break;
				case 22:
					res = _MP4_720P;
					break;
				case 34:
					res = _FLV_360P;
					break;
				case 35:
					res = _FLV_480P;
					break;
				case 36:
					res = _3GP_240P;
					break;
				case 37:
					res = _MP4_1080P;
					break;
				case 38:
					res = _MP4_ORIGINAL;
					break;
				case 43:
					res = _WEBM_360P;
					break;
				case 44:
					res = _WEBM_480P;
					break;
				case 45:
					res = _WEBM_720P;
					break;
				case 46:
					res = _WEBM_1080P;
					break;
				case 59:
					res = _MP4_480P;
					break;
				case 78:
					res = _MP4_360P;
					break;
				case 82:
					res = _MP4_360P_3D;
					break;
				case 83:
					res = _MP4_240P_3D;
					break;
				case 84:
					res = _MP4_720P_3D;
					break;
				case 85:
					res = _MP4_520P_3D;
					break;
				case 100:
					res = _WEBM_360P_3D;
					break;
				case 101:
					res = _WEBM_360P_3D;
					break;
				case 102:
					res = _WEBM_720P_3D;
					break;
				// *** adaptive streams ***
				case 133:
					res = _VO_MP4_240P;
					break;
				case 134:
					res = _VO_MP4_360P;
					break;
				case 135:
					res = _VO_MP4_480P;
					break;
				case 136:
					res = _VO_MP4_720P;
					break;
				case 137:
					res = _VO_MP4_1080P;
					break;
				case 138:
					res = _VO_MP4_ORIGINAL;
					break;
				case 139:
					res = _AO_M4A_LOW_Q;
					break;
				case 140:
					res = _AO_M4A_MED_Q;
					break;
				case 141:
					res = _AO_M4A_HI_Q;
					break;
				case 160:
					res = _VO_MP4_144P;
					break;
				case 171:
					res = _AO_OGG_MED_Q;
					break;
				case 172:
					res = _AO_OGG_HI_Q;
					break;
				case 242:
					res = _VO_WEBM_240P;
					break;
				case 243:
					res = _VO_WEBM_360P;
					break;
				case 244:
					res = _VO_WEBM_480P;
					break;
				case 245:
					res = _VO_WEBM_480P;
					break;
				case 246:
					res = _VO_WEBM_480P;
					break;
				case 247:
					res = _VO_WEBM_720P;
					break;
				case 248:
					res = _VO_WEBM_1080P;
					break;
				case 264:
					res = _VO_MP4_1080P_HBR;
					break;
				default:
					res = _UNKNOWN;
				}
				
			} catch (NumberFormatException e) {
				Log.e(DEBUG_TAG, "findItag --> " + e.getMessage());
			}
		}
		return res;
	}
	
	private String[] generateThumbUrls() {
		
		String url1 = "http://i1.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
		String url2 = "http://i2.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
		String url3 = "http://i3.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
		String url4 = "http://i4.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
		
		String[] urls = { url1, url2, url3, url4 };
		return urls;
	}
	
	private Bitmap downloadThumbnail(String fileUrl) {
		InputStream is = null;
		URL myFileUrl = null;
		try {
			myFileUrl = new URL(fileUrl);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "IOException: " + e.getMessage());
			return null;
		}
	}
	
	private void assignBitmapToVideoListThumbnail(String[] url) {
		Bitmap bm0  = downloadThumbnail(url[0]);
		if (bm0 != null && !asyncDownload.isCancelled()) {
			img = bm0;
			Utils.logger("d", "assigning bitmap from url[0]: " + url[0], DEBUG_TAG);
		} else {
			Bitmap bm1  = downloadThumbnail(url[1]);
			if (bm1 != null && !asyncDownload.isCancelled()) {
				img = bm1;
				Utils.logger("d", "assigning bitmap from url[1]: " + url[1], DEBUG_TAG);
			} else {
				Bitmap bm2  = downloadThumbnail(url[2]);
				if (bm2 != null && !asyncDownload.isCancelled()) {
					img = bm2;
					Utils.logger("d", "assigning bitmap from url[2]: " + url[2], DEBUG_TAG);
				} else {
					Bitmap bm3  = downloadThumbnail(url[3]);
					if (bm3 != null && !asyncDownload.isCancelled()) {
						img = bm3;
						Utils.logger("d", "assigning bitmap from url[3]: " + url[3], DEBUG_TAG);
					} else {
						Log.e(DEBUG_TAG, "Falling back on asset's placeholder");
						InputStream assIs = null;
						AssetManager assMan = getAssets();
						try {
							assIs = assMan.open("placeholder.png");
						} catch (IOException e1) {
							Log.e(DEBUG_TAG, "downloadThumbnail -> " + e1.getMessage());
						}
						img = BitmapFactory.decodeStream(assIs);
					}
				}
			}
		}
	}
	
	private void writeThumbToDisk() {
		File thumbFile = new File(sShare.getDir(YTD.THUMBS_FOLDER, 0), videoId + ".png");
		//if (!thumbFile.exists()) {
			try {
				FileOutputStream os = new FileOutputStream(thumbFile);
				img.compress(Bitmap.CompressFormat.PNG, 50, os);
			} catch (FileNotFoundException e) {
				Log.e(DEBUG_TAG, "writeThumbToDisk -> " + e.getMessage());
			}
		//}
	}
	
	/*private void updateInit() {
		int prefSig = YTD.settings.getInt("APP_SIGNATURE", 0);
		Utils.logger("d", "prefSig: " + prefSig, DEBUG_TAG);
		
		if (prefSig == YTD.SIG_HASH) {
				Utils.logger("d", "YTD signature in PREFS: update check possile", DEBUG_TAG);
				
				if (YTD.settings.getBoolean("autoupdate", false)) {
					Utils.logger("i", "autoupdate enabled", DEBUG_TAG);
					YTD.autoUpdate();
				}
		} else {
			Utils.logger("d", "different or null YTD signature. Update check cancelled.", DEBUG_TAG);
		}
	}*/
}

