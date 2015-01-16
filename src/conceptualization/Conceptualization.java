package conceptualization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prior.Prior;
import util.Corpus;
import util.Probase;
import util.ReadWriteFile;

public class Conceptualization {

	/**
	 * 对指定的领域用Naive bayes概念化，结果写文件
	 * 
	 * @param domain
	 *            领域名
	 * @return 实体-(概念-Rep score)映射
	 * @throws IOException
	 * @throws SQLException
	 */
	public static Map<String, Map<String, Double>> conceptualization(
			String domain) throws IOException, SQLException {

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		int[][] docs = Corpus.getDocuments("data//" + domain + "//" + domain
				+ ".docs");

		int line = 0;

		StringBuilder sb = new StringBuilder();

		Connection conn = Probase.getConnectionMySql();

		// 实体-(概念-Rep)
		Map<String, Map<String, Double>> entity_concept_rep = new HashMap<>();

		for (int[] doc : docs) {

			List<String> entities = new ArrayList<>();

			for (int word : doc) {
				entities.add(vocab.get(word));
			}

			String conceptual = Prior.getConceptualiztion(entities,
					entity_concept_rep, conn);

			line++;
			System.out.println(line + "\t" + conceptual);
			sb.append(line + "\t" + conceptual + "\n");

		}
		conn.close();

		String filename = "file//Conceptualization_" + domain
				+ "_NaiveBayes_0.8.txt";

		ReadWriteFile.writeFile(filename, sb.toString());

		return entity_concept_rep;

	}

	/**
	 * 获取领域语料库中概念化后的集合
	 * 
	 * @param domain
	 *            领域名
	 * @return
	 * @throws IOException
	 * 
	 */
	public static Set<String> getConceptualizationSet(String domain)
			throws IOException {

		String filename = "file//Conceptualization_" + domain
				+ "_NaiveBayes_0.8.txt";
		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		Set<String> concept_set = new HashSet<>();

		while ((line = reader.readLine()) != null) {

			String[] temp = line.trim().split("\t");

			for (int i = 0; i < temp.length; i++) {
				if (i != 0 && i < 4)
					concept_set.add(temp[i]);
			}

		}

		reader.close();

		return concept_set;
	}

}
