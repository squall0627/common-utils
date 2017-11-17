package com.akigo.common.utils.lock.nonfairLock;

public class Lock {

    private boolean isLocked = false;
    private Thread lockingThread = null;

    public synchronized void lock() {
        while (isLocked) {
            try {
                System.out.println(Thread.currentThread().getName() + " Wait");
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isLocked = true;
        lockingThread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + " Got Lock");
    }

    public synchronized void unlock() {
        if (lockingThread != Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread has not locked this lock");
        }

        isLocked = false;
        lockingThread = null;
        System.out.println(Thread.currentThread().getName() + " Release Lock");
        notify();
    }

}
