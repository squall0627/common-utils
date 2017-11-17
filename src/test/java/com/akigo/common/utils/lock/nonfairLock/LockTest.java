package com.akigo.common.utils.lock.nonfairLock;

import org.junit.Test;

import static org.junit.Assert.*;

public class LockTest {
    @Test
    public void lock() throws Exception {
        Runnable run = new Runnable() {
            Lock lock = new Lock();

            int i = 0;

            @Override
            public void run() {
                lock.lock();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(Thread.currentThread().getName() + ": " + i);
                i++;

                lock.unlock();
            }
        };

        Thread ta = new Thread(run, "Thread A");
        Thread tb = new Thread(run, "Thread B");
        Thread tc = new Thread(run, "Thread C");
        Thread td = new Thread(run, "Thread D");
        Thread te = new Thread(run, "Thread E");
        ta.start();
        tb.start();
        tc.start();
        td.start();
        te.start();
    }

}