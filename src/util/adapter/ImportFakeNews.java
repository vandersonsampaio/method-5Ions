package util.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import io.db.SaveDocuments;

public class ImportFakeNews implements Runnable {

	private final int NUMBERS = 5000;
	List<StringBuilder> lsInsert;

	public ImportFakeNews() {
		lsInsert = null;
	}

	public ImportFakeNews(List<StringBuilder> lsInsert) {
		this.lsInsert = lsInsert;
	}

	public static void main(String[] args) {
		new ImportFakeNews().splitCSV("D:\\news_cleaned_2018_02_13.csv");
	}

	private void splitCSV(String pathFile) {
		File file = new File(pathFile);
		List<StringBuilder> lsFiles = new ArrayList<StringBuilder>();
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

			// Recebe o cabeçalho
			String sCurrentLine = br.readLine();
			System.out.println(sCurrentLine);

			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.trim().equals(""))
					continue;

				String[] parts = sCurrentLine.split(",");
				if (parts.length > 2 && StringUtils.isNumeric(parts[0]) && StringUtils.isNumeric(parts[1])
						&& !parts[0].equals("0")) {
					lsFiles.add(str);
					str = new StringBuilder();
				}

				str.append(sCurrentLine);

				if (lsFiles.size() == NUMBERS) {
					new Thread(new ImportFakeNews(lsFiles)).start();

					lsFiles.clear();

					/*
					 * try { Thread.sleep(3000); } catch (InterruptedException e) { // TODO
					 * Auto-generated catch block e.printStackTrace(); }
					 */
				}
			}

			if (lsFiles.size() > 0)
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
		SaveDocuments sd = null;
		try {
			sd = new SaveDocuments("localhost", "db_poll_fakenews", "documents");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i = 0; i < lsInsert.size(); i++) {
			String[] parts = lsInsert.get(i).toString().split(",");
			String number = parts[0];
			String id = parts[1];
			String domain = parts[2];
			String type = parts[3];
			String url = parts[4];
			StringBuilder content = new StringBuilder();
			content.append(parts[5]);

			String scraped_at = "";
			String inserted_at = "";
			String updated_at = "";
			String title = "";
			String authors = "";
			String keywords = "";
			String meta_keywords = "";
			String meta_description = "";
			String tags = "";
			String summary = "";

			int j = 6;
			for (; j < parts.length; j++) {
				if (parts[j].split("-").length == 3 && parts[j].split(":").length == 3) {
					scraped_at = parts[j];
					break;
				} else {
					content.append(", ");
					content.append(parts[j]);
				}
			}
			if (j + 1 < parts.length) {
				inserted_at = parts[++j];
				updated_at = parts[++j];
				title = parts[++j];

				if (title.startsWith("\"")) {
					int k = ++j;

					for (; k < parts.length; k++) {
						title += ", " + parts[k];

						if (parts[k].endsWith("\""))
							break;
					}

					j = k;
				}

				if (j + 1 < parts.length) {
					authors = parts[++j];

					if (authors.startsWith("\"")) {
						int k = ++j;

						for (; k < parts.length; k++) {
							authors += ", " + parts[k];

							if (parts[k].endsWith("\""))
								break;
						}

						j = k;
					}

					if (j + 1 < parts.length) {
						keywords = parts[++j];

						if (keywords.startsWith("\"")) {
							int k = ++j;

							for (; k < parts.length; k++) {
								keywords += ", " + parts[k];

								if (parts[k].endsWith("\""))
									break;
							}

							j = k;
						}

						if (j + 1 < parts.length) {
							meta_keywords = parts[++j];

							if (meta_keywords.startsWith("\"")) {
								int k = ++j;

								for (; k < parts.length; k++) {
									meta_keywords += ", " + parts[k];

									if (parts[k].endsWith("\""))
										break;
								}

								j = k;
							}

							if (j + 1 < parts.length) {

								meta_description = parts[++j];

								if (meta_description.startsWith("\"")) {
									int k = ++j;

									for (; k < parts.length; k++) {
										meta_description += ", " + parts[k];

										if (parts[k].endsWith("\""))
											break;
									}

									j = k;
								}

								if (j + 1 < parts.length) {
									tags = parts[++j];

									if (tags.startsWith("\"")) {
										int k = ++j;

										for (; k < parts.length; k++) {
											tags += ", " + parts[k];

											if (parts[k].endsWith("\""))
												break;
										}

										j = k;
									}

									if (j + 1 < parts.length) {
										summary = parts[++j];

										if (summary.startsWith("\"")) {
											int k = ++j;

											for (; k < parts.length; k++) {
												summary += ", " + parts[k];

												if (parts[k].endsWith("\""))
													break;
											}

											j = k;
										}
									}
								}
							}
						}
					}
				}
			}

			// Inserir no banco de dados
			DBObject json = new BasicDBObject().append("title", title).append("text", content.toString())
					.append("date", scraped_at).append("url", url).append("source", domain).append("type", type)
					.append("language", "en").append("is_entityannotation", "false")
					.append("is_entitysentiment", "false").append("is_sentiment", "false").append("entities", null)
					.append("sentiments", null);

			System.out.println("URL: <" + url + ">");

			try {
				sd.insertDocument(json);
			} catch (@SuppressWarnings("deprecation") MongoException.DuplicateKey e) {
				System.out.println("Duplicado Ignorado");
			}

			// System.out.println(lsInsert.get(i).toString());

			// System.out.println(" = " + number + "\r\n" + "id = " + id + "\r\n" + "domain
			// = " + domain + "\r\n"
			// + "type = " + type + "\r\n" + "url = " + url + "\r\n" + "content = " +
			// content + "\r\n"
			// + "scraped_at = " + scraped_at + "\r\n" + "inserted_at = " + inserted_at +
			// "\r\n" + "updated_at = "
			// + updated_at + "\r\n" + "title = " + title + "\r\n" + "authors = " + authors
			// + "\r\n"
			// + "keywords = " + keywords + "\r\n" + "meta_keywords = " + meta_keywords +
			// "\r\n"
			// + "meta_description = " + meta_description + "\r\n" + "tags = " + tags +
			// "\r\n" + "summary = "
			// + summary + "\r\n\r\n");

		}
	}

}
