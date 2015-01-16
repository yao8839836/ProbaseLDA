package test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import prior.Prior;
import topic.PriorLDABurnLag;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class RunPriorLDA {

	public static void main(String[] args) throws IOException, SQLException {

		String domain = "Computer";

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		Map<String, Map<String, Double>> vocab_concept_map = Prior
				.getVocabConceptMap(vocab);

		Map<String, Integer> concept_cluster = ReadWriteFile
				.getConceptCluster("file//concept_cluster.txt");

		int K = 15;
		int V = vocab.size();

		double[][] beta = Prior.getAsymmetricBeta(K, vocab, vocab_concept_map,
				concept_cluster);

		int[][] docs = Corpus.getDocuments("data//" + domain + "//" + domain
				+ ".docs");

		// alpha 对称先验
		double[][] alpha = Prior.getAsymmetricAlpha(docs, beta);

		for (int d = 0; d < docs.length; d++) {
			for (int k = 0; k < K; k++) {
				// alpha[d][k] = 1;
				System.out.print(alpha[d][k] + "\t");

			}
			System.out.println();
		}

		for (int k = 0; k < K; k++) {

			for (int v = 0; v < V; v++) {

				System.out.print(beta[k][v] + "\t");
			}

			System.out.println();
		}

		int iterations = 2000;

		int top_word_count = 10;

		PriorLDABurnLag plda = new PriorLDABurnLag(docs, V);

		plda.markovChain(K, alpha, beta, iterations);

		double[][] phi = plda.estimatePhi();

		double[][] phi_copy = Common.makeCopy(phi);

		// 将每个主题的前10个词写文件
		double[][] phi_for_write = Common.makeCopy(phi);

		StringBuilder sb = new StringBuilder();

		for (double[] phi_t : phi_for_write) {

			for (int i = 0; i < 10; i++) {

				int max_index = Common.maxIndex(phi_t);

				sb.append(vocab.get(max_index) + "\t");

				phi_t[max_index] = 0;

			}
			sb.append("\n");

		}

		String filename = "file//" + domain + ".txt";

		// 语义一致性
		double average_coherence = Corpus.average_coherence(docs, phi_copy,
				top_word_count);

		// 多线程每个领域将coherence写在自己文件
		sb.append("average coherence\t" + average_coherence);

		ReadWriteFile.writeFile(filename, sb.toString());

		double[][] theta = plda.estimateTheta();

		// perplexity
		double perplexity = Corpus.perplexity(theta, phi, docs);
		System.out.println("perplexity : " + perplexity);

	}
}
