package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadDomainTask implements Runnable {

	List<String> domain_task_list;

	public MultiThreadDomainTask(List<String> task) {

		this.domain_task_list = task;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		for (String domain : domain_task_list) {

			try {
				MultiDomainTask.runPriorLDA(domain);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) throws InterruptedException {

		File[] files = new File("data//").listFiles();

		List<String> domain_list = new ArrayList<String>();

		for (File f : files) {

			String file_path = f.toString();
			String domain = file_path.substring(file_path.indexOf("\\") + 1,
					file_path.length());

			domain_list.add(domain);
		}
		int threads = 10; // 线程数

		ExecutorService pool = Executors.newFixedThreadPool(threads); // 线程池
		for (int i = 0; i < threads; i++) {

			List<String> sub_list = domain_list.subList(i * 5, (i + 1) * 5);
			System.out.println(sub_list);
			pool.submit(new Thread(new MultiThreadDomainTask(sub_list)));

		}
		pool.shutdown();
		while (!pool.isTerminated())
			Thread.sleep(1000);

	}

}
