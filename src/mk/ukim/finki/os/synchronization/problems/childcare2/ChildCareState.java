package mk.ukim.finki.os.synchronization.problems.childcare2;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.PointsException;

public class ChildCareState extends AbstractState {

	private static final String ADULTS_LEAVING_DEADLOCK = "Adult leaving deadlock... Adults should be able to leave when numberOfChildren <= 3 * (numberOfAdults - 1).";
	private static final String CHILDREN_ENTERING_DEADLOCK = "Children entering deadlock... Children should be able to enter when numberOfChildren < 3 * numberOfAdults";

	private static final int ADULTS_LEAVING_DEADLOCK_POINTS = 15;
	private static final int CHILDREN_ENTERING_DEADLOCK_POINTS = 15;

	public int adults;
	public int children;

	public int adultsArrived;
	public int childrenArrived;

	public ChildCareState() {
		adults = 0;
		children = 0;

		adultsArrived = 0;
		childrenArrived = 0;

	}

	public void adultArrived() {
		synchronized (this) {
			log(null, "Adult arrived!");
			adultsArrived++;
		}
	}

	public void adultEntered() {
		synchronized (this) {
			log(null, "Adult entered!");
			adults++;
		}
	}

	public void adultLeft() {
		synchronized (this) {
			log(null, "Adult left");
			adults -- ;
			if (children  >  3 * adults) {
				PointsException e = new PointsException(
						ADULTS_LEAVING_DEADLOCK_POINTS, ADULTS_LEAVING_DEADLOCK);
				log(e, null);
			}
		}
	}

	public void childArrived() {
		synchronized (this) {
			log(null, "Child arrived!");
			childrenArrived++;
		}
	}

//	public void childrenEntered(int n) {
//		synchronized (this) {
//			log(null, (n == 1 ? "Child entered!" : (n + " children entered!")));
//			if (childrenWaitingToEnter > 0)
//				childrenWaitingToEnter -= n;
//			children += n;
//		}
//	}

	public void childLeft() {
		synchronized (this) {
			log(null, "Child left!");
			children--;
		}
	}

	public void childEntered() {
		synchronized (this) {
			log(null, "Child entered ");
			children ++ ;
			if (children > 3 * adults) {
				PointsException e = new PointsException(
						CHILDREN_ENTERING_DEADLOCK_POINTS,
						CHILDREN_ENTERING_DEADLOCK);
				log(e, null);
			}
		}
	}

	public void reset() {
		adults = 0;
		children = 0;

	
		adultsArrived = 0;
		childrenArrived = 0; 
	}

	@Override
	public void finalize() {
		reset();
	}
}
