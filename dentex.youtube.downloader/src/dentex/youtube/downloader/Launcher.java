package dentex.youtube.downloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Launcher extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		String launcher = YTD.settings.getString("launcher", "settings");
		Class<?> cl = null;
		
		if (launcher.equals("settings")) cl = SettingsActivity.class;

		if (launcher.equals("dashboard")) cl = DashboardActivity.class;
		
	    startActivity(new Intent(this, cl));
	    finish();
	}
}
