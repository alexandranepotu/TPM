import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;

interface SimpleLock {
    void lock();
    void unlock();
}

class TASLock implements SimpleLock {
    private final AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (state.getAndSet(true)) {
            // busy-wait
        }
    }

    public void unlock() {
        state.set(false);
    }
}

class TTASLock implements SimpleLock {
    private final AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (true) {
            // prima bucla reduce numarul de getAndSet
            while (state.get()) { /* busy-wait until seems free */ }
            if (!state.getAndSet(true)) {
                return;
            }
        }
    }

    public void unlock() {
        state.set(false);
    }
}

class CCASLock implements SimpleLock {
    private final AtomicInteger state = new AtomicInteger(0); // 0 unlocked, 1 locked

    public void lock() {
        while (true) {
            // wait while locked (cheap spinning read)
            while (state.get() == 1) { /* spin */ }
            // attempt to acquire with CAS
            if (state.compareAndSet(0, 1)) {
                return;
            }
            // altfel reincearca
        }
    }

    public void unlock() {
        state.set(0);
    }
}

class SharedCounter {
    private int value = 0;

    public int get() {
        return value;
    }

    public void increment() {
        value++;
    }
}

class Worker implements Runnable {
    private final SimpleLock lock;
    private final SharedCounter counter;
    private final int limit;
    private final long[] results;
    private final int index;

    Worker(SimpleLock lock, SharedCounter counter, int limit, long[] results, int index) {
        this.lock = lock;
        this.counter = counter;
        this.limit = limit;
        this.results = results;
        this.index = index;
    }

    @Override
    public void run() {
        long localCount = 0;
        while (true) {
            lock.lock();
            try {
                if (counter.get() >= limit) {
                    // done, exit run() — but must release lock in finally
                    break;
                }
                counter.increment();
                localCount++;
            } finally {
                lock.unlock();
            }
        }
        results[index] = localCount;
    }
}

public class LockComparison {
    static final int LIMIT = 300_000; // cerut

    public static void runExperiment(String lockName, SimpleLock lock, int nThreads) throws InterruptedException {
        SharedCounter counter = new SharedCounter();
        long[] perThread = new long[nThreads];
        Thread[] threads = new Thread[nThreads];

        long start = System.nanoTime();

        for (int i = 0; i < nThreads; ++i) {
            threads[i] = new Thread(new Worker(lock, counter, LIMIT, perThread, i));
            threads[i].start();
        }
        for (int i = 0; i < nThreads; ++i) {
            threads[i].join();
        }

        long end = System.nanoTime();
        long elapsedNs = end - start;
        double elapsedSec = elapsedNs / 1_000_000_000.0;

        long totalIncrements = Arrays.stream(perThread).sum();

        System.out.printf("Lock=%-6s | threads=%2d | limit=%d | totalIncrements=%d | time=%.3fs\n",
                lockName, nThreads, LIMIT, totalIncrements, elapsedSec);

        System.out.println("Per-thread increments: " + Arrays.toString(perThread));
        System.out.println("Final shared counter value: " + counter.get());
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleLock tas   = new TASLock();
        SimpleLock ttas  = new TTASLock();
        SimpleLock ccas  = new CCASLock();

        int[] threadCounts = {4, 8};

        for (int t : threadCounts) {
            runExperiment("TAS", tas, t);
        }
        for (int t : threadCounts) {
            runExperiment("TTAS", ttas, t);
        }
        for (int t : threadCounts) {
            runExperiment("CCAS", ccas, t);
        }
    }
}
