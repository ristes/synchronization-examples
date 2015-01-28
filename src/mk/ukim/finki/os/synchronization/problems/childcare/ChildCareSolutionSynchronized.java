/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package childcareproblem_testing;

import static java.lang.Integer.min;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Nikolina
 */

// Sihronizirano resenie.
public class ChildCareSolution_Synchronized {
    public static Semaphore adultQueue;
    public static Semaphore childQueue;

    public static Semaphore mutex;



    public static void init()
    {
        adultQueue = new Semaphore(20);
        childQueue = new Semaphore(20);

        mutex = new Semaphore(1);
    }



    // Nitka za vospituvac.
    public static class Adult extends TemplateThread
    {
        public Adult(int numberOfRuns)
        {
            super(numberOfRuns);
        }

        @Override
	public void execute() throws InterruptedException {
            // Kod za vleguvanje na vospituvacot.
            mutex.acquire();

           
            // Vospituvacot vleguva - ne vrsime proverki, vospituvacot moze da vleze bilo koga.
            state.adultEntered();
            // ako ima deca sto cekaat da vlezat...
            if(state.childrenEntering> 0)
            {
                // moze da vlezat max 3 deca...
                int n = min(3, state.childrenEntering);
                
                // Im se dozvoluva na n deca koi cekaat da vlezat, zatoa sto vlegol uste eden vospituvac.
                state.childrenEntered(n);
                childQueue.release(n);
            }

            mutex.release();

            // Kriticen region.

            // Kod za izleguvanje na vospituvacot.
            mutex.acquire();

            // Ako ima dovolno vospituvaci za da gi cuvaat decata...
            if(state.children <= 3 * (state.adults - 1))
            {
                // Vospituvacot izleguva.
                state.adultLeft();
                mutex.release();
            }
            // Ako nema dovolno vospituvaci za da gi cuvaat decata...
            else
            {
               
                // Vospituvacot ne moze da izleze, se stava vo redicata na vosp. koi cekaat da izlezat.
                state.adultLeaving();
                mutex.release();
                adultQueue.acquire();
            }
	}
    }



    // Nitka za dete.
    public static class Child extends TemplateThread
    {
        public Child(int numberOfRuns)
        {
            super(numberOfRuns);
        }

        @Override
	public void execute() throws InterruptedException {
            // Kod za vleguvanje na deteto.
            mutex.acquire();

           
            // Ako ima dovolno vospituvaci za decata koi se vnatre i deteto koe saka da vleze....
            if(state.children < 3 * state.adults)
            {
                // Deteto vleguva
                state.childrenEntered(1);
                mutex.release();
            }
            else
            // Ako bi nemalo dovolno vospituvaci dokolku bi vleglo deteto, koi se vnatre i deteto koe saka da vleze.
            {
            
                // Deteto ne moze da vleze, se stava vo redicata na deca koi cekaat da vlezat.
                state.childEntering();
                mutex.release();
                childQueue.acquire();
            }

            // Kriticen region

            // Kod za izleguvanje na deteto
            mutex.acquire();

            // Deteto izleguva-ne vrsime proverki, deteto moze da izleze bilo koga.
    
            state.childLeft();

            // Ako ima vospituvaci koi cekaat da izlezat i pritoa ima dovolno
            // vospituvaci za decata, otkako si otislo edno dete...
            if((state.adultsLeaving > 0) && state.children <= 3 * (state.adults - 1))
            {
                // Mu se dozvoluva na eden vospituvac da izleze, zatoa sto 
                // otkako si otislo edno dete ima dovolno vospituvaci.
                state.adultLeft();
                adultQueue.release();
            }

            mutex.release();
	}
    }



    static ChildCareState state = new ChildCareState();



    public static void main(String[] args) {

        for (int i = 1; i <= 10; i++) {
            System.out.println("Run: " + i);
            run();
        }
    }



    public static void run() {
        try {
            int numChildren = 20;
            HashSet<Thread> threads = new HashSet<>();
            ChildCareSolution_Synchronized.Adult adult = new ChildCareSolution_Synchronized.Adult((numChildren / 3) + 10);
            threads.add(adult);

            for (int i = 0; i < numChildren; i++) {
                ChildCareSolution_Synchronized.Child child = new ChildCareSolution_Synchronized.Child(1);
                threads.add(child);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
