package mk.ukim.finki.os.synchronization.SantaClausProblem;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;

public class SantaClausState extends AbstractState {

	private static final String ALL_RAINDEERS_MUST_ARRIVE = "All of the nine reindeers must arrive in order to start hitching";
	private static final String SLEIGH_NOT_READY = "Santa has not finished preparing the sleigh";
	private static final String REINDEERS_NOT_PRESENT = "Cannot prepare sleigh - not all reindeers have returned from the Sounth Pacific";
	private static final String MAX_3_ElVES = "Maximum three elves are allowed to work in the workshop until they are all done";
	private static final String ALL_ELVES_MUST_ENTER = "There should be three elves in order to get the tools";
	private static final String SANTA_DID_NOT_HELP = "Santa should first bring the tools needed for making toys";
	private static final String MUST_PREPARE_SLEIGH_FIRST = "Santa must prepare the sleigh first - reindeers have higher priority";
	private static final String ELVES_NOT_PRESENT = "Cannot bring tools - there should be three elves in the workshop";
	
	private static final int ALL_RAINDEERS_MUST_ARRIVE_POINTS = 5;
	private static final int SLEIGH_NOT_READY_POINTS = 5;
	private static final int REINDEERS_NOT_PRESENT_POINTS = 5;
	private static final int MAX_3_ELVES_POINTS = 5;
	private static final int ALL_ELVES_MUST_ENTER_POINTS = 5;
	private static final int SANTA_DID_NOT_HELP_POINTS = 5;
	private static final int MUST_PREPARE_SLEIGH_FIRST_POINTS = 5; 
	private static final int ELVES_NOT_PRESENT_POINTS = 5;

	private int arrivedReindeers;
	private boolean sleighReady;
	private boolean firstHitched;
	private int enteredElves;
	private boolean elvesWantHelp;
	private boolean santaHelped;
	
	public SantaClausState() {
		arrivedReindeers = 0;
		sleighReady = false;
		firstHitched = false;
		enteredElves = 0;
		elvesWantHelp = false;
		santaHelped = false;
	}
	
	public void reindeerArrived() {
		Switcher.forceSwitch(3);
		synchronized (this) {
			log(null, "Reindeer arrived");
			arrivedReindeers++;
		}
	}
	
	public void getHitched() {
		Switcher.forceSwitch(3);
		synchronized (this) {
			if (!firstHitched) {
				firstHitched = true;
				if (arrivedReindeers != 9) {
					PointsException e = new PointsException(ALL_RAINDEERS_MUST_ARRIVE_POINTS, ALL_RAINDEERS_MUST_ARRIVE);
					log(e, null);
				} else if (!sleighReady) {
					PointsException e = new PointsException(SLEIGH_NOT_READY_POINTS, SLEIGH_NOT_READY);
					log(e, null);
				} else {
					log(null, "Reindeers starting to hitch to the sleigh");
				}
			}
		}
	}
	
	public synchronized void prepSleigh() {
		if (arrivedReindeers != 9) {
			PointsException e = new PointsException(REINDEERS_NOT_PRESENT_POINTS, REINDEERS_NOT_PRESENT);
			log(e, null);
		} else {
			log(null, "Preparing sleigh");
			sleighReady = true;
		}
	}
	
	public void elfEntered() {
		Switcher.forceSwitch(3);
		synchronized (this) {
			if (elvesWantHelp) {
				PointsException e = new PointsException(MAX_3_ELVES_POINTS, MAX_3_ElVES);
				log(e, null);
			} else {
				log(null, "Elf entering the workshop");
				enteredElves++;
				if (enteredElves == 3) {
					elvesWantHelp = true;
				}
			}
		}
	} 
	
	public void getHelp() {
		Switcher.forceSwitch(3);
		synchronized (this) {
			if (!elvesWantHelp) {
				PointsException e = new PointsException(ALL_ELVES_MUST_ENTER_POINTS, ALL_ELVES_MUST_ENTER);
				log(e, null);
			} else if (!santaHelped) {
				PointsException e = new PointsException(SANTA_DID_NOT_HELP_POINTS, SANTA_DID_NOT_HELP);
				log(e, null);
			} else { 
				log(null, "Elf working");
				enteredElves--;
				if (enteredElves == 0) {
					elvesWantHelp = false;
					santaHelped = false;
				}	
			}
		}
	}
	
	public synchronized void helpElves() {
		if (arrivedReindeers == 9 && !sleighReady && elvesWantHelp) {
			PointsException e = new PointsException(MUST_PREPARE_SLEIGH_FIRST_POINTS, MUST_PREPARE_SLEIGH_FIRST);
			log(e, null);
		} else if (!elvesWantHelp) {
			PointsException e = new PointsException(ELVES_NOT_PRESENT_POINTS, ELVES_NOT_PRESENT);
			log(e, null);
		} else if (elvesWantHelp) {
			log(null, "Helping elves - bringing tools");
			santaHelped = true;
		}
	}
	
	public void reset() {
		arrivedReindeers = 0;
		sleighReady = false;
		firstHitched = false;
		enteredElves = 0;
		elvesWantHelp = false;
		santaHelped = false;
	}
	
	@Override
	public void finalize() {
		reset();
		// printLog();
	}

}
