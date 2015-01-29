package mk.ukim.finki.os.synchronization.problems.childcare2;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

public class ChildCareSolutionSynchronized {

	static int adults;
	static int children;

	static Semaphore childSemaphore;

	static Semaphore adultLeave;

	static Semaphore lock;

	public static void init() {

		adults = 0;
		children = 0;

		childSemaphore = new Semaphore(3);

		adultLeave = new Semaphore(0);

		lock = new Semaphore(1);

	}

	public static class Adult extends TemplateThread {
		public Adult(int numberOfRuns) {
			super(numberOfRuns);

		}

		@Override
		public void execute() throws InterruptedException {

			state.adultArrived();
			state.adultEntered();
			lock.acquire();
			adults++;
			if (adults * 3 - children > 0)
				childSemaphore.release(adults * 3 - children);
			lock.release();
			adultLeave.acquire();
			state.adultLeft();
			lock.acquire();
			adults--;
			lock.release();
		}
	}

	public static class Child extends TemplateThread {
		public Child(int numberOfRuns) {
			super(numberOfRuns);
		}

		@Override
		public void execute() throws InterruptedException {

			state.childArrived();
			childSemaphore.acquire();
			lock.acquire();
			children++;
			if ((int) Math.floor(adults - children / 3) > 0)
				adultLeave.release((int) Math.floor(adults - children / 3));
			state.childLeft();
			children--;
			lock.release();

		}
	}

	static ChildCareState state = new ChildCareState();

	public static void main(String[] args) {

		for (int i = 1; i <= 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			int numChildren = 100;
			HashSet<Thread> threads = new HashSet<>();
			ChildCareSolutionSynchronized.Adult adult = new ChildCareSolutionSynchronized.Adult(
					(numChildren / 3) + 10);
			threads.add(adult);

			for (int i = 0; i < numChildren; i++) {
				ChildCareSolutionSynchronized.Child child = new ChildCareSolutionSynchronized.Child(
						1);
				threads.add(child);
			}

			init();

			ProblemExecution.start(threads, state);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
