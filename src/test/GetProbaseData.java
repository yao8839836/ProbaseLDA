package test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import prior.Prior;
import util.Corpus;
import util.Probase;
import util.ReadWriteFile;

public class GetProbaseData {

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

		System.out.println(concepts.size());
		
		//测试相似度
		
		Connection conn = Probase.getConnectionMySql();
		
		System.out.println(Probase.getConceptSimilarity(conn, "120V DC motor related product", "fan DC related product"));

	}
}
