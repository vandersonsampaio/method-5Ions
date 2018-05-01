package util.document;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.file.Load;
import io.file.Save;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslateDocument {
	@SuppressWarnings("unchecked")
	public static void main(String... args) {
		Translate translate = TranslateOptions.getDefaultInstance()
				.getService();

		Load load = new Load();
		Save save = new Save();
		save.setPath("C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DW_Alemao_Trans");
		save.setExtension("json");
		
		// Obtem o texto e o Titulo
		JSONArray jaDocuments = load
				.getDocuments("C:\\Users\\Vanderson\\Dropbox\\Mestrado\\Dissertação\\Dados\\DW_Alemao\\");

		for (int i = 2213; i < jaDocuments.size(); i++) {
			JSONObject joDocument = (JSONObject) jaDocuments.get(i);
			String tittle = joDocument.get("tittle").toString();
			String text = joDocument.get("text").toString();

			Translation transTittle = translate.translate(tittle,
					TranslateOption.sourceLanguage("de"),
					TranslateOption.targetLanguage("en"));

			
			Translation transText = translate.translate(text,
					TranslateOption.sourceLanguage("de"),
					TranslateOption.targetLanguage("en"));

			joDocument.remove("tittle");
			joDocument.remove("text");
			
			joDocument.put("tittle", transTittle.getTranslatedText());
			joDocument.put("text", transText.getTranslatedText());
			
			
			save.setFileName(joDocument.get("fileName").toString().replace(".json", ""));
			
			joDocument.remove("fileName");
			
			save.save(joDocument);
			
			// Salva a tradução
			System.out.println(i);
			System.out.printf("Tittle: %s%n", tittle);
			System.out.printf("Translation: %s%n",
					transTittle.getTranslatedText());
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

