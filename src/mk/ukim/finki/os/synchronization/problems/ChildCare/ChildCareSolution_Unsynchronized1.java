/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mk.ukim.finki.os.synchronization.problems.ChildCare;


import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Nikolina
 */

// Несинхронизирано решение - deadlocks при излегувањето на воспитувачите и влегувањето на децата.
public class ChildCareSolution_Unsynchronized1 {
    public static Semaphore multiplex;



    public static void init()
    {
        multiplex = new Semaphore(3);
    }



    public static class Adult extends TemplateThread
    {
        public Adult(int numberOfRuns)
        {
            super(numberOfRuns);
        }

        @Override
	public void execute() throws InterruptedException {
            // Adult entering.
            state.adultEntered();
            multiplex.release(3);

            // Critical region.

            // Adult leaving.
            state.adultLeaving();
            state.adultLeft();
            multiplex.acquire();
            multiplex.acquire();
            multiplex.acquire();
	}
    }



    public static class Child extends TemplateThread
    {
        public Child(int numberOfRuns)
        {
            super(numberOfRuns);
        }

        @Override
	public void execute() throws InterruptedException {
            // Child entering.
            state.childEntering();
            state.childrenEntered(1);
            multiplex.acquire();

            // Critical region.

            // Child leaving.
            state.childLeft();
            multiplex.release();
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
            ChildCareSolution_Unsynchronized1.Adult adult = new ChildCareSolution_Unsynchronized1.Adult((numChildren / 3) + 1);
            threads.add(adult);

            int numRuns = 1;
            for (int i = 0; i < numChildren; i++) {
                ChildCareSolution_Unsynchronized1.Child child = new ChildCareSolution_Unsynchronized1.Child(numRuns);
                threads.add(child);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
