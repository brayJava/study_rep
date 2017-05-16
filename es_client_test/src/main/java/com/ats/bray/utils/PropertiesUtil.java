package com.ats.bray.utils;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil
{
	static Properties props = new Properties();

	static
	{
		try
		{
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sys.properties");
			props.load(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getProperty(String key)
	{
		String value = null;
		try {
			 value = props.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

}
