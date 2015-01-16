package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.Dataset;
import prior.Prior;
import util.Probase;
import util.ReadWriteFile;
import conceptcluster.Kmedoids;

/**
 * @author Liang Yao
 * 
 * @description 尝试对一个主题的十个词概念化,寻找新的概念空间
 *
 */
public class RunTopicConceptualization {

	public static void main(String[] args) throws Exception {

		String domain = "Computer";

		// 测试将Topic概念化

		Connection conn = Probase.getConnectionMySql();

		String[][] top_words_array = getTopicalWords(domain);

		Set<String> concept_set = new HashSet<>();

		for (String[] topic : top_words_array) {

			for (String entity : topic) {

				Map<String, Double> concept_rep_map = Probase.getConceptList(
						conn, entity);
				concept_set.addAll(concept_rep_map.keySet());

			}

		}

		conn.close();

		List<String> concepts = new ArrayList<>(concept_set);

		Dataset data = Prior.getConceptEntityRepSparseDataSet(concepts);

		int[] assignment = Kmedoids.RunKmedoidsCosine(data, 15);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < assignment.length; i++) {
			sb.append(concepts.get(i) + ":" + assignment[i] + "\n");
		}

		String filename = "file//concept_cluster.txt";

		ReadWriteFile.writeFile(filename, sb.toString());

	}

	/**
	 * 获得指定domain的Topic列表
	 * 
	 * @param domain
	 * @return
	 * @throws IOException
	 * 
	 */
	public static String[][] getTopicalWords(String domain) throws IOException {

		File f = new File("output//LTM//LearningIteration0//DomainModels//"
				+ domain + "//" + domain + ".twords");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		reader.readLine();

		String[][] top_words_array = new String[15][20];

		int line_no = 0;

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split("\t");

			for (int i = 0; i < temp.length; i++) {
				top_words_array[i][line_no] = temp[i];

			}

			line_no++;

			if (line_no == 20)
				break;

		}

		reader.close();

		return top_words_array;

	}

}
