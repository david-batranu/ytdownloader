package dentex.youtube.downloader.queue;

import java.io.File;

import android.content.Context;
import android.util.Log;
import dentex.youtube.downloader.DashboardActivity;
import dentex.youtube.downloader.DashboardListItem;
import dentex.youtube.downloader.ffmpeg.FfmpegController;
import dentex.youtube.downloader.ffmpeg.ShellUtils.ShellCallback;
import dentex.youtube.downloader.utils.Utils;

public class FFmpegExtractFlvThumbTask implements Runnable {

	private static final String DEBUG_TAG = "FFmpegExtractFlvThumbTask";
	private Context aContext;
	private File aFileToConvert;
	private File aPngFile;
	
	public FFmpegExtractFlvThumbTask(Context context, 
			File fileToConvert, File pngFile) {
		aContext = context;
		aFileToConvert = fileToConvert;
		aPngFile = pngFile;
	}
	
	@Override
	public void run() {
		FfmpegController ffmpeg = null;
		try {
			ffmpeg = new FfmpegController(aContext);
			ShellDummy shell = new ShellDummy();
			ffmpeg.extractFlvThumb(aFileToConvert, aPngFile, null, shell);
		} catch (Throwable t) {
			Log.e(DEBUG_TAG, "Error in FFmpegExtractFlvThumbTask", t);
		}
	}
	
	private class ShellDummy implements ShellCallback {

		@Override
		public void shellOut(String shellLine) {
			//Utils.logger("d", shellLine, DEBUG_TAG);
		}

		@Override
		public void processComplete(DashboardListItem item, int exitValue) {
			Utils.logger("v", aPngFile.getName() + ": processComplete with exit value: " + exitValue, DEBUG_TAG);

			DashboardActivity.refreshlist();
		}

		@Override
		public void processNotStartedCheck(boolean started) {
			if (!started) {
				Utils.logger("w", "FFmpegExtractFlvThumbTask process not started or not completed", DEBUG_TAG);
			}
		}

		@Override
		public void preProcess() {
			// unused
		}
	}
}
