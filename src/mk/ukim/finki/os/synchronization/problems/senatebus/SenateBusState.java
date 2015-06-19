package mk.ukim.finki.os.synchronization.problems.senatebus;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;

public class SenateBusState extends AbstractState {
	
	private static final int ABOVE_MAXIMUM_RIDERS_ON_BOARD_POINTS = 7;
	private static final int ABOVE_MAXIMUM_BUSSES_ON_STATION_POINTS = 7;
	private static final int NOT_ENOUGH_RIDERS_BOARDED_POINTS = 5;
	private static final int MORE_THAN_ENOUGH_RIDERS_BOARDED_POINTS = 5;
	private static final int BUS_MUST_LEAVE_STATION_POINTS = 5;
	private static final int BUS_IS_NOT_ON_STATION_POINTS = 7;
	private static final int ZERO_RIDERS_ON_STATION_POINTS = 7;
	private static final int NEGATIVE_NUMBER_OF_RIDERS_ON_STATION_POINTS = 7;
	
	private static final String ABOVE_MAXIMUM_RIDERS_ON_BOARD = "Povekje od 50 patnici probuvaat da se kacat vo avtobusot!";
	private static final String ABOVE_MAXIMUM_BUSSES_ON_STATION = "Ne e dozvoleno povekje od eden avtobus da stoi na stanicata!";
	private static final String NOT_ENOUGH_RIDERS_BOARDED = "Avtobusot ne smee da ja napusti avtobuskata stanica dokolku vo nego ne se kaceni site predvideni patnici(ne povekje od 50) koi prvicno bile na stanicata koga toj pristignal!";
	private static final String MORE_THAN_ENOUGH_RIDERS_BOARDED = "Vo avtobusot ne smeat da vleguvaat patnici koi pristignale na avtobuskata stanica koga istiot vekje bil pristignat na nea!";
	private static final String BUS_MUST_LEAVE_STATION = "Avtobusot ne smee da stoi na avtobuskata stanica dokolku pri negovoto pristignuvanje na nea nemalo nitu eden patnik, tuku treba vednas da si zamine!";
	private static final String BUS_IS_NOT_ON_STATION = "Povikan e metodot za zaminuvanje na avtobus od stanicata vo moment koga nema avtobus na stanicata!";
	private static final String ZERO_RIDERS_ON_STATION = "Povikan e metodot za kacuvanje na patnik vo avtobusot vo moment koga nema nitu eden patnik na avtobuskata stanica!";
	private static final String NEGATIVE_NUMBER_OF_RIDERS_ON_STATION = "Na avtobuskata stanica ne e vozmozno da ima negativen broj na patnici!";
	
	private BoundCounterWithRaceConditionCheck ridersOnBoard;
	private BoundCounterWithRaceConditionCheck bussesOnStation;
	
	private boolean busOnStation;
	
	private int ridersArrivedOnStation;
	private int ridersToBeBoarded;
	private int ridersBoarded;
	
	public SenateBusState() {
		ridersOnBoard = new BoundCounterWithRaceConditionCheck(0, 50, ABOVE_MAXIMUM_RIDERS_ON_BOARD_POINTS, ABOVE_MAXIMUM_RIDERS_ON_BOARD, null, 0, null);
		bussesOnStation = new BoundCounterWithRaceConditionCheck(0, 1, ABOVE_MAXIMUM_BUSSES_ON_STATION_POINTS, ABOVE_MAXIMUM_BUSSES_ON_STATION, null, 0, null);
		
		busOnStation = false;
		
		ridersArrivedOnStation = 0;
		ridersToBeBoarded = 0;
		ridersBoarded = 0;
	}
	
	/**
	 * Bus methods
	 */
	public void busArrives() {
		log(bussesOnStation.incrementWithMax(false), "Bus arrived!");
		
		busOnStation = true;
		ridersToBeBoarded = Math.min(ridersArrivedOnStation, 50);
	}
	
	public void busDeparts() {
		if (busOnStation) {
			if (ridersBoarded < ridersToBeBoarded) {
				new PointsException(NOT_ENOUGH_RIDERS_BOARDED_POINTS, NOT_ENOUGH_RIDERS_BOARDED);
			}
			
			if (ridersBoarded > ridersToBeBoarded) {
				if (ridersToBeBoarded == 0) {
					new PointsException(BUS_MUST_LEAVE_STATION_POINTS, BUS_MUST_LEAVE_STATION);
				}
	
				new PointsException(MORE_THAN_ENOUGH_RIDERS_BOARDED_POINTS, MORE_THAN_ENOUGH_RIDERS_BOARDED);
			}
			
			
			for (int i = 0; i < ridersToBeBoarded; i++) {
				ridersOnBoard.decrementWithMin(false);
			}
			
			log(bussesOnStation.decrementWithMin(false), "Bus departed!");
			busOnStation = false;
			ridersBoarded = 0;
			ridersToBeBoarded = 0;
		}
		else {
			new PointsException(BUS_IS_NOT_ON_STATION_POINTS, BUS_IS_NOT_ON_STATION);
		}
	}

	/**
	 * Rider methods
	 */
	public void riderArrives() {
		ridersArrivedOnStation++;
	}
	
	public void riderBoardsBus() {
		ridersArrivedOnStation--;
		
		if (busOnStation) {
			if (ridersArrivedOnStation < 0) {
				new PointsException(ZERO_RIDERS_ON_STATION_POINTS, ZERO_RIDERS_ON_STATION);
			}
			ridersBoarded++;
			log(ridersOnBoard.incrementWithMax(false), "Rider on board!");
		}
		else {
			if (ridersArrivedOnStation < 0) {
				new PointsException(NEGATIVE_NUMBER_OF_RIDERS_ON_STATION_POINTS, NEGATIVE_NUMBER_OF_RIDERS_ON_STATION);
			}
		}
	}
	
	public void reset() {
		ridersOnBoard = new BoundCounterWithRaceConditionCheck(0, 50, ABOVE_MAXIMUM_RIDERS_ON_BOARD_POINTS, ABOVE_MAXIMUM_RIDERS_ON_BOARD, null, 0, null);
		bussesOnStation = new BoundCounterWithRaceConditionCheck(0, 1, ABOVE_MAXIMUM_BUSSES_ON_STATION_POINTS, ABOVE_MAXIMUM_BUSSES_ON_STATION, null, 0, null);
		
		busOnStation = false;
		
		ridersArrivedOnStation = 0;
		ridersToBeBoarded = 0;
		ridersBoarded = 0;
	}
	
	@Override
	public void finalize() {
		reset();
	}
	
}
