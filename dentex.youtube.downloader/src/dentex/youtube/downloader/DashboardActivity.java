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

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.matsuhiro.android.download.DownloadTask;
import com.matsuhiro.android.download.DownloadTaskListener;
import com.matsuhiro.android.download.InvalidYoutubeLinkException;
import com.matsuhiro.android.download.Maps;
import com.squareup.picasso.Picasso;

import dentex.youtube.downloader.ffmpeg.FfmpegController;
import dentex.youtube.downloader.ffmpeg.ShellUtils.ShellCallback;
import dentex.youtube.downloader.queue.FFmpegExtractAudioTask;
import dentex.youtube.downloader.queue.FFmpegExtractFlvThumbTask;
import dentex.youtube.downloader.utils.DashboardClearHelper;
import dentex.youtube.downloader.utils.JsonHelper;
import dentex.youtube.downloader.utils.PopUps;
import dentex.youtube.downloader.utils.Utils;

public class DashboardActivity extends Activity {
	
	private final static String DEBUG_TAG = "DashboardActivity";
	public static boolean isDashboardRunning;
	protected File audioFile;
	protected String basename;
	private String aSuffix;
	private String vfilename;
	private boolean removeVideo;
	private boolean removeAudio;
	private boolean removeAoVo;
	private ListView lv;
	private static TextView status;
	private Editable searchText;
	//private static RelativeLayout bkgRl;
	private static ImageView bkgImg;
	
	//public static int entries = 0;
	public static List<String> idEntries = new ArrayList<String>();
	static List<String> typeEntries = new ArrayList<String>();
	static List<String> ytidEntries = new ArrayList<String>();
	static List<Integer> posEntries = new ArrayList<Integer>();
	static List<String> statusEntries = new ArrayList<String>();
	public static List<String> pathEntries = new ArrayList<String>();
	public static List<String> filenameEntries = new ArrayList<String>();
	static List<String> basenameEntries = new ArrayList<String>();
	static List<String> audioExtEntries = new ArrayList<String>();
	static List<String> sizeEntries = new ArrayList<String>();
	static List<String> partSizeEntries = new ArrayList<String>();
	static List<Long> progressEntries = new ArrayList<Long>();
	static List<Long> speedEntries = new ArrayList<Long>();
	
	private static List<DashboardListItem> itemsList = new ArrayList<DashboardListItem>();
	private static DashboardAdapter da;
	private boolean isSearchBarVisible;
	private DashboardListItem currentItem = null;
	private boolean extrTypeIsMp3Conv;
	private String type;
	private boolean isFfmpegRunning = false;
	
	private String tagArtist;
	private String tagAlbum;
	private String tagTitle;
	private String tagYear;
	private String tagGenre;
	
    private String ogg = "OGG Vorbis";
    private String aac = "AAC (Advanced Audio Codec)";
    private String mp3 = "MP3 (low quality)";
    //private String aac_mp3 = "AAC / MP3";
    
	private boolean newClick;
	public static long countdown;
	
	public static Activity sDashboard;
	private Timer autoUpdate;
	public static boolean isLandscape;
	private static int entriesInProgress;
	private String muxedFileName;
	private File muxedVideo;
	private long aNewId;
	private int totSeconds;
	private int currentTime;
	private File audioOnlyFile;
	public String audioOnlyExt = "";
	DashboardListItem aoItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sDashboard = DashboardActivity.this;

