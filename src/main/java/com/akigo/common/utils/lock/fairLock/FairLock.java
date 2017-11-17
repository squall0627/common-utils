package com.akigo.common.utils.lock.fairLock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FairLock {

    private boolean isLocked = false;
    private Thread lockingThread = null;
    private final BlockingQueue<LockHandler> waitQueue = new LinkedBlockingQueue<>();

    public void lock() throws InterruptedException {
        LockHandler handler = new LockHandler();
        boolean isLockedForThisThread = true;
        synchronized (this) {
            waitQueue.put(handler);
            System.out.println(Thread.currentThread().getName() + " put to queue");
        }

        while (isLockedForThisThread) {
            synchronized (this) {
                isLockedForThisThread = isLocked || waitQueue.peek() != handler;
                if (!isLockedForThisThread) {
                    isLocked = true;
                    waitQueue.remove(handler);
                    lockingThread = Thread.currentThread();
                    System.out.println(Thread.currentThread().getName() + " Got Lock");
                    return;
                }
            }
            try {
                System.out.println(Thread.currentThread().getName() + " Wait");
                handler.doWait();
            } catch (InterruptedException e) {
                synchronized (this) {
                    System.out.println(Thread.currentThread().getName() + " InterruptedException Release Lock");
                    waitQueue.remove(handler);
                }
                throw e;
            }
        }
    }

    public synchronized void unlock() {
        if (lockingThread != Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread has not locked this lock");
        }
        isLocked = false;
        lockingThread = null;
        if (waitQueue.size() > 0) {
            System.out.println(Thread.currentThread().getName() + " doNotify unlock");
            waitQueue.peek().doNotify();
        }
    }

}
