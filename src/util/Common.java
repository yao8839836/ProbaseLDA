package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Common {

	/**
	 * 返回数组中最大元素的下标
	 * 
	 * @param array
	 *            输入数组
	 * @return 最大元素的下标
	 */
	public static int maxIndex(double[] array) {
		double max = array[0];
		int maxIndex = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
				maxIndex = i;
			}

		}
		return maxIndex;

	}

	/**
	 * 返回数组中的最小值
	 * 
	 * @param array
	 *            输入数组
	 * @return
	 */
	public static double min(double[] array) {

		double min = array[0];

		for (int i = 0; i < array.length; i++) {
			if (array[i] < min)
				min = array[i];
		}

		return min;

	}

	/**
	 * 复制矩阵
	 * 
	 * @param array
	 *            矩阵
	 * @return
	 */
	public static double[][] makeCopy(double[][] array) {

		double[][] copy = new double[array.length][];

		for (int i = 0; i < copy.length; i++) {

			copy[i] = new double[array[i].length];

			for (int j = 0; j < copy[i].length; j++) {
				copy[i][j] = array[i][j];
			}
		}

		return copy;
	}

	/**
	 * 把数据规范化到[0,1]
	 * 
	 * @param array
	 *            输入数组
	 * @return
	 */
	public static double[] normalize(double[] array) {

		double[] normalized_array = new double[array.length];

		double max = array[maxIndex(array)];

		double min = min(array);

		for (int i = 0; i < array.length; i++) {
			if (min == max)
				normalized_array[i] = max;
			else
				normalized_array[i] = 1 * (array[i] - min) / (max - min);
		}

		return normalized_array;
	}

	/**
	 * 换底公式
	 * 
	 * @param value
	 *            值
	 * @param base
	 *            底
	 * @return
	 */
	public static double log(double value, double base) {
		return Math.log(value) / Math.log(base);
	}

	/**
	 * 以2为底对数
	 * 
	 * @param value
	 *            值
	 * @return
	 */
	public static double log2(double value) {

		return log(value, 2);
	}

	/**
	 * 对Map按值由大到小排序，返回LinkedHashMap
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, Double> getSortedMap(Map<String, Double> map) {

		List<Entry<String, Double>> entry_list = new ArrayList<>();

		for (Entry<String, Double> entry : map.entrySet()) {
			entry_list.add(entry);
		}

		Collections.sort(entry_list, new Comparator<Entry<String, Double>>() {

			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return -o1.getValue().compareTo(o2.getValue());
			}
		});

		// 按值排序的键列表

		Map<String, Double> sorted_Map = new LinkedHashMap<>();

		for (Entry<String, Double> entry : entry_list) {
			sorted_Map.put(entry.getKey(), entry.getValue());
		}

		return sorted_Map;

	}

}
