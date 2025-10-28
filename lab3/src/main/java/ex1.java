 import java.util.concurrent.Semaphore;

    public class ex1 {
        static int counter = 0;
        static Semaphore semaphore = new Semaphore(1); // semafor binar

        static class IncThread extends Thread {
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    try {
                        semaphore.acquire(); //pentru wait
                        counter++; // crestem contorul
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release(); //pentru signal
                    }
                }
            }
        }

        static class DecThread extends Thread {
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    try {
                        semaphore.acquire();
                        counter--;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }
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

