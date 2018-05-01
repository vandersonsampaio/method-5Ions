package util.commom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Files {

	public static boolean existsFile(String path, String name, String extension) {
		File file = new File(path + File.separator + name + "." + extension);
		return file.exists();
	}

	public static List<String> getAllFileNames(String path) {
		List<String> listFileNames = new ArrayList<>();

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			
			if (listOfFiles[i].isFile()) {
				listFileNames.add(listOfFiles[i].getName());
			}
		}
		
		return listFileNames;
	}
}
