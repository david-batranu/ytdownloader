package dentex.youtube.downloader;

public class ShareActivityListItem {
	
	private String text;
	private int itag;
	
	public ShareActivityListItem(String text, int itag) {
		this.text = text;
		this.itag = itag;
	}
	
	public int getItag() {
		return itag;
	}

	public String getText() {
		return text;
	}
}
