import java.util.concurrent.atomic.AtomicInteger;

public class ex1     {
    static final int N = 4; // numarul de thread-uri
    static final int LIMIT = 150000; // limita contorului

    static AtomicInteger counter = new AtomicInteger(0);
    static AtomicInteger[] level = new AtomicInteger[N];
    static AtomicInteger[] victim = new AtomicInteger[N];
    static int[] accessCount = new int[N];

    static {
        for (int i = 0; i < N; i++) {
            level[i] = new AtomicInteger(0);
            victim[i] = new AtomicInteger(-1);
        }
    }

    static void lock(int threadId) {
        for (int L = 1; L < N; L++) {
            level[threadId].set(L);
            victim[L].set(threadId);

            while (existsConflict(threadId, L) && victim[L].get() == threadId) {
                // asteptare
            }
        }
    }

    static boolean existsConflict(int threadId, int L) {
        for (int k = 0; k < N; k++) {
            if (k != threadId && level[k].get() >= L) {
                return true;
            }
        }
        return false;
    }

    static void unlock(int threadId) {
        level[threadId].set(0);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[N];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                while (counter.get() < LIMIT) {
                    lock(threadId);

                    if (counter.get() < LIMIT) {
                        counter.incrementAndGet();
                        accessCount[threadId]++;
                    }

                    unlock(threadId);
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < N; i++) {
            threads[i].join();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Timpul total de executie: " + (endTime - startTime) + " ms");
        System.out.println("Valoarea finala a contorului: " + counter.get());
        System.out.println("\nNumar de accese per thread:");
        for (int i = 0; i < N; i++) {
            System.out.println("Thread " + i + ": " + accessCount[i] + " accese");
        }
    }
}
