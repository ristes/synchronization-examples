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
	
	static int elfCount;
	static int reindeerCount;
	
	static Semaphore santaSem;
	static Semaphore reindeerSem;
	static Semaphore elfSem;
	
	static Semaphore mutex;
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
			mutex.acquire();
			state.reindeerArrived();
			reindeerCount++;
			if (reindeerCount == 9) {
				santaSem.release();
			}
			mutex.release();
			
			reindeerSem.acquire();
			state.getHitched();
		}
	}

	public static class Elf extends TemplateThread {

		public Elf(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			elfMutex.acquire();
			mutex.acquire();
			state.elfEntered();
			elfCount++;
			if (elfCount == 3) {
				santaSem.release();
			} else {
				elfMutex.release();
			}
			mutex.release();
			
			elfSem.acquire();
			state.getHelp();
			
			mutex.acquire();
			elfCount--;
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
			santaSem.acquire();
			mutex.acquire();
			if (reindeerCount == 9) {
				reindeerCount = 0;
				state.prepSleigh();
				reindeerSem.release(9);
			} else if (elfCount == 3) {
				state.helpElves();
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

