package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Corpus {

	/**
	 * 读取词表，一行一个词
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static List<String> getVocab(String filename) throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		List<String> vocab = new ArrayList<String>();

		while ((line = reader.readLine()) != null) {
			vocab.add(line.substring(line.indexOf(":") + 1, line.length()));
		}
		reader.close();
		return vocab;
	}

	/**
	 * 读取文档集，一行一篇文章
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static int[][] getDocuments(String filename) throws IOException {

		File f = new File(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
		String line = "";

		List<String> docsLines = new ArrayList<String>();

		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 0)
				docsLines.add(line);
		}
		reader.close();

		int[][] docs = new int[docsLines.size()][];

		for (int d = 0; d < docs.length; d++) {

			String doc = docsLines.get(d);
			String[] tokens = doc.trim().split(" ");

			docs[d] = new int[tokens.length];

			for (int n = 0; n < tokens.length; n++) {
				int wordid = Integer.parseInt(tokens[n]);
				docs[d][n] = wordid;
			}

		}

		return docs;
	}

	/**
	 * 出现某个词的文档个数
	 * 
	 * @param documents
	 * @param word
	 * @return
	 */
	public static int DocumentFrequency(int[][] documents, int word) {
		int count = 0;
		for (int i = 0; i < documents.length; i++) {
			for (int j = 0; j < documents[i].length; j++) {
				if (documents[i][j] == word) {
					count++;
					break;
				}

			}
		}
		return count;
	}

	/**
	 * 两个词共生的文档个数
	 * 
	 * @param documents
	 * @param word_i
	 * @param word_j
	 * @return
	 */
	public static int DocumentFrequency(int[][] documents, int word_i,
			int word_j) {
		int count = 0;
		for (int i = 0; i < documents.length; i++) {

			boolean exsit_i = false;
			boolean exsit_j = false;
			for (int j = 0; j < documents[i].length; j++) {
				if (documents[i][j] == word_i) {
					exsit_i = true;
					break;
				}
			}

			for (int j = 0; j < documents[i].length; j++) {
				if (documents[i][j] == word_j) {
					exsit_j = true;
					break;
				}
			}
			if (exsit_i && exsit_j)
				count++;

		}
		return count;
	}

	/**
	 * 主题的语义一致性 (EMNLP'11)
	 * 
	 * @param top_words
	 *            主题的top words
	 * @param documents
	 *            文档集
	 * @return 语义一致性
	 */
	public static double coherence(int[] top_words, int[][] documents) {

		double coherence_score = 0.0;
		for (int m = 1; m < top_words.length; m++) {
			for (int l = 0; l < m; l++) {

				if (top_words[m] != top_words[l])
					coherence_score += Math.log((double) (DocumentFrequency(
							documents, top_words[m], top_words[l]) + 1)
							/ DocumentFrequency(documents, top_words[l]));
				else
					coherence_score += Math.log((double) 2
							/ DocumentFrequency(documents, top_words[l]));

			}
		}
		return coherence_score;
	}

	/**
	 * 主题的语义一致性(EACL'14)
	 * 
	 * @param top_words
	 *            主题的top words
	 * @param documents
	 *            文档集
	 * @return 正则化互信息
	 */
	public static double NPMI(int[] top_words, int[][] documents) {

		double coherence_score = 0.0;

		int window_size = documents.length;
		for (int m = 1; m < top_words.length; m++) {
			for (int l = 0; l < m; l++) {

				int co_occurrence = DocumentFrequency(documents, top_words[m],
						top_words[l]);

				int occurrence_1 = DocumentFrequency(documents, top_words[l]);

				int occurrence_2 = DocumentFrequency(documents, top_words[m]);

				if (co_occurrence * occurrence_1 * occurrence_2 != 0) {

					coherence_score += Math
							.log((double) (co_occurrence * window_size)
									/ (occurrence_1 * occurrence_2))
							/ (-1.0 * Math.log((double) (co_occurrence)
									/ window_size));

				}

			}
		}
		return coherence_score;
	}

	/**
	 * 主题的语义一致性(NAACL'10)
	 * 
	 * @param top_words
	 *            主题的top words
	 * @param documents
	 *            文档集
	 * @return 互信息
	 */
	public static double PMI(int[] top_words, int[][] documents) {

		double coherence_score = 0.0;

		int window_size = documents.length;
		for (int m = 1; m < top_words.length; m++) {
			for (int l = 0; l < m; l++) {

				int co_occurrence = DocumentFrequency(documents, top_words[m],
						top_words[l]);

				int occurrence_1 = DocumentFrequency(documents, top_words[l]);

				int occurrence_2 = DocumentFrequency(documents, top_words[m]);

				if (co_occurrence * occurrence_1 * occurrence_2 != 0) {

					coherence_score += Math
							.log((double) (co_occurrence * window_size)
									/ (occurrence_1 * occurrence_2));

				}

			}
		}
		return coherence_score;
	}

	/**
	 * K个主题的平均语义一致性
	 * 
	 * @param docs
	 * @param phi
	 * @param top_words_size
	 * @return
	 */
	public static double average_coherence(int[][] docs, double[][] phi,
			int top_words_size) {

		double total_coherence = 0;

		for (double[] phi_t : phi) {

			int[] top_words = new int[top_words_size];

			for (int i = 0; i < top_words_size; i++) {

				int max_index = Common.maxIndex(phi_t);
				top_words[i] = max_index;
				phi_t[max_index] = 0;

			}

			double coherence = coherence(top_words, docs);
			total_coherence += coherence;
		}
		double average_coherence = total_coherence / phi.length;
		return average_coherence;
	}

	/**
	 * LDA主题模型的Training perplexity
	 * 
	 * @param theta
	 *            文档-主题分布
	 * @param phi
	 *            主题-词分布
	 * @param docs
	 *            文档集
	 * @return
	 */
	public static double perplexity(double[][] theta, double[][] phi,
			int[][] docs) {
		double perplexity = 0.0;

		int total_length = 0;
		for (int i = 0; i < docs.length; i++) {
			total_length += docs[i].length;
		}

		for (int i = 0; i < docs.length; i++) {

			for (int j = 0; j < docs[i].length; j++) {

				double prob = 0.0;
				for (int k = 0; k < phi.length; k++) {
					prob += theta[i][k] * phi[k][docs[i][j]];
				}

				perplexity += Math.log(prob);

			}
		}

		perplexity = Math.exp(-1 * perplexity / total_length);

		return perplexity;
	}

	/**
	 * 获取一个词在文档集中的IDF
	 * 
	 * @param documents
	 * @param word
	 * @return
	 */
	public static double IDF(int[][] documents, int word) {
		int count = 0;

		for (int[] document : documents) {

			for (int e : document) {
				if (e == word) {
					count++;
					break;
				}

			}
		}
		return Math.log((double) documents.length / count);
	}

	/**
	 * 统计词在文档中的词频
	 * 
	 * @param documents
	 * @param word
	 * @return
	 */
	public static int TF(int[] document, int word) {
		int count = 0;
		for (int e : document) {
			if (e == word)
				count++;
		}
		return count;
	}

}
