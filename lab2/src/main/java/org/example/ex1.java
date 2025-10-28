package org.example;

public class ex1 {
    static int counter = 0;

    static class IncThread extends Thread {
        public void run() {
            for (int i = 0; i < 100000; i++) {
                int localValue = counter;
                localValue++;
                counter = localValue;
            }
        }
    }

    static class DecThread extends Thread {
        public void run() {
            for (int i = 0; i < 100000; i++) {
                int localValue = counter;
                localValue--;
                counter = localValue;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int n = 5;
        int m = 5;

        long start = System.currentTimeMillis();

        Thread[] inc = new Thread[n];
        Thread[] dec = new Thread[m];

        for (int i = 0; i < n; i++) {
            inc[i] = new IncThread();
            inc[i].start();
        }

        for (int i = 0; i < m; i++) {
            dec[i] = new DecThread();
            dec[i].start();
        }

        for (int i = 0; i < n; i++) {
            inc[i].join();
        }

        for (int i = 0; i < m; i++) {
            dec[i].join();
        }

        long time = System.currentTimeMillis() - start;

        System.out.println("Counter: " + counter);
        System.out.println("Time: " + time + " ms");
    }
}