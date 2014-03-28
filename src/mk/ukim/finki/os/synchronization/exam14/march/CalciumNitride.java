package mk.ukim.finki.os.synchronization.exam14.march;

import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

public class CalciumNitride {

	static int caNum;
	static Semaphore lock;

	static Semaphore n;
	static Semaphore ca;

	static Semaphore nHere;
	static Semaphore caHere;
	static Semaphore ready;

	static Semaphore done;
	static Semaphore next;

	public static void init() {
		caNum = 0;
		lock = new Semaphore(1);

		n = new Semaphore(2);
		ca = new Semaphore(3);

		nHere = new Semaphore(0);
		caHere = new Semaphore(0);
		ready = new Semaphore(0);

		done = new Semaphore(0);
		next = new Semaphore(0);

	}

	public static class Calcium extends TemplateThread {

		public Calcium(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			ca.acquire();
			lock.acquire();
			caNum++;
			if (caNum == 3) {
				caNum = 0;
				lock.release();
				nHere.acquire(2);
				caHere.acquire(2);
				ready.release(4);
				state.bond();
				done.acquire(4);
				next.release(4);
				state.validate();
			} else {
				lock.release();
				caHere.release();
				ready.acquire();
				state.bond();
				done.release();
				next.acquire();
			}
			ca.release();
		}

	}

	public static class Nitrogen extends TemplateThread {

		public Nitrogen(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			n.acquire();
			nHere.release();
			ready.acquire();
			state.bond();
			done.release();
			next.acquire();
			n.release();
		}

	}

	static CalciumNitrideState state = new CalciumNitrideState();

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			Scanner s = new Scanner(System.in);
			int numRuns = 1;
			int numIterations = 100;
			s.close();

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numIterations; i++) {
				Nitrogen n = new Nitrogen(numRuns);
				threads.add(n);
				Calcium ca = new Calcium(numRuns);
				threads.add(ca);
				ca = new Calcium(numRuns);
				threads.add(ca);
				n = new Nitrogen(numRuns);
				threads.add(n);
				ca = new Calcium(numRuns);
				threads.add(ca);
			}

			init();

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
