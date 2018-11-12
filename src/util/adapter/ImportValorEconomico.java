package util.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.mongodb.util.JSON;

import io.db.SaveDocuments;

public class ImportValorEconomico implements Runnable {

	private List<File> lsFiles = null;
	
	public ImportValorEconomico(List<File> lsFiles) {
		this.lsFiles = lsFiles;
	}
	
	public static void main(String[] args){
		File folder = new File("");
		List<File> listOfFiles = Arrays.asList(folder.listFiles());
		int length = listOfFiles.size() / 4;//Validar isso.
		
		for(int i = 0; i < 4; i++){
			ImportValorEconomico ive = new ImportValorEconomico(listOfFiles.subList(length * i, i < 4 ? length * (i + 1) : listOfFiles.size()));
			
			new Thread(ive);
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (File file : lsFiles) {
			BufferedReader br = null;
			FileReader fr = null;

			try {

				fr = new FileReader(file);
				br = new BufferedReader(fr);

				StringBuilder str = new StringBuilder();
				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
					str.append(sCurrentLine);
				}
				
				String[] contents = str.toString().substring(1, str.length() - 2).split("\";\"");
				String[] date = contents[0].split("/");
				
				JSON json = (JSON) JSON.parse("{'title' : " + contents[5]
						+ "'text' : " + contents[4]
						+ "'date' : " + date[2] + "." + date[1] + "." + date[0]
						+ "'url' : " + contents[6]
						+ "'source' : 'valor economico'"
						+ "'type' : 'news'}");
				
				System.out.println("URL: <" + contents[6] + ">");
				SaveDocuments sd = new SaveDocuments("localhost", "db_news_brasil", "documents");
				sd.insertDocument(json);

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
}
