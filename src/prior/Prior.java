package prior;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import util.Common;
import util.Corpus;
import util.Probase;
import Jama.Matrix;

public class Prior {

	/**
	 * 获取词表的 词 - (概念 - Reprensent score 映射)
	 * 
	 * @param vocab
	 *            词表
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Map<String, Double>> getVocabConceptMap(
			List<String> vocab) throws SQLException {

		Connection conn = Probase.getConnectionMySql();

		// 停止概念
		Set<String> stop = Probase.getStopConcept(conn);

		// 实体-(概念-Reprensent Score)
		Map<String, Map<String, Double>> vocab_concept_map = new HashMap<>();

		// 概念列表，附带Rep Score
		for (String word : vocab) {

			Map<String, Double> concepts = Probase.getConceptList(conn, word);

			Set<String> concept_set = concepts.keySet();

			List<String> concept_list = new ArrayList<String>(concept_set);

			// 去除停止概念
			for (String concept : concept_list) {
				if (stop.contains(concept))
					concepts.remove(concept);
			}

			vocab_concept_map.put(word, concepts);

		}
		conn.close();

		return vocab_concept_map;
	}

	/**
	 * 获取词表映射到的所有概念
	 * 
	 * @param vocab_concept_map
	 * @return
	 */
	public static List<String> getConceptList(
			Map<String, Map<String, Double>> vocab_concept_map) {

		Set<String> all_concepts = new HashSet<String>();

		for (String word : vocab_concept_map.keySet()) {

			Map<String, Double> concept_score_map = vocab_concept_map.get(word);
			all_concepts.addAll(concept_score_map.keySet());
		}

		List<String> concepts = new ArrayList<String>(all_concepts);
		return concepts;
	}

	/**
	 * 获取概念-实体 Representative Score 矩阵, 实体限于词表
	 * 
	 * @param concepts
	 * @param vocab
	 * @param vocab_concept_map
	 * @return
	 */
	public static float[][] getConceptEntityRrepScoreMatrix(
			List<String> concepts, List<String> vocab,
			Map<String, Map<String, Double>> vocab_concept_map) {

		float[][] matrix = new float[concepts.size()][vocab.size()];

		for (String entity : vocab) {
			Map<String, Double> entity_concepts = vocab_concept_map.get(entity);

			for (String concept : entity_concepts.keySet()) {

				// List性能不如HashSet， 但为了与之前代码兼容，先这样
				int concept_index = concepts.indexOf(concept);

				if (concept_index != -1) {

					double score = entity_concepts.get(concept);
					matrix[concept_index][vocab.indexOf(entity)] = (float) score;
				}

			}
		}

		return matrix;

	}

	/**
	 * 获取概念-实体 Representative Score 矩阵, 实体不限于词表
	 * 
	 * @param concepts
	 * @return
	 * @throws SQLException
	 */
	public static float[][] getConceptEntityRrepScoreMatrix(
			List<String> concepts) throws SQLException {

		Map<String, Map<String, Double>> concept_entity_map = new HashMap<>();

		Connection conn = Probase.getConnectionMySql();

		Set<String> entity_set = new HashSet<>();

		for (String concept : concepts) {

			Map<String, Double> entity_score_map = Probase.getEntityList(conn,
					concept);
			entity_set.addAll(entity_score_map.keySet());
			concept_entity_map.put(concept, entity_score_map);

		}

		List<String> entities = new ArrayList<>(entity_set);

		float[][] matrix = new float[concepts.size()][entities.size()];

		for (String concept : concepts) {

			Map<String, Double> entity_score_map = concept_entity_map
					.get(concept);

			for (String entity : entity_score_map.keySet()) {

				double score = entity_score_map.get(entity);

				matrix[concepts.indexOf(concept)][entities.indexOf(entity)] = (float) score;

			}

		}

		conn.close();

		return matrix;
	}

