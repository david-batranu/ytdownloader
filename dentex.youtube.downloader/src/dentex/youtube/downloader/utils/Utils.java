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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import dentex.youtube.downloader.R;
import dentex.youtube.downloader.SettingsActivity;
import dentex.youtube.downloader.YTD;

public class Utils {
	
	static String DEBUG_TAG = "Utils";
	static MediaScannerConnection msc;
	
	public static void reload(Activity activity) {
		//finish
    	activity.finish();
    	activity.overridePendingTransition(0, 0);
    	
    	//start
    	Intent intent = activity.getIntent();
    	intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    	activity.startActivity(intent);
    	activity.overridePendingTransition(0, 0);
    }
    
    public static void themeInit(Context context) {
		String theme = YTD.settings.getString("choose_theme", "D");
    	if (theme.equals("D")) {
    		context.setTheme(R.style.AppThemeDark);
    	} else {
    		context.setTheme(R.style.AppThemeLight);
    	}
	}
    
	public static int selectThemedInfoIcon() {
		String theme = YTD.settings.getString("choose_theme", "D");
    	if (theme.equals("D")) {
    		return R.drawable.ic_dialog_info_holo_dark;
    	} else {
    		return R.drawable.ic_dialog_info_holo_light;
    	}
	}
	
	public static int selectThemedAlertIcon() {
		String theme = YTD.settings.getString("choose_theme", "D");
    	if (theme.equals("D")) {
    		return R.drawable.ic_dialog_alert_holo_dark;
    	} else {
    		return R.drawable.ic_dialog_alert_holo_light;
    	}
	}
	
    public static void langInit(Context context) {
    	String lang  = YTD.settings.getString("lang", "default");
        Locale locale;
		if (!lang.equals("default")) {
			String[] fLang = filterLang(lang);
	        locale = new Locale(fLang[0], fLang[1]);
	        Locale.setDefault(locale);
	        Configuration config = new Configuration();
	        config.locale = locale;
        } else {
        	locale = new Locale(YTD.settings.getString("DEF_LANG", ""));
        	Locale.setDefault(locale);
        }
		
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
	}
    
    private static String[] filterLang(String lang) {
		if (lang.equals("el_GR") || 
			lang.equals("bg_BG") || 
			lang.equals("hu_HU") || 
			lang.equals("ja_JP") || 
			lang.equals("pl_PL") ||
			lang.equals("pt_BR") || 
			lang.equals("pt_PT") || 
			lang.equals("tr_TR") || 
			lang.equals("cs_CZ") || 
			lang.equals("zh_CN") ||
			lang.equals("zh_HK") ||
			lang.equals("zh_TW")) 
				return lang.split("_");
		return new String[] { lang, "" };
	}

	public static void logger(String type, String msg, String tag) {
    	if (YTD.settings.getBoolean("enable_logging", false)) {
	    	if (type.equals("v")) {
	    		Log.v(tag, msg);
	    	} else if (type.equals("d")) {
	    		Log.d(tag, msg);
	    	} else if (type.equals("i")) {
	    		Log.i(tag, msg);
	    	} else if (type.equals("w")) {
	    		Log.w(tag, msg);
	    	}
    	}
    }
    
