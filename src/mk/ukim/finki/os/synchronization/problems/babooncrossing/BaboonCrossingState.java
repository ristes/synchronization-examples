package mk.ukim.finki.os.synchronization.problems.babooncrossing;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;
import mk.ukim.finki.os.synchronization.TemplateThread;
import mk.ukim.finki.os.synchronization.problems.babooncrossing.BaboonCrossingSolution.BaboonLeft;
import mk.ukim.finki.os.synchronization.problems.babooncrossing.BaboonCrossingSolution.BaboonRight;

/**
 * 
 * @author Valentin Ambaroski
 * @author Vladica Jovanovski
 * 
 */

public class BaboonCrossingState extends AbstractState {

	public enum StateRope {
		LEFT_PASSING, RIGHT_PASSING, NEUTRAL
	}

	private static final String CROSSING_NOT_PARALLEL = "The Crossing is NOT parallel!";
	private static final String MAXIMUM_5_BABOONS = "Maximum 5 Baboons are allowed on the rope. The rope broke";
	private static final String LEFT_NOT_ALLOWED = "The left baboon is not allowed to pass. (State not changed or there is right baboon passing)";
	private static final String RIGHT_NOT_ALLOWED = "The right baboon is not allowed to pass. (State not changed or there is left baboon passing)";
	private static final String UNSATISFIED_CONDITIONS_STATE = "The conditions for passing are not satisfied";
	private static final String STARVATION = "The crossing is starving. (Starvation condition unfilled)";

	private static final int STARVATION_VALUE = 30;
	private static final int STARVATION_POINTS = 10;
	private static final int MAXIMUM_5_BABOONS_POINTS = 10;
	private static final int CROSSING_NOT_PARALLEL_POINTS = 5;
	private static final int LEFT_NOT_ALLOWED_POINTS = 10;
	private static final int RIGHT_NOT_ALLOWED_POINTS = 10;
	private static final int UNSATISFIED_CONDITIONS_STATE_POINTS = 5;

	private BoundCounterWithRaceConditionCheck crossing;
	private BoundCounterWithRaceConditionCheck left;
	private BoundCounterWithRaceConditionCheck right;

	private StateRope state;

	public BaboonCrossingState() {
		state = StateRope.NEUTRAL;
		right = new BoundCounterWithRaceConditionCheck(0, STARVATION_VALUE, 5,
				"RIGHT-111", null, 0, null);
		left = new BoundCounterWithRaceConditionCheck(0, STARVATION_VALUE, 5,
				"LEFT-111", null, 0, null);
		crossing = new BoundCounterWithRaceConditionCheck(0, 5,
				MAXIMUM_5_BABOONS_POINTS, MAXIMUM_5_BABOONS, null, 0, null);
	}

	public void enter(TemplateThread baboon) {
		if (baboon instanceof BaboonLeft) {
			log(left.incrementWithMax(false), "LeftBaboon entering");
		} else if (baboon instanceof BaboonRight) {
			log(right.incrementWithMax(false), "RightBaboon entering");
		}
		Switcher.forceSwitch(10);
	}

	public void cross(TemplateThread baboon) {
		if (baboon instanceof BaboonLeft) {
			if (state == StateRope.LEFT_PASSING)
				log(crossing.incrementWithMax(false), "LeftBaboon crossing");
			else
				logException(new PointsException(LEFT_NOT_ALLOWED_POINTS,
						LEFT_NOT_ALLOWED));
		} else if (baboon instanceof BaboonRight) {
			if (state == StateRope.RIGHT_PASSING)
				log(crossing.incrementWithMax(false), "Right crossing");
			else
				logException(new PointsException(RIGHT_NOT_ALLOWED_POINTS,
						RIGHT_NOT_ALLOWED));
		}
		Switcher.forceSwitch(10);
		log(crossing.decrementWithMin(false), null);

	}

	public void leftPassing() {
		Switcher.forceSwitch(5);
		log(left.assertEquals(1, UNSATISFIED_CONDITIONS_STATE_POINTS,
				UNSATISFIED_CONDITIONS_STATE), "Condition 1 - OK");
		log(right.assertEquals(0, UNSATISFIED_CONDITIONS_STATE_POINTS,
				UNSATISFIED_CONDITIONS_STATE), "Condition 2 - OK");
		state = StateRope.LEFT_PASSING;
	}

	public void rightPassing() {
		Switcher.forceSwitch(5);
		log(right.assertEquals(1, UNSATISFIED_CONDITIONS_STATE_POINTS,
				UNSATISFIED_CONDITIONS_STATE), "Condition 1 - OK");
		log(left.assertEquals(0, UNSATISFIED_CONDITIONS_STATE_POINTS,
				UNSATISFIED_CONDITIONS_STATE), "Condition 2 - OK");
		state = StateRope.RIGHT_PASSING;
	}

	public void leave(TemplateThread baboon) {
		if (baboon instanceof BaboonLeft) {
			log(left.decrementWithMin(false), "LeftBaboon leaving");
		} else if (baboon instanceof BaboonRight) {
			log(right.decrementWithMin(false), "RightBaboon leaving");
		}
		Switcher.forceSwitch(3);
	}

	@Override
	public synchronized void finalize() {
		if (right.getMax() == 1 && left.getMax() == 1) {
			logException(new PointsException(CROSSING_NOT_PARALLEL_POINTS,
					CROSSING_NOT_PARALLEL));
		}

		if (right.getMax() > STARVATION_VALUE
				|| left.getMax() > STARVATION_VALUE)
			logException(new PointsException(STARVATION_POINTS, STARVATION));
	}

}