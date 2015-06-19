package mk.ukim.finki.os.synchronization.problems.roomparty;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * @author Tashe Kjosev kjosevtasho@gmail.com
 * @author Gavril Ognjanovski g.ognjanovski@hotmail.com
 * @version 1.0
 * @since 2014-15-09
 */

public class PartySolution {

	static final int DEAN_NOT_HERE = 0;
	static final int DEAN_WAITING = 1;
	static final int DEAN_INSIDE = 2;

	static int students = 0;
	static int dean = DEAN_NOT_HERE;
	static Semaphore mutex = new Semaphore(1); // sinhronizacija na if
												// razgranuvanjeto
	static Semaphore turn = new Semaphore(1); // koj e na red da proba da vleze
	static Semaphore clear = new Semaphore(0); // signalizira na dekanot koga
												// sobata e prazna
	static Semaphore lieIn = new Semaphore(0); // signalizira na dekanot da
												// vleze ako cekal pred sobata

	public static void init() {

	}

	static class Student extends TemplateThread {

		public Student(int numRuns) {
			super(numRuns);
		}

		// studentot pristignuva i moze
		@Override
		public void execute() throws InterruptedException {

			mutex.acquire();
			// dokolku e dekanot vo sobata studentot nema pravo da vleze
			if (dean == DEAN_INSIDE) {
				mutex.release();
				turn.acquire();
				turn.release();
				mutex.acquire();
			}

			students++;
			state.studentEnter();

			// 51 student mu signalizira na dekanot deka moze da vleze
			if (students == 51 && dean == DEAN_WAITING) {
				lieIn.release();
			} else {
				mutex.release();
			}
			state.dance();
			mutex.acquire();
			students--;
			state.studentLeave();

			// ako dekanot ceka pred sobata posledniot student mu signalizira
			// deka moze da vleze
			if (students == 0 && dean == DEAN_WAITING) {
				lieIn.release();
			}
			// ako dekanot e vnatre, posledniot student mu signalizira deka
			// sobata e prazna
			else if (students == 0 && dean == DEAN_INSIDE) {
				clear.release();
			} else {
				mutex.release();
			}

		}

	}

	static class Dean extends TemplateThread {

		public Dean(int numRuns) {
			super(numRuns);
		}

		// dekanot pristignuva i ima 3 mozni slucaevi
		@Override
		public void execute() throws InterruptedException {

			mutex.acquire();
			// dokolku brojot na studenti e pomegju 0 i 51 togas dekanot mora da
			// ceka pred sobata

			// vo ovoj uslov dekanot ceka na studentite
			if (students > 0 && students < 51) {
				dean = DEAN_WAITING;
				mutex.release();
				// dekanot ceka za pogoden moment pred sobata
				lieIn.acquire();
			}

			// dokolku ima nad 50 studenti koga ke dojde dekanot
			// vo ovoj uslov dekanot ceka na studentite
			if (students > 50) {

				// vleguva vo soba i ja rastura zabavata
				dean = DEAN_INSIDE;

				state.deanEnter();

				state.breakUpParty();

				// redot e na studentite i se ceka site da izlezat
				turn.acquire();
				mutex.release();
				// posledniot student signalizira deka sobata e prazna
				clear.acquire();
				turn.release();

			}
			// dekanot nema potreba da ceka na studentite
			else {

				// ako brojot na studenti e 0 dekanot vleguva i go izvrsuva
				// prebaruvanjeto
				state.deanEnter();
				state.conductSearch();

			}
			state.deanLeave();
			dean = DEAN_NOT_HERE;
			mutex.release();

		}

	}

	static PartyState state = new PartyState();

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			int numRuns = 1;
			int numScenarios = 100;

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numScenarios; i++) {
				Student s = new Student(numRuns);
				threads.add(s);

			}
			threads.add(new Dean(numRuns));

			init();

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
