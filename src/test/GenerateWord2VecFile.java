package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.Corpus;
import util.ReadWriteFile;

public class GenerateWord2VecFile {

	public static void main(String[] args) throws IOException {

		File[] files = new File("data//").listFiles();

		List<String> domain_list = new ArrayList<String>();

		for (File f : files) {
			String file_path = f.toString();
			String domain = file_path.substring(file_path.indexOf("\\") + 1,
					file_path.length());

			domain_list.add(domain);
		}

		for (String domain : domain_list) {

			StringBuilder sb = new StringBuilder();

			List<String> vocab = Corpus.getVocab("data//" + domain + "//"
					+ domain + ".vocab");

			int[][] docs = Corpus.getDocuments("data//" + domain + "//"
					+ domain + ".docs");

			for (int[] doc : docs) {

				for (int word : doc) {

					sb.append(vocab.get(word) + " ");
				}
				sb.append("\n");
			}

			String content = sb.toString().replaceAll(" \n", "\n");

			ReadWriteFile.writeFile("file//docs//" + domain + "_docs.txt",
					content);

		}

	}

}
