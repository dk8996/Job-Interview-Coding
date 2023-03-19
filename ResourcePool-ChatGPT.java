import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourcePool<R> {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final BlockingQueue<R> pool;
    private boolean isOpen = false;

    public ResourcePool() {
        pool = new LinkedBlockingQueue<>();
    }

    public void open() {
        lock.lock();
        try {
            isOpen = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isOpen() {
        lock.lock();
        try {
            return isOpen;
        } finally {
            lock.unlock();
        }
    }

    public void close() throws InterruptedException {
        lock.lock();
        try {
            while (!pool.isEmpty()) {
                condition.await();
            }
            isOpen = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void closeNow() {
        lock.lock();
        try {
            isOpen = false;
            pool.clear();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public R acquire() throws InterruptedException {
        lock.lock();
        try {
            while (pool.isEmpty() && isOpen) {
                condition.await();
            }
            if (isOpen) {
                return pool.take();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public R acquire(long timeout, TimeUnit timeUnit) throws InterruptedException {
        lock.lock();
        try {
            long nanosTimeout = timeUnit.toNanos(timeout);
            while (pool.isEmpty() && isOpen) {
                if (nanosTimeout <= 0) {
                    return null;
                }
                nanosTimeout = condition.awaitNanos(nanosTimeout);
            }
            if (isOpen) {
                return pool.poll();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void release(R resource) {
        lock.lock();
        try {
            if (isOpen) {
                pool.offer(resource);
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean add(R resource) {
        lock.lock();
        try {
            boolean result = pool.offer(resource);
            if (result) {
                condition.signalAll();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(R resource) throws InterruptedException {
        lock.lock();
        try {
            while (pool.contains(resource)) {
                if (!isOpen) {
                    return false;
                }
                condition.await();
            }
            boolean result = pool.remove(resource);
            if (result) {
                condition.signalAll();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public boolean removeNow(R resource) {
        lock.lock();
        try {
            boolean result = pool.remove(resource);
            if (result) {
                condition.signalAll();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
