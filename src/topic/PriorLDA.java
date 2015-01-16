package topic;

import java.io.Serializable;

public class PriorLDA implements Serializable {

	private static final long serialVersionUID = 1L;// 为了可以保存模型文件

	int[][] documents;

	int V;

	int K; // 主题数，等于概念簇个数

	double[][] alpha;

	double[][] beta;

	int[][] z;

	int[][] nw;

	int[][] nd;

	int[] nwsum;

	int[] ndsum;

	double[] alpha_sum;

	double[] beta_sum;

	int iterations;

	public PriorLDA(int[][] docs, int V) {

		this.documents = docs;
		this.V = V;

	}

	public void initialState() {

		int D = documents.length;
		nw = new int[V][K];
		nd = new int[D][K];
		nwsum = new int[K];
		ndsum = new int[D];

		z = new int[D][];

		// 随机分配主题
		for (int d = 0; d < D; d++) {

			int Nd = documents[d].length;

			z[d] = new int[Nd];

			for (int n = 0; n < Nd; n++) {
				int topic = (int) (Math.random() * K);

				z[d][n] = topic;

				nw[documents[d][n]][topic]++;

				nd[d][topic]++;

				nwsum[topic]++;
			}
			ndsum[d] = Nd;
		}

		// 求alpha_sum, beta_sum
		alpha_sum = new double[D];
		for (int d = 0; d < D; d++) {

			for (int k = 0; k < K; k++)
				alpha_sum[d] += alpha[d][k];

		}

		beta_sum = new double[K];

		for (int k = 0; k < K; k++) {
			for (int v = 0; v < V; v++)
				beta_sum[k] += beta[k][v];
		}

	}

	public void markovChain(int K, double[][] alpha, double[][] beta,
			int iterations) {

		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.iterations = iterations;

		initialState();

		for (int i = 0; i < this.iterations; i++) {

			// System.out.println("iterations: " + i);
			gibbs();
		}
	}

	public void gibbs() {

		for (int d = 0; d < z.length; d++) {
			for (int n = 0; n < z[d].length; n++) {

				int topic = sampleFullConditional(d, n);
				z[d][n] = topic;

			}
		}
	}

	int sampleFullConditional(int d, int n) {

		int topic = z[d][n];
		nw[documents[d][n]][topic]--;
		nd[d][topic]--;
		nwsum[topic]--;
		ndsum[d]--;

		double[] p = new double[K];

		for (int k = 0; k < K; k++) {

			p[k] = (nd[d][k] + alpha[d][k]) / (ndsum[d] + alpha_sum[d])
					* (nw[documents[d][n]][k] + beta[k][documents[d][n]])
					/ (nwsum[k] + beta_sum[k]);
		}
		for (int k = 1; k < K; k++) {
			p[k] += p[k - 1];
		}
		double u = Math.random() * p[K - 1];
		for (int t = 0; t < K; t++) {
			if (u < p[t]) {
				topic = t;
				break;
			}
		}
		nw[documents[d][n]][topic]++;
		nd[d][topic]++;
		nwsum[topic]++;
		ndsum[d]++;
		return topic;

	}

	public double[][] estimateTheta() {

		double[][] theta = new double[documents.length][K];

		for (int d = 0; d < documents.length; d++) {

			for (int k = 0; k < K; k++) {
				theta[d][k] = (nd[d][k] + alpha[d][k])
						/ (ndsum[d] + alpha_sum[d]);
			}
		}
		return theta;
	}

	public double[][] estimatePhi() {

		double[][] phi = new double[K][V];

		for (int k = 0; k < K; k++) {

			for (int w = 0; w < V; w++) {
				phi[k][w] = (nw[w][k] + beta[k][w]) / (nwsum[k] + beta_sum[k]);
			}
		}
		return phi;
	}

}
