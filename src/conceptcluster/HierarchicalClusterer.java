package conceptcluster;

import java.util.List;
import java.util.Set;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;

public class HierarchicalClusterer {

	public static void main(String[] args) {
		String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
		double[][] distances = new double[][] { { 0, 1, 9, 7, 11, 14 },
				{ 1, 0, 4, 3, 8, 10 }, { 9, 4, 0, 9, 2, 8 },
				{ 7, 3, 9, 0, 6, 13 }, { 11, 8, 2, 6, 0, 10 },
				{ 14, 10, 8, 13, 10, 0 } };

		DefaultClusteringAlgorithm alg = new DefaultClusteringAlgorithm();

		Cluster cluster = alg.performClustering(distances, names,
				new AverageLinkageStrategy());
		System.out.println(cluster.toString());
		System.out.println(cluster.getChildren().size());
		System.out.println(cluster.countLeafs());

		cluster.toConsole(3);

		int K = 2;

		List<Cluster> clusters = alg.performClustering(distances, names,
				new AverageLinkageStrategy(), K);

		for (Cluster c : clusters) {

			Set<String> leafs = c.getLeafs();
			System.out.println(leafs);

		}

	}

	/**
	 * 对概念的距离矩阵层次聚类
	 * 
	 * @param distance
	 *            距离矩阵
	 * @param K
	 *            簇的个数，即主题数
	 * @return
	 */
	public static int[] hierarchicalClusterer(double[][] distance, int K) {

		int N = distance.length;

		// 命名
		String[] names = new String[N];

		for (int i = 0; i < N; i++)
			names[i] = i + "";

		DefaultClusteringAlgorithm alg = new DefaultClusteringAlgorithm();

		List<Cluster> clusters = alg.performClustering(distance, names,
				new SingleLinkageStrategy(), K);

		int[] assignments = new int[N];

		for (int k = 0; k < K; k++) {

			Cluster c = clusters.get(k);
			Set<String> leafs = c.getLeafs();

			for (String s : leafs) {
				assignments[Integer.parseInt(s)] = k;
			}

		}

		return assignments;
	}

}
