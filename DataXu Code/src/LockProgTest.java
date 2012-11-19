import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */

/**
 * @author Dimitry
 */
public class LockProgTest {

    private LockProg<Integer> lockProg = new LockProg<Integer>();

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        lockProg = new LockProg<Integer>();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        lockProg = null;
    }

    /**
     * Test method for {@link LockProg#open()}.
     */
    @Test
    public void testOpen() {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
    }

    /**
     * Test method for {@link LockProg#close()}.
     * 
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void testCloseException() throws InterruptedException {

        lockProg.close();
    }

    /**
     * Test method for {@link LockProg#close()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testClose() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        lockProg.close();
        Assert.assertFalse(lockProg.isOpen());
    }

    /**
     * Test method for {@link LockProg#close()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCloseRelease() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertTrue(lockProg.isOpen());
        lockProg.release(value);
        lockProg.close();
        Assert.assertFalse(lockProg.isOpen());
    }

    /**
     * Test method for {@link LockProg#close()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCloseNowRelease() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        lockProg.closeNow();
        Assert.assertFalse(lockProg.isOpen());
    }

    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());

    }

    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire2() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());

        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        Assert.assertEquals(value, lockProg.acquire());

    }
    
    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire3() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value, lockProg.acquire());
    }
    
    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire4() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(10);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
    }
    
    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire5() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(10);
                    lockProg.close();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                    lockProg.removeNow(value2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();
        Assert.assertEquals(value, lockProg.acquire());
    }
    
    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void testAcquire6() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.closeNow();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(10);
                    lockProg.release(value);
                    lockProg.removeNow(value2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertNull(lockProg.acquire());
    }

    /**
     * Test method for {@link LockProg#acquire()}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquire7() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(100);
                    lockProg.close();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                    lockProg.removeNow(value2);
                    Assert.assertTrue(lockProg.add(value2));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
    }
    
    /**
     * Test method for {@link LockProg#acquire(long, java.util.concurrent.TimeUnit)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquireLongTimeUnit() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire(1000, TimeUnit.NANOSECONDS));
    }

    /**
     * Test method for {@link LockProg#acquire(long, java.util.concurrent.TimeUnit)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquireLongTimeUnit2() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertNull(lockProg.acquire(1000, TimeUnit.NANOSECONDS));
    }

    /**
     * Test method for {@link LockProg#acquire(long, java.util.concurrent.TimeUnit)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAcquireLongTimeUnit3() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());

        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(100);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        Assert.assertEquals(value, lockProg.acquire(1000, TimeUnit.MILLISECONDS));
    }

    /**
     * Test method for {@link LockProg#release(java.lang.Object)}.
     */
    @Test
    public void testRelease() {

        lockProg.open();
        Assert.assertTrue(lockProg.isOpen());
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        lockProg.release(value);
    }

    /**
     * Test method for {@link LockProg#add(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAdd() throws InterruptedException {

        lockProg.open();
        Assert.assertTrue(lockProg.add(new Integer(5)));
        Assert.assertTrue(lockProg.add(new Integer(6)));

    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemove() throws InterruptedException {

        lockProg.open();
        Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.remove(value));
        Assert.assertFalse(lockProg.remove(value));
    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemoveNow() throws InterruptedException {

        lockProg.open();
        Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertTrue(lockProg.removeNow(value));
    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemoveNow2() throws InterruptedException {

        lockProg.open();
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());

        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.removeNow(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        Assert.assertFalse(lockProg.remove(value));

    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemoveLarge() throws InterruptedException {

        lockProg.open();
        for (int i = 0; i < 50; i++) {
            Integer value = new Integer(i);
            Assert.assertTrue(lockProg.add(value));
        }
        for (int i = 0; i < 50; i++) {
            Integer value = new Integer(i);
            Assert.assertTrue(lockProg.remove(value));
        }

    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemove2() throws InterruptedException {

        lockProg.open();
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();

        Assert.assertTrue(lockProg.remove(value));
    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test(expected = IllegalStateException.class)
    public void testRemove3() throws InterruptedException {

        lockProg.open();
        final Integer value = new Integer(5);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertEquals(value, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.closeNow();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();

        Assert.assertFalse(lockProg.remove(value));
    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemove4() throws InterruptedException {

        lockProg.open();
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value2);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        Assert.assertTrue(lockProg.remove(value));
    }

    /**
     * Test method for {@link LockProg#remove(java.lang.Object)}.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemove5() throws InterruptedException {

        lockProg.open();
        final Integer value = new Integer(5);
        final Integer value2 = new Integer(6);
        Assert.assertTrue(lockProg.add(value));
        Assert.assertTrue(lockProg.add(value2));
        Assert.assertEquals(value, lockProg.acquire());
        Assert.assertEquals(value2, lockProg.acquire());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();

        Runnable r2 = new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    lockProg.release(value2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t2 = new Thread(r2);
        t2.start();

        Assert.assertTrue(lockProg.remove(value));
        Assert.assertTrue(lockProg.remove(value2));
    }
}
