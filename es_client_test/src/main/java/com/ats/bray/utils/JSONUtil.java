package com.ats.bray.utils;

public class JSONUtil {


	
	 public static boolean getJSONType(String str){ 
	         
	         
	        final char[] strChar = str.substring(0, 1).toCharArray(); 
	        final char firstChar = strChar[0]; 
	         
//	        LogUtils.d(JSONUtil.class, "getJSONType", " firstChar = "+firstChar); 
	         
	        if(firstChar == '{'){ 
	            return true; 
	        }else if(firstChar == '['){ 
	            return false; 
	        }else{ 
	            return true; 
	        } 
	    }
}