	/**
	 * 返回概念实体稀疏表示的数据集(利用Java ML)
	 * 
	 * @param concepts
	 * @return
	 * @throws SQLException
	 */
	public static Dataset getConceptEntityRepSparseDataSet(List<String> concepts)
			throws SQLException {

		Map<String, Map<String, Double>> concept_entity_map = new HashMap<>();

		Connection conn = Probase.getConnectionMySql();

		Set<String> entity_set = new HashSet<>();

		for (String concept : concepts) {

			Map<String, Double> entity_score_map = Probase.getEntityList(conn,
					concept);
			entity_set.addAll(entity_score_map.keySet());
			concept_entity_map.put(concept, entity_score_map);

		}

		List<String> entities = new ArrayList<>(entity_set);

		int feature_size = entities.size();

		Dataset data = new DefaultDataset();

		for (String concept : concepts) {

			Map<String, Double> entity_score_map = concept_entity_map
					.get(concept);
			// 稀疏向量
			Instance instance = new SparseInstance(feature_size);

			for (String entity : entity_score_map.keySet()) {

				double score = entity_score_map.get(entity);

				instance.put(entities.indexOf(entity), score);
			}

			data.add(instance);

		}

		return data;

	}

	/**
	 * 
	 * 返回非对称beta先验
	 * 
	 * @param K
	 *            主题数
	 * @param vocab
	 *            词表
	 * @param vocab_concept_map
	 *            词 - (概念 - Reprensent score 映射)
	 * @param concept_cluster
	 *            概念-簇 映射
	 * @return
	 */
	public static double[][] getAsymmetricBeta(int K, List<String> vocab,
			Map<String, Map<String, Double>> vocab_concept_map,
			Map<String, Integer> concept_cluster) {

		int V = vocab.size();
		double[][] beta = new double[K][V];

		// 获得词在某个概念簇的score
		for (int w = 0; w < V; w++) {

			String word = vocab.get(w);

			Map<String, Double> concept_score = vocab_concept_map.get(word);

			for (String concept : concept_score.keySet()) {

				double score = concept_score.get(concept);

				if (concept_cluster.containsKey(concept)) {

					int cluster = concept_cluster.get(concept);

					beta[cluster][w] += score;
				}

			}

		}
		// 转置
		Matrix transpose = new Matrix(beta).transpose();

		double[][] beta_t = transpose.getArray();

		for (int i = 0; i < beta_t.length; i++) {

			beta_t[i] = Common.normalize(beta_t[i]);// 正则化到[0,1]
		}

		transpose = new Matrix(beta_t).transpose();

		beta = transpose.getArray();

		// 都乘以标量
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++)

