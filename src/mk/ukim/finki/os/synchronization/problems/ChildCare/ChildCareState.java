package mk.ukim.finki.os.synchronization.problems.ChildCare;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;


import static java.lang.Integer.max;

/**
 *
 * @author Nikolina
 */

public class ChildCareState extends AbstractState {

	private static final String ADULTS_LEAVING_DEADLOCK
                = "Adult leaving deadlock... Adults should be able to leave when numberOfChildren <= 3 * (numberOfAdults - 1).";
	private static final String CHILDREN_ENTERING_DEADLOCK
                = "Children entering deadlock... Children should be able to enter when numberOfChildren <= 3 * (numberOfAdults - 1)";

	private static final int ADULTS_LEAVING_DEADLOCK_POINTS = 15;
	private static final int CHILDREN_ENTERING_DEADLOCK_POINTS = 15;

        private int adults;
        private int children;

        public int adultsLeaving;
        public int childrenEntering;

	public ChildCareState() {
            adults = 0;
            children = 0;

            adultsLeaving = 0;
            childrenEntering = 0;
	}

        public void adultEntered() {
            synchronized (this) {
                //System.out.println("Adult entered!");
                log(null, "Adult entered!");
                adults++;
            }
        }

        public void adultLeft() {
            synchronized (this) {
                //System.out.println("Adult left!");
                log(null, "Adult left!");
                if (adultsLeaving > 0)
                    adultsLeaving--;
                adults--;
            }
        }

        public void adultLeaving() {
            synchronized (this) {
                //System.out.println("Adult leaving...");
                log(null, "Adult leaving...");
                adultsLeaving++;
                if(children <= 3 * (adults - 1))
                {
                    PointsException e = new PointsException(ADULTS_LEAVING_DEADLOCK_POINTS, ADULTS_LEAVING_DEADLOCK);
                    log(e, null);
                }
            }
        }

	public void childrenEntered(int n) {
            synchronized (this) {
                //System.out.println(n == 1 ? "Child entered!" : (n + " children entered!"));
		log(null, (n == 1 ? "Child entered!" : (n + " children entered!")));
                if (childrenEntering > 0)
                    childrenEntering -= max(n, childrenEntering);
		children += n;
            }
	}

        public void childLeft() {
            synchronized (this) {
                //System.out.println("Child left!");
                log(null, "Child left!");
                children--;
            }
        }

        public void childEntering() {
            synchronized (this) {
                //System.out.println("Child entering...");
                log(null, "Child entering...");
                childrenEntering++;
                if(children <= 3 * (adults - 1))
                {
                    PointsException e = new PointsException(CHILDREN_ENTERING_DEADLOCK_POINTS, CHILDREN_ENTERING_DEADLOCK);
                    log(e, null);
                }
            }
        }

	public void reset() {
            adults = 0;
            children = 0;

            adultsLeaving = 0;
            childrenEntering = 0;
	}

	@Override
	public void finalize() {
            reset();
            // printLog();
	}
}
