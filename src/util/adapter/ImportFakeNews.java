package util.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImportFakeNews implements Runnable {

	private final int NUMBERS = 100;
	List<StringBuilder> lsInsert;

	public ImportFakeNews() {
		lsInsert = null;
	}

	public ImportFakeNews(List<StringBuilder> lsInsert) {
		this.lsInsert = lsInsert;
	}

	private void splitCSV(String pathFile) {
		File file = new File(pathFile);
		List<StringBuilder> lsFiles = new ArrayList<StringBuilder>();
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.trim().equals(""))
					continue;
				
				String[] parts = sCurrentLine.split(",");
				
				//verifica se o primeiro termo é um número se for insiro str
				if(true){
					lsFiles.add(str);
					str = new StringBuilder();
				}
				
				str.append(sCurrentLine);
				

				if (lsFiles.size() == NUMBERS) {
					new Thread(new ImportFakeNews(lsFiles)).start();

					lsFiles.clear();
				}
			}
			
			if(lsFiles.size() > 0)
				new Thread(new ImportFakeNews(lsFiles)).start();
			
		} catch (IOException e) {
			System.out.println(e.toString());

		} finally {
			try {
				if (br != null)
					br.close();

			} catch (IOException ex) {
				System.out.println(ex.toString());

			}
		}
	}

	@Override
	public void run() {
		// Irei passar lista de arquivos para formatar e inserir no mongodb
		for (int i = 0; i < lsInsert.size(); i++) {

		}
	}

}