				beta[k][w] = 0.1 * beta[k][w];
		}

		return beta;

	}

	/**
	 * 获取文档的概念向量，是词概念向量的TFIDF加权
	 * 
	 * @param docs
	 *            文档集
	 * @param beta
	 *            主题-词先验
	 * @return
	 */
	public static double[][] getAsymmetricAlpha(int[][] docs, double[][] beta) {

		int D = docs.length;
		int K = beta.length;

		// beta转置，即每个词的概念向量，类似ESA
		double[][] beta_t = new Matrix(beta).transpose().getArray();

		// idf 先算好，提高效率
		double[] idf = new double[beta_t.length];

		for (int w = 0; w < idf.length; w++) {
			idf[w] = Corpus.IDF(docs, w);
		}

		double[][] alpha = new double[D][K];

		for (int d = 0; d < docs.length; d++) {

			// 文档的概念向量
			double[] doc_vector = new double[K];

			for (int n = 0; n < docs[d].length; n++) {

				int word = docs[d][n];

				double[] word_vector = beta_t[word];

				double tf_idf = Corpus.TF(docs[d], word) * idf[word];

				for (int k = 0; k < K; k++) {
					doc_vector[k] += tf_idf * word_vector[k];
				}

			}
			alpha[d] = Common.normalize(doc_vector);
		}

		return alpha;

	}

	/**
	 * 获得概念的距离矩阵
	 * 
	 * @param concepts
	 *            概念列表
	 * @return
	 * @throws SQLException
	 */
	public static double[][] getConceptDistance(List<String> concepts)
			throws SQLException {

		int N = concepts.size();
		double[][] distance = new double[N][N];

		// 初始化
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i == j)
					distance[i][j] = 0;
				else
					distance[i][j] = 10000;
			}
		}

		Connection conn = Probase.getConnectionMySql();

		// 相似度表中含有的概念
		Set<String> concepts_has_sim = Probase.getConceptHasSimilarity(conn);

		for (int i = 0; i < N; i++) {

			for (int j = 0; j < N; j++) {

				// 只算一半，等下复制过去
				if (i < j) {

					String concept_A = concepts.get(i);
					String concept_B = concepts.get(j);

					if (concepts_has_sim.contains(concept_A)
							&& concepts_has_sim.contains(concept_B)) {

						double similarity = Probase.getConceptSimilarity(conn,
								concept_A, concept_B);
						distance[i][j] = 1 - similarity;
					}

					System.out.println(i + "　" + j + " " + distance[i][j]);
				}

			}
		}
		// 1变为10000
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (distance[i][j] == 1)
					distance[i][j] = 10000;
			}
		}
		// 搬过去
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i > j) {
					distance[i][j] = distance[j][i];
				}
			}
		}

		conn.close();

		return distance;
	}

	/**
	 * 获取概念-簇映射表
	 * 
	 * @param concepts
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, String> getConceptListClusters(
			List<String> concepts) throws SQLException {

		Connection conn = Probase.getConnectionMySql();

		Map<Integer, String> concept_cluster_map = new HashMap<>();

		for (String concept : concepts) {
			String cluster_center = Probase.getConceptClusterCenter(conn,
					concept);
			concept_cluster_map.put(concepts.indexOf(concept), cluster_center);
		}
		conn.close();
		return concept_cluster_map;
	}

	/**
	 * 
	 * 像IJCAI'11里面那样，用Naive Bayes 找到一篇文档(若干entity) 的最好概念，顺便消歧
	 * 
	 * @param entities
	 *            概念列表（一篇文档）
	 * @param entity_concept_rep
	 *            实体-(概念- Rep score), 传进来, 添加
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getConceptualiztion(List<String> entities,
			Map<String, Map<String, Double>> entity_concept_rep, Connection conn)
			throws SQLException {

		Map<String, Map<String, List<Double>>> entity_concept_map = new HashMap<>();

		// 每个概念出现的频次
		Map<String, Double> concept_freq_map = new HashMap<>();

		// 停止概念
		Set<String> stop = Probase.getStopConcept(conn);

		int entity_size = entities.size();

		for (String entity : entities) {

			Map<String, List<Double>> concept_typical_map = Probase
					.getConceptTypicalList(conn, entity, entity_size);
			// 概念列表
			List<String> concept_list = new ArrayList<String>(
					concept_typical_map.keySet());

			// 去除停止概念
			for (String concept : concept_list) {
				if (stop.contains(concept))
					concept_typical_map.remove(concept);
			}

			Map<String, Double> concept_rep_map = new HashMap<>();

			for (String concept : concept_typical_map.keySet()) {

				concept_freq_map.put(concept, concept_typical_map.get(concept)
						.get(1));
				concept_rep_map.put(concept, concept_typical_map.get(concept)
						.get(2));

			}
			// 添加到实体-(概念-Rep)
			if (!entity_concept_rep.containsKey(entity))
				entity_concept_rep.put(entity, concept_rep_map);

			entity_concept_map.put(entity, concept_typical_map);

		}

		/*
		 * 求每个概念的后验概率
		 */
		Map<String, Double> concept_posterior_map = new HashMap<>();

		for (String concept : concept_freq_map.keySet()) {

			double posterior = 1;

			double concept_frequency = concept_freq_map.get(concept);

			for (String entity : entities) {

				Map<String, List<Double>> concept_typical_map = entity_concept_map
						.get(entity);

				if (concept_typical_map.containsKey(concept)) {

					posterior *= concept_typical_map.get(concept).get(0);

				} else {
					// 拉普拉斯校准
					posterior *= (1.0 / (concept_frequency + entity_size));
				}

			}

			posterior *= concept_frequency;

			if (posterior != 0)

				concept_posterior_map.put(concept, posterior);

		}

		// 后验概率Map按值排序

		Map<String, Double> sorted_Map = Common
				.getSortedMap(concept_posterior_map);

		// conn.close();

		StringBuilder top_concepts = new StringBuilder();

		// 前十概念

		int count = 0;

		for (String top_concept : sorted_Map.keySet()) {

			top_concepts.append(top_concept + "\t");

			count++;
			if (count == 10)
				break;
		}

		return top_concepts.toString();
	}

	/**
	 * IJCAI'11中的Mixture Model 公式(8)
	 * 
	 * @param entities
	 *            概念列表（一篇文档）
	 * @param entity_concept_rep
	 *            实体-(概念- Rep score), 传进来, 添加
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static String getConceptualiztionMixture(List<String> entities,
			Map<String, Map<String, Double>> entity_concept_rep, Connection conn)
			throws SQLException {

		Map<String, Map<String, List<Double>>> entity_concept_map = new HashMap<>();

		// 属性-(概念-Score)
		Map<String, Map<String, Double>> attribute_concept_map = new HashMap<>();

		// 每个概念出现的频次
		Map<String, Double> concept_freq_map = new HashMap<>();

		// 停止概念
		Set<String> stop = Probase.getStopConcept(conn);

		int entity_size = entities.size();

		for (String entity : entities) {

			Map<String, List<Double>> concept_typical_map = Probase
					.getConceptTypicalList(conn, entity, entity_size);
			// 概念列表
			List<String> concept_list = new ArrayList<String>(
					concept_typical_map.keySet());

			// 去除停止概念
			for (String concept : concept_list) {
				if (stop.contains(concept))
					concept_typical_map.remove(concept);
			}

			Map<String, Double> concept_rep_map = new HashMap<>();

			for (String concept : concept_typical_map.keySet()) {

				concept_freq_map.put(concept, concept_typical_map.get(concept)
						.get(1));
				concept_rep_map.put(concept, concept_typical_map.get(concept)
						.get(2));

			}
			// 添加到实体-(概念-Rep)
			if (!entity_concept_rep.containsKey(entity))
				entity_concept_rep.put(entity, concept_rep_map);

			entity_concept_map.put(entity, concept_typical_map);

			/*
			 * 属性
			 */
			Map<String, Double> concept_attr_score_map = Probase
					.getConceptByAttribute(conn, entity);

			attribute_concept_map.put(entity, concept_attr_score_map);

			for (String concept : concept_attr_score_map.keySet()) {

				if (!stop.contains(concept)
						&& !concept_freq_map.containsKey(concept)) {

					int concept_freq = Probase.getConceptFrequency(conn,
							concept);

					concept_freq_map.put(concept, (double) concept_freq);

				}

			}

		}

		/*
		 * 求每个概念的后验概率
		 */
		Map<String, Double> concept_posterior_map = new HashMap<>();

		for (String concept : concept_freq_map.keySet()) {

			double posterior = 1;

			double concept_frequency = concept_freq_map.get(concept);

			for (String entity : entities) {

				Map<String, List<Double>> concept_typical_map = entity_concept_map
						.get(entity);

				Map<String, Double> concept_attr_score_map = attribute_concept_map
						.get(entity);

				double entitiy_typical_score = 0;

				double attr_score = 0;

				if (concept_typical_map.containsKey(concept)
						&& concept_attr_score_map.containsKey(concept)) {

					entitiy_typical_score = concept_typical_map.get(concept)
							.get(3);

					attr_score = concept_attr_score_map.get(concept);

				} else if (concept_typical_map.containsKey(concept)
						&& !concept_attr_score_map.containsKey(concept)) {

					entitiy_typical_score = concept_typical_map.get(concept)
							.get(3);

				} else if (!concept_typical_map.containsKey(concept)
						&& concept_attr_score_map.containsKey(concept)) {

					attr_score = concept_attr_score_map.get(concept);

				}

				// 公式(7)
				double p_ck_tl = 1 - (1 - entitiy_typical_score)
						* (1 - attr_score);

				double concept_freq = concept_freq_map.get(concept);
				posterior *= (p_ck_tl / concept_freq);

			}

			posterior *= concept_frequency;

			if (posterior != 0)

				concept_posterior_map.put(concept, posterior);

		}

		Map<String, Double> sorted_Map = Common
				.getSortedMap(concept_posterior_map);

		StringBuilder top_concepts = new StringBuilder();

		// 前十概念

		int count = 0;

		for (String top_concept : sorted_Map.keySet()) {

			top_concepts.append(top_concept + "\t");

			count++;
			if (count == 10)
				break;
		}

		return top_concepts.toString();
	}
}
