package com.music.frame.asynctask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [description about this class]
 * 线程池生成器
 * @author jack
 */

public class ThreadPoolProducer {

    /**
     * 有N处理器，便长期保持N个活跃线程。
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT;
    //第1个线程池
    /**
     * 创建一个单线程的线程池。这个线程池只有一个线程在工作，也就是相当于单线程串行执行所有任务。
     * 如果这个唯一的线程因为异常结束，那么会有一个新的线程来替代它。此线程池保证所有任务的执行顺序按照任务的提交顺序执行。
     */
    public static final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
    //第2个线程池
    /**
     * 创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小。
     * 线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
     */
    public static final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    //第3个线程池
    /**
     * 创建一个可缓存的线程池。如果线程池的大小超过了处理任务所需要的线程，
       那么就会回收部分空闲（60秒不执行任务）的线程，当任务数增加时，此线程池又可以智能的添加新线程来处理任务。
       此线程池不会对线程池大小做限制，
       线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小。
     */
    public static final ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    //第4个线程池
    /**
     * 创建一个大小无限的线程池。此线程池支持定时以及周期性执行任务的需求。
     */
    public static final ScheduledThreadPoolExecutor newScheduledThreadPool = new ScheduledThreadPoolExecutor(1);

    //=========================================================================================================
    /**
     * ThreadPoolExecutor
     */
    //第5个线程池(根据你的具体需要,设计你的线程池)
    /**
     * 如果运行的线程少于 corePoolSize，则 Executor始终首选添加新的线程，而不进行排队。
     *（如果当前运行的线程小于corePoolSize，则任务根本不会存放，添加到queue中，而是直接抄家伙（thread）开始运行）
     如果运行的线程等于或多于 corePoolSize，则 Executor始终首选将请求加入队列，而不添加新的线程。
     如果无法将请求加入队列，则创建新的线程，除非创建此线程超出 maximumPoolSize，在这种情况下，任务将被拒绝。
     */
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE = 10;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new SynchronousQueue<Runnable>();
    /**
     * An Executorthat can be used to execute tasks in parallel.
     * 核心线程数为CORE_POOL_SIZE，不限制并发总线程数!
     * 这就使得任务总能得到执行，且高效执行少量ORE_POOL_SIZE异步任务。
     * 线程完成任务后保持KEEP_ALIVE秒销毁，这段时间内可重用以应付短时间内较大量并发，提升性能。
     * 它实际控制并执行线程任务。
     */
    public static final ThreadPoolExecutor cachedSerialExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    //第6个线程池
    /*********************************** 线程并发控制器 *******************************/
    /**
     * 并发量控制: 根据cpu能力控制一段时间内并发数量，并发量大时采用Lru方式移除旧的异步任务，默认采用LIFO策略调度线程运作，开发者可选调度策略有LIFO、FIFO。
     */
    public static final Executor lruSerialExecutor = new SmartSerialExecutor();

    /**
     * 它大大改善Android自带异步任务框架的处理能力和速度。
     * 默认地，它使用LIFO（后进先出）策略来调度线程，可将最新的任务快速执行，当然你自己可以换为FIFO调度策略。
     * 这有助于用户当前任务优先完成（比如加载图片时，很容易做到当前屏幕上的图片优先加载）。
     */
    private static class SmartSerialExecutor implements Executor {
        /**
         * 这里使用ArrayDequeCompat当栈比Stack性能高
         */
        private ArrayDequeCompat<Runnable> mQueue = new ArrayDequeCompat<Runnable>(serialMaxCount);
        private ScheduleStrategy mStrategy = ScheduleStrategy.LIFO;

        private enum ScheduleStrategy {
            /**
             * 队列中最后加入的任务最先执行
             */
            LIFO,
            /**
             * 队列中最先加入的任务最先执行
             */
            FIFO;
        }
        /**
         * 一次同时并发的线程数量，根据处理器数量调节
         *
         * <p>cpu count (base)  :  1    2    3    4    8    16    32
         * <p>once exe (base*2) :  1    2    3    4    8    16    32
         *
         * <p>一个时间段内最多并发线程个数：
         * 双核手机：2
         * 四核手机：4
         * ...
         * 计算公式如下：
         */
        private static int serialOneTime;
        /**
         * 最大排队任务数量，当投入的任务过多大于此值时，根据Lru规则，将最老的任务移除（将得不到执行）
         * <p>cpu count   :  1    2    3    4    8    16    32
         * <p>base(cpu+3) :  4    5    6    7    11   19    35
         * <p>max(base*16):  64   80   96   112  176  304   560
         */
        private static int serialMaxCount;
        private int cpuCount = CPU_COUNT;

        private void reSettings(int cpuCount) {
            this.cpuCount = cpuCount;
            serialOneTime = cpuCount;
            serialMaxCount = (cpuCount + 3) * 16;
        }

        public SmartSerialExecutor() {
            reSettings(CPU_COUNT);
        }

        @Override
        public synchronized void execute(final Runnable command) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    command.run();
                    next();
                }
            };
            if (cachedSerialExecutor.getActiveCount() < serialOneTime) {
                // 小于单次并发量直接运行
                cachedSerialExecutor.execute(r);
            } else {
                // 如果大于并发上限，那么移除最老的任务
                if (mQueue.size() >= serialMaxCount) {
                    mQueue.pollFirst();
                }
                // 新任务放在队尾
                mQueue.offerLast(r);
                // 动态获取目前cpu处理器数目,并调整设置。
                // int proCount = Runtime.getRuntime().availableProcessors();
                // if (proCount != cpuCount) {
                // cpuCount = proCount;
                // reSettings(proCount);
                // }
            }
        }

        private synchronized void next() {
            Runnable mActive;
            switch (mStrategy) {
                case LIFO :
                    mActive = mQueue.pollLast();
                    break;
                case FIFO :
                    mActive = mQueue.pollFirst();
                    break;
                default :
                    mActive = mQueue.pollLast();
                    break;
            }
            if (mActive != null) {
                cachedSerialExecutor.execute(mActive);
            }
        }
    }
    public static Executor defaultExecutor = newCachedThreadPool;
    private static ThreadPoolProducer instance;

    private ThreadPoolProducer(){
    }

    public static ThreadPoolProducer getInstance(){
        if (instance == null)
        {
            synchronized (ThreadPoolProducer.class){
                if(instance == null){
                    instance = new ThreadPoolProducer();
                }
            }
        }
        return instance;
    }
}
