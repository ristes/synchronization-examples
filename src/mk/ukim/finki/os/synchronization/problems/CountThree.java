package mk.ukim.finki.os.synchronization.problems;

import java.util.HashSet;
import java.util.Scanner;

/**
 * 
 * @author ristes
 */
public class CountThree {

	public static int NUM_RUNS = 100;
	/**
	 * Promenlivata koja treba da go sodrzi brojot na pojavuvanja na elementot 3
	 */
	int count = 0;
	/**
	 * TODO: definirajte gi potrebnite semafori ovde
	 */

	// Monitor za kriticniot region za pristap na count
	final Object lock = new Object();

	/*
	 * TODO: implementiraj spored baranjeto
	 */
	public void init() {
	}

	class Counter extends Thread {

		public void count(int[] data) throws InterruptedException {
			for (int i = 0; i < data.length; i++) {
				if (data[i] == 3) {
					synchronized (lock) {
						count++;
					}
				}
			}
		}

		private int[] data;

		public Counter(int[] data) {
			this.data = data;
		}

		@Override
		public void run() {
			try {
				count(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		try {
			CountThree environment = new CountThree();
			environment.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void start() throws Exception {

		init();

		HashSet<Thread> threads = new HashSet<Thread>();
		Scanner s = new Scanner(System.in);
		int total = s.nextInt();
		for (int i = 0; i < NUM_RUNS; i++) {
			int[] data = new int[total];
			for (int j = 0; j < total; j++) {
				data[j] = s.nextInt();
			}
			Counter prod = new Counter(data);
			threads.add(prod);
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}
		System.out.println(count);

	}
}
