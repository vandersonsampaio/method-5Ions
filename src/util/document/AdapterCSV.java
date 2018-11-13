package util.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AdapterCSV {

	public static void main(String[] args) {

		BufferedWriter bw = null;
		FileWriter fw = null;

		BufferedReader br = null;
		FileReader fr = null;

		try {
			fr = new FileReader("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Moeda\\Valor Econômico.csv");
			br = new BufferedReader(fr);

			String content;
			int line = 1;
			while ((content = br.readLine()) != null) {
				System.out.println(line++);
				try {

					fw = new FileWriter("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Moeda\\Valor Econômico\\" + System.currentTimeMillis() + ".txt");
					bw = new BufferedWriter(fw);
					bw.write(content);

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (bw != null)
							bw.close();

						if (fw != null)
							fw.close();
						
						Thread.sleep(1000);
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}
	}
}
