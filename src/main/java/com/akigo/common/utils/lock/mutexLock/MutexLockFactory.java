/*
 * MutexLockFactory.java
 * Created on  2018/2/26 下午10:18
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2018/2/26     chenhao         V1.0.0     InitVer
 */
package com.akigo.common.utils.lock.mutexLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author chenhao
 * @version 1.0.0
 */
public class MutexLockFactory {

    private Object globalLock = new Object();

    private List<MutexLock> locks = new ArrayList<>();

    public class MutexLock {
        private volatile AtomicInteger countLock = new AtomicInteger(0);
        private boolean isNotified = false;
        private String name;

        private MutexLock(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void lock() {

            while (!tryLock()) {
                try {
                    doWait();
                } catch (InterruptedException e) {
                    System.out.println(getName() + " Interrupted " + Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void unlock() {
            if (countLock.decrementAndGet() == 0) {

                System.out.println(getName() + " release Lock " + Thread.currentThread().getName());

                locks.stream().filter(lock -> lock != this).forEach(lock -> {
                    lock.doNotify();
                });
            }
        }

        private boolean tryLock() {
            synchronized (globalLock) {
                long count = locks.stream().filter(lock -> lock != this).mapToInt(lock -> lock.countLock.get()).sum();

                if (count > 0) {
                    System.out.println(" other count of " + getName() + " = " + count + " , " + getName() + " wait " + Thread.currentThread().getName());
                    return false;
                } else {
                    System.out.println(" other count of " + getName() + " = " + count + " , " + getName() + " got lock " + Thread.currentThread().getName());

                    countLock.incrementAndGet();
                    return true;
                }
            }
        }

        private synchronized void doWait() throws InterruptedException {
            while (!isNotified) {
                wait();
            }
            isNotified = false;
        }

        private synchronized void doNotify() {
            isNotified = true;
            notifyAll();
        }
    }

    public MutexLock mutexLock() {
        MutexLock ml = new MutexLock("mutexLock_" + this.locks.size());
        this.locks.add(ml);
        return ml;
    }

    public MutexLock mutexLock(String name) {
        MutexLock ml = new MutexLock(name);
        this.locks.add(ml);
        return ml;
    }
}
