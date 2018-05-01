package core.summarization;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;

public class SumyPython {

	@SuppressWarnings("unchecked")
	public JSONObject summarizationLSA(String text, String date) {
		JSONObject json = new JSONObject();
		json.put("date", date);
		json.put("text", execCommand("lsa", text));
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject summarizationEdmundson(String text, String date) {
		JSONObject json = new JSONObject();
		json.put("date", date);
		json.put("text", execCommand("edmundson", text));
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject summarizationLuhn(String text, String date) {
		JSONObject json = new JSONObject();
		json.put("date", date);
		json.put("text", execCommand("luhn", text));
		return json;
	}
	
	public String execCommand(String method, String text) {
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec("sumy " + method + " --length=30% --text=\"" + text + "\"");
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			/*BufferedReader reader1 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			String line1 = "";
			while ((line1 = reader1.readLine()) != null) {
				output.append(line1 + "\n");
			}
			
			System.out.println(output);
			output = new StringBuffer();*/
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}
}