	public static int pathCheck(File path) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (path.canWrite()) {
				return 0;
			} else {
				logger("w", "Path not writable", DEBUG_TAG);
				return 1;
			}
		} else {
			logger("w", "Path not mounted", DEBUG_TAG);
			return 2;
		}
	 }
	
	 public static void writeToFile(File file, String content) {
    	try {
	        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
	        OutputStream os = new FileOutputStream(file);
	        byte[] data = new byte[is.available()];
	        is.read(data);
	        os.write(data);
	        is.close();
	        os.close();
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "Error creating '" + file.getName() + "' Log file", e);
		}
	}
    
    public static void setNotificationDefaults(NotificationCompat.Builder aBuilder) {
    	String def = YTD.settings.getString("notification_defaults", "0");
    	if (aBuilder != null) {
			if (def.equals("0")) {
				aBuilder.setDefaults(Notification.DEFAULT_SOUND
						| Notification.DEFAULT_VIBRATE);
			}
			if (def.equals("1")) {
				aBuilder.setDefaults(Notification.DEFAULT_SOUND
						| Notification.DEFAULT_LIGHTS);
			}
			if (def.equals("2")) {
				aBuilder.setDefaults(Notification.DEFAULT_LIGHTS
						| Notification.DEFAULT_VIBRATE);
			}
			if (def.equals("3")) {
				aBuilder.setDefaults(Notification.DEFAULT_ALL);
			}
			if (def.equals("4")) {
				aBuilder.setDefaults(Notification.DEFAULT_SOUND);
			}
			if (def.equals("5")) {
				aBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
			}
			if (def.equals("6")) {
				aBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			}
			if (def.equals("7")) {
				// nothing...
			}
		}
    }
    
	public static String[] retrieveBitrateValuesFromPref(Context ctx) {
		String[] bitrateValues = ctx.getResources()
				   .getStringArray(R.array.mp3_bitrate_entry_values);
		   
		String[] bitrateEntries = ctx.getResources()
				   .getStringArray(R.array.mp3_bitrate_entries);
		
		String bitrateValue = YTD.settings.getString("auto-mp3_bitrates", "192k");
		String bitrateType = null;
		if (bitrateValue.contains("k")) {
			bitrateType = "CBR";
		} else {
			bitrateType = "VBR";
		}
		
		String bitrateEntry = null;
		for (int i = 0; i < bitrateEntries.length; i++) {
			if (bitrateValue.equals(bitrateValues[i]))
			 bitrateEntry = bitrateEntries[i];
		}
		
		Utils.logger("v", /*"selected bitrate value: " + bitrateValue + */
				"\nselected bitrate entry: " + bitrateEntry , DEBUG_TAG);
		
		return new String[] { bitrateType, bitrateValue };
	}
	
	public static void removeFromMediaStore(Context ctx, File fileToDel, String mediaUriString) {
		if (mediaUriString != null) {
			Uri mediaUri = Uri.parse(mediaUriString);
			// remove media file reference from MediaStore library via ContentResolver
			if (ctx.getContentResolver().delete(mediaUri, null, null) > 0) {
				Utils.logger("d", mediaUri.toString() + " Removed", DEBUG_TAG);
			} else {
				Utils.logger("w", mediaUri.toString() + " NOT removed", DEBUG_TAG);
			}
		} else {
			Utils.logger("w", "mediaUriString for " + fileToDel.getName() + " null", DEBUG_TAG);
		}
	}
	
	public static String cpuVersion() {
    	String cpuAbi = Build.CPU_ABI;
		Utils.logger("d", "CPU_ABI: " + cpuAbi, DEBUG_TAG);
		if (cpuAbi.equals("armeabi-v7a")) {
			if (neonCpu()) {
				Utils.logger("d", " -> v7a NEON", DEBUG_TAG);
				return YTD.ARMv7a_NEON;
			} else {
				Utils.logger("d", " -> v7a", DEBUG_TAG);
				return YTD.ARMv7a;
			}
		} else if (cpuAbi.equals("armeabi")) {
			Utils.logger("d", " -> v5te", DEBUG_TAG);
			return YTD.ARMv5te;
		} else {
			return YTD.UNSUPPORTED_CPU;
		}
	}
	
	public static void secureShowDialog(final Activity act, final AlertDialog.Builder adb) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(!act.isFinishing()){
					adb.show();
				}
			}
		});
	}
	
	public static void offerDevMail(final Context ctx) {
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setIcon(Utils.selectThemedAlertIcon());
		adb.setTitle(ctx.getString(R.string.ffmpeg_device_not_supported));
		adb.setMessage(ctx.getString(R.string.ffmpeg_support_mail));
		
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
		    public void onClick(DialogInterface dialog, int which) {
		    	/*
		    	 * adapted form same source as createEmailOnlyChooserIntent below
		    	 */
		    	Intent i = new Intent(Intent.ACTION_SEND);
		        i.setType("*/*");
		        
		        String content = Utils.getCpuInfo();
		        /*File destDir = getActivity().getExternalFilesDir(null); 
		        String filename = "cpuInfo.txt";
		        try {
					Utils.createLogFile(destDir, filename, content);
					i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(destDir, filename)));*/
		            i.putExtra(Intent.EXTRA_EMAIL, new String[] { "samuele.rini76@gmail.com" });
		            i.putExtra(Intent.EXTRA_SUBJECT, "YTD: device info report");
		            i.putExtra(Intent.EXTRA_TEXT, content);

		            ctx.startActivity(createEmailOnlyChooserIntent(ctx, i, ctx.getString(R.string.email_via)));
				/*} catch (IOException e) {
					Log.e(DEBUG_TAG, "IOException on creating cpuInfo Log file ", e);
				}*/
		    }
		});
		
		adb.setNegativeButton(ctx.getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
		    	// cancel
		    }
		});

		AlertDialog helpDialog = adb.create();
		if (! ((Activity) ctx).isFinishing()) {
			helpDialog.show();
		}
	}
    
	/* Intent createEmailOnlyChooserIntent from Stack Overflow:
	 * 
	 * http://stackoverflow.com/questions/2197741/how-to-send-email-from-my-android-application/12804063#12804063
	 * 
	 * Q: http://stackoverflow.com/users/138030/rakesh
	 * A: http://stackoverflow.com/users/1473663/nobu-games
	 */
	public static Intent createEmailOnlyChooserIntent(Context ctx, Intent source, CharSequence chooserTitle) {
		Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
        		"info@domain.com", null));
        List<ResolveInfo> activities = ctx.getPackageManager()
                .queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            target.setPackage(ri.activityInfo.packageName);
            intents.add(target);
        }

        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0),
                    chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new Parcelable[intents.size()]));

            return chooserIntent;
        } else {
        	return Intent.createChooser(source, chooserTitle);
        }
	}
	
	public static void notifyFfmpegNotInstalled(final Activity act) {
		Utils.logger("w", "FFmpeg not installed/enabled", DEBUG_TAG);
		AlertDialog.Builder adb = new AlertDialog.Builder(act);
		adb.setTitle(act.getString(R.string.ffmpeg_not_enabled_title));
		adb.setMessage(act.getString(R.string.ffmpeg_not_enabled_msg));
		
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				act.startActivity(new Intent(act,  SettingsActivity.class));
			}
		});
		
		adb.setNegativeButton(act.getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		        // cancel
		    }
		});
		
		if (!act.isFinishing()) {
			adb.show();
		}
	}
    
    /*
     * method readFromFile adapted from Stack Overflow:
	 * http://stackoverflow.com/questions/2902689/how-can-i-read-a-text-file-from-the-sd-card-in-android
	 * 
	 * Q: http://stackoverflow.com/users/349664/rsss
	 * A: http://stackoverflow.com/users/3171/dave-webb
	 */
    public static String readFromFile(File file) throws IOException {
 
        StringBuilder text = null;
        if(file.exists()) {   
            text = new StringBuilder();  
            BufferedReader br = new BufferedReader(new FileReader(file));  
            String line;  
            while ((line = br.readLine()) != null) {  
                text.append(line);  
                text.append('\n');  
            }
            br.close();
        }
        return text.toString();
    }
    
    /*
     * method MakeSizeHumanReadable(int bytes, boolean si) from Stack Overflow:
	 * http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * 
	 * Q: http://stackoverflow.com/users/404615/iimuhin
	 * A: http://stackoverflow.com/users/276052/aioobe
	 */
	 
	@SuppressLint("DefaultLocale")
	public static String MakeSizeHumanReadable(long bytes, boolean decimal) {
		String hr = "-";
		int unit = decimal ? 1000 : 1024;
	    if (bytes < unit) {
	    	hr = bytes + " B";
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = (decimal ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (decimal ? "" : "i");
			hr = String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
		hr = hr.replace("-1 B", "-").replace("0 B", "-");
	    return hr;
	}
    
	/* class VersionComparator from Stack Overflow:
	 * 
	 * http://stackoverflow.com/questions/198431/how-do-you-compare-two-version-strings-in-java
	 * 
	 * Q: http://stackoverflow.com/users/1288/bill-the-lizard
	 * A: http://stackoverflow.com/users/57695/peter-lawrey
	 */
	
    public static class VersionComparator {

        public static String compare(String v1, String v2) {
            String s1 = normalisedVersion(v1);
            String s2 = normalisedVersion(v2);
            int cmp = s1.compareTo(s2);
            String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
            return cmpStr;
        }

        public static String normalisedVersion(String version) {
            return normalisedVersion(version, ".", 4);
        }

        public static String normalisedVersion(String version, String sep, int maxWidth) {
            String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
            StringBuilder sb = new StringBuilder();
            for (String s : split) {
                sb.append(String.format("%" + maxWidth + 's', s));
            }
            return sb.toString();
        }
    }
	
	public static int getSigHash(Activity act) {
		int currentHash = 0;
		try {
			Signature[] sigs = act.getPackageManager()
					.getPackageInfo(act.getPackageName(), PackageManager.GET_SIGNATURES)
					.signatures;
			
			for (Signature sig : sigs) {
				currentHash = sig.hashCode();
				logger("d", "getSigHash: App signature " + currentHash, DEBUG_TAG);
			}
		} catch (NameNotFoundException e) {
		    Log.e(DEBUG_TAG, "getSigHash: App signature not found; " + e.getMessage());
		}
		return currentHash;
	}

	/*
	 * checkMD5(String md5, File file)
	 * -------------------------------
	 * 
	 * Copyright (C) 2012 The CyanogenMod Project
	 *
	 * * Licensed under the GNU GPLv2 license
	 *
	 * The text of the license can be found in the LICENSE_GPL file
	 * or at https://www.gnu.org/licenses/gpl-2.0.txt
	 */
	
	public static boolean checkMD5(String md5, File file) {
        if (md5 == null || md5.equals("") || file == null) {
            Log.e(DEBUG_TAG, "MD5 String NULL or File NULL");
            return false;
        }

        String calculatedDigest = calculateMD5(file);
        if (calculatedDigest == null) {
            Log.e(DEBUG_TAG, "calculatedDigest NULL");
            return false;
        }

        Log.i(DEBUG_TAG, "Calculated digest: " + calculatedDigest);
        Log.i(DEBUG_TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

	/*
	 * calculateMD5(File file)
	 * -----------------------
	 * 
	 * Copyright (C) 2012 The CyanogenMod Project
	 *
	 * * Licensed under the GNU GPLv2 license
	 *
	 * The text of the license can be found in the LICENSE_GPL file
	 * or at https://www.gnu.org/licenses/gpl-2.0.txt
	 */
	
    public static String calculateMD5(File file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEBUG_TAG, "Exception while getting Digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(DEBUG_TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
        	//throw new RuntimeException("Unable to process file for MD5", e);
        	Log.e(DEBUG_TAG, "Unable to process file for MD5", e); //TODO check if actually avoid FC 
        	return "00000000000000000000000000000000"; // fictional bad MD5: needed without "throw new RuntimeException"
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
    
    /* method copyFile(File src, File dst, Context context) adapted from Stack Overflow:
	 * 
	 * http://stackoverflow.com/questions/4770004/how-to-move-rename-file-from-internal-app-storage-to-external-storage-on-android
	 * 
	 * Q: http://stackoverflow.com/users/131871/codefusionmobile
	 * A: http://stackoverflow.com/users/472270/barmaley
	 */
    
    @SuppressWarnings("resource")
	public static void copyFile(File src, File dst) throws IOException {
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    //if (!dst.exists()) {
		    try {
		        inChannel.transferTo(0, inChannel.size(), outChannel);
		    } finally {
		        if (inChannel != null) inChannel.close();
		        if (outChannel != null) outChannel.close();
		    }
	    /*} else {
	    	logger("w", "copyFile: destination already exists", DEBUG_TAG);
	    }*/
	}
    
    /*
     * getCpuInfo() from:
     *   http://www.roman10.net/how-to-get-cpu-information-on-android/
     * by:
     *   Liu Feipeng 
     */
    
    public static String getCpuInfo() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("abi: ").append(Build.CPU_ABI).append("\n");
    	if (new File("/proc/cpuinfo").exists()) {
        	try {
        		BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
	        	String aLine;
				while ((aLine = br.readLine()) != null) {
					sb.append(aLine + "\n");
				}
				if (br != null) {
		    		br.close();
		    	}
			} catch (IOException e) {
				e.printStackTrace();
			} 
        }
    	return sb.toString();
    }
    
    // ------------
    
    public static boolean neonCpu() {
    	if (new File("/proc/cpuinfo").exists()) {
        	try {
        		BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
	        	String aLine;
				while ((aLine = br.readLine()) != null) {
					Pattern p = Pattern.compile("[F|f]eatures.*neon.*");
					Matcher m = p.matcher(aLine);
					if (m.find()) {
						br.close();
						return true;
					}
				}
				if (br != null) {
		    		br.close();
		    	}
			} catch (IOException e) {
				e.printStackTrace();
			} 
        }
    	return false;
    }
    
    // -----------
    
    /*
     * scanMedia method adapted from Wolfram Rittmeyer's blog:
     * http://www.grokkingandroid.com/adding-files-to-androids-media-library-using-the-mediascanner/
     */
    
    public static void scanMedia(Context context, final String[] filePath, final String[] mime) {
    	MediaScannerConnection.scanFile(context, filePath, mime, new OnScanCompletedListener() {
    		@Override
    		public void onScanCompleted(String path, Uri uri) {
    			Log.v(DEBUG_TAG, "file " + path + " was scanned successfully: " + uri);
    			//YTD.videoinfo.edit().putString(path, uri.toString()).apply();
    		}
    	});
    }
    
    //-----------
    
    public static String getFileNameWithoutExt(String filename) {
    	int index = filename.lastIndexOf('.');
    	if (index > 0 && index <= filename.length() - 2) {
    		return filename.substring(0, index);
    	}  
    	return filename;
	}
    
    public static String getExtFromFileName(String filename) {
    	int index = filename.lastIndexOf('.');
    	if (index > 0 && index <= filename.length() - 2) {
    		return filename.substring(index + 1);
    	}
    	return filename;
    }
    
    
    /* 
     * 'getContentUriFromFilePath' and 'getFilePathFromContentUri' adapted from StackOverflow:
     * http://stackoverflow.com/a/11603899/1865860
     * 
     * Q: http://stackoverflow.com/users/315998/stealthcopter
     * A: http://stackoverflow.com/users/429108/jon-o
     */
    
    /**
     * Gets the MediaStore video ID of a given file on external storage
     * @param filePath The path (on external storage) of the file to resolve the ID of
     * @param contentResolver The content resolver to use to perform the query.
     * @return the video ID as a long
     */
    public static String getContentUriFromFile(File file, ContentResolver contentResolver) {
    	
    	String filePath = file.getAbsolutePath();
        long videoId;
        logger("d","Loading file " + filePath, DEBUG_TAG);

        String[] vprojection = {MediaStore.Video.VideoColumns._ID};
        String[] aprojection = {MediaStore.Audio.AudioColumns._ID};
        
        String ext = getExtFromFileName(filePath).toLowerCase(Locale.ENGLISH);
        //logger("d","ext: " + ext, DEBUG_TAG);
        
        Uri videosUri = null;
        String[] projection = null;
        String dataType = null;
        /*if (file.getName().contains("_AO_")) {
        	videosUri = MediaStore.Audio.Media.getContentUri("external");
        	projection = aprojection;
        	dataType = MediaStore.Audio.AudioColumns.DATA;
        	logger("d", " -> contentUri on Audio-Only file", DEBUG_TAG);
        } else */if (ext.equals("mp4") || ext.equals("3gp") || ext.equals("webm")) {
        	videosUri = MediaStore.Video.Media.getContentUri("external");
        	projection = vprojection;
        	dataType = MediaStore.Video.VideoColumns.DATA;
        } else if (ext.equals("mp3") || ext.equals("ogg") || ext.equals("aac") || ext.equals("m4a")){
        	videosUri = MediaStore.Audio.Media.getContentUri("external");
        	projection = aprojection;
        	dataType = MediaStore.Audio.AudioColumns.DATA;
        } else if (ext.equals("flv")) {
        	logger("w", " -> contentUri not available [FLV video]", DEBUG_TAG);
        	return null;
        }
        
        Cursor cursor = null;
        String videoUri = null;
        
        try {
			cursor = contentResolver.query(videosUri, projection, dataType + " LIKE ?", new String[] { filePath }, null);
	        cursor.moveToFirst();
	
	        int columnIndex = cursor.getColumnIndex(projection[0]);
		
        	videoId = cursor.getLong(columnIndex);
        	videoUri = videosUri + "/" + videoId; 
        	logger("d", " -> contentUri: " + videoUri, DEBUG_TAG);
        	
        	cursor.close();
        } catch (Exception e) {
        	Log.e(DEBUG_TAG, "ContentUri not available: " + e.getMessage());
        } /*finally {
        	cursor.close();
        }*/
		
        return videoUri;
    }
    
    /**
     * Gets the corresponding path to a file from the given content:// URI
     * @param selectedVideoUri The content:// URI to find the file path from
     * @param contentResolver The content resolver to use to perform the query.
     * @return the file path as a string
     */
    public static String getFilePathFromContentUri(Uri selectedVideoUri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
    
    public static void appendStringToFile(File file, String text) {
    	PrintWriter out = null;
    	try {
    	    out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)));
    	    out.println("\n\n" + System.currentTimeMillis() + ":\n" + text);
    	} catch (IOException e) {
    	    Log.e(DEBUG_TAG, "appendStringToFile: " + e.getMessage());
    	} finally {
    	    if (out != null) {
    	        out.close();
    	    }
    	} 
    }
    
    /*private static int totSeconds = 0;
	private static int currentTime = 0;
	
	public static int[] getAudioJobProgress(String shellLine) {
		Pattern totalTimePattern = Pattern.compile("Duration: (..):(..):(..)\\.(..)");
		Matcher totalTimeMatcher = totalTimePattern.matcher(shellLine);
		if (totalTimeMatcher.find()) {
			totSeconds = getTotSeconds(totalTimeMatcher);
		}
		Pattern currentTimePattern = Pattern.compile("time=(..):(..):(..)\\.(..)");
		Matcher currentTimeMatcher = currentTimePattern.matcher(shellLine);
		if (currentTimeMatcher.find()) {
			currentTime = getTotSeconds(currentTimeMatcher);
		}

		return new int[] { totSeconds, currentTime };
	}*/

    public static int getTotSeconds(Matcher timeMatcher) {
		int h = Integer.parseInt(timeMatcher.group(1));
		int m = Integer.parseInt(timeMatcher.group(2));
		int s = Integer.parseInt(timeMatcher.group(3));
		int f = Integer.parseInt(timeMatcher.group(4));
		
		long hToSec = TimeUnit.HOURS.toSeconds(h);
		long mToSec = TimeUnit.MINUTES.toSeconds(m);
		
		int tot = (int) (hToSec + mToSec + s);
		if (f > 50) tot = tot + 1;
		
		//logger("v", "h=" + h + " m=" + m + " s=" + s + "." + f + " -> tot=" + tot,	DEBUG_TAG);
		return tot;
	}
}

// ---------------------------------------------------------

	/*
	 *  to get the name of an executing method, 
	 *  call this from inside the method itself:
	 *  
	 *  String name = new Exception().getStackTrace()[0].getMethodName();
	 *  Log.i(DEBUG_TAG, "==> " + name);
	 */
	
