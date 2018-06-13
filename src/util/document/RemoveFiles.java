package util.document;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RemoveFiles {
	
	private static final String FILENAME = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Sentimentos.txt";
	private static final String PATH = "C:\\Users\\Home\\Dropbox\\Mestrado\\Dissertação\\Dados\\Sentimentos\\DW_Alemao_Trans\\";
	private static final String EXT = ".sents";

	public static void main(String[] args) {
		BufferedReader br = null;
		FileReader fr = null;

		try {
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String lineFile;

			while ((lineFile = br.readLine()) != null) {
				System.out.println(lineFile);
				
				File file = new File(PATH + lineFile.trim() + EXT);
				
				if(file.isFile()){
					file.delete();
					System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed.");
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
