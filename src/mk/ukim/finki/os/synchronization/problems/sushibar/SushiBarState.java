package mk.ukim.finki.os.synchronization.problems.sushibar;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;

/**
 * 
 * @author Atanas Dimitrovski
 *
 */
public class SushiBarState extends AbstractState {

	private static final int MAXIMUM_5_PLAYERS_POINTS = 10;
	private static final int GROUP_IN_THE_BAR_POINTS = 5;
	private static final int GROUP_NOT_GATHERED_POINTS = 5;
	private static final int NOT_ENOUGH_CUSTOMERS_POINTS = 10;
	private static final int NOT_LAST_MEMBER_OF_GROUP_POINTS = 5;

	private static final String MAXIMUM_5_PLAYERS = "Poveke od 5 posetiteli probuvaat da sednat istovremeno!!!";
	private static final String GROUP_IN_THE_BAR = "Ima grupa vo barot!!!";
	private static final String GROUP_NOT_GATHERED = "Ne e sostavena grupa a ima dovolno posetiteli!!!";
	private static final String NOT_ENOUGH_CUSTOMERS = "nema dovolno posetiteli za da se sostavi grupa!!!";
	private static final String NOT_LAST_MEMBER_OF_GROUP = "ima seuste clenovi od grupata!!!";

	private BoundCounterWithRaceConditionCheck customersAtTable;
	private BoundCounterWithRaceConditionCheck peopleEating;
	private boolean groupOnTable;
	private boolean checkForParallelismAfterGroup;
	private int maxAfterGroup;

	public SushiBarState() {
		// TODO Auto-generated constructor stub
		customersAtTable = new BoundCounterWithRaceConditionCheck(0, 5,
				MAXIMUM_5_PLAYERS_POINTS, MAXIMUM_5_PLAYERS, null, 0, null);

		peopleEating = new BoundCounterWithRaceConditionCheck(0);
		groupOnTable = false;
		checkForParallelismAfterGroup = false;
		maxAfterGroup = 1;

	}

	/*
	 * Posetitel sednuva vo barot
	 */
	public void seat() {
		if (groupOnTable) {
			log(new PointsException(GROUP_IN_THE_BAR_POINTS, GROUP_IN_THE_BAR),
					null);
		}
		// Proverka dali ima mesto
		log(customersAtTable.incrementWithMax(false), "Posetitel sednuva");
		if (checkForParallelismAfterGroup) {
			if (customersAtTable.getValue() > 1)
				maxAfterGroup = customersAtTable.getValue();
		}
		Switcher.forceSwitch(5);
	}

	/*
	 * Posetitel ruca
	 */
	public void eat() {
		// Zgolemuvanje na brojot koi jadat vo barot
		log(peopleEating.incrementWithMax(false), "Posetitel se hrani");
		// Proverka dali e formirana grupa dokolku ima 5 posetiteli vo barot
		if ((peopleEating.getValue() == 5) && (!groupOnTable))
			log(new PointsException(GROUP_NOT_GATHERED_POINTS,
					GROUP_NOT_GATHERED), null);
		Switcher.forceSwitch(10);
	}

	/*
	 * Petiot posetitel mu signalizira na barot deka clenovite na masata se
	 * grupa
	 */
	public void groupGathered() {
		// Proverka dali ima grupa veke vo barot
		if (groupOnTable) {
			log(new PointsException(GROUP_IN_THE_BAR_POINTS, GROUP_IN_THE_BAR),
					null);
		}
		// Proverka dali ima dovolno posetiteli za da se sozdade grupa
		log(customersAtTable.assertEquals(5, NOT_ENOUGH_CUSTOMERS_POINTS,
				NOT_ENOUGH_CUSTOMERS), null);
		groupOnTable = true;
	}

	/*
	 * Posledniot clen na grupata mu signalizira na barot deka se zavrseni i
	 * deka masata e slobodna
	 */
	public void groupDone() {
		// Naglasuvanje deka grupata e zavrsena. Se povikuva od posledniot clen
		// Proverka dali bila sostavena grupata
		if (!groupOnTable) {
			log(new PointsException(5, "Nemalo grupa vo barot za da si odi!!!"),
					null);
		}
		groupOnTable = false;
		// Namaluvanje na brojot vo barot
		log(customersAtTable.decrementWithMin(false),
				"Posledniot clen na grupata zaminuva");
		peopleEating.decrementWithMin(false);

		// Proverka dali posetitelot e posledniot clen na grupata
		log(customersAtTable.assertEquals(0, NOT_LAST_MEMBER_OF_GROUP_POINTS,
				NOT_LAST_MEMBER_OF_GROUP), null);
		if (!checkForParallelismAfterGroup) {
			checkForParallelismAfterGroup = true;
		}
		Switcher.forceSwitch(3);
	}

	/*
	 * Posetitel go napusta barot
	 */
	public void done() {
		// Proverka dali ima clenovi za da mozat da ja izvrsat funkcijata
		log(customersAtTable.assertNotEquals(0, 5,
				"Nema clenovi koi bi mozele da zaminat!!!"), null);
		// Namaluvanje na brojot na clenovi
		peopleEating.decrementWithMin(false);
		log(customersAtTable.decrementWithMin(false), "Posetitel zaminuva");

	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		// Proverka dali sednuvanjeto e paralelno
		if (customersAtTable.getMax() == 1) {
			logException(new PointsException(10, "ne e paralelno Sednuvanjeto"));
		}
		// Proverka dali e dozoleno da vlezat 5 clena vo barot
		if (customersAtTable.getMax() != 5) {
			logException(new PointsException(10, "Ne e sostavena grupa"));
		}
		// proverka dali jadenjeto e paralelno
		if (peopleEating.getMax() == 1) {
			logException(new PointsException(10, "ne e paralelno jadenjeto"));
		}
		// Proverka dali sednuvanjeto ostanuva paralelno po zaminuvanje na
		// grupata
		if (maxAfterGroup == 1) {
			logException(new PointsException(10,
					"Po zaminuvanje na grupata, sednuvanjeto ne e paralelno"));
		}
	}

}