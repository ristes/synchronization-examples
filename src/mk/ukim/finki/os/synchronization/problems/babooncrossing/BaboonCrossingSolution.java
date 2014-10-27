package mk.ukim.finki.os.synchronization.problems.babooncrossing;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Valentin Ambaroski
 * @author Vladica Jovanovski
 * 
 */

public class BaboonCrossingSolution {

	// Kontrola na jazheto od koja strana pominuvaat
	static Semaphore mutexRope;

	// Muteksi za promenlivite left i right levo i desno
	static Semaphore mutexLeft;
	static Semaphore mutexRight;

	// Kontrola na majmuni za vlez vo chekalnata
	static Semaphore turnStyle;

	// Kontrola na brojot na majmuni koi se kacheni na jazhe
	static Semaphore onRope;

	static int left;
	static int right;

	public static void init() {
		mutexRope = new Semaphore(1);
		mutexLeft = new Semaphore(1);
		mutexRight = new Semaphore(1);
		turnStyle = new Semaphore(1);
		onRope = new Semaphore(5);
		left = right = 0;
	}

	public static class BaboonLeft extends TemplateThread {

		public BaboonLeft(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {

			// Kontrola za izgladnuvanje.
			// So ovaj mutex se zabranuva vlez na majmuni od ist tip
			// bidejkiposle odredeno vreme che dojdi majmun od sprotivnata
			// strana
			// ovaj mutex che go pomini bidejki e osloboden od majmunite koi
			// chekaat
			// za da preminat na jazheto i se zaglaveni na onRope.acquire()
			// Majunot od sprotivnata strana che zaglavi na mutexRope.acquire()
			// I so toa nema da se dozvoli vlez na majmuni od istata strana.
			// Koga che preminat site majmuni koi chekaat za premin preku
			// jazheto
			// togash idi na red da preminuvaat majmuni od sprotivnata strana.
			turnStyle.acquire();

			// vlez na majmin
			state.enter(this);

			//
			mutexLeft.acquire();
			left++;
			// Samo prviot majmun ja smenuva sostojbata na jazheto
			if (left == 1) {
				mutexRope.acquire();
				state.leftPassing();
			}

			mutexLeft.release();

			// Ko ova se dozvoluva nekoj nareden majmun da dojdi od bilo koja
			// strana
			turnStyle.release();

			// Kontrola na brojot na majmuni na jazheto
			onRope.acquire();
			state.cross(this);
			onRope.release();

			mutexLeft.acquire();
			left--;
			state.leave(this);
			// Posledniot majmun od redicata koja shto cheka go osloboduva
			// jazheto
			if (left == 0) {
				mutexRope.release();
			}

			mutexLeft.release();
		}

	}

	public static class BaboonRight extends TemplateThread {

		public BaboonRight(int numRuns) {
			super(numRuns);
		}

		// Istiot koe e prepishan od BaboonLeft klasata so promena na left vo
		// right i obratno
		@Override
		public void execute() throws InterruptedException {
			turnStyle.acquire();
			state.enter(this);
			mutexRight.acquire();
			right++;
			if (right == 1) {
				mutexRope.acquire();
				state.rightPassing();
			}
			mutexRight.release();
			turnStyle.release();

			onRope.acquire();
			state.cross(this);
			onRope.release();

			mutexRight.acquire();
			right--;
			state.leave(this);
			if (right == 0) {
				mutexRope.release();
			}
			mutexRight.release();
		}
	}

	static BaboonCrossingState state = new BaboonCrossingState();

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			int numRuns = 1;
			int numScenarios = 500;

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numScenarios; i++) {
				BaboonLeft l = new BaboonLeft(numRuns);
				BaboonRight r = new BaboonRight(numRuns);
				threads.add(l);
				threads.add(r);
			}

			init();

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}