package mk.ukim.finki.os.synchronization.problems.faneuilhall;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;



/**
 * 
 * @author Peeva Viki
 *
 */


public class TheFaneuilHallSolution {

	//Semafor koj ni kazuva dali ima sudija vo salata 1 - nema, 0 - ima
	static Semaphore judgeNotInside;
	//Brojot na doselenici vo salata
	static Semaphore immigrantsInside;
	//Semafor koj go koristime za da ogranicime samo eden immigrant da moze da go povika metodot checkIn
	static Semaphore canCheckIn;
	//Semafor za da znaeme kolku doselenici smeat da gi povikaat metodite swear i getCertificate
	static Semaphore judgeConfirmed;
	//Broj na doselenici koi go povikale checkIn
	static Semaphore immigrantsCheckedIn;
	//Oznacuva dali smee da vleze nov sudija 1 - smee, 0 - ne. Pocetno e na 0 za prviot sudija
	static Semaphore newJudge;
	//Broj na doselenici koi bile del od momentalnata ceremonija
	static Semaphore immigrantsToFinish;
	//Semafor za ogranicuvanje na posledovatelno povikuvanje na metodite swear i getCertificate
	static Semaphore certificates;
	
	
	public static void init(){
		//Inicijalizacija na semaforite
		judgeNotInside = new Semaphore(1);
		immigrantsInside = new Semaphore(0);
		canCheckIn = new Semaphore(1);
		judgeConfirmed = new Semaphore(0);
		immigrantsCheckedIn = new Semaphore(0);
		newJudge = new Semaphore(1);
		immigrantsToFinish = new Semaphore(0);
		certificates = new Semaphore(1);
		
	}
	
	public static class Immigrant extends TemplateThread{
		
		public Immigrant(int numRuns){
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			//Doselenicite ne smeat da vleguvaat ako ima sudija vo salata
			judgeNotInside.acquire();
			state.enterImmigrant();
			immigrantsInside.release();	//Go zgolemuvame brojot na doselenici koi se vo salata
			judgeNotInside.release();
			
			//Posledovatelno povikuvanje na checkIn
			canCheckIn.acquire();
			state.checkIn();
			
			immigrantsCheckedIn.release();	//Brojot na doselenici koi go povikale checkIn
			
			canCheckIn.release();
			
			
			
			state.sitDown();
			
			
			
			judgeConfirmed.acquire();	//Cekame sudijata da potvrdi deka moze da se prodolzi ponatamu
			
			certificates.acquire();	//Posledovatelno povikuvanje na swear i getCertificate
			
			state.swear();
			state.getCertificate();
			
			certificates.release();
			
			
			immigrantsInside.acquire();
			judgeNotInside.acquire();	//Doselenicite ne smeat da izleguvaat od salata ako ima sudija vnatre
			immigrantsToFinish.acquire();	//Brojot na doselenici koi se del od momentalnata ceremonija
			//Pred da izleze posledniot doselenik od momentalnata ceremonija dava signal deka sleden sudija moze da vleze vo salata
			if(immigrantsToFinish.availablePermits() == 0){
				newJudge.release();
			}
			
			state.leaveImmigrant();
			
			judgeNotInside.release();
		}
	}
	
	
	public static class Judge extends TemplateThread{
		
		public Judge(int numRuns){
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			newJudge.acquire();	//Dali smee da vleze sudija
			judgeNotInside.acquire();
			state.enterJudge();
			
			//Dali site doselenici koi se vo salata go povikale checkIn
			immigrantsCheckedIn.acquire(immigrantsInside.availablePermits());
			//Ako vo salata nema doselenici sudijata dava signal deka sleden sudija moze da vleze vo salata otkako toj ke izleze
			if(immigrantsInside.availablePermits() == 0){
				newJudge.release();
			}
			//Brojot na doselenici koi prisustvuvale na momentalnata ceremonija
			immigrantsToFinish.release(immigrantsInside.availablePermits());	
			state.confirm();
			//Kolku od doselenicite imaat pravo da gi povikaat swear i getCertificate
			judgeConfirmed.release(immigrantsInside.availablePermits());
			
			
			state.leaveJudge();
			judgeNotInside.release();
		}
	}
	
	
	public static class Spectator extends TemplateThread{
		
		public Spectator(int numRuns){
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			//Gledac ne smee da vleze ako vo salata ima sudija
			judgeNotInside.acquire();
			state.enterSpectator();
			judgeNotInside.release();
			
			state.spectate();
			state.leaveSpectator();
		}
	}
	
	
	static TheFaneuilHallState state = new TheFaneuilHallState();
	
	public static void main(String [] args){
		for(int i=0;i<10;++i){
			run();
		}
		
	}
	
	public static void run(){
		try{
			int numRuns = 1;
			int numIterations = 100;
			
			HashSet<Thread> threads = new HashSet<Thread>();
			
			for(int i=0;i<numIterations;++i){
				Immigrant im = new Immigrant(numRuns);
				threads.add(im);
			}
			
			for(int i=0;i<numIterations;++i){
				Spectator sp = new Spectator(numRuns);
				threads.add(sp);
			}
			
			for(int i=0;i<10;++i){
				Judge judge = new Judge(1);
				threads.add(judge);
			}
			
			
			
			init();
			
			ProblemExecution.start(threads, state);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
