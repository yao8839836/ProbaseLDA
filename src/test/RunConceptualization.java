package test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.Dataset;
import prior.Prior;
import util.Probase;
import util.ReadWriteFile;
import conceptcluster.Kmedoids;
import conceptualization.Conceptualization;

public class RunConceptualization {

	public static void main(String[] args) throws Exception {

		List<String> entities = new ArrayList<String>();

		entities.add("bayes");

		entities.add("svm");

		Connection conn = Probase.getConnectionMySql();

		// 实体-(概念-Rep)
		Map<String, Map<String, Double>> entity_concept_rep = new HashMap<>();

		String concept = Prior.getConceptualiztion(entities,
				entity_concept_rep, conn);

		System.out.println(entity_concept_rep);
		conn.close();
		System.out.println(concept);

		// 概念化

		String domain = "Computer";

		// Mixture
		Conceptualization.conceptualization(domain);

		Set<String> concept_set = Conceptualization
				.getConceptualizationSet(domain);

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
}
