package mk.ukim.finki.os.synchronization.problems.diningphilosophers;

import java.util.Random;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.problems.diningphilosophers.DiningPhilosophers;
import mk.ukim.finki.os.synchronization.problems.diningphilosophers.DiningPhilosophers.Philosopher;

/**
 * 
 * @author Александар Митревски
 * @author Гроздан Маџаров
 * 
 */
public class DiningPhilosophersState extends AbstractState {
	// main issues for this problem:
	// +deadlock - checked by the architecture itself - see ProblemExecution
	// +fork used by more than one philosopher at one time - checked under
	// SHARED_FORKS
	// +no parallel eating - checked under EATING_NOT_PARALLEL
	// +starvation - checked under STARVATION

	private static final String SHARED_FORKS = "A fork is used by two philosophers simultaneously.";
	private static final String STARVATION = "Some philosophers starve.";
	private static final String EATING_NOT_PARALLEL = "Philosophers do not eat in parallel.";
	private static final int SHARED_FORKS_POINTS = 25;
	private static final int STARVATION_POINTS = 10;
	private static final int EATING_NOT_PARALLEL_POINTS = 5;

	private enum PhilosopherState {
		EATING, THINKING
	} // THINKING here includes 'waiting to eat'

	private BoundCounterWithRaceConditionCheck eating;
	private Integer[] meals;
	volatile private PhilosopherState[] states;

	private static final Random numberGenerator = new Random();

	public DiningPhilosophersState() {
		clear();
	}

	// (re)initializes State
	public void clear() {
		eating = new BoundCounterWithRaceConditionCheck(0,
				DiningPhilosophers.NUMBER_OF_PHILOSOPHERS / 2,
				SHARED_FORKS_POINTS, SHARED_FORKS, null, 0, null);

		meals = new Integer[DiningPhilosophers.NUMBER_OF_PHILOSOPHERS];
		states = new PhilosopherState[DiningPhilosophers.NUMBER_OF_PHILOSOPHERS];
		for (int i = 0; i < DiningPhilosophers.NUMBER_OF_PHILOSOPHERS; i++) {
			meals[i] = 0;
			states[i] = PhilosopherState.THINKING;
		}
	}

	public static int randomIntegerInRange(int a, int b) {
		return numberGenerator.nextInt((b - a) + 1) + a;
	}

	// since eat() and think() are called by several threads simultaneously,
	// the sections within the methods that modify class member variables should
	// be synchronized.
	// synchronization issues are explained by using comments with format: /***
	// <comment text here> ***/.

	public void eat() throws InterruptedException {
		/***
		 * following line only reads from current Thread - no possibility of
		 * race condition
		 ***/

		// which philosopher calls this method
		int key = ((Philosopher) this.getThread()).getIndex();

		/***
		 * lines that modify states[] and meals[] modify only the element at
		 * index 'key'. since 'key' is the index of the current Philosopher
		 * thread and only Philosopher threads call this method, there is no
		 * risk of race conditions since each Philosopher has a unique index.
		 * there are no race conditions when multiple threads change different
		 * elements within the same array simultaneously. only one Philosopher
		 * thread modifies the element "states[key]" or "meals[key]" at any
		 * time. later however, in this method, data is read from states[] which
		 * could be modified by other threads calling this same method,
		 * therefore, insurance must be made that reads and writes to states[]
		 * are made atomically, which is why states[] is declared volatile
		 * (volatile insures atomic read/write and that all threads see the
		 * latest value of the variable - see documentation)
		 ***/

		// indicate that philosopher started eating
		states[key] = PhilosopherState.EATING;

		/***
		 * following lines only read State object information - no possibility
		 * of race condition
		 ***/

		// check if any neighbors eat at this time
		int indexNeighbor1 = key - 1;
		if (indexNeighbor1 < 0) {
			indexNeighbor1 = DiningPhilosophers.NUMBER_OF_PHILOSOPHERS - 1;
		}
		int indexNeighbor2 = (key + 1)
				% DiningPhilosophers.NUMBER_OF_PHILOSOPHERS;

		if (states[indexNeighbor1] == PhilosopherState.EATING
				|| states[indexNeighbor2] == PhilosopherState.EATING) {
			// given manner of checking might not catch all instances of sharing
			// the same fork,
			// but instead guarantees that at least one instance will be
			// detected, and consequentially, at least one exception will be
			// logged
			// which, in this context, is enough
			log(new PointsException(SHARED_FORKS_POINTS, SHARED_FORKS), null);
		}

		/***
		 * 'eating' is of class BoundCounterWithRaceConditionCheck - it is
		 * thread safe
		 ***/
		// increment number of currently eating philosophers
		log(eating.incrementWithMax(false), "Philosopher " + key
				+ " started eating.");

		/*** following line is thread-safe for reasons explained previously ***/
		// update that current philosopher got a meal
		meals[key]++;

		// take some time to eat
		Thread.sleep(DiningPhilosophersState.randomIntegerInRange(1, 5));
		// this is used instead of Switcher because authors consider it to be
		// less obscure.
		// also, a lower limit on the number of milliseconds can be set, whereas
		// Switcher hardcodes the lower limit.
		// if needed, replace with Switcher method

		/*** following line is thread-safe for reasons explained previously ***/
		// indicate that philosopher finished eating
		states[key] = PhilosopherState.THINKING;

		/***
		 * 'eating' is of class BoundCounterWithRaceConditionCheck - it is
		 * thread safe
		 ***/
		// finished eating, decrement number of eating philosophers
		log(eating.decrementWithMin(false), "Philosopher " + key
				+ " finished eating.");
	}

	public void think() throws InterruptedException {
		/***
		 * following line only reads information from current Thread - no
		 * possibility of race condition
		 ***/

		// which philosopher calls this method
		int key = ((Philosopher) this.getThread()).getIndex();

		log(null, "Philosopher " + key + " is thinking.");

		// taking some time to think
		Thread.sleep(DiningPhilosophersState.randomIntegerInRange(1, 10));
	}

	@Override
	public synchronized void finalize() {
		// checking parallel eating
		if (eating.getMax() == 1) {
			logException(new PointsException(EATING_NOT_PARALLEL_POINTS,
					EATING_NOT_PARALLEL));
		}

		// checking starvation
		for (int i = 0; i < meals.length; i++) {
			if (meals[i] == 0) {
				logException(new PointsException(STARVATION_POINTS, STARVATION));
				break;
			}
		}

		/*
		 * uncomment for outputting the number of meals each philosopher had
		 * for(int i = 0; i < meals.length; i++){
		 * System.out.println("Philosopher " + i + " had " + meals[i] + " meal"
		 * + (meals[i] > 1 ? "s" : "") + "."); }
		 */
	}

}
