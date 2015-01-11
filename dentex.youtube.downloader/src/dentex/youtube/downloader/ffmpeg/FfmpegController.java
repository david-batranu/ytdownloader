package dentex.youtube.downloader.ffmpeg;

/*  code adapted from: https://github.com/guardianproject/android-ffmpeg-java
 *  Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian
 *  See LICENSE for licensing information (GPL-3.0)
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import dentex.youtube.downloader.DashboardListItem;
import dentex.youtube.downloader.YTD;
import dentex.youtube.downloader.utils.Utils;

public class FfmpegController {

	private final static String DEBUG_TAG = "FfmpegController";
	
	public File mBinFileDir;
	public String mFfmpegBinPath;
	private Context mContext;

	public FfmpegController(Context context) throws FileNotFoundException, IOException {
		mContext = context;
		
		mBinFileDir = context.getDir("bin", 0);
		mFfmpegBinPath = new File(mBinFileDir, YTD.ffmpegBinName).getAbsolutePath();
	}
	
	public void execFFMPEG (List<String> cmd, DashboardListItem item, ShellUtils.ShellCallback sc) {
		execChmod(mFfmpegBinPath, "755");
		execProcess(cmd, item, sc);
	}
	
	public  void execChmod(String filepath, String code) {
		Utils.logger("v", "Trying to chmod '" + filepath + "' to: " + code, DEBUG_TAG);
		try {
			Runtime.getRuntime().exec("chmod " + code + " " + filepath);
			SystemClock.sleep(500);
		} catch (IOException e) {
			Log.e(DEBUG_TAG, "Error changing file permissions!", e);
		}
	}
	
	public  int execProcess(List<String> cmds, DashboardListItem item, ShellUtils.ShellCallback sc) {
		sc.preProcess();
		
		StringBuilder cmdlog = new StringBuilder();
		for (String cmd : cmds) {
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}
		Utils.logger("v", cmdlog.toString(), DEBUG_TAG);
		
		ProcessBuilder pb = new ProcessBuilder(/*"liblame.so"*/);
		
		Map<String, String> envMap = pb.environment();
		envMap.put("LD_LIBRARY_PATH", mContext.getApplicationInfo().nativeLibraryDir);

		pb.directory(mBinFileDir);
		pb.command(cmds);

    	Process process = null;
    	int exitVal = 1; // Default error
    	boolean started = true;
    	try {	
    		process = pb.start();
    	
    		// any error message?
    		StreamGobbler errorGobbler = new 
    				StreamGobbler(process.getErrorStream(), "ERROR", sc);            
        
    		// any output?
    		StreamGobbler outputGobbler = new 
    				StreamGobbler(process.getInputStream(), "OUTPUT", sc);
            
    		// kick them off
    		errorGobbler.start();
    		outputGobbler.start();
     
    		exitVal = process.waitFor();
        
    		sc.processComplete(item, exitVal);
    		
    	} catch (Exception e) {
    		Log.e(DEBUG_TAG, "Error executing ffmpeg command", e);
    		started = false;
    	} finally {
    		if (process != null) {
    			Utils.logger("v", "destroyng process", DEBUG_TAG);
    			process.destroy();
    		}
    		sc.processNotStartedCheck(started);
    	}
        return exitVal;
	}
	
	public void extractAudio(File videoIn, File audioOut, String bitrateType, String bitrateValue, 
			DashboardListItem item, ShellUtils.ShellCallback sc) throws IOException, InterruptedException {
		
		List<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBinPath);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(videoIn.getAbsolutePath());
		cmd.add("-vn");
		cmd.add("-acodec");
		
		if (bitrateValue != null) {
			cmd.add("libmp3lame"); 
			if (bitrateType.equals("CBR")) {
				cmd.add("-ab");
			} else {
				cmd.add("-aq");
			}
			cmd.add(bitrateValue);
		} else {
			cmd.add("copy");
		}
		
		cmd.add(audioOut.getAbsolutePath());

		execFFMPEG(cmd, item, sc);
	}
	
	public void extractFlvThumb(File videoIn, File pngOut, 
			DashboardListItem item, ShellUtils.ShellCallback sc) throws IOException, InterruptedException {
		
		List<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBinPath);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(videoIn.getAbsolutePath());
		cmd.add("-vframes");
		cmd.add("1");
		cmd.add("-an");
		cmd.add("-ss");
		cmd.add("00:00:02");
		cmd.add("-s");
		cmd.add("320x180");
		cmd.add(pngOut.getAbsolutePath());

		execFFMPEG(cmd, item, sc);
		
	}
	
	public void downloadAndMuxAoVoStreams(String aoLink, String voLink, File out, 
			DashboardListItem item, ShellUtils.ShellCallback sc) throws IOException, InterruptedException {
		
		List<String> cmd = new ArrayList<String>();
		
		cmd.add(mFfmpegBinPath);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(aoLink);
		cmd.add("-i");
		cmd.add(voLink);
		cmd.add("-acodec");
		cmd.add("copy");
		cmd.add("-vcodec");
		cmd.add("copy");
		cmd.add(out.getAbsolutePath());
		
		execFFMPEG(cmd, item, sc);
	}
	
	class StreamGobbler extends Thread {
	    InputStream is;
	    String type;
	    ShellUtils.ShellCallback sc;
	    
	    StreamGobbler(InputStream is, String type, ShellUtils.ShellCallback sc) {
	        this.is = is;
	        this.type = type;
	        this.sc = sc;
		}
	    
	    public void run() {
	    	try {
	    		InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ((line = br.readLine()) != null) {
	            	if (sc != null) {
	            		sc.shellOut(line);
	            	}
	            }
	        } catch (IOException ioe) {
	                Log.e(DEBUG_TAG,"error reading shell log", ioe);
	        }
	    }
	}
}