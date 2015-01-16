package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import util.ReadWriteFile;

public class ProbaseCoherence {

	public static void main(String[] args) throws IOException {

		File[] files = new File("data//").listFiles();

		List<String> domain_list = new ArrayList<String>();

		for (File f : files) {
			String file_path = f.toString();
			String domain = file_path.substring(file_path.indexOf("\\") + 1,
					file_path.length());

			domain_list.add(domain);
		}

		StringBuilder sb = new StringBuilder();

		double average = 0;

		for (String domain : domain_list) {

			double score = getFileContent(domain);

			sb.append(domain + "\t" + score + "\n");

			average += score;

		}

		sb.append("average coherence\t" + average / 50 + "\n");

		String file = "output//ProbaseLDA_30//Probase_LDA_coherence.txt";

		ReadWriteFile.writeFile(file, sb.toString());

	}

	public static double getFileContent(String domain) throws IOException {

		File f = new File("output//ProbaseLDA_30//" + domain + ".txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		double score = 0;
		while ((line = reader.readLine()) != null) {

			if (line.contains("average coherence")) {
				String[] temp = line.trim().split("\t");
				score = Double.parseDouble(temp[1]);
			}

		}

		reader.close();

		return score;
	}

}
