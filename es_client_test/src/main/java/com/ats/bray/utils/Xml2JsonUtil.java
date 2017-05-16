package com.ats.bray.utils;

import net.sf.json.xml.XMLSerializer;

public class Xml2JsonUtil {

	
	public static String xml2JSON(String xml) {
		return new XMLSerializer().read(xml).toString(1);
	}
	

}
