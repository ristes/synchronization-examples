package mk.ukim.finki.os.synchronization.HilzersBarbershop;

import java.util.HashSet;
import java.util.Iterator;
import mk.ukim.finki.os.synchronization.*;

/**
 * 
 * @author Robert Armenski 121189 & Nikola Furnadziski 121156
 *
 */
public class State extends AbstractState {
	@SuppressWarnings("unused")
	private final int numBarbers, numCustomers;
	
	private BoundCounterWithRaceConditionCheck arrivedCount;
	private HashSet<Integer> arrivedCustomers;
	
	private BoundCounterWithRaceConditionCheck sofaCount;
	private HashSet<Integer> sofaCustomers;
	
	private BoundCounterWithRaceConditionCheck chairCount;
	private HashSet<Integer> chairCustomers;
	private HashSet<Integer> waitingCustomers;
	
	private BoundCounterWithRaceConditionCheck toPayCount;
	private BoundCounterWithRaceConditionCheck cuttedCount;
	private BoundCounterWithRaceConditionCheck payedCount;
	
	public State(int numBarbers, int numCust) {
		super();
		this.numBarbers = numBarbers;
		this.numCustomers = numCust;
		
		arrivedCount = new BoundCounterWithRaceConditionCheck(0, 
				20, 5, "More than 20 customers cannot be inside at the same time.", 
				0, 5, "There cannot be less than 0 customers in the store");
		
		sofaCount = new BoundCounterWithRaceConditionCheck(0,
				4,5,"More than 4 customers cannot sit on the sofa at the same time",
				0,5, "There cannot be less than 0 customers on the sofa");
		
		chairCount = new BoundCounterWithRaceConditionCheck(0, 
				numBarbers, 5, "More than  " + numBarbers + " customers cannot get a haircut at the same time",
				0,5,"There cannot be less than 0 customers on the chairs" );
		
		toPayCount = new BoundCounterWithRaceConditionCheck(0, 
				numCustomers, 5, "More than " + numCustomers + " customers want to pay", 
				0, 5, "Less than 0 customers want to pay");
		
		cuttedCount = new BoundCounterWithRaceConditionCheck(0, 
				numCustomers, 5, "More than " + numCustomers + " customers had a haircut", 
				0, 5, "Cutted less than 0 customers");
		
		payedCount = new BoundCounterWithRaceConditionCheck(0, 
				numCustomers, 5, "More than " + numCustomers + " customers payed for a haircut", 
				0, 5, "Less than 0 customers payed for a haircut");
		
		arrivedCustomers = new HashSet<Integer>();
		sofaCustomers = new HashSet<Integer>(4);
		chairCustomers = new HashSet<Integer>(numBarbers);
		waitingCustomers = new HashSet<Integer>();
	}
	@Override
	public synchronized void finalize() {
		//TODO: final step of state
		if(arrivedCount.getValue() != 0) {
			log(new PointsException(10, "There are still " + arrivedCount.getValue() + " customers inside the store"), null);
		}
		if(payedCount.getValue() != cuttedCount.getValue()){
			log(new PointsException(10, payedCount.getValue() + " customers payed but " + cuttedCount.getValue() + " were cutted"), null);
		}
	}
	
	
	/*
	 * Customer methods
	 */
	public void enterShop(){
		synchronized(this) {
			Resenie.Customer c = (Resenie.Customer)Thread.currentThread();
			if(arrivedCustomers.contains(c.custId)){
				log(new PointsException(5, "The customer has already arrived"), null);
			}
			else{
				arrivedCustomers.add(c.custId);
				log(null, "A customer enters the shop");
				Switcher.forceSwitch(10);
				log(arrivedCount.incrementWithMax(false), null);
			}
		}
	}
	public void sitOnSofa() {
		synchronized(this) {
			Resenie.Customer c = (Resenie.Customer)Thread.currentThread();
			if(sofaCustomers.contains(c.custId)){
				log(new PointsException(5, "The customer is already seated on the sofa"), null);
			}
			else {
				sofaCustomers.add(c.custId);
				log(null, "A customer sits on the sofa");
				log(sofaCount.incrementWithMax(false), null);
			}
		}
	}
	public synchronized void sitInBarberChair(){
		synchronized(this) {
			Resenie.Customer c = (Resenie.Customer)Thread.currentThread();
			if(chairCustomers.contains(c.custId)){
				log(new PointsException(5, "The customer is already seated in the chair"), null);
			} else {
				Switcher.forceSwitch(10);
				chairCustomers.add(c.custId);
				sofaCustomers.remove(c.custId);
				log(null, "A customer sits in the chair");
				log(sofaCount.decrementWithMin(false), null);
				//Switcher.forceSwitch(10);
				log(chairCount.incrementWithMax(false), null);
			}
		}
	}
	public synchronized void getHairCut(){
		Resenie.Customer c = (Resenie.Customer)Thread.currentThread();
		if(!chairCustomers.contains(c.custId)){
			log(new PointsException(5, "The customer " + c.custId + " is not seated in the chair"), null);
		}
		else {
			if(waitingCustomers.contains(c.custId)) {
				log(new PointsException(5, "The customer " + c.custId + " is already waiting"), null);
			} else{
				Switcher.forceSwitch(10);
				waitingCustomers.add(c.custId);
				log(null, "A customer waits for haircut");
			}
		}
	}
	
	public void pay(){
		log(null, "A customer is waiting to pay");
		Switcher.forceSwitch(10);
		log(toPayCount.incrementWithMax(false), null);
	}
	public synchronized void exitShop(){
		Resenie.Customer c = (Resenie.Customer)Thread.currentThread();
		chairCustomers.remove(c.custId);
		arrivedCustomers.remove(c.custId);
		log(null, "A customer is leaving the shop");
		Switcher.forceSwitch(10);
		log(arrivedCount.decrementWithMin(false), null);
		Switcher.forceSwitch(10);
		log(chairCount.decrementWithMin(false), null);
	}
	
	/*
	 * Barber methods
	 */
	public synchronized void cutHair(){
		Iterator<Integer> iter = waitingCustomers.iterator();
		if(!iter.hasNext()){
			log(new PointsException(5, "There is no customer to cut their hair"), null);
		} else {
			int cId = iter.next();
			waitingCustomers.remove(cId);
			log(null, "A customer is getting his haircut");
			log(cuttedCount.incrementWithMax(false),null);
		}
	}
	public void acceptPayment(){
		log(null, "A customer has payed");
		Switcher.forceSwitch(10);
		log(toPayCount.decrementWithMin(false), null);
		Switcher.forceSwitch(10);
		log(payedCount.incrementWithMax(false), null);
	}
}
