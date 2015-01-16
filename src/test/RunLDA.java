package test;

import java.io.IOException;
import java.util.List;

import topic.LDA;
import util.Common;
import util.Corpus;

public class RunLDA {

	public static void main(String[] args) throws IOException {

		String domain = "Computer";

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		int[][] docs = Corpus.getDocuments("data//" + domain + "//" + domain
				+ ".docs");

		int K = 15;
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
	}

}
