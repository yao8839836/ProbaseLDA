package test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prior.Prior;
import util.Corpus;
import util.ReadWriteFile;
import conceptcluster.HierarchicalClusterer;

public class RunHierarchicalCluster {

	public static void main(String[] args) throws IOException, SQLException {
		List<String> vocab = Corpus.getVocab("file//Watch.vocab");

		Map<String, Map<String, Double>> vocab_concept_map = Prior
				.getVocabConceptMap(vocab);

		// 词表映射到的所有概念
		List<String> concepts = Prior.getConceptList(vocab_concept_map);

		Map<Integer, String> concept_cluster_map = Prior
				.getConceptListClusters(concepts);

		Set<String> center_set = new HashSet<String>();
		// 簇心的集合，再聚类

		for (String cluster : concept_cluster_map.values()) {
			center_set.add(cluster);
		}

		List<String> concept_centers = new ArrayList<String>(center_set);

		double[][] distance = Prior.getConceptDistance(concept_centers);

		int K = 20;

		int[] assignment = HierarchicalClusterer.hierarchicalClusterer(
				distance, K);

		StringBuilder sb = new StringBuilder();

		int N = concepts.size();

		for (int i = 0; i < N; i++) {

			// sb.append(concepts.get(i) + ":" + assignment[i] + "\n");
			String cluster = concept_cluster_map.get(i);

			int cluster_index = concept_centers.indexOf(cluster);

			sb.append(concepts.get(i) + ":" + assignment[cluster_index] + "\n");
		}

		String filename = "file//concept_cluster.txt";

		ReadWriteFile.writeFile(filename, sb.toString());
	}
}
