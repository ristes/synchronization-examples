package mk.ukim.finki.os.synchronization.problems.rollercoaster;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;

/**
 * 
 * @author kikodamjan hristijan sardzoski damjan gjurovski
 * 
 */

public class RollerCoasterState extends AbstractState {

	private static final String MAXIMUM_10_PEOPLE = "Maximum 10 people for car ride are allowed";
	private static final String MAXIMUM_1_CAR = "Maximum 1 car is allowed";
	private static final String DONE_SHOULD_CALLED_ONCE = "The validate() method should be called only once per car ride.";
	private static final String BOARD_BEFORE_LOAD = "Can't invoke board before car load";
	private static final String LOAD_BEFORE_UNLOAD = "Can't invoke load before car unload";
	private static final String RUN_BEFORE_10_BOARD = "Can't invoke run before 10 passengers invoke board";
	private static final String UNBOARD_BEFORE_UNLOAD = "Can't invoke unboard before car unload";
	private static final String PARALEL_BOARD = "The passangers board is not paralel";
	private static final String CAR_RIDE_NOT_FINNISHED = "The car has still passangers";

	private static final int MAXIMUM_10_PEOPLE_POINTS = 5;
	private static final int MAXIMUM_1_CAR_POINTS = 5;
	private static final int DONE_SHOULD_CALLED_ONCE_POINTS = 5;
	private static final int BOARD_BEFORE_LOAD_POINTS = 5;
	private static final int LOAD_BEFORE_UNLOAD_POINTS = 5;
	private static final int RUN_BEFORE_10_BOARD_POINTS = 5;
	private static final int UNBOARD_BEFORE_UNLOAD_POINTS = 5;
	private static final int PARALEL_BOARD_POINTS = 5;
	private static final int CAR_RIDE_NOT_FINNISHED_POINTS = 5;

	private BoundCounterWithRaceConditionCheck car;
	private BoundCounterWithRaceConditionCheck passengers;

	public RollerCoasterState() {
		car = new BoundCounterWithRaceConditionCheck(0, 1,
				MAXIMUM_1_CAR_POINTS, MAXIMUM_1_CAR, null, 0, null);
		passengers = new BoundCounterWithRaceConditionCheck(0, 10,
				MAXIMUM_10_PEOPLE_POINTS, MAXIMUM_10_PEOPLE, null, 0, null);

	}

	public void board() {
		synchronized (this) {
			log(car.assertEquals(1, BOARD_BEFORE_LOAD_POINTS, BOARD_BEFORE_LOAD),
					"Car ready");
			log(passengers.incrementWithMax(false), "One passenger for the car");
			Switcher.forceSwitch(5);
		}
	}

	public void unboard() {
		synchronized (this) {
			log(car.assertEquals(0, UNBOARD_BEFORE_UNLOAD_POINTS,
					UNBOARD_BEFORE_UNLOAD), null);
			log(passengers.decrementWithMin(false),
					"One passenger left the car");
			Switcher.forceSwitch(5);
		}
	}

	public void load() {
		synchronized (this) {
			log(car.assertEquals(0, LOAD_BEFORE_UNLOAD_POINTS,
					LOAD_BEFORE_UNLOAD), null);
			log(car.incrementWithMax(false), "Car acquired");
		}
	}

	public void run() {
		synchronized (this) {
			log(passengers.assertEquals(10, RUN_BEFORE_10_BOARD_POINTS,
					RUN_BEFORE_10_BOARD), "Starting the ride");
			log(car.assertEquals(1, MAXIMUM_1_CAR_POINTS, MAXIMUM_1_CAR),
					"Car is ready");
			Switcher.forceSwitch(5);
		}
	}

	public void unload() {
		synchronized (this) {
			log(car.decrementWithMin(false), "Car released");
		}

	}

	public void validate() {
		synchronized (this) {
			if (car.getMax() == 1 && car.getValue() == 0
					&& passengers.getMax() == 10 && passengers.getValue() == 0) {
				reset();
				log(null, "Car ride finished successfully");

			} else if (passengers.getMax() == 10 && car.getMax() == 1
					&& (passengers.getValue() != 0 || car.getValue() != 0)) {
				log(new PointsException(CAR_RIDE_NOT_FINNISHED_POINTS,
						CAR_RIDE_NOT_FINNISHED), null);

			} else {
				log(new PointsException(DONE_SHOULD_CALLED_ONCE_POINTS,
						DONE_SHOULD_CALLED_ONCE), null);

			}
		}
	}

	public synchronized void reset() {
		car.setValue(0);
		passengers.setValue(0);
	}

	@Override
	public void finalize() {
		if (passengers.getMax() == 1) {
			logException(new PointsException(PARALEL_BOARD_POINTS,
					PARALEL_BOARD));
		}

	}

}
