package mk.ukim.finki.os.synchronization.HilzersBarbershop;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import mk.ukim.finki.os.synchronization.*;

/**
 * 
 * @author Robert Armenski 121189 & Nikola Furnadziski 121156
 *
 */
public class Resenie {
	static State state;

	//TODO: Definicija na globalni promenlivi i semafori
	
	//Za musterijata da vleze vo berbernicata i da visecki da ceka red
	static Semaphore standingRoom;
	//Za musterijata da sedne na sofata
	static Semaphore sofa;
	//Za musterijata da sedne na berberskiot stol
	static Semaphore chair;
	//Za berberot da kaze deka e sloboden da go potstrize musterijata
	static Semaphore barber;
	//Za musterijata da oznaci deka sedi na stolot i saka da bide potstrizan
	static Semaphore customer;
	//Za musterijata da oznaci deka e spremen za potstrizuvanje
	static Semaphore readyForHaircut;
	//Za berberot da kaze koga e gotovo potstrizuvanjeto
	static Semaphore hairDone;
	//Za musterijata da gi dade parite za potstrizuvanje
	static Semaphore cash;
	//Za berberot da ja dade fiskalnata smetka
	static Semaphore receipt;
	
	public static void init(int numBarbers) {
		//Bidejki vkupno mora da ima ne poveke od 20 musterii vnatre
		//A 4 mesta se rezervirani za sofata
		//Ostanuvaat 16 mesta, od koi numBarbers se rezervirani za stolovite
		//Zatoa moze da ima maksimalno 15 barberi (t.e. stolovi)
		standingRoom = new Semaphore(16 - numBarbers);
		
		//4 mesta rezervirani za sofata
		sofa = new Semaphore(4);
		//Po eden stol za sekoj berber
		chair = new Semaphore(numBarbers);
		//Inicijalno nema spremni berberi
		barber = new Semaphore(0);
		//Inicijalno nema pristignati musterii koi sedat na stol
		customer = new Semaphore(0);
		//Inicijalno nema spremni musterii
		readyForHaircut = new Semaphore(0);
		//Inicijalno nema potstrizani musterii
		hairDone = new  Semaphore(0);
		//Inicijalno nikoj ne dal pari
		cash = new Semaphore(0);
		//Inicijalno ne se izdadeni fiskalni smetki
		receipt = new Semaphore(0);
	}

	public static class Barber extends TemplateThread {
		public int barberId;
		
		public Barber(int numRuns, int id) {
			super(numRuns);
			barberId = id;
		}

		@Override
		public void execute() throws InterruptedException {	
			//Ceka musterija da sedne na stol
			customer.acquire();
			//Kazuva deka e sloboden za da go potstrize
			barber.release();
			//Ceka musterijata da bide spremen za potstrizuvanje
			readyForHaircut.acquire();
			//Go potstrizuva musterijata
			state.cutHair();
			//Signalizira deka musterijata e potstrizan
			hairDone.release();
			
			//Ceka musterijata da plati
			cash.acquire();
			//Gi prifaka parite (i ja pecati fiskalnata)
			state.acceptPayment();
			//Mu ja dava fiskalnata smetka na musterijata
			receipt.release();
		}
	}
	
	public static class Customer extends TemplateThread{
		public int custId;
		
		public Customer(int numRuns, int cId) {
			super(numRuns);
			this.custId = cId;
		}

		@Override
		public void execute() throws InterruptedException {
			//Ogranicuva max. 20 musterii da bidat prisutni vnatre
			standingRoom.acquire();	
			//Otkako se oslobodilo mesto za visenje, vleguva vo prodavnicata
			state.enterShop();
			
			//Ceka red za sedenje na sofata
			sofa.acquire();
			//Sednuva na sofata
			state.sitOnSofa();	
			//Osloboduva edno mesto za visenje
			standingRoom.release();
			
			//Ceka red za sedenje na stolot
			chair.acquire();				
			//Sednuva na berberskiot stol
			state.sitInBarberChair();
			//Osloboduva edno mesto za sedenje na sofata
			sofa.release();
			
			//Kazuva deka e pristignat i sedi na stolot
			customer.release();
			//Ceka da se oslobodi berber
			barber.acquire();
			//Signalizira deka saka da bide potstrizan
			state.getHairCut();
			//Kakuva deka e spremen za potstrizuvanje
			readyForHaircut.release();
			
			//Ceka da bide potstrizan
			hairDone.acquire();
			//Signalizira deka e spremen da plati za uslugata
			state.pay();	
			//Gi dava parite
			cash.release();
			//Ceka fiskalna smetka
			receipt.acquire();
					
			//Trgnuva kon izlezot
			state.exitShop();
			//Kazuva deka ima sloboden stol
			chair.release();
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 50; i++) {
			System.out.print((i+1) + ": "); 
			run();
		}
	}

	public static void run() {
		try {
			int numBarbers = 5; //Max: 15
			int numCustomers = 10*numBarbers;
			
			state = new State(numBarbers, numCustomers);
			HashSet<Thread> threads = new HashSet<Thread>();
			
			for (int i = 0; i < numBarbers; i++) {
				Barber b = new Barber(Math.max(numCustomers/numBarbers,1), i);
				threads.add(b);
			}
			for (int i=0; i<numCustomers; i++){
				Customer cust = new Customer(1, i);
				threads.add(cust);
			}
			
			init(numBarbers);

			ProblemExecution.start(threads, state);
			System.out.println(new Date().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
