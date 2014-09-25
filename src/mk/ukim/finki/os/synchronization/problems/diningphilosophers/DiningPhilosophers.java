package mk.ukim.finki.os.synchronization.problems.diningphilosophers;

import java.util.Date;
import java.util.HashSet;
//import java.util.Scanner;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Александар Митревски
 * @author Гроздан Маџаров
 * 
 */
public class DiningPhilosophers {
	public static int NUMBER_OF_PHILOSOPHERS;

	// comments of the form /*** <comment text here> ***/ point out places the
	// actual solution is implemented

	/*** Place to declare synchronization variables ***/
	private static Semaphore[] forks;

	public static void init() {
		/*** Place to initialize synchronization variables ***/
		forks = new Semaphore[DiningPhilosophers.NUMBER_OF_PHILOSOPHERS];
		for (int i = 0; i < forks.length; i++) {
			forks[i] = new Semaphore(1, true);
		}
	}

	public static class Philosopher extends TemplateThread {
		private int index;

		/*** Place to declare synchronization variables ***/
		private Semaphore firstFork;
		private Semaphore secondFork;

		public int getFirstFork() {
			/*** Place to choose first fork ***/
			return Math.min(this.index, (this.index + 1)
					% DiningPhilosophers.NUMBER_OF_PHILOSOPHERS);
		}

		public int getSecondFork() {
			/*** Place to choose second fork ***/
			return Math.max(this.index, (this.index + 1)
					% DiningPhilosophers.NUMBER_OF_PHILOSOPHERS);
		}

		public Philosopher(int numRuns, int index) {
			super(numRuns);

			this.index = index;

			/*** Place to initialize synchronization variables ***/
			firstFork = DiningPhilosophers.forks[this.getFirstFork()];
			secondFork = DiningPhilosophers.forks[this.getSecondFork()];
		}

		@Override
		public void execute() throws InterruptedException {
			/*** Place to synchronize philosopher ***/

			// pick up forks
			firstFork.acquire();
			secondFork.acquire();

			// eat
			state.eat();

			// leave forks
			firstFork.release();
			secondFork.release();

			// think
			state.think();
		}

		public int getIndex() {
			return index;
		}
	}

	static DiningPhilosophersState state = new DiningPhilosophersState();

	public static void main(String[] args) {
		// removed [for i = 1 to 10 do run()] loop here - there are settings in
		// run() to mimic similar behavior
		run();
	}

	public static void run() {
		try {
			DiningPhilosophers.NUMBER_OF_PHILOSOPHERS = 5; // default initial
															// value
			int numRuns = 5; // number of times will Philosopher.execute() be
								// executed
			int numIterations = 1; // default number of iterations tested
			int step = 5; // the increment for NUMBER_OF_PHILOSOPHERS on each
							// iteration

			/*
			 * may become useful for building test cases Scanner s = new
			 * Scanner(System.in); DiningPhilosophers.NUMBER_OF_PHILOSOPHERS =
			 * s.nextInt(); numRuns = s.nextInt(); numIterations = s.nextInt();
			 * step = s.nextInt(); s.close();
			 */

			System.out.println("Start time: " + new Date().getTime());

			for (int a = 0; a < numIterations; a++) {
				System.out.println("\nTesting "
						+ DiningPhilosophers.NUMBER_OF_PHILOSOPHERS
						+ " philosophers.");

				// the order of these lines needs to be preserved

				state.clear();
				init();

				HashSet<Thread> threads = new HashSet<Thread>();

				for (int i = 0; i < DiningPhilosophers.NUMBER_OF_PHILOSOPHERS; i++) {
					threads.add(new Philosopher(numRuns, i));
				}

				ProblemExecution.start(threads, state);

				DiningPhilosophers.NUMBER_OF_PHILOSOPHERS += step;
			}

			System.out.println("\nFinish time: " + new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}