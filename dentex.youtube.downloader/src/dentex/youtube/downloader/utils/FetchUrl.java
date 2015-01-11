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

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

import com.matsuhiro.android.connect.NetworkUtils;

public class FetchUrl {
	
	private Context ctx;
	
	public FetchUrl(Context ctx) {
		this.ctx = ctx;
	}
	
	String DEBUG_TAG = "FetchUrl";
	
	public String doFetch(String url) {
        try {
        	Utils.logger("d", "Trying url: " + url, DEBUG_TAG);
            return downloadWebPage(url);
        } catch (IOException e) {
        	Log.e(DEBUG_TAG, "doFetch IOException: " + e.getMessage());
            return "e";
        } catch (RuntimeException e) {
        	Log.e(DEBUG_TAG, "doFetch RuntimeException: " + e.getMessage());
	    	return "e";
        }
    }

    private String downloadWebPage(String myurl) throws IOException {
    	HttpClient httpclient = new DefaultHttpClient();
    	HttpGet httpget = new HttpGet(myurl); 
    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	
    	if (NetworkUtils.isNetworkAvailable(ctx)) {
    		String responseBody = httpclient.execute(httpget, responseHandler);
    		httpclient.getConnectionManager().shutdown();
    		return responseBody;
    	} else {
    		Log.e(DEBUG_TAG, "doFetch: Network not Available");
    		return "e";
    	}
	}
}
