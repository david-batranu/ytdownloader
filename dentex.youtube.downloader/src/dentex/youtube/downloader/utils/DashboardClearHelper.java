package dentex.youtube.downloader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import dentex.youtube.downloader.DashboardActivity;
import dentex.youtube.downloader.R;
import dentex.youtube.downloader.YTD;

public class DashboardClearHelper {
	public static final String DEBUG_TAG = "DashboardClearHelper";
	public static String previousJson;
	public static List<File> fileList = new ArrayList<File>();
	public static boolean sDoReload;
	public static Activity sAct;
	
	public static void confirmClearDashboard(final Activity act, final boolean doReload) {
		previousJson = JsonHelper.readJsonDashboardFile();
		sDoReload = doReload;
		sAct = act;
		boolean smtInProgressOrPaused = (previousJson.contains(YTD.JSON_DATA_STATUS_IN_PROGRESS) || 
										 previousJson.contains(YTD.JSON_DATA_STATUS_PAUSED)) ;
		
		if (YTD.JSON_FILE.exists() && !previousJson.equals("{}\n") && !smtInProgressOrPaused) {
			
			AlertDialog.Builder adb = new AlertDialog.Builder(act);
			
			LayoutInflater adbInflater = LayoutInflater.from(act);
		    View deleteDataView = adbInflater.inflate(R.layout.dialog_inflatable_checkbox, null);
		    final CheckBox deleteData = (CheckBox) deleteDataView.findViewById(R.id.infl_cb);
		    deleteData.setChecked(false);
		    deleteData.setText(act.getString(R.string.dashboard_delete_data_cb));
		    adb.setView(deleteDataView);
		    
		    adb.setIcon(Utils.selectThemedInfoIcon());
		    adb.setTitle(act.getString(R.string.information));
		    adb.setMessage(act.getString(R.string.clear_dashboard_msg));
		    
		    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	
		        public void onClick(DialogInterface dialog, int which) {
		        	if (deleteData.isChecked()) {
		        		Utils.logger("i", "delete data checkbox checked", DEBUG_TAG);
			            new AsyncDeleteDasboardFiles().execute();
		        	} else {
			        	if (YTD.JSON_FILE.delete()) {
			        		clearThumbsAndVideoinfopref();
			        		if (doReload) Utils.reload(act);
			        	} else {
			        		Toast.makeText(act, act.getString(R.string.clear_dashboard_failed), Toast.LENGTH_SHORT).show();
			        		Utils.logger("w", "clear_dashboard_failed", DEBUG_TAG);
			        	}
		        	}
		        }
		    });
		    
		    adb.setNegativeButton(act.getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
		    	
		    	public void onClick(DialogInterface dialog, int which) {
		        	// cancel
		        }
		    });

		    AlertDialog helpDialog = adb.create();
		    if (! act.isFinishing()) {
		    	helpDialog.show();
		    }
		} else {
			Toast.makeText(act, act.getString(R.string.long_press_warning_title) + 
					"\n- " + act.getString(R.string.notification_downloading_pt1) + " (" + 
					act.getString(R.string.json_status_paused) + "/" + act.getString(R.string.json_status_in_progress) + " )" + 
					"\n- " + act.getString(R.string.empty_dashboard), 
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private static void clearThumbsAndVideoinfopref() {
		Toast.makeText(YTD.ctx, YTD.ctx.getString(R.string.clear_dashboard_ok), Toast.LENGTH_SHORT).show();
		Utils.logger("v", "Dashboard cleared", DEBUG_TAG);
		
		// clean thumbnails dir
		File thFolder = YTD.ctx.getDir(YTD.THUMBS_FOLDER, 0);
		for(File file: thFolder.listFiles()) file.delete();
		
		// clear the videoinfo shared pref
		YTD.videoinfo.edit().clear().apply();
	}

	public static class AsyncDeleteDasboardFiles extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			DashboardActivity.dashboardAsyncTaskInProgress(sAct, true);
		}
		
		@Override
		protected Integer doInBackground(Void... unused) {
			int result = 0;
			JSONObject jV = null;
			try {
				jV = new JSONObject(previousJson);

				@SuppressWarnings("unchecked")
				Iterator<Object> ids = jV.keys();
				while (ids.hasNext()) {
					String id = (String) ids.next();
					JSONObject jO = new JSONObject();
					jO = jV.getJSONObject(id);
					fileList.add(new File(jO.getString(YTD.JSON_DATA_PATH), jO.getString(YTD.JSON_DATA_FILENAME)));
				}
			} catch (JSONException e) {
				Log.e(DEBUG_TAG, "JSONException @ parseJson: " + e.getMessage());
			}
            
            // remove files
            for (int i = 0; i < fileList.size(); i++) {
	    		if (fileList.get(i).exists() && fileList.get(i).delete()) {
	    			// remove library reference
	    			String mediaUriString;
	    			try {
	    				mediaUriString = Utils.getContentUriFromFile(fileList.get(i), YTD.ctx.getContentResolver());
	    				Utils.removeFromMediaStore(YTD.ctx, fileList.get(i), mediaUriString);
	    			} catch (NullPointerException e) {
	    				result = 1;
	    				Utils.logger("w", fileList.get(i).getName() + " UriString NOT found", DEBUG_TAG);
	    			}
	    		}
            }
			return result;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if (result == 0 && YTD.JSON_FILE.delete()) {
				clearThumbsAndVideoinfopref();
				/*Utils.logger("d", "all files successfully deleted.", DEBUG_TAG);
				Toast.makeText(YTD.ctx, 
						YTD.ctx.getString(R.string.json_status_completed), 
						Toast.LENGTH_SHORT).show();*/
			} else {
				/*Utils.logger("w", "Dashboard files deletion error.", DEBUG_TAG);
				Toast.makeText(YTD.ctx, 
						YTD.ctx.getString(R.string.error), 
						Toast.LENGTH_SHORT).show();*/
				Toast.makeText(YTD.ctx, YTD.ctx.getString(R.string.clear_dashboard_failed), Toast.LENGTH_SHORT).show();
        		Utils.logger("w", "clear_dashboard_failed", DEBUG_TAG);
			}
			if (sDoReload) Utils.reload(sAct);
			DashboardActivity.dashboardAsyncTaskInProgress(sAct, false);
		}
	}
}
