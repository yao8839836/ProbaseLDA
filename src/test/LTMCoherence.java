package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import util.Corpus;
import util.ReadWriteFile;

public class LTMCoherence {

	public static void main(String[] args) throws IOException {

		File[] files = new File("data//").listFiles();

		List<String> domain_list = new ArrayList<String>();

		for (File f : files) {
			String file_path = f.toString();
			String domain = file_path.substring(file_path.indexOf("\\") + 1,
					file_path.length());

			domain_list.add(domain);
		}

		double coherence = 0;

		StringBuilder sb = new StringBuilder();

		for (String domain : domain_list) {

			double domain_coherence = coherence(domain);
			coherence += domain_coherence;
			sb.append(domain + "\t" + domain_coherence + "\n");
		}

		sb.append("average\t" + coherence / domain_list.size() + "\n");

		String filename = "file//GKLDA_top30_word2vec.txt";

		ReadWriteFile.writeFile(filename, sb.toString());

	}

	/**
	 * LTM 每个领域的coherence
	 * 
	 * @param domain
	 * @return
	 * @throws IOException
	 */
	public static double coherence(String domain) throws IOException {

		File f = new File("output//GKLDA//GKLDA_word2vec//DomainModels//"
				+ domain + "//" + domain + ".twords");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		reader.readLine();

		List<String> vocab = Corpus
				.getVocab("output//LTM//LearningIteration6//DomainModels//"
						+ domain + "//" + domain + ".vocab");

		int[][] docs = Corpus
				.getDocuments("output//LTM//LearningIteration4//DomainModels//"
						+ domain + "//" + domain + ".docs");

		double coherence = 0;

		int[][] top_words_array = new int[15][30];

		int line_no = 0;

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			for (int i = 0; i < temp.length; i++) {
				top_words_array[i][line_no] = vocab.indexOf(temp[i]);

			}

			line_no++;

			if (line_no == 30)
				break;

		}

		reader.close();

		for (int i = 0; i < 15; i++) {

			coherence += Corpus.coherence(top_words_array[i], docs);

		}

		System.out.println(coherence / 15);

		return coherence / 15;
	}
}
