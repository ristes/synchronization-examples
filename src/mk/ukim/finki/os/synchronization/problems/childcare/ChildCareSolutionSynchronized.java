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

// Синхронизирано решение.
public class ChildCareSolution_Synchronized {    
    public static Semaphore adultQueue;
    public static Semaphore childQueue;
    
    public static Semaphore mutex;
    
    
    
    public static void init()
    {            
        adultQueue = new Semaphore(100);
        childQueue = new Semaphore(100);
        
        mutex = new Semaphore(1);
    }
    
    
    
    // Нитка за воспитувач.
    public static class Adult extends TemplateThread
    {
        public Adult(int numberOfRuns)
        {
            super(numberOfRuns);
        }
        
        @Override
	public void execute() throws InterruptedException {
            // Код за влегување на воспитувачот.
            mutex.acquire();
            
            // Воспитувачот влегува - не вршиме проверки, воспитувачот може
            // да влезе било кога.
            state.adultEntered();
            // Ако има деца што чекаат да влезат...
            if(state.childrenEntering> 0)
            {
                // Може да влезат максимум три деца...
                int n = min(3, state.childrenEntering);
                // Им се дозволува на n деца кои чекаат да влезат, затоа што
                // влегол уште еден воспитувач.
                state.childrenEntered(n);
                childQueue.release(n);
            }
            
            mutex.release();
            
            // Критичен регион.
            
            // Код за излегување на воспитувачот.
            mutex.acquire();
            
            // Ако има доволно воспитувачи за да ги чуваат децата...
            if(state.children <= 3 * (state.adults - 1))
            {
                // Воспитувачот излегува.
                state.adultLeft();
                mutex.release();
            }
            // Ако нема доволно воспитувачи за да ги чуваат децата...
            else
            {
                // Воспитувачот не може да излезе, се става во редицата на
                // воспитувачи кои чекаат да излезат.
                state.adultLeaving();
                mutex.release();
                adultQueue.acquire();
            }
	}
    }
    
    
    
    // Нитка за дете.
    public static class Child extends TemplateThread
    {
        public Child(int numberOfRuns)
        {
            super(numberOfRuns);
        }
        
        @Override
	public void execute() throws InterruptedException {
            // Код за влегување на детето.
            mutex.acquire();
            
            // Ако има доволно воспитувачи за децата кои се внатре и детето
            // кое сака да влезе...
            if(state.children < 3 * state.adults)
            {
                // Детето влегува.
                state.childrenEntered(1);
                mutex.release();
            }
            else
            // Ако би немало доволно воспитувачи доколку би влегло детето... кои се внатре и детето
            // кое сака да влезе...
            {
                // Детето не може да влезе, се става во редицата на деца кои
                // чекаат да влезат.
                state.childEntering();
                mutex.release();
                childQueue.acquire();
            }            
            
            // Критичен регион.
            
            // Код за излегување на детето.
            mutex.acquire();
            
            // Детето излегува - не вршиме проверки, детето може да излезе
            // било кога.
            state.childLeft();
            
            // Ако има воспитувачи кои чекаат да излезат и притоа има доволно
            // воспитувачи за децата, откако си отишло едно дете...
            if((state.adultsLeaving > 0) && state.children <= 3 * (state.adults - 1))
            {
                // Му се дозволува на еден воспитувач да излезе, затоа што
                // откако си отишло едно дете има доволно воспитувачи.
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
            int numChildren = 100;
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
