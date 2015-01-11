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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class RhinoRunner {
	
	static String DEBUG_TAG = "RhinoRunner";
	
	/*
	 * methods adapted from Stack Overflow:
	 * http://stackoverflow.com/questions/3995897/rhino-how-to-call-js-function-from-java/3996115#3996115
	 * 
	 * Q:http://stackoverflow.com/users/391441/instantsetsuna
	 * A:http://stackoverflow.com/users/72673/maurice-perry
	 */
	
	/*
     * "function decryptSignature(sig)" from the Javascript Greasemonkey script 
     * http://userscripts.org/scripts/show/25105 (released under the MIT License)
     * by Gantt: http://userscripts.org/users/gantt
     */
	
	public static String decipher(String S, String function) {
		Context rhino = Context.enter();
		rhino.setOptimizationLevel(-1);
		try {
		    ScriptableObject scope = rhino.initStandardObjects();
		    
		    /*Scriptable that = rhino.newObject(scope);
		    Function fct = rhino.compileFunction(scope, function, "script", 1, null);
		    
		    Object result = fct.call(rhino, scope, that, new Object[] {S});*/
		    
		    rhino.evaluateString(scope, function, "script", 1, null);
		    Function fct = (Function)scope.get("decryptSignature", scope);
		    
		    Object result = fct.call(rhino, scope, scope, new Object[] {S});
		    
		    return (String) Context.jsToJava(result, String.class);
		    
		} finally {
		    Context.exit();
		}
	}
	
	public static String[] obtainDecryptionArray(String code, String function) {
		Context rhino = Context.enter();
		rhino.setOptimizationLevel(-1);
		try {
		    ScriptableObject scope = rhino.initStandardObjects();

		    rhino.evaluateString(scope, function, "script", 1, null);
		    Function fct = (Function)scope.get("findSignatureCode", scope);
		    
		    Object result = fct.call(rhino, scope, scope, new Object[] {code});
		    
		    return (String[]) Context.jsToJava(result, String[].class);
		} catch (Exception e) {
			return new String[] { "e" };
		} finally {
		    Context.exit();
		}   
	}
}
