package mk.ukim.finki.os.synchronization.problems.senatebus;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Martin Pavlovski
 * 
 */

public class SenateBusSolution {
	
	// Brojac koj gi broi pristignatite patnici koi cekaat da se kacat vo prviot avtobus koj kje pristigne 
	// posle niv
	static int waiting;
	
	// Mutex koj go regulira pristignuvanjeto na patnicite na avtobuskata stanica i kacuvanjeto na patnicite
	// koi cekaat vo momentalno pristignatiot avtobus
	static Semaphore mutex;
	
	// Semafor koj signalizira koga daden avtobus pristignuva na avtobuskata stanica
	static Semaphore bus;
	
	// Semafor koj go signalizira kacuvanjeto na daden patnik vo avtobusot
	static Semaphore boarded;
	
	// Promenliva koja go pretstavuva brojot na preostanati patnici koi ne si zaminale so daden avtobus od
	// avtobuskata stanica
	static int ridersLeft;
	
	public static void init() {
		waiting = 0;
		mutex = new Semaphore(1);
		bus = new Semaphore(0);
		boarded = new Semaphore(0);
	}
	
	public static class Bus extends TemplateThread {
		
		public Bus(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			while (true) {
				// Vzaemno isklucuvanje so pomos na mutex-ot pri pristignuvanjeto na tekovniot avtobus
				mutex.acquire();
				// Pristignuvanje na avtobus na avtobuskata stanica
				state.busArrives();
				// Presmetuvanje na brojot na patnici koi kje treba da se kacat vo avtobusot koj tukusto
				// pristignal, pritoa nedozvoluvajkji istiot da bide pogolem od 50
				int n = Math.min(waiting, 50);
				
				// Odzemanje na brojot na patnici koi kje treba da se kacat vo avtobusot od brojot na
				// vkupnite preostanati patnici
				ridersLeft -= n;
				
				// Signaliziranje i cekanje na sekoj od patnicite da se kacat vo avtobusot
				for (int i = 0; i < n; i++) {
					bus.release();
					boarded.acquire();
				}
				
				// Presmetuvanje na brojot na preostanatite patnici na stanicata koi cekaat da se kacat
				// vo sledniot avtobus koj kje pristigne
				waiting = Math.max((waiting - 50), 0);
				mutex.release();
				
				// Zaminuvanje na avtobusot
				state.busDeparts();
				
				// Proverka na brojot na vkupno preostanati patnici
				if(ridersLeft == 0) {
					break;
				}
			}
		}
	}
	
	public static class Rider extends TemplateThread {
		
		public Rider(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			// Vzaemno isklucuvanje so pomos na mutex-ot pri pristignuvanjeto na tekovniot patnik
			mutex.acquire();
			// Pristignuvanje na patnik na avtobuskata stanica
			state.riderArrives();
			// Zgolemuvanje na brojot na pristignatite patnici koi cekaat da se kacat vo prviot 
			// avtobus koj kje pristigne posle niv
			waiting++;
			mutex.release();
			
			// Cekanje na moment vo koj patnikot kje moze da se kaci vo tekovniot avtobus na stanicata
			bus.acquire();
			// Kacuvanje na patnikot vo tekovniot avtobus
			state.riderBoardsBus();
			// Signaliziranje deka patnikot e kacen vo tekovniot avtobus
			boarded.release();
		}
	}
	
	static SenateBusState state = new SenateBusState();
	
	public static void main(String[] args) {
		for (int i = 1; i <= 10; i++) {
			System.out.println("Run: " + i);
			run();
		}
	}
	
	public static void run() {
		try {
			int numRiders = 125;
			ridersLeft = numRiders;
			HashSet<Thread> threads = new HashSet<>();
			
			int numRuns = 1;
			
			Bus bus = new Bus(numRuns);
			threads.add(bus);
			
			for (int i = 0; i < numRiders; i++) {
				Rider rider = new Rider(numRuns);
				threads.add(rider);
			}
			
			init();
				
			ProblemExecution.start(threads, state);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
