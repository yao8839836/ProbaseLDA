package test;

import java.util.List;
import java.util.Map;

import prior.Prior;
import util.Corpus;
import util.ReadWriteFile;
import conceptcluster.Kmeans;

public class RunKmeans {

	public static void main(String[] args) throws Exception {

		List<String> vocab = Corpus.getVocab("file//Watch.vocab");

		Map<String, Map<String, Double>> vocab_concept_map = Prior
				.getVocabConceptMap(vocab);

		// 词表映射到的所有概念
		List<String> concepts = Prior.getConceptList(vocab_concept_map);

		// 概念-实体的Rep矩阵
		float[][] represent_score = Prior.getConceptEntityRrepScoreMatrix(
				concepts, vocab, vocab_concept_map);

		String filename = "file//concept_entity.arff";

		ReadWriteFile.writeFile(filename, represent_score);

		int[] assignment = Kmeans.RunKmeansCosine(filename, 15);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < assignment.length; i++) {
			sb.append(concepts.get(i) + ":" + assignment[i] + "\n");
		}

		filename = "file//concept_cluster.txt";

		ReadWriteFile.writeFile(filename, sb.toString());
	}

}
