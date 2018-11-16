package com.music.frame.asynctask;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.ListView;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @param <Params>启动任务执行的输入参数，比如HTTP请求的URL
 * @param <Progress>后台任务执行的百分比
 * @param <Result>后台执行任务最终返回的结果，比如String
 */
public abstract class AsyncTask<Params, Progress, Result> {

    // =============================================================================
    // Static members
    // =============================================================================
    private static final int MESSAGE_POST_RESULT = 0x1;//显示结果
    private static final int MESSAGE_POST_PROGRESS = 0x2;//更新进度
    @SuppressWarnings({"RawUseOfParameterizedType"})
    private static class AsyncTaskResult<Data> {
        final AsyncTask mTask;
        final Data[] mData;

        AsyncTaskResult(AsyncTask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }
    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super();
        }

        public InternalHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT :
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS :
                    result.mTask.onProgressUpdate(result.mData);
                    break;
                    default:break;
            }
        }
    }
    /**
     * Handler一定要在主线程实例化吗?new Handler()和new Handler(Looper.getMainLooper())的区别
       如果你不带参数的实例化：Handler handler = new Handler();那么这个会默认用当前线程的looper
       一般而言，如果你的Handler是要来刷新操作UI的，那么就需要在主线程下跑。
       1.要刷新UI，handler要用到主线程的looper。那么在主线程 Handler handler = new Handler();
         如果在其他线程，也要满足这个功能的话，要Handler handler = new Handler(Looper.getMainLooper());
       2.因为只有UI线程默认Loop.prepare();Loop.loop();过，其他线程需要手动调用这两个，否则会报错。
     */
    protected static final InternalHandler sHandler;
    static {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            sHandler = new InternalHandler(Looper.getMainLooper());
        } else {
            sHandler = new InternalHandler();
        }
    }

    /**
     * 默认线程池
     */
    public static final Executor mLruSerialExecutor = ThreadPoolProducer.lruSerialExecutor;
    private static volatile Executor sDefaultExecutor = ThreadPoolProducer.cachedSerialExecutor;

    // =============================================================================
    // Private members
    // =============================================================================
    /**
     * mWorker实际上是AsyncTask的一个的抽象内部类的实现对象实例，它实现了Callable<Result>接口中的call()方法
     */
    private final WorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result> mFuture;
    private volatile Status mStatus = Status.PENDING;
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
    private FinishedListener finishedListener;

    // =============================================================================
    // Static Methods
    // =============================================================================

    /** @hide Used to force static handler to be created. */
    public static void init() {
        sHandler.getLooper();
    }

    /** @hide */
    public static void setDefaultExecutor(Executor exec) {
        sDefaultExecutor = exec;
    }

    /**
     * Convenience version of {@link #execute(Object...)} for use with
     * a simple Runnable object. See {@link #execute(Object[])} for more
     * information on the order of execution.
     * <p> 用于重要、紧急、单独的异步任务，该Runnable立即得到执行。
     * <p> 加载类似瀑布流时产生的大量并发（任务数超出限制允许任务被剔除队列）时请用{@link AsyncTask#executeAllowingLoss(Runnable)}
     * @see #execute(Object[])
     */
    public static void execute(Runnable runnable) {
        sDefaultExecutor.execute(runnable);
    }

    /**
     * <p> 用于瞬间大量并发的场景，比如，假设用户拖动{@link ListView}时如果需要启动大量异步线程，而拖动过去时间很久的用户已经看不到，允许任务丢失。
     * <p> This method execute runnable wisely when a large number of task will be submitted.
     * <p> 任务数限制情况见{SmartSerialExecutor}
     * immediate execution for important or urgent task.
     * @param runnable
     */
    public static void executeAllowingLoss(Runnable runnable) {
        mLruSerialExecutor.execute(runnable);
    }

    // =============================================================================
    // Constructors
    // =============================================================================
    /**
     * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     */
    public AsyncTask() {

        //java中的匿名内部类
        mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                /**
                 * Android在线程方面主要使用的是Java本身的Thread类，
                 * 我们可以在Thread或Runnable接口中的run方法首句加入Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND),
                 * 设置线程优先级为后台，这样当多个线程并发后很多无关紧要的线程分配的CPU时间将会减少，有利于主线程的处理
                 */
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                //call方法被调用后,将设置优先级为后台级别,然后调用AsyncTask的doInBackground方法
                return postResult(doInBackground(mParams));
            }
        };

        /**
         *在mFuture实例中,将会调用mWorker做后台任务,完成后会调用done方法
         */
        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occured while executing doInBackground()", e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    // =============================================================================
    // Method
    // =============================================================================
    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        @SuppressWarnings("unchecked")
        Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult<Result>(this, result));
        message.sendToTarget();
        return result;
    }

    /**
     * 这个方法是我们必须要重写的，用来做后台计算 所在线程：后台线程
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * 在doInBackground之前调用，用来做初始化工作 所在线程：UI线程
     */
    protected void onPreExecute() {}

    /**
     *
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void onPostExecute(Result result) {}

    /**
     * 在publishProgress之后调用，用来更新计算进度 所在线程：UI线程
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void onProgressUpdate(Progress... values) {}

    /**
     * 所在线程：UI线程<br>
     * doInBackground执行结束并且{@link #cancel(boolean)} 被调用。<br>
     * 如果本函数被调用则表示任务已被取消，这个时候onPostExecute不会再被调用。
     */
    @SuppressWarnings({"UnusedParameters"})
    protected void onCancelled(Result result) {
        onCancelled();
    }

    /**
     */
    protected void onCancelled() {}

    /**
     */
    public final boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    /**
     */
    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    /**
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        return mFuture.get(timeout, unit);
    }

    /**
     */
    public final AsyncTask<Params, Progress, Result> execute(final Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    /**
     * <p> 用于瞬间大量并发的场景，比如，假设用户拖动{@link ListView}时如果需要加载大量图片，而拖动过去时间很久的用户已经看不到，允许任务丢失。
     * <p> This method execute task wisely when a large number of task will be submitted.
     * @param params
     * @return
     */
    public final AsyncTask<Params, Progress, Result> executeAllowingLoss(Params... params) {
        return executeOnExecutor(mLruSerialExecutor, params);
    }

    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING :
                    throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
                case FINISHED :
                    throw new IllegalStateException("Cannot execute task:" + " the task has already been executed "
                            + "(a task can be executed only once)");
                    default:break;
            }
        }
        mStatus = Status.RUNNING;
        onPreExecute();
        mWorker.mParams = params;
        exec.execute(mFuture);
        return this;
    }

    /**
     * 打印后台计算进度,onProgressUpdate会被调用
     * 使用内部handle发送一个进度消息，让onProgressUpdate被调用
     */
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            sHandler.obtainMessage(MESSAGE_POST_PROGRESS, new AsyncTaskResult<Progress>(this, values)).sendToTarget();
        }
    }

    /**
     * 任务结束的时候会进行判断：如果任务没有被取消，则调用onPostExecute;否则调用onCancelled
     * @param result
     */
    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
            if (finishedListener != null) {

                finishedListener.onCancelled();
            }
        } else {
            onPostExecute(result);
            if (finishedListener != null){
                finishedListener.onPostExecute();
            }
        }
        mStatus = Status.FINISHED;
    }

    // =============================================================================
    // Abstract
    // =============================================================================
    /**
     * AsyncTask的一个的抽象内部类的实现
     * @param <Params>
     * @param <Result>
     */
    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    // =============================================================================
    // Interface
    // =============================================================================
    public static interface FinishedListener {
        void onCancelled();

        void onPostExecute();
    }

    // =============================================================================
    // enum
    // =============================================================================
    /**
     * Indicates the current status of the task. Each status will be set only once
     * during the lifetime of a task.
     */
    public enum Status {
        /**
         * 待定状态.
         */
        PENDING,
        /**
         * 执行状态.
         */
        RUNNING,
        /**
         * 结束状态.
         */
        FINISHED,
    }

    // =============================================================================
    // Getter / Setter
    // =============================================================================
    protected FinishedListener getFinishedListener() {
        return finishedListener;
    }

    protected void setFinishedListener(FinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    public final Status getStatus() {
        return mStatus;
    }

    /*AsyncTask定义了三种泛型类型 Params，Progress和Result。

    Params 启动任务执行的输入参数，比如HTTP请求的URL。
    Progress 后台任务执行的百分比。
    Result 后台执行任务最终返回的结果，比如String。

    onPreExecute() 第一个执行的方法,执行时机：在执行实际的后台操作前，被UI 线程调用
    作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。

    doInBackground(Params…) 后台执行，比较耗时的操作都可以放在这里。注意这里不能直接操作UI。此方法在后台线程执行，完成任务的主要工作，通常需要较长的时间。在执行过程中可以调用publicProgress(Progress…)来更新任务的进度。
    onPostExecute(Result)  相当于Handler 处理UI的方式，在这里面可以使用在doInBackground 得到的结果处理操作UI。 此方法在主线程执行，任务执行的结果作为此方法的参数返回
    有必要的话你还得重写以下这三个方法，但不是必须的：

    onProgressUpdate(Progress…)   可以使用进度条增加用户体验度。 此方法在主线程执行，用于显示任务执行的进度。
    onCancelled()             用户调用取消时，要做的操作
    使用AsyncTask类，以下是几条必须遵守的准则：

    Task的实例必须在UI thread中创建；
    execute方法必须在UI thread中调用；
    不要手动的调用onPreExecute(), onPostExecute(Result)，doInBackground(Params...), onProgressUpdate(Progress...)这几个方法；
    该task只能被执行一次，否则多次调用时将会出现异常；*/
}
