package conceptcluster;

import java.io.File;

import net.sf.javaml.clustering.KMedoids;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.CosineDistance;
import net.sf.javaml.tools.data.ARFFHandler;

public class Kmedoids {

	/**
	 * 用KMedoids对概念聚类，采用余弦距离(JavaML)
	 * 
	 * @param filename
	 *            文件名
	 * @param K
	 *            簇的个数
	 * @return 每个概念属于哪个簇
	 * @throws Exception
	 */
	public static int[] RunKmedoidsCosine(String filename, int K)
			throws Exception {

		Dataset data = ARFFHandler.loadARFF(new File(filename));
		KMedoids km = new KMedoids(15, 100, new CosineDistance());
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
	 * 用KMedoids对概念聚类，采用余弦距离(JavaML)
	 * 
	 * @param data
	 *            数据集
	 * @param K
	 *            簇的个数
	 * @return
	 * @throws Exception
	 */
	public static int[] RunKmedoidsCosine(Dataset data, int K) throws Exception {

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
