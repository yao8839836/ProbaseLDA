package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import prior.Prior;
import topic.LDA;
import topic.PriorLDABurnLag;
import util.Common;
import util.Corpus;
import util.ReadWriteFile;

public class MultiDomainTask {

	public static void main(String[] args) throws Exception {


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

			double domain_coherence = runPriorLDA(domain);
			coherence += domain_coherence;
			sb.append(domain + "\t" + domain_coherence + "\n");
			System.out.println(domain + "\t" + domain_coherence + "\n");
		}

		sb.append("average : " + coherence / domain_list.size() + "\n");

		String filename = "output//ProbaseLDA_top3_all_kmedoids//LDA_coherence_alpha_1.txt";

		ReadWriteFile.writeFile(filename, sb.toString());

	}

	/**
	 * 对指定的文档集执行PriorLDA
	 * 
	 * @param domain
	 *            领域名
	 * @throws Exception
	 */
	public static double runPriorLDA(String domain) throws Exception {

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		Map<String, Map<String, Double>> vocab_concept_map = Prior
				.getVocabConceptMap(vocab);

		int K = 15;

		String filename = "output//ProbaseLDA_top3_all_kmedoids//concept_cluster_"
				+ domain + ".txt";

		// ReadWriteFile.writeFile(filename, sb.toString());

		Map<String, Integer> concept_cluster = ReadWriteFile
				.getConceptCluster(filename);

		int V = vocab.size();

		double[][] beta = Prior.getAsymmetricBeta(K, vocab, vocab_concept_map,
				concept_cluster);

		int[][] docs = Corpus.getDocuments("data//" + domain + "//" + domain
				+ ".docs");
		// alpha 对称先验
		double[][] alpha = Prior.getAsymmetricAlpha(docs, beta);

		// for (int d = 0; d < docs.length; d++) {
		// for (int k = 0; k < K; k++)
		// alpha[d][k] = 1;
		// }
		//
		// for (int k = 0; k < K; k++) {
		// for (int v = 0; v < V; v++)
		// beta[k][v] = 0.1;
		//
		// }

		int iterations = 2000;

		int top_word_count = 30;

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

		filename = "file//" + domain + ".txt";

		// 语义一致性
		double average_coherence = Corpus.average_coherence(docs, phi_copy,
				top_word_count);

		// 多线程每个领域将coherence写在自己文件
		sb.append("average coherence\t" + average_coherence);

		// ReadWriteFile.writeFile(filename, sb.toString());

		double[][] theta = plda.estimateTheta();

		// perplexity
		double perplexity = Corpus.perplexity(theta, phi, docs);
		System.out.println("perplexity : " + perplexity);

		return average_coherence;

	}

	/**
	 * 对指定文档集执行LDA
	 * 
	 * @param domain
	 *            领域名
	 * @return
	 * @throws IOException
	 */
	public static double runLDA(String domain) throws IOException {

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		int[][] docs = Corpus.getDocuments("data//" + domain + "//" + domain
				+ ".docs");

		int K = 30;
		double alpha = 1;
		double beta = 0.1;
		int iterations = 2000;

		int top_word_count = 30;

		LDA lda = new LDA(docs, vocab.size());

		lda.markovChain(K, alpha, beta, iterations);

		double[][] phi = lda.estimatePhi();

		double[][] phi_copy = Common.makeCopy(phi);

		// 语义一致性
		double average_coherence = Corpus.average_coherence(docs, phi_copy,
				top_word_count);

		System.out.println("average coherence : " + average_coherence);

		double[][] theta = lda.estimateTheta();

		// perplexity
		double perplexity = Corpus.perplexity(theta, phi, docs);
		System.out.println("perplexity : " + perplexity);

		return average_coherence;
	}

}
