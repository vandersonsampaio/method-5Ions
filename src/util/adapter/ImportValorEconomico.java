package util.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import io.db.SaveDocuments;

public class ImportValorEconomico implements Runnable {

	private List<File> lsFiles = null;
	private int number;

	public ImportValorEconomico(int number, List<File> lsFiles) {
		this.lsFiles = lsFiles;
		this.number = number;
	}

	public static void main(String[] args) throws InterruptedException {
		File folder = new File("C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Moeda\\Valor Econômico");
		List<File> listOfFiles = Arrays.asList(folder.listFiles());

		int length = listOfFiles.size() / 4;

		for (int i = 0; i < 4; i++) {
			ImportValorEconomico ive = new ImportValorEconomico(i,
					listOfFiles.subList(length * i, i + 1 < 4 ? length * (i + 1) : listOfFiles.size()));
			(new Thread(ive)).start();
		}
	}

	public int size() {
		return lsFiles.size();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		SaveDocuments sd = null;
		try {
			sd = new SaveDocuments("localhost", "db_news_brazil", "documents");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (File file : lsFiles) {
			System.out.println("Thread n: " + number);
			BufferedReader br = null;

			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

				StringBuilder str = new StringBuilder();
				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
					str.append(sCurrentLine);
				}

				String[] contents = str.toString().substring(1, str.length() - 2).split("\";\"");
				String[] date = contents[0].split("/");

				DBObject json = new BasicDBObject().append("title", contents[5]).append("text", contents[4])
						.append("date", date[2] + "-" + date[1] + "-" + date[0]).append("url", contents[6])
						.append("source", "valor economico").append("type", "news");

				System.out.println("URL: <" + contents[6] + ">");

				try {
					sd.insertDocument(json);
				} catch (@SuppressWarnings("deprecation") MongoException.DuplicateKey e) {
					System.out.println("Duplicado Ignorado");
				}

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
	}
}
