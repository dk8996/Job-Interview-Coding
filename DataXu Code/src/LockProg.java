import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author KudryD<br>
 *         Design Decisions<br>
 *         -Used a queue to keep track of freed resources and to select the next available resource in constant time.<br>
 *         -Used a Hash to keep track of all the resource and another Hash to keep track of acquired resources. Leveraged in the
 *         release operation runs in constant time. Note that his can be improved on.<br>
 *         -Used Conditions for thread communications. Has a number of advantages; conditions are bound to the lock, waiting
 *         condition releases the lock suspends the current thread, supports waiting with timout.<br>
 *         -Used ReentrantLock. This has a lot of performance advantages over other methods of synchronization.<br>
 *         -Most functions run in constant time besides remove it runs in<br>
 *         Things that can be improved<br>
 *         -Tighter locks. Maybe incorporating one writer / many readers pattern.<br>
 *         -Reduce the number of collections used. Probably can get away with one collection.<br>
 *         -Tighter signals. The change condition object used for signals can be reduced in scope.<br>
 *         -Performance on remove and removeNow can be improved from O(n) to O(ln n).<br>
 * @param <R>
 */
public class LockProg<R> {
    private boolean isOpen = false;

    private Map<R, Object> locks;

    private BlockingDeque<R> freeQueue;

    private Map<R, Object> acquiredMap;

    private final ReentrantLock mainLock = new ReentrantLock();

    private Condition acquiredMapEmpty = mainLock.newCondition();

    private Condition freeQueueNotEmpty = mainLock.newCondition();

    private Condition change = mainLock.newCondition();

    /**
     * Opens the pool. No resource are allowed to be acquired unless the poll is open.
     */
    public void open() {

        System.out.println("Open, with thread: " + Thread.currentThread().getName());
        mainLock.lock();
        try {
            if (isOpen) {
                throw new IllegalStateException("Already open");
            }
            isOpen = true;
            locks = new HashMap<R, Object>();
            acquiredMap = new HashMap<R, Object>();
            freeQueue = new LinkedBlockingDeque<R>();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * @return <code>true</code> if the pool is open <code>false</code> if the pool is closed
     */
    public boolean isOpen() {

        return isOpen;
    }

    /**
     * This blocks until all acquired resources are released.
     * 
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code>
     */
    public void close() throws InterruptedException {

        System.out.println("Close, with thread: " + Thread.currentThread().getName());
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }

            while (!acquiredMap.isEmpty()) {
                acquiredMapEmpty.await();
                if (!isOpen) {
                    throw new IllegalStateException("Not open");
                }
            }
            isOpen = false;
            locks = null;
            acquiredMap = null;
            freeQueue = null;
            change = null;
        } finally {
            mainLock.unlock();

        }
    }

