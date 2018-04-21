package net.kasterma.thread;

import lombok.extern.log4j.Log4j2;

/**
 * This Thread runs for a given number of seconds.
 *
 * In visualvm this thread is Running (green).
 */
@Log4j2
class RunSecs extends Thread {
    private static Integer id = 0;
    private final Integer runSeconds;

    RunSecs(Integer runSeconds) {
        super("RunSecs-" + id++);
        this.runSeconds = runSeconds;
    }

    @Override
    public void run() {
        Long start = System.nanoTime();
        Long idx = 0L;
        while ((System.nanoTime() - start) / (1000 * 1000 * 1000) < runSeconds) {
            idx += 1;
            if (idx % 1_000_000 == 0) {
                log.info("ping {}",  idx);
            }
        }
        log.info("done");    }
}

/**
 * Sleep for given number of seconds.
 *
 * In visualvm this thread is Sleeping (purple).
 */
@Log4j2
class SleepSecs extends Thread {
    private static Integer id = 0;
    private final Integer sleepSeconds;

    SleepSecs(Integer sleepSeconds) {
        super("SleepSecs-" + id++);
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public void run() {
        log.info("starting");
        try {
            sleep(1000 * sleepSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("done");    }
}

/**
 * After starting immediately join another thread.
 *
 * In visualvm this thread is in state Wait (yellow).
 */
@Log4j2
class JoinOther extends Thread {
    private static Integer id = 0;
    private final Thread other;

    public JoinOther(Thread other) {
        super("JoinOther-" + id++);
        this.other = other;
    }

    @Override
    public void run() {
        log.info("join now");
        try {
            other.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("done");    }
}

/**
 * All the SyncStatic threads share an object they sync on, each grabbing the monitor
 * on the object and then sleeping 10 seconds.  The ones that have to wait for the
 * monitor are in state Monitor in visualvm (Red), BLOCKED according to Thread.getState()
 */
@Log4j2
class SyncStatic extends Thread {
    private static Integer id = 0;
    private static Integer syncObject = 0;
    private final Integer sleepSeconds;

    public SyncStatic(Integer sleepSeconds) {
        super("SleepStatic-" + id++);
        this.sleepSeconds = sleepSeconds;
    }

    @Override
    public void run() {
        log.info("starting");
        synchronized (syncObject) {
            log.info("sleeping");
            try {
                Thread.sleep(1000 * sleepSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("waking");
        }
        log.info("done");    }
}

@Log4j2
public class ThreadStates {
    public static void main(String[] args) {
        Thread th = new RunSecs(30);
        th.start();
        Thread th1 = new RunSecs(60);
        th1.start();
        Thread th2 = new SleepSecs(60);
        th2.start();
        Thread th3 = new JoinOther(th);
        th3.start();
        Thread th4 = new JoinOther(th); // never started
        Thread th5 = new RunSecs(3);
        th5.start();
        Thread th6 = new SyncStatic(20);
        Thread th7 = new SyncStatic(20);
        th6.start();
        th7.start();
        log.info("started");
        try {
            Thread.sleep(10_000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // The next list shows all states according to
        //    https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html
        // have now been achieved.
        // In visualvm we still have a state Park which we have not achieved yet.
        log.info("th state {}", th.getState());  // RUNNABLE
        log.info("th1 state {}", th1.getState()); // RUNNABLE
        log.info("th2 state {}", th2.getState()); // TIMED_WAITING
        log.info("th3 state {}", th3.getState()); // WAITING
        log.info("th4 state {}", th4.getState()); // NEW
        log.info("th5 state {}", th5.getState()); // TERMINATED
        log.info("th6 state {}", th6.getState()); // TIMED_WAITING
        log.info("th7 state {}", th7.getState()); // BLOCKED
    }
}
