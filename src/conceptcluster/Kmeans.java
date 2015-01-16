package conceptcluster;

import java.io.File;

import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.KMedoids;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.CosineDistance;
import net.sf.javaml.tools.data.ARFFHandler;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Kmeans {

	/**
	 * 返回每个概念属于哪个簇
	 * 
	 * @param filename
	 * @param K
	 *            簇的个数，即主题数
	 * @return
	 * @throws Exception
	 */
	public static int[] RunKmeans(String filename, int K) throws Exception {

		File file = new File(filename);

		ArffLoader loader = new ArffLoader();

		loader.setFile(file);

		Instances ins = loader.getDataSet();

		SimpleKMeans km = new SimpleKMeans();

		km.setNumClusters(K);

		km.buildClusterer(ins);

		int[] assignments = new int[ins.numInstances()];

		for (int i = 0; i < assignments.length; i++) {
			int cluster = km.clusterInstance(ins.instance(i));

			assignments[i] = cluster;
		}

		return assignments;

	}

	/**
	 * 用Kmeans对概念聚类，采用余弦距离(JavaML)
	 * 
	 * @param filename
	 *            文件名
	 * @param K
	 *            簇的个数
	 * @return 每个概念属于哪个簇
	 * @throws Exception
	 */
	public static int[] RunKmeansCosine(String filename, int K)
			throws Exception {

		Dataset data = ARFFHandler.loadARFF(new File(filename));
		KMeans km = new KMeans(15, 100, new CosineDistance());
		Dataset[] clusters = km.cluster(data);

		int[] assignments = new int[data.size()];

		for (int i = 0; i < clusters.length; i++) {
			Dataset cluster = clusters[i];
			for (Instance ins : cluster) {

				int index = data.indexOf(ins);
				assignments[index] = i;

			}

		}

		return assignments;

	}

	/**
	 * 用KMeans对概念聚类，采用余弦距离(JavaML)
	 * 
	 * @param data
	 *            数据集
	 * @param K
	 *            簇的个数
	 * @return
	 * @throws Exception
	 */
	public static int[] RunKmeansCosine(Dataset data, int K) throws Exception {

		KMedoids km = new KMedoids(K, 100, new CosineDistance());
		Dataset[] clusters = km.cluster(data);

		int[] assignments = new int[data.size()];

		for (int i = 0; i < clusters.length; i++) {
			Dataset cluster = clusters[i];
			for (Instance ins : cluster) {

				int index = data.indexOf(ins);
				assignments[index] = i;

			}

		}

		return assignments;

	}

}
