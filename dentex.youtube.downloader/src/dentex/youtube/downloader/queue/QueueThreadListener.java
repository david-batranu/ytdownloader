package dentex.youtube.downloader.queue;

/*
 * ref: http://mindtherobot.com/blog/159/android-guts-intro-to-loopers-and-handlers/
 * by Ivan Memruk: http://mindtherobot.com/blog/about/
 */

public interface QueueThreadListener {

	void handleQueueThreadUpdate();
}
