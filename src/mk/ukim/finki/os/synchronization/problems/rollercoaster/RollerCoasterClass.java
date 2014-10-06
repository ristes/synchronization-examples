package mk.ukim.finki.os.synchronization.problems.rollercoaster;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

/**
 * 
 * @author kikodamjan
 * hristijan sardzoski
 * damjan gjurovski
 * 
 */


public class RollerCoasterClass {

	public static int numOfPepole;
	public static Semaphore car;
	public static Semaphore passengers;
	public static Semaphore numOfPeopleSemaphore;
	public static Semaphore passengerHere;
	public static Semaphore carUnload;
	public static Semaphore sValidate;
	public static Semaphore numOfPeopleSemaphore2;
	
	public static void init(){
		
		numOfPepole = 0; //broj na lugje koi se prisutni vo kolata
		car=new Semaphore(1);
		passengers=new Semaphore(0);
		numOfPeopleSemaphore=new Semaphore(1); //semafor koj ne dozvoluva povekje od eden patnik da vleze
		passengerHere=new Semaphore(0); //semafor koj sto kazuva deka eden patnik e vlezen vo kolata
		carUnload=new Semaphore(0); // semafor koj sto kazuva deka moze povtorno da se kacuvaat patnici	
		sValidate=new Semaphore(0); // semafor za validacija na kraj od vozenjeto
		numOfPeopleSemaphore2=new Semaphore(1);  //semafor koj ne dozvoluva povekje od eden patnik da izleze 
		
	}
	
	public static class Car extends TemplateThread{

		public Car(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			
			car.acquire();
			state.load();
			
			passengers.release(10);
			
			passengerHere.acquire(10);
			state.run();
			
			state.unload();
			carUnload.release(10);
			
			sValidate.acquire();
			state.validate();
			
			car.release();
		}
	}
	
	public static class Passenger extends TemplateThread{

		public Passenger(int numRuns) {
			super(numRuns);
		}

		@Override
		public void execute() throws InterruptedException {
			
			
			passengers.acquire();
			state.board();
			passengerHere.release();
			
			
			numOfPeopleSemaphore.acquire();
			numOfPepole++;
			numOfPeopleSemaphore.release();
			
			carUnload.acquire();
			state.unboard();
			
			numOfPeopleSemaphore2.acquire();
			numOfPepole--;
			
			if(numOfPepole==0){
				sValidate.release();
			}
			numOfPeopleSemaphore2.release();
		}
	}
	
	static RollerCoasterState state = new RollerCoasterState();
	
	public static void main(String[] args) {
		for(int i=0;i<10;i++){
			run();
		}
	}
	
	public static void run(){
		try{
			int numRuns=1;
			int numScenarios=100;
			HashSet<Thread> threads=new HashSet<Thread>();
			
			for(int i=0;i<numScenarios;i++){
				Passenger p=new Passenger(numRuns);
				threads.add(p);
				if(i%10==0){
					Car c=new Car(numRuns);
					threads.add(c);
				}
			}
			
			init();
			
			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
}
