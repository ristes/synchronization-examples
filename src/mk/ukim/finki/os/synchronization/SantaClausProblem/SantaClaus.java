package mk.ukim.finki.os.synchronization.SantaClausProblem;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author Kiril Pepovski 121102 and Marija Nikolova 121195
 *
 */
public class SantaClaus {
	
	// Brojac koj ke gi broi elfovite koi vleguvaat vo rabotilnicata
	static int elfCount;
	// Brojac koj ke gi broi irvasite koi pristignale na severniot pol
	static int reindeerCount;
	
	// Semafor koj regulira koga Dedo Mraz treba da asistira na elfovite ili irvasite
	static Semaphore santaSem;
	// Semafor na koj treba da zastane sekoj irvas pred Dedo Mraz da ja pripremi sankata
	static Semaphore reindeerSem;
	// Semafor na koj treba da zastane sekoj elf pred Dedo Mraz da gi donese alatkite za izrabotka na igracki
	static Semaphore elfSem;
	
	// Muteks za brojacite
	static Semaphore mutex;
	// Muteks koj kje kazuva dali e slobodna rabotilnicata za vleguvanje
	static Semaphore elfMutex;

	public static void init() {
		elfCount = 0;
		reindeerCount = 0;
		
		santaSem = new Semaphore(0);
		reindeerSem = new Semaphore(0);
		elfSem = new Semaphore(0);
		
		mutex = new Semaphore(1);
		elfMutex = new Semaphore(1);
	}
	
	public static class Reindeer extends TemplateThread {

		public Reindeer(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			// Za mutual exclusion na brojacot reindeerCount (za da nema race condition)
			mutex.acquire();
			// Irvas pristignal - sleduva zgolemuvanje na brojacot
			state.reindeerArrived();
			reindeerCount++;
			// Ako pristignal i posledniot irvas - dozvoli mu na Dedo Mraz da se izvrsuva, odnosno da ja pripremi sankata
			if (reindeerCount == 9) {
				santaSem.release();
			}
			mutex.release();
			
			// Cekanje dodeka Dedo Mraz ne ja podgotvi sankata 
			reindeerSem.acquire();
			// Spregni se na sankata 
			state.getHitched();
		}
	}

	public static class Elf extends TemplateThread {

		public Elf(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			// elfMutex nema da dade pristap ako prethodno vlegle tri elfovi da rabotat i ne se zavrseni se' uste
			elfMutex.acquire();
			// Za mutual exclusion na brojacot elfCount (za da nema race condition)
			mutex.acquire();
			// Elf pristignal - sleduva zgolemuvanje na brojacot
			state.elfEntered();
			elfCount++;
			// Ako pristignal i tret elf, togas vikni go Dedo Mraz da gi donese alatkite
			// Ako ne - togas oslobodi ja vratata od rabotilnicata za da moze uste nekoj elf da vleze
			if (elfCount == 3) {
				santaSem.release();
			} else {
				elfMutex.release();
			}
			mutex.release();
			
			// Cekaj dodeka ne bidat alatkite doneseni
			elfSem.acquire();
			// Elfot raboti, pa izleguva
			state.getHelp();
			
			// Go stitime brojacot
			mutex.acquire();
			// Namaluvanje na brojot na elfovi 
			elfCount--;
			// Ako zavrsile site tri elfovi, togas oslovodi ja vratata za da vlezat tie sto cekaat (ako gi ima)
			if (elfCount == 0) {
				elfMutex.release();
			}
			mutex.release();
		}
	}

	public static class Santa extends TemplateThread {

		public Santa(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			// Cekaj dodeka elf ili irvas ne te povika
			santaSem.acquire();
			// Stiti gi brojacite
			mutex.acquire();
			// Prvo se proveruva dali irvasite se sobrale, bidejki tie imaat povisok prioritet
			// Ako se sobrale, togas podgotvi ja sankata
			// Ako ne - togas asistiraj na elfovite, odnosno donesi gi alatkite
			if (reindeerCount == 9) {
				reindeerCount = 0;
				state.prepSleigh();
				// Otkako sankata kje se podgotvi, ovozmozi im na irvasite da se spregnat
				reindeerSem.release(9);
			} else if (elfCount == 3) {
				state.helpElves();
				// Otkako kje bidat doneseni alatkite, ovozmozi im na elfovite da rabotat
				elfSem.release(3);
			}
			mutex.release();
		}
	}

	static SantaClausState state = new SantaClausState();
	
	public static void main(String[] args) {
		for (int i = 1; i <= 10; i++) {
			System.out.println("Run: " + i);
			run();	
		}
	}

	public static void run() {
		try {
			int numElves = 180;
			HashSet<Thread> threads = new HashSet<>();
			Santa santa = new Santa((numElves / 3) + 1);
			threads.add(santa);
			
			int numRuns = 1;
			for (int i = 0; i < numElves; i++) {
				Elf elf = new Elf(numRuns);
				threads.add(elf);
				if (i % 20 == 0) {
					Reindeer reindeer = new Reindeer(numRuns);
					threads.add(reindeer);
				}
			}
			
			init();
			
			ProblemExecution.start(threads, state);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

