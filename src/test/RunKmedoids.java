package test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.Dataset;
import prior.Prior;
import util.Corpus;
import util.Probase;
import util.ReadWriteFile;
import conceptcluster.Kmedoids;

public class RunKmedoids {

	public static void main(String[] args) throws Exception {

		String domain = "Computer";

		List<String> vocab = Corpus.getVocab("data//" + domain + "//" + domain
				+ ".vocab");

		Set<String> concept_set = new HashSet<>();

		Connection conn = Probase.getConnectionMySql();

		for (String entity : vocab) {

			Map<String, Double> concept_rep_map = Probase.getConceptList(conn,
					entity);
			concept_set.addAll(concept_rep_map.keySet());

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

}
