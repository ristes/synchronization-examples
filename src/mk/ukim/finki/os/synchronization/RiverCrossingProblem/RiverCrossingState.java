package mk.ukim.finki.os.synchronization.RiverCrossingProblem;


/**
 * 
 * @authors Petre Petrov 121099 and Ivica Obadic 121144
 *
 */

import java.util.HashSet;

import mk.ukim.finki.os.synchronization.AbstractState;
import mk.ukim.finki.os.synchronization.BoundCounterWithRaceConditionCheck;
import mk.ukim.finki.os.synchronization.PointsException;
import mk.ukim.finki.os.synchronization.Switcher;
import mk.ukim.finki.os.synchronization.RiverCrossingProblem.RiverCrossingProblem.Hacker;


public class RiverCrossingState extends AbstractState {

	
	private static final int MAXIMUM_4_PEOPLE_ON_BOARD_POINTS = 10;
	private static final int RACE_CONDITION_POINTS=25;
	private static final int NOT_ALL_COMBINATIONS_INCLUDED_POINTS=10;
	private static final int INVALID_COMBINATION_POINTS=25;

	private static final String MAXIMUM_4_PEOPLE_ON_BOARD_MESSAGE = "Na brodot ima kaceno poveke od cetvorica.";
	private static final String MORE_THEN_ONE_CALL_OF_ROWBOAT="Poveke od 1 patnik ja povikuva rowBoat()";
	private static final String NOT_ALL_COMBINATIONS_INCLUDED_MESSAGE="Ne gi dozvoluvate site kombinacii za kacuvanje.";
	private static final String CALL_OF_ROWBOAT="Ja povikuvam rowBoat()";
	
	private BoundCounterWithRaceConditionCheck Boat;
	
	
	
	

	
	private HashSet<String> combinations;
	
	private int hackers =0;
	private int serfs =0;
	private long checkRaceConditionInt;
	
	public RiverCrossingState()
	{
		combinations=new HashSet<String> ();
		Boat=new BoundCounterWithRaceConditionCheck(0,4,MAXIMUM_4_PEOPLE_ON_BOARD_POINTS,MAXIMUM_4_PEOPLE_ON_BOARD_MESSAGE,null,0,null);
		checkRaceConditionInt=Long.MIN_VALUE;
	}
	
	
	
	
	public synchronized void board()
	{
		
		log(Boat.incrementWithMax(false),"se kacuvam na brod");
		Thread t=Thread.currentThread();
		if (t instanceof Hacker)
		{
			hackers++;
		}
		else
		{
			serfs++;
		}
	}
	
	public void rowBoat()
	{
		checkRaceConditionInt++;
		if (hackers+serfs==4 && hackers%2==0 && serfs%2==0)
		{
			combinations.add(encode());
			
			RaceConditionMethod();
			
			log(null, CALL_OF_ROWBOAT);
			
			hackers=0;
			serfs=0;
			Boat.setValue(0);
		}
		else
		{
			logException(new PointsException(INVALID_COMBINATION_POINTS, InvalidCombinationMessage() ));
		}
	}
	
	private String InvalidCombinationMessage()
	{
		return "Nevalidna kombinacija na hackers so serfs. Hackers : " +hackers+" Serfs : "+serfs;
	}
	
	private void RaceConditionMethod()
	{
		long check;
		synchronized (this) {
			check=checkRaceConditionInt;
		}
		Switcher.forceSwitch(3);
		
		if (check!=checkRaceConditionInt)
		{
			logException(new PointsException(RACE_CONDITION_POINTS,MORE_THEN_ONE_CALL_OF_ROWBOAT));
		}
	}
	
	
	public String encode ()
	{
		return hackers+" "+serfs;
	}
	
	@Override
	public void finalize() {		
		
		if (combinations.size()!=3)
		{
			logException(new PointsException(NOT_ALL_COMBINATIONS_INCLUDED_POINTS,NOT_ALL_COMBINATIONS_INCLUDED_MESSAGE));
		}
		
	}
}

