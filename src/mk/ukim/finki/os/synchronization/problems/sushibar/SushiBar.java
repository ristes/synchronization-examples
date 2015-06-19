package mk.ukim.finki.os.synchronization.problems.sushibar;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Atanas Dimitrovski
 * 
 */
public class SushiBar {

	static SushiBarState state = new SushiBarState();

	static Semaphore customers;
	static int numOfPeopleOnTable;
	static Object lock;
	static boolean fullTable;

	public static void init() {
		customers = new Semaphore(5);
		numOfPeopleOnTable = 0;
		lock = new Object();
		fullTable = false;
	}

	public static class Customer extends TemplateThread {

		public Customer(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {

			// Semafor koj oznacuva kolku mesta slobodni ima vo barot
			customers.acquire();

			state.seat();

			// Zgolemuvanje na brojot na luge vo barot i proverka dali ima
			// moznost za sozdavanje grupa
			synchronized (lock) {
				numOfPeopleOnTable++;
				if (numOfPeopleOnTable == 5) {
					fullTable = true;
					state.groupGathered();
				}
			}

			// Posetitelot jade
			state.eat();

			// Zaminuvanje na posetitelto od barot i proverka dali e posleden
			// clen na grupata.
			// Dokolku e, go izvestuva barot deka barot ne e zafaten od grupata
			synchronized (lock) {
				numOfPeopleOnTable--;

				if (numOfPeopleOnTable == 0 && fullTable) {
					fullTable = false;
					state.groupDone();
					customers.release(5);
				} else if (numOfPeopleOnTable > 0 && fullTable) {
					state.done();
				} else {
					state.done();
					customers.release();
				}
			}
		}

	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			int numRuns = 1;
			int numIterations = 1200;

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numIterations; i++) {
				Customer c = new Customer(numRuns);
				threads.add(c);
			}

			init();

			ProblemExecution.start(threads, state);
			// System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
