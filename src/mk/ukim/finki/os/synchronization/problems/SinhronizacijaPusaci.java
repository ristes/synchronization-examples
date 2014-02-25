package mk.ukim.finki.os.synchronization.problems;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class SinhronizacijaPusaci {

	public static int NUM_RUNS = 10;
	public static final int RIZLA = 0;
	public static final int KIBRIT = 1;
	public static final int TUTUN = 2;
	// TODO: definirajte gi semaforite i ostanatite promenlivi ovde
	boolean[] sostojki;
	boolean[] cekaat;
	Semaphore pristapMasa;
	Semaphore[] cekaj;
	Semaphore uste;

	/**
	 * Metod koj treba da gi inicijalizira vrednostite na semaforite i
	 * ostanatite promenlivi za sinhronizacija.
	 * 
	 * @param brUcesnici
	 *            broj na ucesnici vo izvlekuvanjeto
	 * 
	 *            TODO: da se implementira
	 * 
	 */
	public void init() {
		sostojki = new boolean[3];
		pristapMasa = new Semaphore(0);

		uste = new Semaphore(1);

		cekaat = new boolean[3];
		cekaj = new Semaphore[3];
		for (int i = 0; i < 3; i++) {
			cekaj[i] = new Semaphore(0);
		}
	}

	class Agent extends Thread {

		private int brUcesnici;

		// TODO: add your variables here

		/**
		 * Da se implementira odnesuvanjeto na domakinot spored baranjeto na
		 * zadacata.
		 * 
		 */
		public void postavi() throws InterruptedException {
			uste.acquire();
			postaviSostojki();
			pristapMasa.release();
		}

		public Agent() {
		}

		@Override
		public void run() {
			try {
				for (int j = 0; j < NUM_RUNS; j++) {
					postavi();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class Pusac extends Thread {

		final int sostojka;
		boolean consumed = false;

		public Pusac(int sostojka) {
			this.sostojka = sostojka;
		}

		/**
		 * Da se implementira odnesuvanjeto na ucesnikot spored uslovite na
		 * zadacata.
		 */
		public void obid(int sostojka) throws InterruptedException {
			pristapMasa.acquire();

			// proverka dali se postaveni ostanatite 2 sostojki
			if (sostojki[(sostojka + 1) % 3]
					&& sostojki[(sostojka + 3 - 1) % 3]) {
				// konzumiraj gi stavkite
				smoke(sostojka);
				// oslobodi gi tie sto cekaat
				for (int i = 0; i < 3; i++) {
					if (cekaat[i]) {
						cekaj[i].release();
					}
				}
				// kazi mu na agentot da postavi novi stavki
				uste.release();
				consumed = true;
			} else {
				cekaat[sostojka] = true;
				pristapMasa.release();
				cekaj[sostojka].acquire();
			}

		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < NUM_RUNS; i++) {
					obid(sostojka);

				}
				System.out.println("done: " + sostojka);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	Random r = new Random();

	public void postaviSostojki() {
		System.out.print("Agent: postavuva: ");
		int a = r.nextInt(3);
		int b = r.nextInt(3);
		while (b == a) {
			b = r.nextInt(3);
		}
		boolean found = false;
		if (a == RIZLA || b == RIZLA) {
			System.out.print("rizla");
			found = true;
		}
		if (found) {
			System.out.print(" i ");
		}
		if (a == KIBRIT || b == KIBRIT) {
			System.out.print("kibrit");
			found = !found;
		}
		if (found) {
			System.out.print(" i ");
		}
		if (a == TUTUN || b == TUTUN) {
			System.out.print("tutun");
		}
		System.out.println("");
		sostojki[a] = sostojki[b] = true;
	}

	public void smoke(int sostojka) {
		sostojki[(sostojka + 1) % 3] = false;
		sostojki[(sostojka + 3 - 1) % 3] = false;
		switch (sostojka) {
		case RIZLA:
			System.out.println("Pusacot so RIZLA ja pusi cigarata.");
			break;
		case KIBRIT:
			System.out.println("Pusacot so KIBRIT ja pusi cigarata.");
			break;
		case TUTUN:
			System.out.println("Pusacot so TUTUN ja pusi cigarata.");
			break;
		default:
			break;
		}
	}

	public static void main(String[] args) {
		try {
			SinhronizacijaPusaci izvlekuvanje = new SinhronizacijaPusaci();
			izvlekuvanje.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void start() throws Exception {

		init();

		Agent prod = new Agent();
		HashSet<Thread> threads = new HashSet<Thread>();
		Pusac soRizla = new Pusac(RIZLA);
		Pusac soKibrit = new Pusac(KIBRIT);
		Pusac soTutun = new Pusac(TUTUN);

		threads.add(prod);
		threads.add(soRizla);
		threads.add(soKibrit);
		threads.add(soTutun);

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

	}
}
