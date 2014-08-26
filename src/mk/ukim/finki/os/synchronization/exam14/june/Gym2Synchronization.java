package mk.ukim.finki.os.synchronization.exam14.june;

import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import mk.ukim.finki.os.synchronization.ProblemExecution;
import mk.ukim.finki.os.synchronization.TemplateThread;

public class Gym2Synchronization {

    static Semaphore max;
    static int br;
    static int br_sala;
    static Semaphore ready;
    static Semaphore max_sob;
    static Semaphore done_sport;
    static Semaphore done_sala;
    static Semaphore ready_pres;
    static Semaphore lock;
    static Semaphore kraj;
    
    public static void init() {
        max=new Semaphore(12);
        br=0;
        br_sala=0;
        ready=new Semaphore(0);
        max_sob=new Semaphore(4);
        done_sport=new Semaphore(0);
        ready_pres=new Semaphore(0);
        done_sala=new Semaphore(0);
        lock=new Semaphore(1);
        kraj=new Semaphore(0);
    }
   

    public static class Player extends TemplateThread {

        public Player(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            max.acquire();
            state.vlezi();
            lock.acquire();
            br++;
            if(br==12)
            {
                br=0;
                lock.release();
                ready.release(11);
                state.sportuvaj();
                done_sport.acquire(11);
                ready_pres.release(11);
                max_sob.acquire();
                state.presobleci();
                max_sob.release();
                done_sala.acquire(11);
                state.slobodnaSala();
                kraj.release(11);
            }
            else
            {
                lock.release();
                ready.acquire();
                state.sportuvaj();
                done_sport.release();
                ready_pres.acquire();
                max_sob.acquire();
                state.presobleci();
                max_sob.release();
                done_sala.release();
                kraj.acquire();
            }
            max.release();
        }
    }
    static Gym2State state = new Gym2State();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            Scanner s = new Scanner(System.in);
            int numRuns = 1;
            int numIterations = 1200;
            s.close();

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numIterations; i++) {
                Player h = new Player(numRuns);
                threads.add(h);
            }

            init();

            ProblemExecution.start(threads, state);
            System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
