package util.commom;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Properties {

	private static java.util.Properties prop = new java.util.Properties();
	private static InputStream input = null;
		
	public static String getProperty(String key){	

		
		try {
			if(input == null)
				input = new FileInputStream("config.properties");
			
			if(prop.isEmpty())
				prop.load(input);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prop.getProperty(key);
	}
	
}
