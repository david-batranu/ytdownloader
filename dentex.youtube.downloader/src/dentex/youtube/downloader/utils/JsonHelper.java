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

package dentex.youtube.downloader.utils;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import dentex.youtube.downloader.R;
import dentex.youtube.downloader.YTD;

public class JsonHelper {
	
	static String DEBUG_TAG = "JsonHelper";

	public static void addEntryToJsonFile(String id, String type, String ytId, int pos, String status, 
			String path, String filename, String basename, String audioExt, String size, boolean forceCopy) {
		
		// parse existing/init new JSON 
		String previousJson = JsonHelper.readJsonDashboardFile();
		
		// create new "complex" object
		JSONObject mO = null;
		JSONObject jO = new JSONObject();
		
		try {
			mO = new JSONObject(previousJson);
			
			JSONObject obj = mO.optJSONObject(id);
			if (obj != null) {
				if (forceCopy) {
					String newId = String.valueOf(System.currentTimeMillis());
					Utils.logger("v", "Copying existent ID " + id + " into " + newId, DEBUG_TAG);
					id = newId;
				} else {
					Utils.logger("v", "Updating existent ID " + id, DEBUG_TAG);
				}
			} else {
				Utils.logger("v", "Addind new ID " + id, DEBUG_TAG);
			}
			
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_completed))) 
				status = YTD.JSON_DATA_STATUS_COMPLETED;
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_in_progress))) 
				status =  YTD.JSON_DATA_STATUS_IN_PROGRESS;
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_failed))) 
				status = YTD.JSON_DATA_STATUS_FAILED;
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_paused))) 
				status = YTD.JSON_DATA_STATUS_PAUSED;
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_imported))) 
				status = YTD.JSON_DATA_STATUS_IMPORTED;
			if (status.equals(YTD.ctx.getResources().getString(R.string.json_status_queued))) 
				status = YTD.JSON_DATA_STATUS_QUEUED;
			
			jO.put(YTD.JSON_DATA_TYPE, type);
			jO.put(YTD.JSON_DATA_YTID, ytId);
			jO.put(YTD.JSON_DATA_POS, pos);
			jO.put(YTD.JSON_DATA_STATUS, status);
			jO.put(YTD.JSON_DATA_PATH, path);
			jO.put(YTD.JSON_DATA_FILENAME, filename);
			jO.put(YTD.JSON_DATA_BASENAME, basename);
			jO.put(YTD.JSON_DATA_AUDIO_EXT, audioExt);
			jO.put(YTD.JSON_DATA_SIZE, size);
			mO.put(id, jO);
		} catch (JSONException e1) {
			Log.e(DEBUG_TAG, "JSONException @ addEntryToJsonFile");
		}
		
		// generate string from the object
		String jsonString = null;
		try {
			jsonString = mO.toString(4);
			
			// write back JSON file
			Utils.logger("v", "-> " + jsonString, DEBUG_TAG);
			Utils.writeToFile(YTD.JSON_FILE, jsonString);
		} catch (JSONException e1) {
			Log.e(DEBUG_TAG, "JSONException @ addEntryToJsonFile");
		} catch (NullPointerException e1) {
			Log.e(DEBUG_TAG, "NPE @ addEntryToJsonFile");
		}
	}

	public static void removeEntryFromJsonFile(String id) {
		String previousJson = JsonHelper.readJsonDashboardFile();
		
		JSONObject mO = null;
		try {
			Utils.logger("v", "Removing ID " + id, DEBUG_TAG);
			mO = new JSONObject(previousJson);
			mO.remove(id);
		} catch (JSONException e1) {
			Log.e(DEBUG_TAG, "JSONException @ addEntryToJsonFile");
		}
		
		String jsonString = null;
		try {
			jsonString = mO.toString(4);
			
			// write back JSON file
			Utils.logger("v", "-> " + jsonString, DEBUG_TAG);
			Utils.writeToFile(YTD.JSON_FILE, jsonString);
		} catch (JSONException e1) {
			Log.e(DEBUG_TAG, "JSONException @ removeEntryFromJsonFile");
		} catch (NullPointerException e1) {
			Log.e(DEBUG_TAG, "NPE @ removeEntryFromJsonFile");
		}
	}

	public static String readJsonDashboardFile() {
		String jsonString = null;
		if (YTD.JSON_FILE.exists()) {
			try {
				jsonString = Utils.readFromFile(YTD.JSON_FILE);
			} catch (IOException e1) {
				jsonString = "{}";
				Log.e(DEBUG_TAG, "JSONException @ addEntryToJsonFile");
			}
		} else {
			jsonString = "{}";
		}
		return jsonString;
	}
}
