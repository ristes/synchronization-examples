package mk.ukim.finki.os.synchronization.problems.roomparty;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;

public class PartyState extends AbstractState {

	private BoundCounterWithRaceConditionCheck studentsAtParty;
	private BoundCounterWithRaceConditionCheck deanPresent;

	public PartyState() {

		studentsAtParty = new BoundCounterWithRaceConditionCheck(0);
		deanPresent = new BoundCounterWithRaceConditionCheck(0);

	}

	/**
	 * Studentot moze da vleze vo sobata dokolku dekanot ne e prisuten
	 */
	public void studentEnter() {
		// ovoj metod treba da trae podolgo vreme. po negovo zavrsuvanje studentot ke izleze od sobata
		log(deanPresent.assertEquals(0, 5,
				"Ne moze da vleze student, dekanot e vnatre"), null);

		log(studentsAtParty.incrementWithMax(false), "student enter");

	}

	/**
	 * Se oznacuva pocetok na zabavata
	 */

	public void dance() {
		Switcher.forceSwitch(100);
	}

	/**
	 * Dekanot smee da vleze samo ako nema studenti vo sobata ili ima poveke od
	 * 50
	 */
	public void deanEnter() {

		// novo assert funkcija koja proveruva dali brojot e pogolem od
		// argumentot ili pak ednakov na 0
		// log(studentsAtParty.assertLargerOrZero(50, 10,
		// "Dekanot nema pravo da vleze"), null);

		log(deanPresent.incrementWithMax(false), "dean enter");

	}

	/**
	 * Studentot ja napusta sobata
	 */

	public void studentLeave() {
		log(studentsAtParty.decrementWithMin(false), "student leave");
	}

	/**
	 * Dekanot smee da ja napusti sobata ako nema studenti vo nea
	 */

	public void deanLeave() {
		log(studentsAtParty.assertEquals(0, 5,
				"Ne smee da izleze dekanot ako ima ushte studenti"),
				"dean leave");
		log(deanPresent.decrementWithMin(false), null);
	}

	/**
	 * Dekanot moze da ja rasipe zabavata ako ima poveke od 50 studenti vo
	 * sobata
	 */
	public void breakUpParty() {
		log(studentsAtParty.assertNotEquals(0, 5,
				"Dekanot rastura zabava bez nieden prisuten student"),
				"break up party");

	}

	/**
	 * Dekanot moze da izvrsi prebaruvanje samo ako nema studenti vo sobata
	 */
	public void conductSearch() {
		log(studentsAtParty.assertEquals(0, 5,
				"Dekanot prebaruva, a ima ushte studenti"), "conduct search");
	}

	@Override
	public void finalize() {
		if (studentsAtParty.getMax() == 1) {
			new PointsException(5,
					"Vleguvanjeto na studentite ne e paralelizirano");
		}

		deanPresent.setValue(0);
		studentsAtParty.setValue(0);

	}

	public void startParty() {
		// TODO Auto-generated method stub

	}

}
