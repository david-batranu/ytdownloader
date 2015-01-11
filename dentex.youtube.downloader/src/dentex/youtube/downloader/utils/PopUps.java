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

import dentex.youtube.downloader.R;
import dentex.youtube.downloader.YTD;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class PopUps {
	
	static int icon;

	public static void showPopUp(String title, String message, String type, Activity activity) {
        
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(activity);
	    helpBuilder.setTitle(title);
	    helpBuilder.setMessage(message);
	
	    String theme = YTD.settings.getString("choose_theme", "D");
    	if (theme.equals("D")) {
		    if ( type == "error" ) {
		        icon = R.drawable.ic_dialog_alert_holo_dark;
		    } else if ( type == "status" ) {
		        icon = R.drawable.ic_dialog_info_holo_dark;
		    }
    	} else {
    		if ( type == "error" ) {
		        icon = R.drawable.ic_dialog_alert_holo_light;
		    } else if ( type == "status" ) {
		        icon = R.drawable.ic_dialog_info_holo_light;
		    }
    	}
	
	    helpBuilder.setIcon(icon);
	    helpBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	
	        public void onClick(DialogInterface dialog, int which) {
	            // close the dialog
	        }
	    });
	
	    AlertDialog helpDialog = helpBuilder.create();
	    if (! activity.isFinishing()) {
	    	helpDialog.show();
	    } else {
	    	Utils.logger("w", "PopUp not showed", "PopUps");
	    }
	}
}
