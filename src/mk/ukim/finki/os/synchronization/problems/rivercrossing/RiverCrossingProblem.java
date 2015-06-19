package mk.ukim.finki.os.synchronization.problems.rivercrossing;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Petre Petrov and
 * @author Ivica Obadic
 * 
 */
public class RiverCrossingProblem {

	/*
	 * променливи кои ни кажуваат колку луѓе од двете групи моментално стојат
	 * покрај бродот и чекаат да се качат
	 */
	static int hackers, serfs;

	/*
	 * Во моментот кога може да се формира комбинација која е дозволена се
	 * одлучува на колку луѓе од секоја од групите им се дозволува да се качат
	 * на бродот
	 */
	static Semaphore hackersWaiting, serfsWaiting;

	/*
	 * За да не настане Race Condition при проверка за тоа колку луѓе стојат до
	 * бродот
	 */
	static Semaphore checkTheBoat;

	/*
	 * Откако сме повикале правилна комбинација од луѓе треба секој прво да
	 * повика board() па после тоа последниот што е дојден и со кого е
	 * комплетирана комбинацијата да го повика row_boat()
	 */
	static Semaphore allowToRow;

	static Semaphore allowed;

	public RiverCrossingProblem() {

	}

	public static void init() {
		// иницијализација на семафорите и променливите
		allowToRow = new Semaphore(0);
		hackersWaiting = new Semaphore(0);
		serfsWaiting = new Semaphore(0);
		checkTheBoat = new Semaphore(1);
		allowed = new Semaphore(0);
		hackers = 0;
		serfs = 0;

	}

	static class Hacker extends TemplateThread {

		public Hacker(int numRuns) {
			super(numRuns);
		}

		/*
		 * решението накратко оди на овој начин:
		 * 
		 * до бродот доаѓаат луѓе (нитки) од двата типа и идејата е кога ќе
		 * дојде последниот човек со кој се формира една од валидните комбинации
		 * да дозволи луѓето кои ја формираат таа валидна комбинација да се
		 * качат на бродот
		 */
		public void cross() throws InterruptedException {

			boolean isCaptain = false;

			// дозволуваме само на еден човек во еден
			// момент да застане покрај бродот
			checkTheBoat.acquire();

			// го инкрементираме бројот на хакери кои моментално
			// стојат покрај бродот
			hackers++;

			if (hackers == 4) {
				/*
				 * доколку бројот на хакери стигнал до 4 значи ние ја имаме
				 * комбинацијата 4h i 0s во овој случај бројот на хакери кои
				 * стојат покрај бродот станува 0 а бројот на серфови си
				 * останува ист дозволуваме тие 4 хакери да се качат на бродот
				 * притоа последниот од нив кој е дојден со кој бројот на хакери
				 * се искачил на 4 го ставаме за капитен т.е. оној кој ќе ја
				 * повика row_boat()
				 */

				hackers = 0;
				hackersWaiting.release(4);

				isCaptain = true;

			} else if (hackers == 2 && serfs >= 2) {
				/*
				 * ова е моментот кога пред бродот има пристигнато повеќе од 2
				 * или 2 серфови и точно 2 хакери притоа се можеме да ја
				 * формираме комбинацијата 2h i 2s со што дозволуваме да се
				 * качат на бродот 2 хакери и 2 серfа
				 */

				hackers = 0;
				serfs -= 2;

				hackersWaiting.release(2);
				serfsWaiting.release(2);

				isCaptain = true;

			}

			else {
				/*
				 * ова е случај кога пред бродот нема потребен на хакери и
				 * серфови со кои би се формирала некоја комбинација, во тој
				 * случај само го ослободуваме влезот пред бродот за да може да
				 * влезе друг човек
				 */
				checkTheBoat.release();
			}

			/*
			 * ова е семафорот кој овозможува хакерите кои ќе дојдат пред бродот
			 * да чекаат за валидна комбинација со што би се ослободил семафорот
			 * и тие да може да се качат (тоа ослободување да потсетам го прави
			 * последниот патник со кој би се склопила таа валидна комбинација)
			 */
			hackersWaiting.acquire();

			// повик на функцијата board()
			state.board();

			/*
			 * ова е моментот кога сите се качени на бродот и доколку човекот не
			 * е капитен тогаш тој му дава еден пермит на капитенот за да ја
			 * повика row_boat(), а капитенот треба да чека три такви пермити со
			 * што би се осигурал дека тројцата патници некапитени се качени на
			 * бродот и ја имаат повикано board()
			 */
			if (!isCaptain) {
				allowToRow.release();
			}

			/*
			 * доколку е капитен тогаш ги чека потребните 3 пермити т.е чека
			 * сите тројца други патници да повикаат board() за тој да повика
			 * row_boat(), што е еден од главните услови на задачата, после тоа
			 * тој само го ослободува пристапот до бродот, за да може друг човек
			 * да дојде до бродот
			 */
			if (isCaptain) {
				allowToRow.acquire(3);

				state.rowBoat();

				checkTheBoat.release();
			}

		}

		@Override
		public void execute() throws InterruptedException {
			cross();
		}

	}

	static class Serf extends TemplateThread {

		public Serf(int numRuns) {
			super(numRuns);
		}

		// објаснувањето за серф класата е исто како и за хакер
		public void cross() throws InterruptedException {

			boolean isCaptain = false;

			checkTheBoat.acquire();

			serfs++;

			if (serfs == 4) {

				serfs = 0;

				serfsWaiting.release(4);

				isCaptain = true;

			} else if (serfs == 2 && hackers >= 2) {

				serfs = 0;

				hackers -= 2;

				isCaptain = true;
				hackersWaiting.release(2);
				serfsWaiting.release(2);

			}

			else {
				checkTheBoat.release();
			}

			serfsWaiting.acquire();

			state.board();

			if (!isCaptain) {

				allowToRow.release();

			}

			if (isCaptain) {
				allowToRow.acquire(3);

				state.rowBoat();

				checkTheBoat.release();
			}

		}

		@Override
		public void execute() throws InterruptedException {
			cross();
		}

	}

	static RiverCrossingState state = new RiverCrossingState();

	public static void main(String[] args) {
		for (int i = 0; i < 15; i++)
			run();
	}

	public static void run() {
		try {
			int numRuns = 1;
			int numIterations = 120;

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numIterations; i++) {
				Hacker h = new Hacker(numRuns);
				Serf s = new Serf(numRuns);
				threads.add(h);
				threads.add(s);
			}

			init();

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
