package mk.ukim.finki.os.synchronization.exam14.june;

import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

public class GymSynchronization {
	// static Semaphore Sala;
	static boolean Sala;
	static Semaphore Soblekuvalna;
	static Semaphore SlobodnoMesto;
	// static Semaphore Obleceni;
	static int vlezeni;
	static int obleceni;
	static int zavrsheni;
	static Object lock = new Object();

	public static void init() {
		// Sala = new Semaphore(1);
		Sala = true;
		SlobodnoMesto = new Semaphore(12);
		Soblekuvalna = new Semaphore(4);
		// Obleceni = new Semaphore(0);
		vlezeni = 0;
		obleceni = 0;
		zavrsheni = 0;
	}

	public static class Player extends TemplateThread {

		public Player(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {

			synchronized (lock) {
				if (Sala == true) {
					SlobodnoMesto.acquire();
					vlezeni++;
					if (vlezeni == 12)
						Sala = false;
				}
			}
			Soblekuvalna.acquire();
			state.presobleci();
			// Obleceni.release();
			obleceni++;
			Soblekuvalna.release();

			// Obleceni.acquire(12);
			if (obleceni == 12) {
				state.sportuvaj();
				zavrsheni++;
			}

			if (zavrsheni == 12) {

				synchronized (lock) {
					SlobodnoMesto.release();
					vlezeni--;
					if (vlezeni == 0) {

						Sala = true;
					}
				}
			}

		}

	}

	static GymState state = new GymState();

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			run();
		}
	}

	public static void run() {
		try {
			Scanner s = new Scanner(System.in);
			int numRuns = 1;
			int numIterations = 1200;
			s.close();

			HashSet<Thread> threads = new HashSet<Thread>();

			for (int i = 0; i < numIterations; i++) {
				Player h = new Player(numRuns);
				threads.add(h);
			}

			init();

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
