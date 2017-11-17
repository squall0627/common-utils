package com.akigo.common.utils.lock.fairLock;

public class LockHandler {

    private boolean isNotified = false;

    public synchronized void doWait() throws InterruptedException {
        while (!isNotified) {
            wait();
        }
        isNotified = false;
    }

    public synchronized void doNotify() {
        isNotified = true;
        notify();
    }

}
