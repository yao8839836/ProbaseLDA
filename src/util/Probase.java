package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Probase {

	/**
	 * 连接MySQL数据库
	 * 
	 * @return Connection
	 */
	public static Connection getConnectionMySql() {
		String url = "jdbc:mysql://10.15.62.29:3306/probase?useUnicode=true&characterEncoding=utf8";
		String user = "root";
		String psw = "123";
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection(url, user, psw);
			conn.setAutoCommit(false);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得Probase中所有概念
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<String> getConceptList(Connection conn)
			throws SQLException {

		String sql = "select distinct Concept from isa_core";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		List<String> concepts = new ArrayList<String>();

		while (rs.next()) {

			String concept = rs.getString("Concept");
			concepts.add(concept);
		}

		rs.close();
		pstmt.close();

		return concepts;

	}

	/**
	 * 获得某个实体的概念列表
	 * 
	 * @param conn
	 * @param entity
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Double> getConceptList(Connection conn,
			String entity) throws SQLException {

		String sql = "select distinct Concept, represent_score as score from isa_core where entity = '"
				+ entity.replaceAll("'", "''")
				+ "' order by represent_score desc limit 0, 5";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Map<String, Double> concept_score_map = new HashMap<String, Double>();

		while (rs.next()) {

			String concept = rs.getString("Concept");

			double represent_score = rs.getDouble("score");

			concept_score_map.put(concept, represent_score);

		}

		rs.close();
		pstmt.close();

		return concept_score_map;

	}

	/**
	 * 获得某个实体的（概念-概念下实体的typical score, 概念频次） 列表
	 * 
	 * @param conn
	 * @param entity
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, List<Double>> getConceptTypicalList(
			Connection conn, String entity, int entity_size)
			throws SQLException {

		// 拉普拉斯校准
		String sql = "select distinct Concept, (frequency + 1) / (ConceptFrequency + "
				+ entity_size
				+ ")"
				+ " as typical_score, frequency / EntityFrequency as entity_typical_score, "
				+ "ConceptFrequency, "
				+ "represent_score as score"
				+ " from isa_core where entity = '"
				+ entity.replaceAll("'", "''")
				+ "' and ConceptVagueness <= 0.7 order by typical_score desc";
		// 试试去掉and ConceptVagueness <= 0.7

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Map<String, List<Double>> concept_typical_map = new HashMap<>();

		while (rs.next()) {

			String concept = rs.getString("Concept");

			// 减少数据库访问次数，提高速度
			List<Double> statistics = new ArrayList<>();

			double typical_score = rs.getDouble("typical_score");

			double concept_frequency = rs.getInt("ConceptFrequency");

			double represent_score = rs.getDouble("score");

			double entity_typical_score = rs.getDouble("entity_typical_score");

			statistics.add(typical_score);
			statistics.add(concept_frequency);
			statistics.add(represent_score);
			statistics.add(entity_typical_score);

			concept_typical_map.put(concept, statistics);

		}

		rs.close();
		pstmt.close();

		return concept_typical_map;

	}

	/**
	 * 获得某个概念的所有实体
	 * 
	 * @param conn
	 * @param entity
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Double> getEntityList(Connection conn,
			String concept) throws SQLException {

		String sql = "select distinct entity, represent_score as score from isa_core where concept = '"
				+ concept.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Map<String, Double> entity_score_map = new HashMap<String, Double>();

		while (rs.next()) {

			String entity = rs.getString("entity");
			double represent_score = rs.getDouble("score");

			entity_score_map.put(entity, represent_score);
		}

		rs.close();
		pstmt.close();

		return entity_score_map;
	}

	/**
	 * 获得概念的实体列表，以及它们和概念的共现频次，用来计算概念相似度
	 * 
	 * @param conn
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Integer> getEntityFreqList(Connection conn,
			String concept) throws SQLException {

		String sql = "select distinct entity, frequency from isa_core where concept = '"
				+ concept.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Map<String, Integer> entity_score_map = new HashMap<String, Integer>();

		while (rs.next()) {

			String entity = rs.getString("entity");
			int frequency = rs.getInt("frequency");

			entity_score_map.put(entity, frequency);
		}

		rs.close();
		pstmt.close();

		return entity_score_map;
	}

	/**
	 * 获取概念的实体，实体要在词表中
	 * 
	 * @param conn
	 * @param concept
	 * @param vocab
	 *            词表
	 * @return
	 * @throws SQLException
	 */
	public static List<String> getEntityList(Connection conn, String concept,
			List<String> vocab) throws SQLException {

		String sql = "select distinct entity from isa_core where concept = '"
				+ concept.replaceAll("'", "''")
				+ "' and ConceptVagueness < 0.7";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		List<String> entities = new ArrayList<String>();

		while (rs.next()) {

			String entity = rs.getString("entity");
			if (vocab.contains(entity))
				entities.add(entity);
		}

		rs.close();
		pstmt.close();

		return entities;
	}

	/**
	 * 获得某个概念-实体对出现的次数
	 * 
	 * @param conn
	 * @param entity
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static int getEntityConceptFrequency(Connection conn, String entity,
			String concept) throws SQLException {

		String sql = "select frequency from isa_core where entity = '"
				+ entity.replaceAll("'", "''") + "' and Concept = '"
				+ concept.replaceAll("'", "''") + "' ";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		int n_c_e = 0;
		while (rs.next()) {
			n_c_e = rs.getInt("frequency");// n(c,e)

		}

		rs.close();
		pstmt.close();

		return n_c_e;
	}

	/**
	 * 获得某个概念出现的频次
	 * 
	 * @param conn
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static int getConceptFrequency(Connection conn, String concept)
			throws SQLException {

		String sql = "select ConceptFrequency as frequency from isa_core where Concept = '"
				+ concept.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		int frequency = 0;
		while (rs.next()) {
			frequency = rs.getInt("frequency");
			break;

		}

		rs.close();
		pstmt.close();

		return frequency;
	}

	/**
	 * 获取某个实体出现的频次
	 * 
	 * @param conn
	 * @param entity
	 * @return
	 * @throws SQLException
	 */
	public static int getEntityFrequency(Connection conn, String entity)
			throws SQLException {

		String sql = "select EntityFrequency as frequency from isa_core where entity = '"
				+ entity.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		int frequency = 0;
		while (rs.next()) {
			frequency = rs.getInt("frequency");
			break;

		}

		rs.close();
		pstmt.close();

		return frequency;
	}

	/**
	 * 获取概念-实体的Representative Score Rep(e,c) = P(c|e)P(e|c)
	 * 
	 * @param conn
	 * @param entity
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static double getEntityConceptRepresentScore(Connection conn,
			String entity, String concept) throws SQLException {

		String sql = "select represent_score as score from isa_core where entity = '"
				+ entity.replaceAll("'", "''")
				+ "' and Concept = '"
				+ concept.replaceAll("'", "''") + "' ";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		double score = 0;
		while (rs.next()) {

			score = rs.getDouble("score");

		}

		rs.close();
		pstmt.close();

		return score;
	}

	/**
	 * 获取Probase停止概念列表
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Set<String> getStopConcept(Connection conn)
			throws SQLException {

		String sql = "select * from stop_concept";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Set<String> stop = new HashSet<String>();

		while (rs.next()) {
			stop.add(rs.getString("Concept"));
		}

		rs.close();
		pstmt.close();

		return stop;
	}

	/**
	 * 获取概念所在簇的ID
	 * 
	 * @param conn
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static int getConceptClusterID(Connection conn, String concept)
			throws SQLException {

		String sql = "select cluster_id as id from conceptcluster where Concept = '"
				+ concept.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		int cluster_id = 0;
		while (rs.next()) {
			cluster_id = rs.getInt("id");
		}

		rs.close();
		pstmt.close();

		return cluster_id;
	}

	/**
	 * 获取概念所在簇的中心概念
	 * 
	 * @param conn
	 * @param concept
	 * @return
	 * @throws SQLException
	 */
	public static String getConceptClusterCenter(Connection conn, String concept)
			throws SQLException {

		String sql = "select center_names as center from conceptcluster where Concept = '"
				+ concept.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		String center = "";
		while (rs.next()) {

			center = rs.getString("center");

			if (center.contains(";"))
				center = center.substring(0, center.indexOf(';'));
		}

		rs.close();
		pstmt.close();

		return center;
	}

	/**
	 * 获取两个概念的相似度
	 * 
	 * @param conn
	 * @param conceptA
	 * @param conceptB
	 * @return
	 * @throws SQLException
	 */
	public static double getConceptSimilarity(Connection conn, String conceptA,
			String conceptB) throws SQLException {

		String sql = "select SimilarityScore as score from similarity_concept where ConceptA = '"
				+ conceptA.replaceAll("'", "''")
				+ "' and ConceptB = '"
				+ conceptB.replaceAll("'", "''") + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		double similarity = 0.0;

		while (rs.next()) {
			similarity = rs.getDouble("score");
			break;
		}

		rs.close();
		pstmt.close();

		return similarity;
	}

	/**
	 * 获取与其他概念有相似度的概念
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Set<String> getConceptHasSimilarity(Connection conn)
			throws SQLException {

		String sql = "select * from concept_has_similarity";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		Set<String> concepts = new HashSet<String>();

		while (rs.next()) {
			concepts.add(rs.getString("Concept"));
		}

		rs.close();
		pstmt.close();

		return concepts;

	}

	/**
	 * 根据属性获取概念，及其Score
	 * 
	 * @param conn
	 * @param attribute
	 *            属性
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, Double> getConceptByAttribute(Connection conn,
			String attribute) throws SQLException {

		Map<String, Double> concept_score_map = new HashMap<String, Double>();

		String sql = "select * from conceptattribute where Attribute = '"
				+ attribute + "'";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			concept_score_map.put(rs.getString("Concept"),
					rs.getDouble("Score"));
		}

		rs.close();
		pstmt.close();

		return concept_score_map;
	}

}
