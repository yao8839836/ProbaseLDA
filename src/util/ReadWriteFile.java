package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class ReadWriteFile {

	/**
	 * 将指定内容写入指定文件
	 * 
	 * @param file
	 * @param content
	 * @throws IOException
	 */
	public static void writeFile(String file, String content)
			throws IOException {

		File result = new File(file);
		OutputStream out = new FileOutputStream(result, false);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
				"utf-8"));

		bw.write(content);

		bw.close();
		out.close();
	}

	/**
	 * 将概念实体矩阵写入Weka的Arff文件
	 *
	 * @param file
	 * @param matrix
	 * @throws IOException
	 */
	public static void writeFile(String file, float[][] matrix)
			throws IOException {

		File result = new File(file);
		OutputStream out = new FileOutputStream(result, false);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,
				"utf-8"));

		bw.write("@relation concept_entity\n\n");

		int feature_size = matrix[0].length;

		for (int i = 0; i < feature_size; i++) {
			bw.write("@attribute entity_" + i + " real\n");
		}

		bw.newLine();

		bw.write("@data\n");

		for (int n = 0; n < matrix.length; n++) {
			for (int i = 0; i < feature_size - 1; i++) {

				bw.write(matrix[n][i] + ",");

			}

			bw.write(matrix[n][feature_size - 1] + "\n");
		}

		bw.close();
		out.close();
	}

	/**
	 * 读入concept属于的簇
	 * 
	 * @param filename
	 *            文件名
	 * @return 概念-簇 映射
	 * @throws IOException
	 */
	public static Map<String, Integer> getConceptCluster(String filename)
			throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		Map<String, Integer> concept_cluster = new HashMap<>();

		while ((line = reader.readLine()) != null) {

			String[] temp = line.split(":");
			concept_cluster.put(temp[0], Integer.parseInt(temp[1]));
		}

		reader.close();

		return concept_cluster;
	}

}
