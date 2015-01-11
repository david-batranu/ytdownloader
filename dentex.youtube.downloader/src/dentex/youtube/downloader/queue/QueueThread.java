package dentex.youtube.downloader.queue;

import dentex.youtube.downloader.DashboardActivity;
import dentex.youtube.downloader.R;
import dentex.youtube.downloader.YTD;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*
 * code adapted from:
 * http://mindtherobot.com/blog/159/android-guts-intro-to-loopers-and-handlers/
 * by Ivan Memruk: http://mindtherobot.com/blog/about/
 */

public final class QueueThread extends Thread {

	private static final String DEBUG_TAG = QueueThread.class.getSimpleName();
	
	private NotificationCompat.Builder aBuilder;
	private NotificationManager aNotificationManager;
	
	private Handler handler;
	
	private int totalQueued;
	
	private int totalCompleted;
	
	private QueueThreadListener listener;
	
	public QueueThread(QueueThreadListener listener) {
		this.listener = listener;
	}
	
	public QueueThreadListener getListener() {
		return this.listener;
	}
	
	@Override
	public void run() {
		try {
			// preparing a looper on current thread			
			// the current thread is being detected implicitly
			Looper.prepare();

			//Log.v(DEBUG_TAG, "QueueThread entering the loop");

			// now, the handler will automatically bind to the
			// Looper that is attached to the current thread
			// You don't need to specify the Looper explicitly
			handler = new Handler();
			
			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();
			
			//Log.v(DEBUG_TAG, "QueueThread exiting gracefully");
		} catch (Throwable t) {
			Log.e(DEBUG_TAG, "QueueThread halted due to an error", t);
		} 
	}
	
	// This method is allowed to be called from any thread
	public synchronized void requestStop() {
		// using the handler, post a Runnable that will quit()
		// the Looper attached to our QueueThread
		// obviously, all previously queued tasks will be executed
		// before the loop gets the quit Runnable
		handler.post(new Runnable() {
			@Override
			public void run() {
				// This is guaranteed to run on the QueueThread
				// so we can use myLooper() to get its looper
				Log.i(DEBUG_TAG, "QueueThread loop quitting by request");
				
				Looper.myLooper().quit();
			}
		});
	}
	
	public synchronized void enqueueTask(final Runnable task, final int type) {
		// Wrap TestTask into another Runnable to track the statistics
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					task.run();
				} finally {					
					// register task completion
					synchronized (QueueThread.this) {
						// type is 0 when the task is an Audio Extraction
						if (type == YTD._AUDIO_EXTR) totalCompleted++;
					}
					// tell the listener something has happened
					if (type == YTD._AUDIO_EXTR) signalUpdate();
				}				
			}
		});
		
		if (type == YTD._AUDIO_EXTR) {
			totalQueued++;
			// tell the listeners the queue is now longer
			signalUpdate();
		}
	}
	
	public synchronized int getTotalQueued() {
		return totalQueued;
	}
	
	public synchronized int getTotalCompleted() {
		return totalCompleted;
	}
	
	public void resetQueue() {
		totalQueued = 0;
		totalCompleted = 0;
	}
	
	/*public synchronized int getType() {
		return type;
	}*/
	
	// In case the listener it's a UI component,
	// it has to execute the signal handling code
	// in the UI thread using 'Handler'
	private void signalUpdate() {
		if (listener != null) {
			listener.handleQueueThreadUpdate();
		}
	}

	public synchronized void pushNotificationText(Context ctx, String text, boolean isOngoing) {
		aBuilder =  new NotificationCompat.Builder(ctx);
		aNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent notificationIntent = new Intent(ctx, DashboardActivity.class);
    	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	
    	PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
		
		aBuilder.setSmallIcon(R.drawable.ic_stat_ytd)
			.setContentTitle(ctx.getString(R.string.app_name))
			.setContentText(text)
			.setOngoing(isOngoing)
			.setContentIntent(contentIntent);
		
		aNotificationManager.notify(3, aBuilder.build());
	}
}
