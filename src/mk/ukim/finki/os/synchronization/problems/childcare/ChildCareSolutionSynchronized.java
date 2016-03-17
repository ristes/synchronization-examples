/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mk.ukim.finki.os.synchronization.problems.childcare;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 *
 * @author Nikolina
 */
public class ChildCareSolutionSynchronized {
	public static Semaphore adultQueue;
	public static Semaphore childQueue;

	public static Semaphore mutex;

	public static void init() {
		adultQueue = new Semaphore(100);
		childQueue = new Semaphore(100);

		mutex = new Semaphore(1);
	}

	public static class Adult extends TemplateThread {
		public Adult(int numberOfRuns) {
			super(numberOfRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			
			state.adultEntered();
//			state.adultInside();
			state.adultLeaving();

			mutex.acquire();
			state.adultEntered();
			
			if (state.childrenEntering > 0) {			
				int n = Math.min(3, state.childrenEntering);
				state.childrenEntered(n);
				childQueue.release(n);
			}
			mutex.release();
//			state.adultInside();
			mutex.acquire();

			if (state.children <= 3 * (state.adults - 1)) {
				state.adultLeft();
				mutex.release();
			} else {
				state.adultLeaving();
				mutex.release();
				adultQueue.acquire();
			}
		}
	}

	public static class Child extends TemplateThread {
		public Child(int numberOfRuns) {
			super(numberOfRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			
//			state.childrenEntered();
//			state.childInside();
//			state.childrenLeave();
			
			mutex.acquire();

			if (state.children < 3 * state.adults) {
				state.childrenEntered(1);
				mutex.release();
			} else {
				state.childEntering();
				mutex.release();
				childQueue.acquire();
			}
			
			mutex.acquire();
			state.childLeft();

			if ((state.adultsLeaving > 0)
					&& state.children <= 3 * (state.adults - 1)) {
				state.adultLeft();
				adultQueue.release();
			}

			mutex.release();
		}
	}

	static ChildCareState state = new ChildCareState();

	public static void main(String[] args) {

		for (int i = 1; i <= 10; i++) {
			System.out.println("Run: " + i);
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