    /**
     * This closes the pool immediately without waiting for all acquired resources to be released.
     * 
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code>
     */
    public void closeNow() throws InterruptedException {

        System.out.println("CloseNow, with thread: " + Thread.currentThread().getName());
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            isOpen = false;
            locks = null;
            acquiredMap = null;
            freeQueue = null;
            acquiredMapEmpty.signalAll();
            freeQueueNotEmpty.signalAll();
            change.signalAll();
        } finally {
            mainLock.unlock();

        }
    }

    /**
     * Blocks until a resource is available.
     * 
     * @return the resource acquired
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code>
     */
    public R acquire() throws InterruptedException {

        System.out.println("Acquire, with thread: " + Thread.currentThread().getName());

        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            while (freeQueue.isEmpty()) {
                freeQueueNotEmpty.await();
                if (!isOpen) {
                    throw new IllegalStateException("Not open");
                }
            }
            R resource = freeQueue.take();
            Object lock = locks.get(resource);
            acquiredMap.put(resource, lock);
            change.signalAll();
            return resource;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Tries to acquire a resource within a given time. If a resource cannot be acquired within the timeout interval specified in
     * the acquire(long, TimeUnit) method, null is returned.
     * 
     * @param timeout the maximum time to wait
     * @param timeUnit the time unit of the {@code timeout} argument
     * @return the resource acquired or null if the resource was not acquired within the timeout interval
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code>
     */
    public R acquire(long timeout, java.util.concurrent.TimeUnit timeUnit) throws InterruptedException {

        System.out.println("Acquire Timed, with thread: " + Thread.currentThread().getName());
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            while (freeQueue.isEmpty()) {
                // timeout, return null
                if (!freeQueueNotEmpty.await(timeout, timeUnit)) {
                    return null;
                }
                if (!isOpen) {
                    throw new IllegalStateException("Not open");
                }
            }
            R resource = freeQueue.take();
            Object lock = locks.get(resource);
            acquiredMap.put(resource, lock);
            change.signalAll();
            return resource;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Release the resource back to the pool.
     * 
     * @param resource the resource released
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code> or if the resource is not in the pool
     */
    public void release(R resource) {

        System.out.println("Release, resource: " + resource + " with thread: " + Thread.currentThread().getName());
        if (resource == null) {
            throw new NullPointerException();
        }
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            Object lock = locks.get(resource);
            if (lock == null) {
                throw new IllegalStateException("No such resource in the pool");
            }
            lock = acquiredMap.remove(resource);
            if (lock == null) {
                return;
            }
            freeQueue.add(resource);
            change.signalAll();
            if (acquiredMap.isEmpty()) {
                acquiredMapEmpty.signalAll();
            }
            if (!freeQueue.isEmpty()) {
                freeQueueNotEmpty.signalAll();
            }

        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Add the resource to the pool.
     * 
     * @param resource the resource to be added to the pool
     * @return <code>true</code> if the resource was added to the pool <code>false</code> if the resource is already in the pool
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code>
     */
    public boolean add(R resource) {

        System.out.println("Add, resource: " + resource + " with thread: " + Thread.currentThread().getName());
        if (resource == null) {
            throw new NullPointerException();
        }
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            Object lock = locks.get(resource);
            if (lock != null) {
                return false;
            }
            locks.put(resource, new Object());
            freeQueue.add(resource);
            change.signalAll();
            if (!freeQueue.isEmpty()) {
                freeQueueNotEmpty.signalAll();
            }
            return true;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Remove the resource from the pool. This method blocks if the resource that is being removed is currently in use, until that
     * resource has been released.
     * 
     * @param resource the resource to be removed from the pool
     * @return <code>true</code> if the resource was removed to the pool <code>false</code> otherwise
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code> or if the resource is not in the pool
     */
    public boolean remove(R resource) throws InterruptedException {

        System.out.println("Remove, resource: " + resource + " with thread: " + Thread.currentThread().getName());
        if (resource == null) {
            throw new NullPointerException();
        }
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            Object lock = locks.get(resource);
            if (lock == null) {
                return false;
            }
            if (freeQueue.remove(resource)) {
                locks.remove(resource);
                if (!freeQueue.isEmpty()) {
                    freeQueueNotEmpty.signalAll();
                }
                return true;
            }
            while (!freeQueue.contains(resource)) {
                change.await();
                if (!isOpen) {
                    throw new IllegalStateException("Not open");
                }
                lock = locks.get(resource);
                if (lock == null) {
                    return false;
                }
            }
            if (freeQueue.remove(resource)) {
                locks.remove(resource);
                if (!freeQueue.isEmpty()) {
                    freeQueueNotEmpty.signalAll();
                }
                return true;
            }
            return false;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Remove the resource from the pool immediately without waiting for it to be released.
     * 
     * @param resource the resource to be removed from the pool
     * @return <code>true</code> if the resource was removed to the pool <code>false</code> otherwise
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if the pool is closed, isOpen returns <code>false</code> or if the resource is not in the pool
     */
    public boolean removeNow(R resource) throws InterruptedException {

        System.out.println("RemoveNow, resource: " + resource + " with thread: " + Thread.currentThread().getName());
        if (resource == null) {
            throw new NullPointerException();
        }
        mainLock.lock();
        try {
            if (!isOpen) {
                throw new IllegalStateException("Not open");
            }
            if (freeQueue.remove(resource)) {
                if (!freeQueue.isEmpty()) {
                    freeQueueNotEmpty.signalAll();
                }
                return true;
            }
            Object lock = acquiredMap.remove(resource);
            boolean removed = freeQueue.remove(resource);
            locks.remove(resource);
            change.signalAll();
            if (acquiredMap.isEmpty()) {
                acquiredMapEmpty.signalAll();
            }
            if (!freeQueue.isEmpty()) {
                freeQueueNotEmpty.signalAll();
            }
            if (lock != null || removed) {
                return true;
            }
            return false;
        } finally {
            mainLock.unlock();
        }
    }
}
