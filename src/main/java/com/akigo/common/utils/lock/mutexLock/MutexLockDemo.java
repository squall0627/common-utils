/*
 * MutexLockDemo.java
 * Created on  2018/2/26 下午10:45
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2018/2/26     chenhao         V1.0.0     InitVer
 */
package com.akigo.common.utils.lock.mutexLock;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author chenhao
 * @version 1.0.0
 */
public class MutexLockDemo {

    public static int count = 0;

    public static MutexLockFactory.MutexLock lockA;
    public static MutexLockFactory.MutexLock lockB;
    public static MutexLockFactory.MutexLock lockC;
    public static MutexLockFactory.MutexLock lockD;

    public static void main(String[] args) throws InterruptedException {
        MutexLockFactory factory = new MutexLockFactory();
        lockA = factory.mutexLock("A");
        lockB = factory.mutexLock("B");
        lockC = factory.mutexLock("C");
        lockD = factory.mutexLock("D");

        MutexLockDemo demo = new MutexLockDemo();

        Thread thA = new Thread(MutexLockDemo::addA);
        Thread thB = new Thread(MutexLockDemo::addB);
        Thread thC = new Thread(MutexLockDemo::addC);
        Thread thD = new Thread(MutexLockDemo::addD);

        thA.start();
        thB.start();
        thC.start();
        thD.start();

        thA.join();
        thB.join();
        thC.join();
        thD.join();

        System.out.println("all count = " + count);
    }

    public static void addA() {
//        int count = 0;
        for (int i = 0; i < 10; i++) {
            lockA.lock();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
//            System.out.println(count);
            lockA.unlock();
        }
        System.out.println("A thread = " + Thread.currentThread().getName());
        System.out.println("A all count = " + count);
    }

    public static void addB() {
//        int count = 0;
        for (int i = 0; i < 10; i++) {
            lockB.lock();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
//            System.out.println(count);
            lockB.unlock();
        }
        System.out.println("B thread = " + Thread.currentThread().getName());
        System.out.println("B all count = " + count);
    }

    public static void addC() {
//        int count = 0;
        for (int i = 0; i < 10; i++) {
            lockC.lock();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
//            System.out.println(count);
            lockC.unlock();
        }
        System.out.println("C thread = " + Thread.currentThread().getName());
        System.out.println("C all count = " + count);
    }

    public static void addD() {
//        int count = 0;
        for (int i = 0; i < 10; i++) {
            lockD.lock();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
//            System.out.println(count);
            lockD.unlock();
        }
        System.out.println("D thread = " + Thread.currentThread().getName());
        System.out.println("D all count = " + count);
    }
}
