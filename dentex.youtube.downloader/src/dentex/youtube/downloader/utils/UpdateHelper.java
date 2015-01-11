package dentex.youtube.downloader.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class UpdateHelper {
	private static final String DEBUG_TAG = "UpdateHelper";
	
	public static String findCurrentAppVersion(Context ctx) {
		String cv;
		try {
		    cv = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		    Utils.logger("d", "current version: " + cv, DEBUG_TAG);
		} catch (NameNotFoundException e) {
		    Log.e(DEBUG_TAG, "version not read: " + e.getMessage());
		    cv = "100";
		}
		return cv;
	}
	
	public static String[] doUpdateCheck(Context ctx, AsyncTask<String, Void, String[]> asyncAutoUpdate, String content) {
		Utils.logger("d", "doUpdateCheck", DEBUG_TAG);
		if (asyncAutoUpdate.isCancelled()) {
			Utils.logger("d", "asyncUpdate cancelled @ 'OnlineUpdateCheck' begin", DEBUG_TAG);
			return null;
		}
		
		String matchedVersion;
		String matchedChangeLog;
		String matchedMd5;
		
		// match version name
		Pattern v_pattern = Pattern.compile("versionName=\\\"(.*)\\\"");
        Matcher v_matcher = v_pattern.matcher(content);
        if (v_matcher.find() && !asyncAutoUpdate.isCancelled()) {
        	matchedVersion = v_matcher.group(1);
	    	Utils.logger("i", "_on-line version: " + matchedVersion, DEBUG_TAG);
	    } else {
        	matchedVersion = "not_found";
        	Log.e(DEBUG_TAG, "_online version: not found!");
        }
        
        // match changelog
        Pattern cl_pattern = Pattern.compile("<pre><code> v(.*?)</code></pre>", Pattern.DOTALL);
    	Matcher cl_matcher = cl_pattern.matcher(content);
    	if (cl_matcher.find() && !asyncAutoUpdate.isCancelled()) {
    		matchedChangeLog = " v" + cl_matcher.group(1);
    		Utils.logger("i", "_online changelog...", DEBUG_TAG);
    	} else {
    		matchedChangeLog = "not_found";
    		Log.e(DEBUG_TAG, "_online changelog not found!");
    	}
    	
    	// match md5
    	// checksum: <code>d7ef1e4668b24517fb54231571b4a74f</code> dentex.youtube.downloader_v1.4
    	Pattern md5_pattern = Pattern.compile("checksum: <code>(.{32})</code> dentex.youtube.downloader_v");
    	Matcher md5_matcher = md5_pattern.matcher(content);
    	if (md5_matcher.find() && !asyncAutoUpdate.isCancelled()) {
    		matchedMd5 = md5_matcher.group(1);
    		Utils.logger("i", "_online md5sum: " + matchedMd5, DEBUG_TAG);
    	} else {
    		matchedMd5 = "not_found";
    		Log.e(DEBUG_TAG, "_online md5sum not found!");
    	}
    	String currentVersion = findCurrentAppVersion(ctx);
    	String res = Utils.VersionComparator.compare(matchedVersion, currentVersion);
    	Utils.logger("d", "version comparison: " + matchedVersion + " " + res + " " + currentVersion, DEBUG_TAG);
    	
    	return new String[] { res, matchedVersion, matchedChangeLog, matchedMd5 };
	}
}