		// Theme init
    	Utils.themeInit(this);
    	
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_dashboard);
		
		// Language init
    	Utils.langInit(this);
    	
    	// Detect screen orientation
    	int or = this.getResources().getConfiguration().orientation;
    	isLandscape = (or == 2) ? true : false;
    	
    	if (da != null) {
    		clearAdapterAndLists();
    	}
    	
    	countdown = 10;
    	
    	lv = (ListView) findViewById(R.id.dashboard_list);
    	status = (TextView) findViewById(R.id.dashboard_status);
    	//bkgRl = (RelativeLayout) findViewById(R.id.bkg_rl);
    	bkgImg = (ImageView) findViewById(R.id.bkg_img);
    	
    	da = new DashboardAdapter(itemsList, this);
    	lv.setAdapter(da);
    	
		/*entries = */parseJson();
		updateProgressBars();
		buildList();
		writeStatus();
		
		// YTD update initialization
    	YTD.updateInit(this, false, null);
    	
    	/*Log.i(DEBUG_TAG, "DM Maps:" +
    			"\ndtMap:                 " + Maps.dtMap +
    			"\nmDownloadPercentMap:   " + Maps.mDownloadPercentMap +
    			"\nmDownloadSizeMap:      " + Maps.mDownloadSizeMap + 
    			"\nmTotalSizeMap:         " + Maps.mTotalSizeMap);*/
    	
    	lv.setTextFilterEnabled(true);
    	
    	lv.setClickable(true);
    	
    	lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (!YTD.isAnyAsyncInProgress) {
					currentItem = da.getItem(position); // in order to refer to the filtered item
					newClick = true;
        		
	        		final boolean ffmpegEnabled = YTD.settings.getBoolean("enable_advanced_features", false);
	        		
	        		AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
	        		builder.setTitle(currentItem.getFilename());
	        		
	        		if (currentItem.getStatus().equals(getString(R.string.json_status_completed)) || 
	        				currentItem.getStatus().equals(getString(R.string.json_status_imported))) {
	        			
	        			final boolean audioIsSupported = !currentItem.getAudioExt().equals("unsupported");
	        			final File in = new File (currentItem.getPath(), currentItem.getFilename());
	        			
		        		if ((currentItem.getType().equals(YTD.JSON_DATA_TYPE_V) ||
		        				currentItem.getType().equals(YTD.JSON_DATA_TYPE_V_M))
		        				&& !currentItem.getAudioExt().equals("x")) {
		        			
		        			// handle click on a **VIDEO** file entry (non-FLV)
			        		builder.setItems(R.array.dashboard_click_entries, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
			
				    				switch (which) {
			    					case 0: // open
			    						openVideoIntent(in);
			    						break;
					    			case 1: // extract audio only
					    				if (!isFfmpegRunning) {
						    				if (audioIsSupported) {
							    				if (ffmpegEnabled) {
								    				extractAudioOnly(in);
							    				} else {
							    					Utils.notifyFfmpegNotInstalled(DashboardActivity.this);
							    				}
						    				} else {
						    					notifyOpsNotSupported();
						    				}
			    						} else {
			    							notifyFfmpegIsAlreadyRunning();
			    						}
			    						break;
					    			case 2: // extract audio and convert to mp3
					    				if (!isFfmpegRunning) {
						    				if (audioIsSupported) {
							    				if (ffmpegEnabled) {
								    				extractAudioAndConvertToMp3(in);
							    				} else {
							    					Utils.notifyFfmpegNotInstalled(DashboardActivity.this);
							    				}
						    				} else {
						    					notifyOpsNotSupported();
						    				}
					    				} else {
			    							notifyFfmpegIsAlreadyRunning();
			    						}
			    					}
								}
			        		});
			        		Utils.secureShowDialog(sDashboard, builder);
			        		
		        		} else if (currentItem.getType().equals(YTD.JSON_DATA_TYPE_V) && 
								currentItem.getAudioExt().equals("x")) {
		        			
		        			// handle click on a **VIDEO** file entry (FLV)
		        			builder.setItems(R.array.dashboard_click_entries_flv, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									
			    					switch (which) {
			    					case 0: // open
			    						openVideoIntent(in);
			    						break;
					    			case 1: // extract audio and convert to mp3
					    				if (!isFfmpegRunning) {
						    				if (audioIsSupported) {
							    				if (ffmpegEnabled) {
								    				extractAudioAndConvertToMp3(in);
							    				} else {
							    					Utils.notifyFfmpegNotInstalled(DashboardActivity.this);
							    				}
						    				} else {
						    					notifyOpsNotSupported();
						    				}
					    				} else {
			    							notifyFfmpegIsAlreadyRunning();
			    						}
			    					}
								}
			        		});
		        			Utils.secureShowDialog(sDashboard, builder);
				    		
						} else if (currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_E) ||
								currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_M) ||
								currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_O)) {
							
							// handle click on a **AUDIO** file entry or
							builder.setItems(R.array.dashboard_click_entries_audio, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									
			    					switch (which) {
			    					case 0: // open
			    						openAudioIntent(in);
			    						break;
					    			case 1: // convert audio to mp3
					    				if (!isFfmpegRunning) {
					    					if (ffmpegEnabled) {
							    				convertAudioToMp3(in);
					    					} else {
						    					Utils.notifyFfmpegNotInstalled(DashboardActivity.this);
						    				}
					    				} else {
			    							notifyFfmpegIsAlreadyRunning();
			    						}
			    					}
								}
			        		});
			        		Utils.secureShowDialog(sDashboard, builder);
			        		
						} else if (currentItem.getType().equals(YTD.JSON_DATA_TYPE_V_O)) {
							
							// handle click on a **VIDEO-ONLY** file entry
							builder.setItems(R.array.dashboard_click_entries_vo, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									
			    					switch (which) {
			    					case 0: // open
			    						openVideoIntent(in);
			    						break;
					    			case 1: // mux
					    				if (!isFfmpegRunning) {
					    					if (ffmpegEnabled) {
					    						if (isFFmpegLatest()) {
					    							addAudioStream(in);
					    						} else {
					    							downloadLatestFFmpeg();
					    						}
					    					} else {
						    					Utils.notifyFfmpegNotInstalled(DashboardActivity.this);
						    				}
					    				} else {
			    							notifyFfmpegIsAlreadyRunning();
			    						}
			    					}
								}

								private void downloadLatestFFmpeg() {
									AlertDialog.Builder adb = new AlertDialog.Builder(sDashboard);
									adb.setTitle(getString(R.string.information));
									adb.setMessage(getString(R.string.ffmpeg_new_v_required));
									adb.setIcon(Utils.selectThemedInfoIcon());
									adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											File oldPrivateFile = new File(getDir("bin", 0), YTD.ffmpegBinName);
											oldPrivateFile.delete();
											
											File oldExtFile = new File(getExternalFilesDir(null), YTD.ffmpegBinName);
											oldExtFile.delete();
											
											Intent sIntent = new Intent(sDashboard, SettingsActivity.class);
							        		sIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							        		sIntent.putExtra("reset_adv_pref", true);
							        		startActivity(sIntent);
										}
									});
									
									adb.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											// cancel
										}
									});
									
									Utils.secureShowDialog(sDashboard, adb);
								}

								private boolean isFFmpegLatest() {
									File extFile = new File(getExternalFilesDir(null), YTD.ffmpegBinName + YTD.FFMPEG_CURRENT_V);
									if (extFile.exists()) {
										return true;
									} else {
										return false;
									}
								}
								
							});
							Utils.secureShowDialog(sDashboard, builder);
						}
	        		}
				} else {
					notifyAnotherOperationIsInProgress();
				}
			}
    	});
    	
    	lv.setLongClickable(true);
    	lv.setOnItemLongClickListener(new OnItemLongClickListener() {

        	@Override
        	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        		if (!YTD.isAnyAsyncInProgress) {
	        		currentItem = da.getItem(position); // in order to refer to the filtered item

	        		int COPY = 0;
	        		int MOVE = 1;
	        		int RENAME = 2;
	        		int REDOWNLOAD = 3;
	        		int SEND = 4;
	        		int REMOVE = 5;
	        		int DELETE = 6;
	        		int PAUSERESUME = 7;
	        		
	        		int[] disabledItems = null;
	
	        		if (currentItem.getStatus().equals(getString(R.string.json_status_in_progress)) ||
	        				currentItem.getStatus().equals(getString(R.string.json_status_paused))) {
	        			// show: DELETE and  PAUSERESUME
	        			disabledItems = new int[] { COPY, MOVE, RENAME, REDOWNLOAD, SEND, REMOVE };
	        		} else if (currentItem.getStatus().equals(getString(R.string.json_status_failed))) {
	        			// check if the item has a real YouTube ID, otherwise it's an imported video.
	        			if (currentItem.getYtId().length() == 11) {
	        				// show: REMOVE and REDOWNLOAD
	        				disabledItems = new int[] { COPY, MOVE, RENAME, SEND, DELETE, PAUSERESUME};
	        			} else {
	        				// show: REMOVE only
	        				disabledItems = new int[] { COPY, MOVE, RENAME, REDOWNLOAD, SEND, DELETE, PAUSERESUME };
	        			}
	        		} else if (currentItem.getStatus().equals(getString(R.string.json_status_imported)) ||
	        					//case for audio entries _completed but from _imported
	        					(currentItem.getStatus().equals(getString(R.string.json_status_completed)) &&
	    	        			!(currentItem.getYtId().length() == 11))) {
	        			// show: COPY, MOVE, RENAME, SEND, REMOVE and DELETE
	        			disabledItems = new int[] { REDOWNLOAD, PAUSERESUME };
	        		} else if (currentItem.getStatus().equals(getString(R.string.json_status_completed))) {
	        			// show: all items except PAUSERESUME
	        			disabledItems = new int[] { PAUSERESUME };
	        		} else if (currentItem.getStatus().equals(getString(R.string.json_status_queued))) {
	        			// show no items
	        			disabledItems = new int[] { COPY, MOVE, RENAME, REDOWNLOAD, SEND, REMOVE, DELETE, PAUSERESUME };
	        		}
	
	        		AlertDialog.Builder builder = new AlertDialog.Builder(sDashboard);
	        		builder.setTitle(currentItem.getFilename());
	
	    			final ArrayAdapter<CharSequence> cla = DashboardLongClickAdapter.createFromResource(
	    					sDashboard,
	    					R.array.dashboard_long_click_entries,
	    		            android.R.layout.simple_list_item_1, 
	    		            disabledItems);
	    			
	    			builder.setAdapter(cla, new DialogInterface.OnClickListener() {
	
						public void onClick(DialogInterface dialog, int which) {
				    		switch (which) {
				    			case 0:
				    				copy(currentItem);
				    				break;
				    			case 1:
				    				move(currentItem);
				    				break;
				    			case 2:
				    				rename(currentItem);
				    				break;
				    			case 3:
				    				if (currentItem.getStatus().equals(getString(R.string.json_status_failed))) {
				    					reDownload(currentItem, "RESTART");
				    				} else {
				    					reDownload(currentItem, "-");
				    				}
				    				break;
				    			case 4:
				    				send(currentItem);
				    				break;
				    			case 5:
				    				removeFromDashboard(currentItem);
				    				break;
				    			case 6:
				    				delete(currentItem);
				    				break;
				    			case 7:
				    				pauseresume(currentItem);
				    		}
	
						}	
	        		});
	        		
		        	Utils.secureShowDialog(sDashboard, builder);
        		} else {
					notifyAnotherOperationIsInProgress();
				}
        		
        		return true;
        	}
    	});
	}
	
	public static Context getContext() {
        return sDashboard;
    }
	
	private void notifyFfmpegIsAlreadyRunning() {
		Utils.logger("d", "notifyFfmpegIsAlreadyRunning()", DEBUG_TAG);
		Toast.makeText(sDashboard, getString(R.string.ffmpeg_already_running), Toast.LENGTH_SHORT).show();
	}
	
	private void notifyOpsNotSupported() {
		Utils.logger("d", "notifyOpsNotSupported()", DEBUG_TAG);
		PopUps.showPopUp(getString(R.string.information), getString(R.string.unsupported_operation), "error", DashboardActivity.this);
	}
	
	private void notifyAnotherOperationIsInProgress() {
		Utils.logger("d", "notifyAnotherOperationIsInProgress()", DEBUG_TAG);
		Toast.makeText(sDashboard, getString(R.string.operation_standby), Toast.LENGTH_SHORT).show();
	}
	
	private void toastOpsNotExecuted() {
		Toast.makeText(sDashboard, getString(R.string.long_press_warning_title) + 
				"\n- " + getString(R.string.notification_downloading_pt1) + " (" + 
				getString(R.string.json_status_paused) + "/" + getString(R.string.json_status_in_progress) + " )" + 
				"\n- " + getString(R.string.empty_dashboard), 
				Toast.LENGTH_SHORT).show();
		Utils.logger("d", "toastOpsNotExecuted()", DEBUG_TAG);
	}
	
	private void copy(DashboardListItem currentItem) {
		Intent intent = new Intent(DashboardActivity.this,  FileChooserActivity.class);
    	if (intent != null) {
    		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
    		intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
    		intent.putExtra("PATH", currentItem.getPath());
    		intent.putExtra("NAME", currentItem.getFilename());
    		startActivityForResult(intent, 1);
    	}
	}
	
	private void move(DashboardListItem currentItem) {
		Intent intent = new Intent(DashboardActivity.this,  FileChooserActivity.class);
    	if (intent != null) {
    		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
    		intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
    		intent.putExtra("PATH", currentItem.getPath());
    		intent.putExtra("NAME", currentItem.getFilename());
    		startActivityForResult(intent, 2);
    	}
	}
	
	private void rename(final DashboardListItem currentItem) {
		AlertDialog.Builder adb = new AlertDialog.Builder(sDashboard);
		LayoutInflater adbInflater = LayoutInflater.from(DashboardActivity.this);
	    View inputFilename = adbInflater.inflate(R.layout.dialog_input_filename, null);
	    final TextView userFilename = (TextView) inputFilename.findViewById(R.id.input_filename);
	    userFilename.setText(currentItem.getFilename());
	    adb.setView(inputFilename);
	    adb.setTitle(getString(R.string.rename_dialog_title));
	    //adb.setMessage(getString(R.string.rename_dialog_msg));
	    
	    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		String input = userFilename.getText().toString();
	    		File in = new File(currentItem.getPath(), currentItem.getFilename());
	    		File renamed = new File(currentItem.getPath(), input);
	    		
	    		if (!currentItem.getFilename().equals(input)) {
		    		if (in.renameTo(renamed)) {
		    			// set new name to the list item
		    			currentItem.setFilename(input);
		    			
		    			// update the JSON file entry
		    			JsonHelper.addEntryToJsonFile(
								currentItem.getId(), 
								currentItem.getType(), 
								currentItem.getYtId(), 
								currentItem.getPos(),
								currentItem.getStatus(), 
								currentItem.getPath(), 
								input, 
								Utils.getFileNameWithoutExt(input), 
								currentItem.getAudioExt(), 
								currentItem.getSize(),
								false);
		    			
		    			if (in != null) {
							// remove references for the old file
							String mediaUriString = Utils.getContentUriFromFile(in, sDashboard.getContentResolver());
							//Utils.logger("d", "mediaString: " + mediaUriString, DEBUG_TAG);
							Utils.removeFromMediaStore(sDashboard, in, mediaUriString);
						}
						// scan the new file
		    			Utils.scanMedia(DashboardActivity.this, 
								new String[]{ renamed.getAbsolutePath() }, 
								new String[]{ "video/*" });
		    			
		    			// refresh the dashboard
		    			refreshlist();
		    			
		    			Utils.logger("d", "'" + in.getName() + "' renamed to '" + input + "'", DEBUG_TAG);
		    		} else {
		    			Log.e(DEBUG_TAG, "'" + in.getName() + "' NOT renamed");
		    		}
	    		} else {
	    			Utils.logger("w", "DashboardActivity#rename -> Same name used", DEBUG_TAG);
	    		}
	    		
	    		// hide keyboard
	    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.hideSoftInputFromWindow(userFilename.getWindowToken(), 0);
	    	}
	    });
	    
	    adb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
                // cancel
            }
        });
	    
	    Utils.secureShowDialog(sDashboard, adb);
	}
	
	public void  send(final DashboardListItem currentItem) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		
		if (currentItem.getType().equals(YTD.JSON_DATA_TYPE_V) || 
				currentItem.getType().equals(YTD.JSON_DATA_TYPE_V_M) || 
					currentItem.getType().equals(YTD.JSON_DATA_TYPE_V_O))
						sharingIntent.setType("video/*");
		
		if (currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_E) || 
				currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_M) || 
					currentItem.getType().equals(YTD.JSON_DATA_TYPE_A_O))
						sharingIntent.setType("audio/*");
		
		File fileToSend = new File(currentItem.getPath(), currentItem.getFilename());
		sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(fileToSend.getAbsolutePath()));
		startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_file_via)));
	}
	
	public void  removeFromDashboard(final DashboardListItem currentItem) {
		AlertDialog.Builder rem = new AlertDialog.Builder(sDashboard);
		//rem.setTitle(getString(R.string.attention));
		rem.setTitle(currentItem.getFilename());
		rem.setMessage(getString(R.string.remove_video_confirm));
		rem.setIcon(Utils.selectThemedAlertIcon());
		rem.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				JsonHelper.removeEntryFromJsonFile(currentItem.getId());
				refreshlist();
				
				YTD.videoinfo.edit().remove(currentItem.getId() + "_link").apply();
			}
		});
		
		rem.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// cancel
			}
		});
		
		Utils.secureShowDialog(sDashboard, rem);
	}

	public void delete(final DashboardListItem currentItem) {
		AlertDialog.Builder del = new AlertDialog.Builder(sDashboard);
		del.setTitle(currentItem.getFilename());
		del.setMessage(getString(R.string.delete_video_confirm));
		del.setIcon(Utils.selectThemedAlertIcon());
		del.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				new AsyncDelete().execute(currentItem);
			}
		});
		
		del.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// cancel
			}
		});
		
		Utils.secureShowDialog(sDashboard, del);
	}
	
	private void pauseresume(final DashboardListItem currentItem) {
		
		final String itemID = currentItem.getId();
		long itemIDlong = Long.parseLong(itemID);
		
		Utils.logger("d", "pauseresume on id " + itemID, DEBUG_TAG);
		
		if (currentItem.getStatus().equals(getString(R.string.json_status_in_progress))) {
			
			try {
				if (Maps.dtMap.containsKey(itemIDlong)) {
					DownloadTask dt = Maps.dtMap.get(itemIDlong);
					dt.cancel();
				} else {
					if (Maps.dtMap.size() > 0) {
						// cancel (pause) every dt found
						Utils.logger("w", "pauseresume: id not found into 'dtMap'; canceling all tasks", DEBUG_TAG);
						for (Iterator<DownloadTask> iterator = Maps.dtMap.values().iterator(); iterator.hasNext();) {
							DownloadTask dt = (DownloadTask) iterator.next();
							dt.cancel();
						}
					}
				}
			} catch (NullPointerException e) {
		    	Log.e(DEBUG_TAG, "dt.cancel() @ pauseresume: " + e.getMessage());
		    }
			
			YTD.removeIdUpdateNotification(itemIDlong);
			
			// update the JSON file entry
			JsonHelper.addEntryToJsonFile(
					itemID, 
					currentItem.getType(),
					currentItem.getYtId(), 
					currentItem.getPos(),
					YTD.JSON_DATA_STATUS_PAUSED,
					currentItem.getPath(), 
					currentItem.getFilename(),
					currentItem.getBasename(), 
					currentItem.getAudioExt(),
					currentItem.getSize(), 
					false);
		}
		
		if (currentItem.getStatus().equals(getString(R.string.json_status_paused))) {
			String link = YTD.videoinfo.getString(String.valueOf(itemID) + "_link", null);
					
			if (link != null) {
				DownloadTaskListener dtl = new DownloadTaskListener() {
					
					@Override
					public void preDownload(DownloadTask task) {
						long ID = task.getDownloadId();
						Utils.logger("d", "__preDownload on ID: " + ID, DEBUG_TAG);
						
						Maps.mNetworkSpeedMap.put(ID, (long) 0);
						
						JsonHelper.addEntryToJsonFile(
								String.valueOf(ID),
								currentItem.getType(),
								currentItem.getYtId(), 
								currentItem.getPos(),
								YTD.JSON_DATA_STATUS_IN_PROGRESS,
								currentItem.getPath(), 
								currentItem.getFilename(),
								currentItem.getBasename(), 
								currentItem.getAudioExt(),
								currentItem.getSize(), 
								false);
						
						YTD.sequence.add(ID);
						
						YTD.NotificationHelper(sDashboard);
					}
					
					@Override
					public void updateProcess(DownloadTask task) {
						/*YTD.downloadPercentMap = task.getDownloadPercentMap();
						YTD.downloadTotalSizeMap = task.getTotalSizeMap();
						YTD.downloadPartialSizeMap = task.getDownloadSizeMap();*/
					}
					
					@Override
					public void finishDownload(DownloadTask task) {
						long ID = task.getDownloadId();
						Utils.logger("d", "__finishDownload on ID: " + ID, DEBUG_TAG);
						
						Utils.scanMedia(getApplicationContext(), 
								new String[] { currentItem.getPath() + File.separator + currentItem.getFilename() }, 
								new String[] {"video/*"});
						
						long downloadTotalSize = task.getTotalSize(); //Maps.mTotalSizeMap.get(ID);
						String size = String.valueOf(Utils.MakeSizeHumanReadable(downloadTotalSize, false));
						
						JsonHelper.addEntryToJsonFile(
								String.valueOf(ID), 
								currentItem.getType(),
								currentItem.getYtId(), 
								currentItem.getPos(),
								YTD.JSON_DATA_STATUS_COMPLETED, 
								currentItem.getPath(), 
								currentItem.getFilename(),
								currentItem.getBasename(), 
								currentItem.getAudioExt(), 
								size, 
								false);
						
						refreshlist();
						
						YTD.removeIdUpdateNotification(ID);
						
						YTD.videoinfo.edit().remove(ID + "_link").apply();
						
						Maps.removeFromAllMaps(ID);
						
						//TODO Auto FFmpeg task
						if (YTD.settings.getBoolean("ffmpeg_auto_cb", false) && 
								!currentItem.getType().equals(YTD.JSON_DATA_TYPE_V_O)) {
							Utils.logger("d", "autoFfmpeg enabled: enqueing task for id: " + ID, DEBUG_TAG);
							
							String[] bitrateData = null;
							String brType = null;
							String brValue = null;
							
							String audioFileName;
							
							String extrType = YTD.settings.getString("audio_extraction_type", "conv");
							if (extrType.equals("conv")) {
								bitrateData = Utils.retrieveBitrateValuesFromPref(sDashboard);
								audioFileName = currentItem.getBasename() + "_" + bitrateData[0] + "-" + bitrateData[1] + ".mp3";
								brType = bitrateData[0];
								brValue = bitrateData[1];
							} else {
								audioFileName = currentItem.getBasename() + currentItem.getAudioExt();
							}
							
							String type = (brValue == null) ? YTD.JSON_DATA_TYPE_A_M : YTD.JSON_DATA_TYPE_A_E;
							File audioFile = new File(currentItem.getPath(), audioFileName);
							
							if (!audioFile.exists()) { 
								File videoFileToConvert = new File(currentItem.getPath(), currentItem.getFilename());
								
								long newId = System.currentTimeMillis();
								
								YTD.queueThread.enqueueTask(new FFmpegExtractAudioTask(
										sDashboard, newId, 
										videoFileToConvert, audioFile, 
										brType, brValue, 
										currentItem.getId(), 
										currentItem.getYtId(), 
										currentItem.getPos()), YTD._AUDIO_EXTR);
								
								JsonHelper.addEntryToJsonFile( 
										String.valueOf(newId), 
										type, 
										currentItem.getYtId(), 
										currentItem.getPos(),
										YTD.JSON_DATA_STATUS_QUEUED,
										currentItem.getPath(), 
										audioFileName, 
										Utils.getFileNameWithoutExt(audioFileName), 
										"", 
										"-", 
										false);
								
								refreshlist();
							}
						} else {
							Utils.logger("v", "Auto FFmpeg task for ID " + ID + " not enabled", DEBUG_TAG);
						}
					}
					
					@Override
					public void errorDownload(DownloadTask task, Throwable error) {
						String nameOfVideo = task.getFileName();
						long ID = task.getDownloadId();
							
						Utils.logger("w", "__errorDownload on ID: " + ID, DEBUG_TAG);
						
						if (error != null && error instanceof InvalidYoutubeLinkException) {
							Toast.makeText(sDashboard,  nameOfVideo
									+ ": " + getString(R.string.downloading) 
									+ "\n"+ getString(R.string.wait), 
									Toast.LENGTH_SHORT).show();
							
							JsonHelper.addEntryToJsonFile(
									String.valueOf(ID), 
									YTD.JSON_DATA_TYPE_V, 
									currentItem.getYtId(), 
									currentItem.getPos(),
									YTD.JSON_DATA_STATUS_PAUSED, 
									currentItem.getPath(), 
									nameOfVideo, 
									currentItem.getBasename(), 
									currentItem.getAudioExt(), 
									currentItem.getSize(), 
									false);
							
							reDownload(currentItem, "AUTO");
						} else {
							Toast.makeText(sDashboard,  nameOfVideo + ": " + getString(R.string.download_failed), 
									Toast.LENGTH_SHORT).show();
							
							JsonHelper.addEntryToJsonFile(
									String.valueOf(ID),  
									YTD.JSON_DATA_TYPE_V, 
									currentItem.getYtId(), 
									currentItem.getPos(),
									YTD.JSON_DATA_STATUS_PAUSED, 
									currentItem.getPath(), 
									nameOfVideo, 
									currentItem.getBasename(), 
									currentItem.getAudioExt(), 
									currentItem.getSize(), 
									false);
							
							if (DashboardActivity.isDashboardRunning)
								refreshlist();
							
							YTD.removeIdUpdateNotification(ID);
						}
					}
				};
				
				//TODO DM
				try {
					DownloadTask dt = new DownloadTask(this, itemIDlong, link, 
							currentItem.getFilename(), currentItem.getPath(), 
							currentItem.getAudioExt(), currentItem.getType(), 
							dtl, true);
					Maps.dtMap.put(itemIDlong, dt);
					dt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} catch (MalformedURLException e) {
					Log.e(DEBUG_TAG, "unable to start Download Manager -> " + e.getMessage());
				}
			} else {
				reDownload(currentItem, "AUTO");
			}
		}
		refreshlist();
	}
	
	private void reDownload(DashboardListItem currentItem, String category) {
		String ytLink = "http://www.youtube.com/watch?v=" + currentItem.getYtId();
		Intent rdIntent = new Intent(this, ShareActivity.class);
		rdIntent.setData(Uri.parse(ytLink));
		rdIntent.addCategory(category);
		rdIntent.putExtra("id", currentItem.getId());
		rdIntent.putExtra("position", currentItem.getPos());
		rdIntent.putExtra("filename", currentItem.getFilename());
		rdIntent.setAction(Intent.ACTION_VIEW);
		//rdIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(rdIntent);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dashboard, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch(item.getItemId()){
        	case R.id.menu_search:
    			if (!isSearchBarVisible) {
    				spawnSearchBar();
    			} else {
    				hideSearchBar();
    			}
    			return true;
        	case R.id.menu_settings:
        		Intent sIntent = new Intent(this, SettingsActivity.class);
        		sIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        		startActivity(sIntent);
    			return true;
        	case R.id.menu_backup:
        		String previousJson = JsonHelper.readJsonDashboardFile();
                boolean smtInProgressOrPaused = (previousJson.contains(YTD.JSON_DATA_STATUS_IN_PROGRESS) || 
        				 previousJson.contains(YTD.JSON_DATA_STATUS_PAUSED));
        		if (YTD.JSON_FILE.exists() && !previousJson.equals("{}\n") && !smtInProgressOrPaused) {
	        		boolean backupCheckboxEnabled = YTD.settings.getBoolean("dashboard_backup_info", true);
				    if (backupCheckboxEnabled == true) {
				    	
			        	AlertDialog.Builder adb = new AlertDialog.Builder(sDashboard);
			        	
			        	LayoutInflater adbInflater = LayoutInflater.from(DashboardActivity.this);
					    View showAgainView = adbInflater.inflate(R.layout.dialog_inflatable_checkbox, null);
					    final CheckBox showAgain = (CheckBox) showAgainView.findViewById(R.id.infl_cb);
					    showAgain.setChecked(true);
					    showAgain.setText(getString(R.string.show_again_checkbox));
					    adb.setView(showAgainView);
					    
			    		adb.setTitle(getString(R.string.information));
			    		adb.setMessage(getString(R.string.menu_backup_info));
			    		adb.setIcon(Utils.selectThemedInfoIcon());
			    		
			    		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
			    				if (!showAgain.isChecked()) {
					    			YTD.settings.edit().putBoolean("dashboard_backup_info", false).apply();
					    			Utils.logger("d", "dashboard backup info checkbox disabled", DEBUG_TAG);
					    		}
			    				launchFcForBackup();
			    			}
			    		});
			    		
			    		adb.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int which) {
			    				// cancel
			    			}
			    		});
			    		
			    		Utils.secureShowDialog(sDashboard, adb);
				    } else {
				    	launchFcForBackup();
				    }
	        	} else {
        			toastOpsNotExecuted();
        		}
        		return true;
        	case R.id.menu_restore:
        		String previousJson2 = JsonHelper.readJsonDashboardFile();
                boolean smtInProgressOrPaused2 = (previousJson2.contains(YTD.JSON_DATA_STATUS_IN_PROGRESS) || 
        				 previousJson2.contains(YTD.JSON_DATA_STATUS_PAUSED));
        		if (!smtInProgressOrPaused2) {
	        		boolean restoreCheckboxEnabled = YTD.settings.getBoolean("dashboard_restore_info", true);
				    if (restoreCheckboxEnabled == true) {
				    	
			        	AlertDialog.Builder adb = new AlertDialog.Builder(sDashboard);
			        	
			        	LayoutInflater adbInflater = LayoutInflater.from(DashboardActivity.this);
					    View showAgainView = adbInflater.inflate(R.layout.dialog_inflatable_checkbox, null);
					    final CheckBox showAgain = (CheckBox) showAgainView.findViewById(R.id.infl_cb);
					    showAgain.setChecked(true);
					    showAgain.setText(getString(R.string.show_again_checkbox));
					    adb.setView(showAgainView);
					    
			    		adb.setTitle(getString(R.string.information));
			    		adb.setMessage(getString(R.string.menu_restore_info) + ".\n" + getString(R.string.menu_restore_info_msg));
			    		adb.setIcon(Utils.selectThemedInfoIcon());
			    		
			    		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	
							public void onClick(DialogInterface dialog, int which) {
			    				if (!showAgain.isChecked()) {
					    			YTD.settings.edit().putBoolean("dashboard_restore_info", false).apply();
					    			Utils.logger("d", "dashboard restore info checkbox disabled", DEBUG_TAG);
					    		}
			    				launchFcForRestore();
			    			}
			    		});
			    		
			    		adb.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
			    			public void onClick(DialogInterface dialog, int which) {
			    				// cancel
			    			}
			    		});
			    		
			    		Utils.secureShowDialog(sDashboard, adb);
				    } else {
				    	launchFcForRestore();
				    }
        		} else {
        			toastOpsNotExecuted();
        		}
        		return true;
        	case R.id.menu_import:
        		boolean importCheckboxEnabled1 = YTD.settings.getBoolean("dashboard_import_info", true);
			    if (importCheckboxEnabled1 == true) {
			    	
		        	AlertDialog.Builder adb = new AlertDialog.Builder(sDashboard);
		        	
		        	LayoutInflater adbInflater = LayoutInflater.from(DashboardActivity.this);
				    View showAgainView = adbInflater.inflate(R.layout.dialog_inflatable_checkbox, null);
				    final CheckBox showAgain = (CheckBox) showAgainView.findViewById(R.id.infl_cb);
				    showAgain.setChecked(true);
				    showAgain.setText(getString(R.string.show_again_checkbox));
				    adb.setView(showAgainView);
				    
		    		adb.setTitle(getString(R.string.information));
		    		adb.setMessage(getString(R.string.menu_import_file_info));
		    		adb.setIcon(Utils.selectThemedInfoIcon());
		    		
		    		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
		    				if (!showAgain.isChecked()) {
				    			YTD.settings.edit().putBoolean("dashboard_import_info", false).apply();
				    			Utils.logger("d", "dashboard import info checkbox disabled", DEBUG_TAG);
				    		}
		    				launchFcForImport();
		    			}
		    		});
		    		
		    		adb.setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int which) {
		    				// cancel
		    			}
		    		});
		    		
		    		Utils.secureShowDialog(sDashboard, adb);
			    } else {
			    	launchFcForImport();
			    }
        		return true;
        	case R.id.menu_clear_dashboard:
        		DashboardClearHelper.confirmClearDashboard(DashboardActivity.this, true);
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }

	private void launchFcForBackup() {
		AlertDialog.Builder adb = new AlertDialog.Builder(DashboardActivity.this);
    	adb.setTitle(getString(R.string.rename_dialog_title));
		LayoutInflater adbInflater = LayoutInflater.from(DashboardActivity.this);
		View inputFilename = adbInflater.inflate(R.layout.dialog_input_filename_dashboard_backup, null);
		final EditText userFilename = (EditText) inputFilename.findViewById(R.id.input_backup_name);
		
		String date = new SimpleDateFormat("yyyy-MM-dd'_'HH-mm", Locale.US).format(new Date());
		userFilename.setText(date + "_" + YTD.JSON_FILENAME_NO_EXT);
		
		adb.setView(inputFilename);
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				Intent intent3 = new Intent(DashboardActivity.this,  FileChooserActivity.class);
				if (intent3 != null) {
					intent3.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
					intent3.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
					intent3.putExtra("BACKUP_NAME", userFilename.getText().toString());
					startActivityForResult(intent3, 3);
				}
			}
		});
		
		adb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// cancel
			}
		});
		
		Utils.secureShowDialog(sDashboard, adb);
	}

	private void launchFcForRestore() {
		Intent intent4 = new Intent(DashboardActivity.this,  FileChooserActivity.class);
		if (intent4 != null) {
			intent4.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
			intent4.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesOnly);
			startActivityForResult(intent4, 4);
		}
	}

	private void launchFcForImport() {
		Intent intent5 = new Intent(DashboardActivity.this,  FileChooserActivity.class);
		if (intent5 != null) {
			intent5.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
			intent5.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesOnly);
			intent5.putExtra(FileChooserActivity._MultiSelection, true);
			startActivityForResult(intent5, 5);
		}
	}
	
	@Override
    public void onResume(){
		super.onResume();
    	Utils.logger("v", "_onResume", DEBUG_TAG);
    	isDashboardRunning = true;
    	
    	refreshlist();
    	
    	/*
    	 * Timer() adapted from Stack Overflow:
    	 * http://stackoverflow.com/questions/3701106/periodically-refresh-reload-activity
    	 * 
    	 * Q: http://stackoverflow.com/users/446413/raffe
    	 * A: http://stackoverflow.com/users/244296/cristian
    	 */
    	autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
        	@Override
        	public void run() {
        		runOnUiThread(new Runnable() {
        			public void run() {
        				
        				int inProgressIndex = 0;
        				
        				for (int i = 0; i < statusEntries.size(); i++ ) {
        					if (statusEntries.get(i).equals(YTD.JSON_DATA_STATUS_IN_PROGRESS)) {
        						inProgressIndex++;
        					}
        				}
        				
        				if (inProgressIndex > 0) {
        					//Utils.logger("v", "refreshing...", DEBUG_TAG);
        					refreshlist();
        				}
        			}
        		});
        	}
        }, 500, 500);
    }
	
    @Override
    public void onPause() {
    	super.onPause();
    	Utils.logger("v", "_onPause", DEBUG_TAG);
    	isDashboardRunning = false;
    	
    	autoUpdate.cancel();
    }
	
	public class DelBundle {
		
		File mFile;
		DashboardListItem mItem;
		
		public DelBundle(File file, DashboardListItem item) {
			mFile = file;
			mItem = item;
		}
		public File getFile() {
			return mFile;
		}
		public DashboardListItem getItem() {
			return mItem;
		}
	}
	
	private class AsyncDelete extends AsyncTask<DashboardListItem, Void, Boolean> {

		File mFile;
		DashboardListItem mItem;

		@Override
		protected void onPreExecute() {
			dashboardAsyncTaskInProgress(sDashboard, true);
		}
		
		@Override
		protected Boolean doInBackground(DashboardListItem... item) {
			mItem = item[0];
			mFile = new File(mItem.getPath(), mItem.getFilename());
			return doDelete(mItem, mFile, true);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				notifyDeletionOk(mItem, mFile);
			} else {
				notifyDeletionUnsuccessful(mItem, mFile);
			}
			dashboardAsyncTaskInProgress(sDashboard, false);
		}
	}
	
	private boolean doDelete(final DashboardListItem item, File fileToDel, boolean removeFromJsonAlso) {
		Utils.logger("v", "----------> BEGIN delete", DEBUG_TAG);
		boolean isResultOk = false;
		long id = Long.parseLong(item.getId());

		if (item.getStatus().equals(getString(R.string.json_status_in_progress))) {
			// stop download, remove temp file and update notification
			try {
				if (Maps.dtMap.containsKey(id)) {
					DownloadTask dt = Maps.dtMap.get(id);
					dt.cancel();
				} else {
					if (Maps.dtMap.size() > 0) {
						// cancel (pause) every dt found
						Utils.logger("w", "doDelete: id not found into 'dtMap'; canceling all tasks", DEBUG_TAG);
						for (Iterator<DownloadTask> iterator = Maps.dtMap.values().iterator(); iterator.hasNext();) {
							DownloadTask dt = (DownloadTask) iterator.next();
							dt.cancel();
						}
					}
				}
				
				isResultOk = removeTemp(fileToDel, id);
			} catch (NullPointerException e) {
				Log.e(DEBUG_TAG, "dt.cancel(): " + e.getMessage());
			}
		} else if (item.getStatus().equals(getString(R.string.json_status_paused))) {
			isResultOk = removeTemp(fileToDel, id);
		} else {
			// remove file and library reference
			isResultOk = removeCompleted(fileToDel);
		}
		
		if (removeFromJsonAlso/* && isResultOk*/) {
			// remove entry from JSON and reload Dashboard
			JsonHelper.removeEntryFromJsonFile(item.getId());
		}
		
		refreshlist();
		Utils.logger("v", "----------> END delete", DEBUG_TAG);
		
		return isResultOk;
	}

	private boolean removeTemp(File fileToDel, long id) {
		// update notification
		YTD.removeIdUpdateNotification(id);
		
		//remove YouTube link from prefs
		YTD.videoinfo.edit().remove(String.valueOf(id) + "_link").apply();
		
		// delete temp file
		File temp = new File(fileToDel.getAbsolutePath() + DownloadTask.TEMP_SUFFIX);
		if (temp.exists()) {
			return (temp.delete()) ? true : false;
		} else {
			return true;
		}
	}

	public boolean removeCompleted(File fileToDel) {
		// remove file
		if (fileToDel.exists() && fileToDel.delete()) {
			// remove library reference
			String mediaUriString;
			try {
				mediaUriString = Utils.getContentUriFromFile(fileToDel, getContentResolver());
				Utils.removeFromMediaStore(sDashboard, fileToDel, mediaUriString);
			} catch (NullPointerException e) {
				Utils.logger("w", fileToDel.getName() + " UriString NOT found", DEBUG_TAG);
			}
			return true;
		} else {
			return false;
		}
	}

	public void notifyDeletionUnsuccessful(final DashboardListItem currentItem, File fileToDel) {
		Utils.logger("w", fileToDel.getAbsolutePath() + " NOT deleted.", DEBUG_TAG);
		Toast.makeText(DashboardActivity.this, 
				getString(R.string.delete_video_failed, currentItem.getFilename()), 
				Toast.LENGTH_SHORT).show();
	}

	public void notifyDeletionOk(final DashboardListItem currentItem, File fileToDel) {
		Utils.logger("d", fileToDel.getAbsolutePath() + " successfully deleted.", DEBUG_TAG);
		Toast.makeText(DashboardActivity.this, 
				getString(R.string.delete_video_ok, currentItem.getFilename()), 
				Toast.LENGTH_SHORT).show();
	}

	public void spawnSearchBar() {
		Utils.logger("d", "showing searchbar...", DEBUG_TAG);
		
		EditText inputSearch = new EditText(DashboardActivity.this);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		inputSearch.setLayoutParams(layoutParams);
		
		if (TextUtils.isEmpty(searchText)) {
			inputSearch.setHint(R.string.menu_search);
		} else {
			inputSearch.setText(searchText);
		}
		
		inputSearch.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
		inputSearch.setSingleLine();
		inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		inputSearch.setId(999);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.dashboard);
		layout.addView(inputSearch, 0);
		isSearchBarVisible = true;
		
    	inputSearch.addTextChangedListener(new TextWatcher() {
        
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Utils.logger("d", "Text ["+s+"] - Start ["+start+"] - Before ["+before+"] - Count ["+count+"]", DEBUG_TAG);
				
				if (count < before) da.resetData();
				da.getFilter().filter(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
    	});
	}
	
	public void hideSearchBar() {
		Utils.logger("d", "hiding searchbar...", DEBUG_TAG); 
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.dashboard);
		EditText inputSearch = (EditText) findViewById(999);
		
		// hide keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
		
		// store text and remove EditText
		searchText = inputSearch.getEditableText();
		layout.removeView(inputSearch);
		
		Utils.reload(DashboardActivity.this);
		
		isSearchBarVisible = false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
            @SuppressWarnings("unchecked")
			List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
            
            String path = data.getStringExtra("PATH");
            String name = data.getStringExtra("NAME");
            	
        	final File chooserSelection = files.get(0);

        	//Utils.logger("d", "file-chooser selection: " + chooserFolder.getAbsolutePath(), DEBUG_TAG);
        	//Utils.logger("d", "origin file's folder:   " + currentItem.getAbsolutePath(), DEBUG_TAG);
			
	        switch (requestCode) {
	        
	        case 1: // ------------- > COPY
	        	File out1 = new File(chooserSelection, name);
	        	File in1 = new File(path, name);
				
	        	if (chooserSelection.getAbsolutePath().equals(currentItem.getPath())) {
	        		out1 = new File(chooserSelection, "copy_" + currentItem.getFilename());
	        	}

    			if (!out1.exists()) {
		        	switch (Utils.pathCheck(chooserSelection)) {
		    		case 0:
		    			// Path on standard sdcard
		    			new AsyncCopy().execute(in1, out1);
		        		break;
		    		case 1:
		    			// Path not writable
		    			PopUps.showPopUp(getString(R.string.system_warning_title), 
		    					getString(R.string.system_warning_msg), "error", DashboardActivity.this);
		    			break;
		    		case 2:
		    			// Path not mounted
		    			Toast.makeText(DashboardActivity.this, 
		    					getString(R.string.sdcard_unmounted_warning), 
		    					Toast.LENGTH_SHORT).show();
		        	}
    			} else {
	        		PopUps.showPopUp(getString(R.string.long_press_warning_title), 
	        				getString(R.string.long_press_warning_msg2), "status", DashboardActivity.this);
	        	}
    			break;
    			
	        case 2: // ------------- > MOVE
				File out2 = new File(chooserSelection, name);
				File in2 = new File(path, name);
				
	        	if (!chooserSelection.getAbsolutePath().equals(currentItem.getPath())) {
	        		if (!out2.exists()) {
			        	switch (Utils.pathCheck(chooserSelection)) {
			    		case 0:
			    			// Path on standard sdcard
			    			new AsyncMove().execute(in2, out2);		
			        		break;
			    		case 1:
			    			// Path not writable
			    			PopUps.showPopUp(getString(R.string.system_warning_title), 
			    					getString(R.string.system_warning_msg), "error", DashboardActivity.this);
			    			break;
			    		case 2:
			    			// Path not mounted
			    			Toast.makeText(DashboardActivity.this, 
			    					getString(R.string.sdcard_unmounted_warning), 
			    					Toast.LENGTH_SHORT).show();
			        	}
	        		} else {
		        		PopUps.showPopUp(getString(R.string.long_press_warning_title), 
		        				getString(R.string.long_press_warning_msg2), "status", DashboardActivity.this);
		        	}
	        	} else {
	        		PopUps.showPopUp(getString(R.string.long_press_warning_title), 
	        				getString(R.string.long_press_warning_msg), "status", DashboardActivity.this);
	        	}
	        	break;
	        	
	        case 3: // ------------- > MENU_BACKUP
	        	final String backupName = data.getStringExtra("BACKUP_NAME");
				new Thread(new Runnable() {
    				@Override
    				public void run() {
    					Looper.prepare();
    					
						final File backup = new File(chooserSelection, backupName + YTD.JSON_FILENAME_EXT_ONLY);
			        	
    					//TODO overwrite check
						try {
							Utils.copyFile(YTD.JSON_FILE, backup);
							Toast.makeText(sDashboard, 
									getString(R.string.menu_backup_result_ok), 
									Toast.LENGTH_SHORT).show();
							Utils.logger("d", "Dashboard backup result ok", DEBUG_TAG);
						} catch (IOException e) {
							Log.e(DEBUG_TAG, "IOException @ MENU_BACKUP: " + e.getMessage());
							Toast.makeText(sDashboard, 
									getString(R.string.menu_backup_result_failed), 
									Toast.LENGTH_SHORT).show();
						}
			        	
			        	Looper.loop();
    				}
    			}).start();
		        	
	        	break;
	        	
	        case 4: // ------------- > MENU_RESTORE
				AsyncRestore ar = new AsyncRestore();
				ar.execute(chooserSelection);
	        	break;
	        	
	        case 5: // ------------- > MENU_IMPORT	        	
	        	for (int i = 0; i < files.size(); i++) {	
	        		AsyncImport ai = new AsyncImport();
	        		
	        		ai.setI(i+1);
	        		ai.setTot(files.size());
	        		
	        		if (i == 0) {
	        			ai.setImportStart(true);
	        		} else {
	        			ai.setImportStart(false);
	        		}
	        		
	        		if (i+1 == files.size()) {
						ai.setImportEnd(true);
					} else {
						ai.setImportEnd(false);
	        		}
					
					ai.execute(files.get(i));
				}
	        }
		}
    }

	private class AsyncImport extends AsyncTask<File, Void, String> {
		
		boolean importStart;
		boolean importEnd;
		int i;
		int tot;
		String filename;
		
		protected void setImportStart(boolean v) {
			importStart = v;
		}
		
		protected void setImportEnd(boolean v) {
			importEnd = v;
		}
		
		protected void setI(int n) {
			i = n;
		}
		
		protected void setTot(int n) {
			tot = n;
		}
		
		@Override
		protected void onPreExecute() {
			if (importStart)
				dashboardAsyncTaskInProgress(DashboardActivity.this, true);
		}

		@Override
		protected String doInBackground(File... params) {
			Utils.logger("i", "import @ " + i + "/" + tot, DEBUG_TAG);

			File chooserSelection = params[0];
			String previousJson = JsonHelper.readJsonDashboardFile();
			filename = chooserSelection.getName();
        	
			if (previousJson.contains(filename)) {
				return "e1";
			} else {
				String id = String.valueOf(System.currentTimeMillis());
	        	String type = YTD.JSON_DATA_TYPE_V;
	        	String path = chooserSelection.getParent();
	        	String basename = Utils.getFileNameWithoutExt(filename);
	        	String size = Utils.MakeSizeHumanReadable((int) chooserSelection.length(), false);
	        	
	        	String ext = Utils.getExtFromFileName(filename).toUpperCase(Locale.ENGLISH);
	        	String aExt = "";
	        	boolean doImport = false;
	        	
	        	if (ext.equals("WEBM")) {
	        		aExt = ".ogg";
	        		doImport = true;
	        	} else if (ext.equals("MP4") || ext.equals("3GP")) {
	        		aExt = ".aac";
	        		doImport = true;
	        	} else if (ext.equals("FLV")) {
	        		aExt = "x";
	        		doImport = true;
	        	} else if (ext.equals("M4A") || ext.equals("OGG") || ext.equals("MP3")) {
	        		type = YTD.JSON_DATA_TYPE_A_E;
	        		doImport = true;
	        	} else {
	        		doImport = false;
	        	}
	        	
	        	if (doImport) {
	        		if (type == YTD.JSON_DATA_TYPE_V)
	        			writeThumbToDiskForSelectedFile(chooserSelection, id);
	        		
					JsonHelper.addEntryToJsonFile(
							id, 
							type, 
							id,
							-1,
							YTD.JSON_DATA_STATUS_IMPORTED, 
							path, 
							filename,
							basename, 
							aExt, 
							size, 
							false);
					
					return filename;
				} else {
					return "e2";
				}
			}
		}
		
		@Override
		protected void onPostExecute(String res) {
			String ratio = i + " / " + tot;
			status.setText(getString(R.string.menu_import_in_progress, ratio));
			
			if (res.equals("e1")) {
				Toast.makeText(DashboardActivity.this, 
						getString(R.string.menu_import_file_double, filename), 
						Toast.LENGTH_SHORT).show();
			} else if (res.equals("e2")) {
				Toast.makeText(DashboardActivity.this, 
						getString(R.string.unsupported_operation), 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DashboardActivity.this, 
						res + " " + getString(R.string.json_status_imported), 
						Toast.LENGTH_SHORT).show();
			}
			
			if (importEnd) {
				dashboardAsyncTaskInProgress(DashboardActivity.this, false);
				Utils.reload(DashboardActivity.this);
			}
		}
	}
	
	private class AsyncRestore extends AsyncTask<File, Void, String[]> {
		
		@Override
		protected void onPreExecute() {
			dashboardAsyncTaskInProgress(DashboardActivity.this, true);
		}

		@Override
		protected String[] doInBackground(File... params) {
			File chooserSelection = params[0];
			if (Utils.getExtFromFileName(chooserSelection.getName()).equals("json")) {
				Utils.logger("v", "Restore: ext \".json\" found", DEBUG_TAG);
				try {
					// copy file
					Utils.copyFile(chooserSelection, YTD.JSON_FILE);
					
					// validate the JSON file 
					String previousJson = JsonHelper.readJsonDashboardFile();
					new JSONObject(previousJson);
					
					int total = parseJsonEntriesNumber();

					int removed = checkIfFilesExist();
					
					int imported = total - removed;
					Utils.logger("d","----- RESTORE: -----" +
									 "\nimported: " + imported +
									 "\nremoved: " + removed +
									 "\n--------------------" +
									 "\ntotal: " + total +
									 "\n--------------------", DEBUG_TAG);
					
					return new String[] { String.valueOf(imported), String.valueOf(total) };
					
				} catch (Exception e) {
					Log.e(DEBUG_TAG, "Exception @ AsyncRestore: " + e.getMessage());
					return new String[] { "e1" };
				}
			} else {
				return new String[] { "e2" };
			}
		}
		
		private int checkIfFilesExist() {
			int n = 0;
			String previousJson = JsonHelper.readJsonDashboardFile();
			
//			List<File> dashboardFilesList = new ArrayList<File>();
//			Map<File, String> dashboardIdsMap = new HashMap<File, String>();
			
			JSONObject jV = null;
			String jId = null;
			
			try {
				jV = new JSONObject(previousJson);

				@SuppressWarnings("unchecked")
				Iterator<Object> ids = jV.keys();
				while (ids.hasNext()) {
					jId = (String) ids.next();
					JSONObject jO = new JSONObject();
					jO = jV.getJSONObject(jId);
					
					final String jYtId = jO.getString(YTD.JSON_DATA_YTID);
					final String jPath = jO.getString(YTD.JSON_DATA_PATH);
					final String jFilename = jO.getString(YTD.JSON_DATA_FILENAME);
					final File jFile = new File(jPath, jFilename);
					
					if (jFile.exists()) {
						writeThumbToDiskForSelectedFile(jFile, jYtId);
					} else {
						n++;
						Utils.logger("w", "Removing (file not found): " + jFile.getName(), DEBUG_TAG);
						JsonHelper.removeEntryFromJsonFile(jId);
					}
				}
			} catch (JSONException e) {
				Log.e(DEBUG_TAG, "JSONException @ checkIfFilesExist: ");
				e.printStackTrace();
				Toast.makeText(YTD.ctx,
						YTD.ctx.getString(R.string.invalid_data),
						Toast.LENGTH_SHORT).show();
				YTD.JSON_FILE.delete();
			}
			return n;
		}

		@Override
		protected void onPostExecute(String[] res) {
			if (res[0].equals("e1")) {
				//JSONException e1
				Toast.makeText(sDashboard, 
						sDashboard.getString(R.string.menu_restore_result_failed), 
						Toast.LENGTH_SHORT).show();
			} else if (res[0].equals("e2")) {
				//file ext not .json
				Toast.makeText(sDashboard, 
						sDashboard.getString(R.string.invalid_data), 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(sDashboard, 
						getString(R.string.menu_restore_result_ok) + " (" + res[0] + "/" + res[1] + ")", 
						Toast.LENGTH_SHORT).show();
			}
			
			dashboardAsyncTaskInProgress(DashboardActivity.this, false);
			Utils.reload(DashboardActivity.this);
		}
	}

	private void writeThumbToDiskForSelectedFile(final File selectedFile, String pngBasename) {
		File bmFile = new File(getDir(YTD.THUMBS_FOLDER, 0), pngBasename + ".png");
		
		if (!Utils.getExtFromFileName(selectedFile.getAbsolutePath()).toUpperCase(Locale.ENGLISH).equals("FLV")) {
			try {
				Utils.logger("d", "trying to write thumbnail for " + selectedFile.getName() + " -> " + pngBasename, DEBUG_TAG);
				FileOutputStream out = new FileOutputStream(bmFile);
				
				Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedFile.getAbsolutePath(), Thumbnails.MINI_KIND);
				bmThumbnail.compress(Bitmap.CompressFormat.PNG, 90, out);
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "writeThumbToDiskForSelectedFile -> " + e.getMessage());
			}
		} else {
			if (!bmFile.exists()) {
				File ffmpeg = new File(getDir("bin", 0), YTD.ffmpegBinName);
				if (ffmpeg.exists())
					YTD.queueThread.enqueueTask(
							new FFmpegExtractFlvThumbTask(sDashboard, selectedFile, bmFile), 
							YTD._THUMB_EXTR);
			}
		}
	}
	
	private class AsyncMove extends AsyncTask<File, Void, Integer> {
		
		File out;
		private boolean delResOk;
		
		protected void onPreExecute() {
			dashboardAsyncTaskInProgress(sDashboard, true);
			Utils.logger("d", currentItem.getFilename() + " ---> BEGIN move", DEBUG_TAG);
			Toast.makeText(DashboardActivity.this, 
					currentItem.getFilename() + ": " + getString(R.string.move_progress), 
					Toast.LENGTH_SHORT).show();
		}
		
		protected Integer doInBackground(File... file) {
			out = file[1];
			try {
				Utils.copyFile(file[0], file[1]);
				delResOk = doDelete(currentItem, file[0], false);
				return 0;
			} catch (IOException e) {
				return 1;
			}
		}
		
		@Override
		protected void onPostExecute(Integer res) {
			switch (res) {
			case 0:
				Toast.makeText(DashboardActivity.this, 
						currentItem.getFilename() + ": " + getString(R.string.move_ok), 
						Toast.LENGTH_SHORT).show();
				Utils.logger("i", currentItem.getFilename() + " --> END move: OK", DEBUG_TAG);
				
				Utils.scanMedia(DashboardActivity.this, 
						new String[]{ out.getAbsolutePath() }, 
						new String[]{ "video/*" });
				
				JsonHelper.addEntryToJsonFile(
						currentItem.getId(), 
						currentItem.getType(), 
						currentItem.getYtId(), 
						currentItem.getPos(),
						currentItem.getStatus(), 
						out.getParent(), 
						out.getName(), 
						currentItem.getBasename(), 
						currentItem.getAudioExt(), 
						currentItem.getSize(), 
						false);
				break;
				
			case 1:
				Toast.makeText(DashboardActivity.this, 
						currentItem.getFilename() + ": " + getString(R.string.move_error), 
						Toast.LENGTH_SHORT).show();
				Log.e(DEBUG_TAG, currentItem.getFilename() + " --> END move: FAILED");
			}
			
			refreshlist();
		
			if (!delResOk) {
				Utils.logger("w", currentItem.getFilename() + " --> Copy OK (but not Deletion: original file still in place)", DEBUG_TAG);
			}
			
			dashboardAsyncTaskInProgress(sDashboard, false);
		}
	}
	
	private class AsyncCopy extends AsyncTask<File, Void, Integer> {
		
		File out;
		
		protected void onPreExecute() {
			dashboardAsyncTaskInProgress(sDashboard, true);
			Utils.logger("d", currentItem.getFilename() + " ---> BEGIN copy", DEBUG_TAG);
			Toast.makeText(DashboardActivity.this, 
					currentItem.getFilename() + ": " + getString(R.string.copy_progress), 
					Toast.LENGTH_SHORT).show();
		}
		
		protected Integer doInBackground(File... file) {
			out = file[1];
			try {
				Utils.copyFile(file[0], file[1]);
				return 0;
			} catch (IOException e) {
				return 1;
			}
		}
		
		@Override
		protected void onPostExecute(Integer res) {
			switch (res) {
			case 0:
				Toast.makeText(DashboardActivity.this, 
						currentItem.getFilename() + ": " + getString(R.string.copy_ok), 
						Toast.LENGTH_SHORT).show();
				Utils.logger("i", currentItem.getFilename() + " --> END copy: OK", DEBUG_TAG);
				
				Utils.scanMedia(DashboardActivity.this, 
						new String[]{ out.getAbsolutePath() }, 
						new String[]{ "video/*" });
				
				JsonHelper.addEntryToJsonFile(
						currentItem.getId(), 
						currentItem.getType(), 
						currentItem.getYtId(), 
						currentItem.getPos(),
						currentItem.getStatus(), 
						out.getParent(), 
						out.getName(), 
						currentItem.getBasename(), 
						currentItem.getAudioExt(), 
						currentItem.getSize(), 
						true);
				break;
			
			case 1:
				Toast.makeText(DashboardActivity.this, 
						currentItem.getFilename() + ": " + getString(R.string.copy_error), 
						Toast.LENGTH_SHORT).show();
				Log.e(DEBUG_TAG, currentItem.getFilename() + " --> END copy: FAILED");
			}
			
			refreshlist();
			dashboardAsyncTaskInProgress(sDashboard, false);
		}
	}
	
	public static void refreshlist() {
		if (isDashboardRunning) {
			sDashboard.runOnUiThread(new Runnable() {
				public void run() {

					clearAdapterAndLists();

					// refill the Lists and re-populate the adapter
					/*entries = */parseJson();
					updateProgressBars();
					buildList();

					writeStatus();

					// refresh the list view
					da.notifyDataSetChanged();
				}
			});
		}
	}
	
	public static void clearAdapterAndLists() {
		// clear the adapter
		if (isDashboardRunning) {
			sDashboard.runOnUiThread(new Runnable() {
				public void run() {
					da.clear();
	
					//entries = 0;
					
					// empty the Lists
					idEntries.clear();
					typeEntries.clear();
					ytidEntries.clear();
					posEntries.clear();
					statusEntries.clear();
					pathEntries.clear();
					filenameEntries.clear();
					basenameEntries.clear();
					audioExtEntries.clear();
					sizeEntries.clear();
					partSizeEntries.clear();
					progressEntries.clear();
					speedEntries.clear();
				}
			});
		}
	}
	
	/*private void clearLists() {
		entries = 0;
		
		// empty the Lists
		idEntries.clear();
		typeEntries.clear();
		ytidEntries.clear();
		posEntries.clear();
		statusEntries.clear();
		pathEntries.clear();
		filenameEntries.clear();
		basenameEntries.clear();
		audioExtEntries.clear();
		sizeEntries.clear();
		partSizeEntries.clear();
		progressEntries.clear();
		speedEntries.clear();
	}*/
	
	private static int parseJsonEntriesNumber() {
		// read existing/init new JSON 
		String previousJson = JsonHelper.readJsonDashboardFile();
		int n = 0;
		JSONObject jV = null;
		try {
			jV = new JSONObject(previousJson);
			n = jV.length();
			//Utils.logger("v", "Json entries found: " + n, DEBUG_TAG);
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, "JSONException @ parseJsonEntriesNumber: " + e.getMessage());
		}
		
		return n;
	}
	
	private static int parseJson() {
		// read existing/init new JSON 
		String previousJson = JsonHelper.readJsonDashboardFile();
				
		JSONObject jV = null;
		try {
			jV = new JSONObject(previousJson);
			//Utils.logger("v", "current json:\n" + previousJson, DEBUG_TAG);
			@SuppressWarnings("unchecked")
			Iterator<Object> ids = jV.keys();
			while (ids.hasNext()) {
				String id = (String) ids.next();
				JSONObject jO = new JSONObject();
				jO = jV.getJSONObject(id);
				idEntries.add(id);
				typeEntries.add(jO.getString(YTD.JSON_DATA_TYPE));
				ytidEntries.add(jO.getString(YTD.JSON_DATA_YTID));
				posEntries.add(jO.getInt(YTD.JSON_DATA_POS));
				statusEntries.add(jO.getString(YTD.JSON_DATA_STATUS));
				pathEntries.add(jO.getString(YTD.JSON_DATA_PATH));
				filenameEntries.add(jO.getString(YTD.JSON_DATA_FILENAME));
				basenameEntries.add(jO.getString(YTD.JSON_DATA_BASENAME));
				audioExtEntries.add(jO.getString(YTD.JSON_DATA_AUDIO_EXT));
				sizeEntries.add(jO.getString(YTD.JSON_DATA_SIZE));
			}
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, "JSONException @ parseJson: " + e.getMessage());
			Toast.makeText(sDashboard, 
					sDashboard.getString(R.string.invalid_data), 
					Toast.LENGTH_SHORT).show();
			//YTD.JSON_FILE.delete();
		}
		
		// do sort by filenames
		List<String> oldFilenameEntries = new ArrayList<String>(filenameEntries);
		List<String> oldIdEntries = new ArrayList<String>(idEntries);
		List<String> oldTypeEntries = new ArrayList<String>(typeEntries);
		List<String> oldLinkEntries = new ArrayList<String>(ytidEntries);
		List<Integer> oldPosEntries = new ArrayList<Integer>(posEntries);
		List<String> oldStatusEntries = new ArrayList<String>(statusEntries);
		List<String> oldPathEntries = new ArrayList<String>(pathEntries);
		List<String> oldBasenameEntries = new ArrayList<String>(basenameEntries);
		List<String> oldAudioExtEntries = new ArrayList<String>(audioExtEntries);
		List<String> oldSizeEntries = new ArrayList<String>(sizeEntries);
		
		idEntries.clear();
		typeEntries.clear();
		ytidEntries.clear();
		posEntries.clear();
		statusEntries.clear();
		pathEntries.clear();
		basenameEntries.clear();
		audioExtEntries.clear();
		sizeEntries.clear();

		Collections.sort(filenameEntries, String.CASE_INSENSITIVE_ORDER);
		
		for (int i = 0; i < filenameEntries.size(); i++ ) {
			for (int j = 0; j < oldFilenameEntries.size(); j++ ) {
				if (oldFilenameEntries.get(j) == filenameEntries.get(i)) {
					idEntries.add(oldIdEntries.get(j));
					typeEntries.add(oldTypeEntries.get(j));
					ytidEntries.add(oldLinkEntries.get(j));
					posEntries.add(oldPosEntries.get(j));
					statusEntries.add(oldStatusEntries.get(j));
					pathEntries.add(oldPathEntries.get(j));
					basenameEntries.add(oldBasenameEntries.get(j));
					audioExtEntries.add(oldAudioExtEntries.get(j));
					sizeEntries.add(oldSizeEntries.get(j));
				}
			}
		}
		return idEntries.size();
	}
	
	//TODO updateProgressBars
	private static void updateProgressBars() {
		entriesInProgress = 0;
		for (int i = 0; i < idEntries.size(); i++ ) {
			try {
				if (statusEntries.get(i).equals(YTD.JSON_DATA_STATUS_IN_PROGRESS)) {
					entriesInProgress++;
					
					String idstr = idEntries.get(i);
					long idlong = Long.parseLong(idstr);
					long bytes_downloaded = 0;
					long bytes_total = 0;
					long progress = 0;
					long speed = 0;

					if (typeEntries.get(i).equals(YTD.JSON_DATA_TYPE_V) || 
						typeEntries.get(i).equals(YTD.JSON_DATA_TYPE_V_O) ||
						typeEntries.get(i).equals(YTD.JSON_DATA_TYPE_A_O)) { 
						//JSON DATA types for downloads
						try {
							if (Maps.mDownloadPercentMap.get(idlong) != null) {
								bytes_downloaded = Maps.mDownloadSizeMap.get(idlong);
								bytes_total = Maps.mTotalSizeMap.get(idlong);
								progress = Maps.mDownloadPercentMap.get(idlong);
								speed = Maps.mNetworkSpeedMap.get(idlong);
							} else {
								countdown--;
								Utils.logger("w", "updateProgressBars: waiting " + idstr + " # " + countdown, DEBUG_TAG);
								progress = -1;
								
								DownloadTask dt = Maps.dtMap.get(idlong);
								
								if (countdown <= 0 && dt == null) {
									Utils.logger("w", "countdown == 0 && dt == null; "
											+ "\nsetting STATUS_PAUSED on (video) id " + idstr, DEBUG_TAG);
									JsonHelper.addEntryToJsonFile(
											idstr, 
											typeEntries.get(i),
											ytidEntries.get(i), 
											posEntries.get(i),
											YTD.JSON_DATA_STATUS_PAUSED,
											pathEntries.get(i), 
											filenameEntries.get(i),
											basenameEntries.get(i), 
											audioExtEntries.get(i),
											sizeEntries.get(i), 
											false);
								}
							}
						} catch (NullPointerException e) {
							Log.e(DEBUG_TAG, "NPE @ updateProgressBars (DM)");
						}
						
						String readableBytesDownloaded = Utils.MakeSizeHumanReadable(bytes_downloaded, false);
						String readableBytesTotal = Utils.MakeSizeHumanReadable(bytes_total, false);
						
						String progressRatio;
						if (readableBytesTotal.equals("-")) {
							progressRatio = "";
						} else {
							progressRatio = readableBytesDownloaded + "/" + readableBytesTotal;
						}
	
						progressEntries.add(i, progress);
						partSizeEntries.add(i, progressRatio + " (" + String.valueOf(progress) + "%)");
						speedEntries.add(i, speed);
					} else {
						// JSON DATA types for FFmpeg output files:
						// YTD.JSON_DATA_TYPE_A_E
						// YTD.JSON_DATA_TYPE_A_M
						// YTD.JSON_DATA_TYPE_V_M
						try {
							if (YTD.mFFmpegPercentMap.get(idlong) != null) {
								progress = (int) YTD.mFFmpegPercentMap.get(idlong);
							} else {
								Utils.logger("w", "updateProgressBars: waiting " + idstr + " # " + countdown, DEBUG_TAG);
								progress = -1;
							}
						} catch (NullPointerException e) {
							Log.e(DEBUG_TAG, "NPE @ updateProgressBars (FFmpeg)");
						}

						progressEntries.add(i, progress);
						if (progress != -1) {
							partSizeEntries.add(i, String.valueOf(progress) + "%");
						} else {
							partSizeEntries.add(i, "");
						}
						speedEntries.add(i, (long) 0);
					}
				} else {
					progressEntries.add(i, (long) 100);
					partSizeEntries.add(i, "-/-");
					speedEntries.add(i, (long) 0);
				}
			} catch (IndexOutOfBoundsException e) {
				Utils.logger("w", "updateProgressBars: " + e.getMessage(), DEBUG_TAG);
			}
		}
	}
	
	private static void buildList() {
		for (int i = 0; i < idEntries.size(); i++) {
			String thisSize;
			try {
				if (statusEntries.get(i).equals(YTD.JSON_DATA_STATUS_IN_PROGRESS) && 
						(speedEntries.get(i) != 0 || progressEntries.get(i) != -1)) {
					thisSize = partSizeEntries.get(i);
				} else {
					thisSize = sizeEntries.get(i);
				}

				itemsList.add(new DashboardListItem(
						idEntries.get(i),
						typeEntries.get(i),
						ytidEntries.get(i), 
						posEntries.get(i), 
						statusEntries.get(i)
							.replace(YTD.JSON_DATA_STATUS_COMPLETED, sDashboard.getString(R.string.json_status_completed))
							.replace(YTD.JSON_DATA_STATUS_IN_PROGRESS, sDashboard.getString(R.string.json_status_in_progress))
							.replace(YTD.JSON_DATA_STATUS_FAILED, sDashboard.getString(R.string.json_status_failed))
							.replace(YTD.JSON_DATA_STATUS_IMPORTED, sDashboard.getString(R.string.json_status_imported))
							.replace(YTD.JSON_DATA_STATUS_PAUSED, sDashboard.getString(R.string.json_status_paused))
							.replace(YTD.JSON_DATA_STATUS_QUEUED, sDashboard.getString(R.string.json_status_queued)),
						pathEntries.get(i), 
						filenameEntries.get(i), 
						basenameEntries.get(i),
						audioExtEntries.get(i), 
						thisSize, 
						progressEntries.get(i),
						speedEntries.get(i)));
			} catch (IndexOutOfBoundsException e) {
				Utils.logger("w", "buildList: " + e.getMessage(), DEBUG_TAG);
			}
		}
	}
	
	private static void writeStatus() {
		if (da.isEmpty()) {
			status.setText(R.string.empty_dashboard);
			//bkgRl.setVisibility(View.VISIBLE);
			if (isLandscape) {
				Picasso.with(sDashboard).load(R.drawable.ic_bkg_gray).resize(256, 256).into(bkgImg);
			} else {
				Picasso.with(sDashboard).load(R.drawable.ic_bkg_gray).resize(512, 512).into(bkgImg);
			}
		} else {
			String text = "";
			if (entriesInProgress > 0) {
				text = String.format(sDashboard.getString(R.string.status_text_in_progress), 
						da.getCount(), 
						entriesInProgress);
			} else {
				text = String.format(sDashboard.getString(R.string.status_text), 
						da.getCount());
			}
			status.setText(text);
			//bkgRl.setVisibility(View.GONE);
		}
	}

	// #####################################################################
	
	public void editId3Tags(View view) {
		if (newClick) {
			tagArtist = "";
			tagAlbum = "";
			tagTitle = "";
			tagGenre = "";
			tagYear = "";
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(sDashboard);
	    LayoutInflater inflater0 = getLayoutInflater();
	    final View id3s = inflater0.inflate(R.layout.dialog_edit_id3, null);
	    
	    final EditText artistEt = (EditText) id3s.findViewById(R.id.id3_et_artist);
	    final EditText titleEt = (EditText) id3s.findViewById(R.id.id3_et_title);	
	    final EditText albumEt = (EditText) id3s.findViewById(R.id.id3_et_album);
	    final EditText genreEt = (EditText) id3s.findViewById(R.id.id3_et_genre);
	    final EditText yearEt = (EditText) id3s.findViewById(R.id.id3_et_year);
	    
	    if (tagTitle.isEmpty()) {
			titleEt.setText(currentItem.getBasename());
		} else {
			titleEt.setText(tagTitle);
		}
	    
	    if (tagYear.isEmpty()) {
			Calendar cal = new GregorianCalendar();
			int y = cal.get(Calendar.YEAR);
			yearEt.setText(String.valueOf(y));
		} else {
			yearEt.setText(tagYear);
		}
	    
		artistEt.setText(tagArtist);
		albumEt.setText(tagAlbum);
		genreEt.setText(tagGenre);
	    
		builder.setView(id3s)
	           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   tagArtist = artistEt.getText().toString();
	            	   tagAlbum = albumEt.getText().toString();
	            	   tagTitle = titleEt.getText().toString();
	            	   tagGenre = genreEt.getText().toString();
	            	   tagYear = yearEt.getText().toString();
	               }
	           })
	           .setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   // cancel
	               }
	           });      
	    
	    Utils.secureShowDialog(sDashboard, builder);
	    newClick = false;
	}

	public void ffmpegJob(final File fileToConvert, final String bitrateType, final String bitrateValue) {
		isFfmpegRunning = true;
		
		vfilename = currentItem.getFilename();
		
		String aExt = currentItem.getAudioExt();
		basename = currentItem.getBasename();
		
		final String audioFileName;
		// "compose" the audio file
		if (bitrateValue != null) {
			extrTypeIsMp3Conv = true;
			audioFileName = basename + "_" + bitrateType + "-" + bitrateValue + ".mp3";
		} else {
			extrTypeIsMp3Conv = false;
			audioFileName = basename + aExt;
			
		}
		
		audioFile = new File(fileToConvert.getParent(), audioFileName);
		
		aNewId = System.currentTimeMillis();

		if (!audioFile.exists() || audioFile.length() == 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();

					FfmpegController ffmpeg = null;
					try {
						ffmpeg = new FfmpegController(DashboardActivity.this);
					} catch (IOException ioe) {
						Log.e(DEBUG_TAG, "Error loading ffmpeg. " + ioe.getMessage());
					}

					ShellDummy shell = new ShellDummy();

					try {
						ffmpeg.extractAudio(fileToConvert, audioFile,
								bitrateType, bitrateValue, currentItem, shell);
					} catch (IOException e) {
						Log.e(DEBUG_TAG, "IOException running ffmpeg" + e.getMessage());
					} catch (InterruptedException e) {
						Log.e(DEBUG_TAG, "InterruptedException running ffmpeg"
								+ e.getMessage());
					}
					Looper.loop();
				}
			}).start();
		} else {
			PopUps.showPopUp(getString(R.string.long_press_warning_title), getString(R.string.audio_extr_warning_msg), "status", DashboardActivity.this);
			isFfmpegRunning = false;
		}
	}
	
	private class ShellDummy implements ShellCallback {
		
		@Override
		public void preProcess() {
			String text = null;
			if (!extrTypeIsMp3Conv) {
				text = getString(R.string.audio_extr_progress);
				type = YTD.JSON_DATA_TYPE_A_E;
			} else {
				text = getString(R.string.audio_conv_progress);
				type = YTD.JSON_DATA_TYPE_A_M;
			}
			Toast.makeText(DashboardActivity.this, vfilename + ": " + text,
					Toast.LENGTH_SHORT).show();
			
			Utils.logger("i", vfilename + ": " + text, DEBUG_TAG);
			
			JsonHelper.addEntryToJsonFile(
					String.valueOf(aNewId),
					type, 
					currentItem.getYtId(),
					currentItem.getPos(),
					YTD.JSON_DATA_STATUS_IN_PROGRESS,
					currentItem.getPath(), 
					audioFile.getName(), 
					currentItem.getBasename(), 
					"", 
					"-", 
					true);
			
			refreshlist();
			
		}

		@Override
		public void shellOut(String shellLine) {
			findAudioSuffix(shellLine);
			getAudioJobProgress(shellLine, 2);
			Utils.logger("d", shellLine, DEBUG_TAG);
		}

		@Override
		public void processComplete(final DashboardListItem item, int exitValue) {
			Utils.logger("i", "FFmpeg process exit value: " + exitValue, DEBUG_TAG);
			String text = null;
			
			if (exitValue == 0) {

				// Toast + Notification + Log ::: Audio job OK
				if (!extrTypeIsMp3Conv) {
					text = getString(R.string.audio_extr_completed);
				} else {
					text = getString(R.string.audio_conv_completed);
				}
				Utils.logger("d", vfilename + ": " + text, DEBUG_TAG);
				
				boolean addItToDb = addSuffixToAudioFileName(item);
				Toast.makeText(DashboardActivity.this,  vfilename + ": " + text, Toast.LENGTH_SHORT).show();
        		
        		// write id3 tags
				if (extrTypeIsMp3Conv) {
					try {
						Utils.logger("d", "writing ID3 tags...", DEBUG_TAG);
						addId3Tags(audioFile, tagArtist, tagAlbum, tagTitle, tagGenre, tagYear);
					} catch (ID3WriteException e) {
						Log.e(DEBUG_TAG, "Unable to write id3 tags", e);
					} catch (IOException e) {
						Log.e(DEBUG_TAG, "Unable to write id3 tags", e);
					}
				}
				
				Utils.scanMedia(getApplicationContext(), 
						new String[] {audioFile.getAbsolutePath()}, 
						new String[] {"audio/*"});
				
				// remove selected video upon successful audio extraction
				if (removeVideo || removeAudio) {
					new AsyncDelete().execute(item);
					
				}
				
				// add audio file to the JSON file entry
				if (addItToDb)
					JsonHelper.addEntryToJsonFile(
							String.valueOf(aNewId), 
							type, 
							item.getYtId(), 
							item.getPos(),
							YTD.JSON_DATA_STATUS_COMPLETED,
							item.getPath(), 
							audioFile.getName(), 
							item.getBasename(), 
							"", 
							Utils.MakeSizeHumanReadable((int) audioFile.length(), false), 
							false);
			} else {
				setNotificationForAudioJobError();
				
				JsonHelper.addEntryToJsonFile(
						String.valueOf(aNewId),
						type, 
						item.getYtId(),
						item.getPos(),
						YTD.JSON_DATA_STATUS_FAILED,
						item.getPath(), 
						audioFile.getName(), 
						item.getBasename(), 
						"", 
						"-", 
						false);
			}
			
			refreshlist();
			isFfmpegRunning = false;
		}
		
		@Override
		public void processNotStartedCheck(boolean started) {
			if (!started) {
				Utils.logger("w", "FFmpeg process not started or not completed", DEBUG_TAG);
				setNotificationForAudioJobError();
			}
			isFfmpegRunning = false;
		}
    }
    
	public boolean addSuffixToAudioFileName(DashboardListItem item) {
		// Rename audio file to add a more detailed suffix, 
		// but only if it has been matched from the FFmpeg console output
		if (!extrTypeIsMp3Conv &&
				audioFile.exists() && 
				aSuffix != null) {
			String newName = basename + aSuffix;
			File newFile = new File(item.getPath(), newName);
			
			if (newFile.exists()) {
				audioFile.delete();
				return false;
			}
			
			if (audioFile.renameTo(newFile)) {
				Utils.logger("i", "'" + audioFile.getName() + "' renamed to: '" + newName + "'", DEBUG_TAG);
				audioFile = newFile;
			} else {
				Log.e(DEBUG_TAG, "Unable to rename '" + audioFile.getName() + "' to: '" + aSuffix + "'");
				return false;
			}
		}
		return true;
	}

	/* method addId3Tags adapted from Stack Overflow:
	 * 
	 * http://stackoverflow.com/questions/9707572/android-how-to-get-and-setchange-id3-tagmetadata-of-audio-files/9770646#9770646
	 * 
	 * Q: http://stackoverflow.com/users/849664/chirag-shah
	 * A: http://stackoverflow.com/users/903469/mkjparekh
	 */

	public void addId3Tags(File src, String artist, String album, String title, String genre, String year ) 
			throws IOException, ID3WriteException {
        MusicMetadataSet src_set = new MyID3().read(src);
        if (src_set == null) {
            Utils.logger("w", "no metadata", DEBUG_TAG);
        } else {
        	MusicMetadata meta = new MusicMetadata("ytd");
        	
        	if (artist == null || artist.isEmpty()) artist = "YTD";
        	meta.setArtist(artist);
        	
        	if (album == null || album.isEmpty()) album = "YTD Extracted Audio";
        	meta.setAlbum(album);
        	
        	if (title == null || title.isEmpty()) title = basename;
        	meta.setSongTitle(title);
	        
        	if (genre != null) meta.setGenre(genre);
        	
        	if (year != null) meta.setYear(year);
        	
        	Utils.logger("d", "metadata used for last id3tag:" +
        			"\n  artist: " + artist +
        			"\n  album: " + album +
        			"\n  title: " + title +
        			"\n  genre: " + genre +
        			"\n  year: " + year, DEBUG_TAG);
	        new MyID3().update(src, src_set, meta);
        }
	}

	private void findAudioSuffix(String shellLine) {
		Pattern audioPattern = Pattern.compile("#0:0.*: Audio: (.+), .+?(mono|stereo .default.|stereo)(, .+ kb|)"); 
		Matcher audioMatcher = audioPattern.matcher(shellLine);
		if (audioMatcher.find() && !extrTypeIsMp3Conv) {
			String oggBr = "a";
			String groupTwo = "n";
			if (audioMatcher.group(2).equals("stereo (default)")) {
				if (vfilename.contains("hd")) {
					oggBr = "192k";
				} else {
					oggBr = "128k";
				}
				groupTwo = "stereo";
			} else {
				oggBr = "";
				groupTwo = audioMatcher.group(2);
			}
			
			aSuffix = "_" +
					groupTwo + 
					"_" + 
					audioMatcher.group(3).replace(", ", "").replace(" kb", "k") + 
					oggBr + 
					"." +
					audioMatcher.group(1).replaceFirst(" (.*/.*)", "").replace("vorbis", "ogg");
			
			Utils.logger("i", "Audio suffix found: " + aSuffix, DEBUG_TAG);
		}
	}
	
	private void getAudioJobProgress(String shellLine, int notNum) {
		int mDownloadPercent;
		
		Pattern initPattern = Pattern.compile("ffmpeg version (\\d\\.\\d)");
		Matcher initMatcher = initPattern.matcher(shellLine);
		if (initMatcher.find()) {
			totSeconds = 0;
			currentTime = 0;
		}
		
		Pattern totalTimePattern = Pattern.compile("Duration: (..):(..):(..)\\.(..)");
		Matcher totalTimeMatcher = totalTimePattern.matcher(shellLine);
		if (totalTimeMatcher.find()) {
			totSeconds = Utils.getTotSeconds(totalTimeMatcher);
		}
		
		Pattern currentTimePattern = Pattern.compile("time=(..):(..):(..)\\.(..)");
		Matcher currentTimeMatcher = currentTimePattern.matcher(shellLine);
		if (currentTimeMatcher.find()) {
			currentTime = Utils.getTotSeconds(currentTimeMatcher);
		}
		
		if (totSeconds == 0) {
            mDownloadPercent = -1;
        } else {
            mDownloadPercent = (int) (currentTime * 100 / totSeconds);
        }
		
		//Utils.logger("i", currentTime + "/" + totSeconds + " -> " + mDownloadPercent, DEBUG_TAG);
        YTD.mFFmpegPercentMap.put(aNewId, mDownloadPercent);
	}

	public void setNotificationForAudioJobError() {
		String text;
		if (!extrTypeIsMp3Conv) {
			text = getString(R.string.audio_extr_error);
		} else {
			text = getString(R.string.audio_conv_error);
		}
		Log.e(DEBUG_TAG, vfilename + ": " + text);
		Toast.makeText(DashboardActivity.this,  vfilename + ": " + text, Toast.LENGTH_SHORT).show();
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	isLandscape = true;
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	isLandscape = false;
	    }
	}

	private String[] retrieveBitrateValuesFromSpinner(final Spinner sp) {
		String bitrateEntry = String.valueOf(sp.getSelectedItem());
		   
		String[] bitrateValues = sDashboard.getResources()
				   .getStringArray(R.array.mp3_bitrate_entry_values);
		   
		String[] bitrateEntries = sDashboard.getResources()
				   .getStringArray(R.array.mp3_bitrate_entries);
		
		String bitrateType = null;
		if (bitrateEntry.contains("CBR")) {
			bitrateType = "CBR";
		} else {
			bitrateType = "VBR";
		}
		   
		String bitrateValue = null;
		for (int i = 0; i < bitrateValues.length; i++ ) {
			if (bitrateEntry.equals(bitrateEntries[i]))
			 bitrateValue = bitrateValues[i];
		}
		Utils.logger("v", "selected bitrate value: " + bitrateValue + 
						"\nselected bitrate entry: " + bitrateEntry , DEBUG_TAG);
		
		return new String[] { bitrateType, bitrateValue };
	}

	public void openVideoIntent(final File in) {
		Intent openIntent = new Intent(Intent.ACTION_VIEW);
		openIntent.setDataAndType(Uri.fromFile(in), "video/*");
		startActivity(Intent.createChooser(openIntent, getString(R.string.open_chooser_title)));
	}

	public void openAudioIntent(final File in) {
		Intent openIntent = new Intent(Intent.ACTION_VIEW);
		openIntent.setDataAndType(Uri.fromFile(in), "audio/*");
		startActivity(Intent.createChooser(openIntent, getString(R.string.open_chooser_title)));
	}

	public void extractAudioOnly(final File in) {
		AlertDialog.Builder builder0 = new AlertDialog.Builder(sDashboard);
		
		String[] title = getResources().getStringArray(R.array.dashboard_click_entries);
		builder0.setTitle(title[1]);
		
		LayoutInflater inflater0 = getLayoutInflater();
		final View view0 = inflater0.inflate(R.layout.dialog_audio_extr_only, null);
		
		String type = null;
		if (currentItem.getAudioExt().equals(".aac")) type = aac;
		if (currentItem.getAudioExt().equals(".ogg")) type = ogg;
		if (currentItem.getAudioExt().equals(".mp3")) type = mp3;
		//if (currentItem.getAudioExt().equals(".auto")) type = aac_mp3;
		
		TextView info = (TextView) view0.findViewById(R.id.audio_extr_info);
		info.setText(getString(R.string.audio_extr_info) + "\n\n" + type);

		builder0.setView(view0)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           @Override
		           public void onClick(DialogInterface dialog, int id) {
		        	   
		        	   CheckBox cb0 = (CheckBox) view0.findViewById(R.id.rem_video_0);
		        	   removeVideo = cb0.isChecked();

		        	   Utils.logger("v", "Launching FFmpeg on: " + in +
		        			   "\n-> mode: extraction only" +
		        			   "\n-> remove video: " + removeVideo, DEBUG_TAG);
		        	   
		        	   ffmpegJob(in, null, null);
		           }
		       })
		       .setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // cancel
		           }
		       });      
		
		Utils.secureShowDialog(sDashboard, builder0);
	}

	public void extractAudioAndConvertToMp3(final File in) {
		AlertDialog.Builder builder = new AlertDialog.Builder(sDashboard);
		
		String[] title = getResources().getStringArray(R.array.dashboard_click_entries);
		builder.setTitle(title[2]);
		
		LayoutInflater inflater1 = getLayoutInflater();
		
		final View view1 = inflater1.inflate(R.layout.dialog_audio_extr_mp3_conv, null);
		
		ScrollView sv = new ScrollView(sDashboard);
		sv.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		sv.addView(view1);

		builder.setView(sv)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           @Override
		           public void onClick(DialogInterface dialog, int id) {
		        	   
		        	   final Spinner sp = (Spinner) view1.findViewById(R.id.mp3_spinner);
		        	   String[] bitrateData = retrieveBitrateValuesFromSpinner(sp);
		        	   
		        	   CheckBox cb1 = (CheckBox) view1.findViewById(R.id.rem_video_1);
		        	   removeVideo = cb1.isChecked();
		        	   
		        	   Utils.logger("v", "Launching FFmpeg on: " + in +
		        			   "\n-> mode: conversion to mp3 from video file" +
		        			   "\n-> remove video: " + removeVideo, DEBUG_TAG);
		        	   
		        	   ffmpegJob(in, bitrateData[0], bitrateData[1]);
		           }
		       })
		       .setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               //
		           }
		       });      
		
		Utils.secureShowDialog(sDashboard, builder);
	}

	public void convertAudioToMp3(final File in) {
		AlertDialog.Builder builder = new AlertDialog.Builder(sDashboard);
		
		String[] title = getResources().getStringArray(R.array.dashboard_click_entries_audio);
		builder.setTitle(title[1]);
		
		LayoutInflater inflater0 = getLayoutInflater();
		final View view2 = inflater0.inflate(R.layout.dialog_audio_mp3_conv, null);
		
		ScrollView sv = new ScrollView(sDashboard);
		sv.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		sv.addView(view2);

		builder.setView(sv)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           @Override
		           public void onClick(DialogInterface dialog, int id) {
		        	   
		        	   final Spinner sp = (Spinner) view2.findViewById(R.id.mp3_spinner_a);
		        	   String[] bitrateData = retrieveBitrateValuesFromSpinner(sp);
		        	   
		        	   CheckBox cb2 = (CheckBox) view2.findViewById(R.id.rem_original_audio);
		        	   removeAudio = cb2.isChecked();

		        	   Utils.logger("v", "Launching FFmpeg on: " + in +
		        			   "\n-> mode: conversion to mp3 from audio file" +
		        			   "\n-> remove audio: " + removeAudio, DEBUG_TAG);
		        	   
		        	   ffmpegJob(in, bitrateData[0], bitrateData[1]);
		           }
		       })
		       .setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               //
		           }
		       });      
		
		Utils.secureShowDialog(sDashboard, builder);
	}
	
	public void addAudioStream(final File in) {
		
		isFfmpegRunning = true;

		// find a dashboard **AO** entry with same ytid and status completed
		for (int i = 0; i < idEntries.size(); i++ ) {
			if (currentItem.getYtId().equals(ytidEntries.get(i)) && 
					typeEntries.get(i).equals(YTD.JSON_DATA_TYPE_A_O) &&
					statusEntries.get(i).equals(YTD.JSON_DATA_STATUS_COMPLETED)	) {
				audioOnlyFile = new File(pathEntries.get(i), filenameEntries.get(i));
				audioOnlyExt = audioExtEntries.get(i);
				
				aoItem = new DashboardListItem(
						idEntries.get(i),
						typeEntries.get(i),
						ytidEntries.get(i), 
						posEntries.get(i), 
						statusEntries.get(i),
						pathEntries.get(i), 
						filenameEntries.get(i), 
						basenameEntries.get(i),
						audioExtEntries.get(i), 
						sizeEntries.get(i), 
						progressEntries.get(i),
						speedEntries.get(i));
			}
		}
		
		if (audioOnlyFile != null && audioOnlyFile.exists()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(sDashboard);
			
			String[] title = getResources().getStringArray(R.array.dashboard_click_entries_vo);
			builder.setTitle(title[1]);
			
			LayoutInflater inflater = getLayoutInflater();
			final View view = inflater.inflate(R.layout.dialog_add_audio_stream, null);
			
			TextView info = (TextView) view.findViewById(R.id.ao_info);
			info.setText(currentItem.getFilename() + "\n\t+\n" + audioOnlyFile.getName());
			
			builder.setView(view)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           @Override
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			        	   CheckBox cb = (CheckBox) view.findViewById(R.id.rem_ao_vo);
			        	   removeAoVo = cb.isChecked();
			        	   
			        	   Utils.logger("v", "Launching FFmpeg MUX on: " + in + 
			        			   "\n + " + audioOnlyFile +
			        			   "\n-> remove ao & vo: " + removeAoVo, DEBUG_TAG);
			        	   
			        	   mux(in);
			           }
			       })
			       .setNegativeButton(R.string.dialogs_negative, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               //
			           }
			       });      
			
				Utils.secureShowDialog(sDashboard, builder);
		} else {
			Toast.makeText(DashboardActivity.this, getString(R.string.ao_not_found), Toast.LENGTH_SHORT).show();
		}
		isFfmpegRunning = false;
	}
	
	public void mux(final File in) {
		
		String ext = Utils.getExtFromFileName(in.getName());
		String basename = currentItem.getBasename();
		muxedFileName = basename + "_MUX." + ext;
		muxedVideo = new File(currentItem.getPath(), muxedFileName);
    	
    	aNewId = System.currentTimeMillis();
    	
    	if (!muxedVideo.exists() || muxedVideo.length() == 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					
			    	FfmpegController ffmpeg = null;
					try {
						ffmpeg = new FfmpegController(DashboardActivity.this);
					} catch (IOException ioe) {
						Log.e(DEBUG_TAG, "Error loading ffmpeg. " + ioe.getMessage());
					}
		
					MuxShellDummy shell = new MuxShellDummy();
		
					try {
						ffmpeg.downloadAndMuxAoVoStreams(in.getAbsolutePath(), audioOnlyFile.getAbsolutePath(), muxedVideo, currentItem, shell);
					} catch (IOException e) {
						Log.e(DEBUG_TAG, "IOException running ffmpeg" + e.getMessage());
					} catch (InterruptedException e) {
						Log.e(DEBUG_TAG, "InterruptedException running ffmpeg" + e.getMessage());
					}
					Looper.loop();
				}
			}).start();
    	} else {
    		PopUps.showPopUp(getString(R.string.long_press_warning_title), getString(R.string.long_press_warning_msg2), "status", DashboardActivity.this);
    	}
	}
	
	private class MuxShellDummy implements ShellCallback {

		@Override
		public void preProcess() {
			String text = "MUX " + getString(R.string.json_status_in_progress);
			Toast.makeText(DashboardActivity.this, muxedFileName + ": " + text,
					Toast.LENGTH_SHORT).show();
			Utils.logger("i", muxedFileName + ": " + text, DEBUG_TAG);
			
			JsonHelper.addEntryToJsonFile(
					String.valueOf(aNewId),
					YTD.JSON_DATA_TYPE_V_M,  
					currentItem.getYtId(),
					currentItem.getPos(),
					YTD.JSON_DATA_STATUS_IN_PROGRESS,
					currentItem.getPath(),
					muxedFileName, 
					Utils.getFileNameWithoutExt(muxedFileName), 
					audioOnlyExt, 
					"-", 
					false);
		
			refreshlist();
		}
		
		@Override
		public void shellOut(String shellLine) {
			getAudioJobProgress(shellLine, 4);
			Utils.logger("d", shellLine, DEBUG_TAG);
		}
		
		@Override
		public void processComplete(DashboardListItem item, int exitValue) {
			Utils.logger("i", "FFmpeg process exit value: " + exitValue, DEBUG_TAG);

			if (exitValue == 0) {
				Toast.makeText(DashboardActivity.this,  muxedFileName + ": MUX " + 
						getString(R.string.json_status_completed), Toast.LENGTH_SHORT).show();
		    	
	    		Utils.scanMedia(getApplicationContext(), 
						new String[] {muxedVideo.getAbsolutePath()}, 
						new String[] {"video/*"});
	    		
	    		if (removeAoVo) {
					new AsyncDelete().execute(currentItem);
					new AsyncDelete().execute(aoItem);
				}
	    		
	    		JsonHelper.addEntryToJsonFile(
	    				String.valueOf(aNewId),
						YTD.JSON_DATA_TYPE_V_M, 
						currentItem.getYtId(),
						currentItem.getPos(),
						YTD.JSON_DATA_STATUS_COMPLETED,
						currentItem.getPath(),
						muxedFileName, 
						Utils.getFileNameWithoutExt(muxedFileName), 
						audioOnlyExt, 
						Utils.MakeSizeHumanReadable((int) muxedVideo.length(), false), 
						false);
			} else {
				setNotificationForAudioJobError();
				
				JsonHelper.addEntryToJsonFile(
						String.valueOf(aNewId),
						YTD.JSON_DATA_TYPE_V_M,  
						currentItem.getYtId(),
						currentItem.getPos(),
						YTD.JSON_DATA_STATUS_FAILED,
						currentItem.getPath(),
						muxedFileName, 
						Utils.getFileNameWithoutExt(muxedFileName), 
						audioOnlyExt, 
						"-", 
						false);
			}
			
			refreshlist();
			isFfmpegRunning = false;
		}

		@Override
		public void processNotStartedCheck(boolean started) {
			if (!started) {
				Utils.logger("w", "FFmpeg process not started or not completed", DEBUG_TAG);
				setNotificationForAudioJobError();
			}
			isFfmpegRunning = false;
		}
		
		public void setNotificationForAudioJobError() {
			Log.e(DEBUG_TAG, muxedFileName + " MUX failed");
			Toast.makeText(DashboardActivity.this,  muxedFileName + ": MUX " + 
					getString(R.string.json_status_failed), Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void dashboardAsyncTaskInProgress(final Activity act, final boolean isIt) {
		Utils.logger("i", "setting dashboardAsyncTaskInProgress to " + isIt, DEBUG_TAG);
		YTD.isAnyAsyncInProgress = isIt;
		act.runOnUiThread(new Runnable() {
			public void run() {
				act.setProgressBarIndeterminateVisibility(isIt);
		    }
		});
	}
}
