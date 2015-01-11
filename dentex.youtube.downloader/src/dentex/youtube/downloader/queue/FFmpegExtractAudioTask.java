package dentex.youtube.downloader.queue;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import dentex.youtube.downloader.DashboardActivity;
import dentex.youtube.downloader.DashboardListItem;
import dentex.youtube.downloader.YTD;
import dentex.youtube.downloader.ffmpeg.FfmpegController;
import dentex.youtube.downloader.ffmpeg.ShellUtils.ShellCallback;
import dentex.youtube.downloader.utils.JsonHelper;
import dentex.youtube.downloader.utils.Utils;

public class FFmpegExtractAudioTask implements Runnable {

	private static final String DEBUG_TAG = "FFmpegExtractAudioTask";
	private Context aContext;
	private File aFileToConvert;
	private File aAudioFile;
	private String aBitrateType = "";
	private String aBitrateValue = "";
	private String aId = "";
	private String aYtId = "";
	private int aPos = 0;
	private String type = "";
	private long aNewId = 0;
	int totSeconds = 0;
	int currentTime = 0;
	
	public FFmpegExtractAudioTask(Context context, long newId, 
			File fileToConvert, File audioFile, 
			String bitrateType, String bitrateValue, 
			String id, String YtId, int pos) {
		
		aContext = context;
		aFileToConvert = fileToConvert;
		aAudioFile = audioFile;
		aBitrateType = bitrateType;
		aBitrateValue = bitrateValue;
		aId = id;
		aYtId = YtId;
		aPos = pos;
		aNewId = newId;
		type = (aBitrateValue == null) ? YTD.JSON_DATA_TYPE_A_M : YTD.JSON_DATA_TYPE_A_E;
	}
	
	@Override
	public void run() {
		FfmpegController ffmpeg = null;
		try {
			ffmpeg = new FfmpegController(aContext);
			ShellDummy shell = new ShellDummy();
			ffmpeg.extractAudio(aFileToConvert, aAudioFile, aBitrateType, aBitrateValue, null, shell);
		} catch (Throwable t) {
			Log.e(DEBUG_TAG, "Error in FFmpegExtractAudioTask", t);
		}
	}
	
	private class ShellDummy implements ShellCallback {
		
		@Override
		public void preProcess() {
			JsonHelper.addEntryToJsonFile(
					String.valueOf(aNewId), 
					type,
					aYtId, 
					aPos,
					YTD.JSON_DATA_STATUS_IN_PROGRESS,
					aAudioFile.getParent(), 
					aAudioFile.getName(), 
					Utils.getFileNameWithoutExt(aAudioFile.getName()), 
					"", 
					"-", 
					false);
			
			DashboardActivity.refreshlist();
		}

		@Override
		public void shellOut(String shellLine) {
			Utils.logger("d", shellLine, DEBUG_TAG);
			getAudioJobProgress(shellLine);
		}

		@Override
		public void processComplete(DashboardListItem item, int exitValue) {
			Utils.logger("i", "FFmpeg process exit value: " + exitValue, DEBUG_TAG);
			
			if (exitValue == 0) {
				Utils.scanMedia(aContext, 
						new String[] {aAudioFile.getAbsolutePath()}, 
						new String[] {"audio/*"});
				
				boolean removeVideo = YTD.settings.getBoolean("ffmpeg_auto_rem_video", false);
				Utils.logger("d", "ffmpeg_auto_rem_video: " + removeVideo, DEBUG_TAG);
				if (removeVideo) {
					new AsyncDelete().execute(aFileToConvert);
				}

				JsonHelper.addEntryToJsonFile(
						String.valueOf(aNewId), 
						type, 
						aYtId, 
						aPos,
						YTD.JSON_DATA_STATUS_COMPLETED,
						aAudioFile.getParent(), 
						aAudioFile.getName(), 
						Utils.getFileNameWithoutExt(aAudioFile.getName()), 
						"", 
						Utils.MakeSizeHumanReadable((int) aAudioFile.length(), false), 
						false);
			} else {
				JsonHelper.addEntryToJsonFile(
						String.valueOf(aNewId), 
						type, 
						aYtId, 
						aPos,
						YTD.JSON_DATA_STATUS_FAILED,
						aAudioFile.getParent(), 
						aAudioFile.getName(), 
						Utils.getFileNameWithoutExt(aAudioFile.getName()), 
						"", 
						"-", 
						false);
			}
			
			DashboardActivity.refreshlist();
		}

		@Override
		public void processNotStartedCheck(boolean started) {
			if (!started) {
				Utils.logger("w", "FFmpegExtractAudioTask process not started or not completed", DEBUG_TAG);
			}
		}
	}
	
	private void getAudioJobProgress(String shellLine) {
		int mDownloadPercent;
		
		Pattern initPattern = Pattern.compile("ffmpeg version 2.1");
		Matcher initMatcher = initPattern.matcher(shellLine);
		if (initMatcher.find()) {
			totSeconds = 0;
			currentTime = 0;
		}
		
		Pattern totalTimePattern = Pattern.compile("Duration: (..):(..):(..)\\.(..)");
		Matcher totalTimeMatcher = totalTimePattern.matcher(shellLine);
		if (totalTimeMatcher.find())
			totSeconds = Utils.getTotSeconds(totalTimeMatcher);
		
		Pattern currentTimePattern = Pattern.compile("time=(..):(..):(..)\\.(..)");
		Matcher currentTimeMatcher = currentTimePattern.matcher(shellLine);
		if (currentTimeMatcher.find())
			currentTime = Utils.getTotSeconds(currentTimeMatcher);
		
		if (totSeconds == 0) {
            mDownloadPercent = -1;
        } else {
            mDownloadPercent = (int) (currentTime * 100 / totSeconds);
        }
		
		//Utils.logger("i", currentTime + "/" + totSeconds + " -> " + mDownloadPercent, DEBUG_TAG);
        YTD.mFFmpegPercentMap.put(aNewId, mDownloadPercent);
	}
	
	private class AsyncDelete extends AsyncTask<File, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			DashboardActivity.dashboardAsyncTaskInProgress((Activity) aContext, true);
		}
		
		@Override
		protected Boolean doInBackground(File... file) {
			if (file[0].exists() && file[0].delete()) {
				// remove library reference
				try {
					String mediaUriString = Utils.getContentUriFromFile(file[0], aContext.getContentResolver());
					Utils.removeFromMediaStore(aContext, file[0], mediaUriString);
				} catch (NullPointerException e) {
					Utils.logger("w", file[0].getName() + " UriString NOT found", DEBUG_TAG);
				}
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				JsonHelper.removeEntryFromJsonFile(aId);
				DashboardActivity.refreshlist();
			} else {
				Utils.logger("w", aFileToConvert.getName() + " NOT deleted", DEBUG_TAG);
			}
			DashboardActivity.dashboardAsyncTaskInProgress((Activity) aContext, false);
		}
	}
}
