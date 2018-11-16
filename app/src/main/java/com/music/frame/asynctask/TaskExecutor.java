package com.music.frame.asynctask;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * [description about this class]
 * 任务执行器(可以根据你的项目自己设计线程池)
 * @author jack
 */

public class TaskExecutor {

    private static TaskExecutor sInstance;
    private Executor executor = ThreadPoolProducer.defaultExecutor;

    private TaskExecutor(){
        if(executor == null){
            executor =ThreadPoolProducer.defaultExecutor;
        }
    }

    public static TaskExecutor getInstance(){
        if (sInstance == null) {
            synchronized (TaskExecutor.class){
                if(sInstance == null){
                    sInstance = new TaskExecutor();
                }
            }
        }
        return sInstance;
    }

    /**
     * 开子线程
     *
     * @param run
     */
    public void start(Runnable run) {
        if(run != null){
            executor.execute(run);
        }
    }

    /**
     * 延时异步任务
     *
     * @param task
     * @param time
     * @param unit
     */
    public void startDelayedTask(final Task task, long time, TimeUnit unit) {
        long delay = time;
        if (unit != null) {
            delay = unit.toMillis(time);
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                executor.execute(task);
            }
        }, delay);
    }

    /**
     * 启动定时任务
     *
     * @param run
     * @param delay  >0 延迟时间
     * @param period >0 心跳间隔时间
     * @return
     */
    public Timer startTimerTask(final Runnable run, long delay, long period) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        };
        timer.scheduleAtFixedRate(timerTask, delay, period);
        return timer;
    }

    public void execute(ITask task) {
        if(task != null){
            executor.execute(task);
        }
    }

    /**
     * Execute a runnable using custom executor
     * @param task
     * @param executor
     */
    public void execute(ITask task, Executor executor)
    {
        if(task != null){
            if(executor != null){
                executor.execute(task);
            }
        }
    }

    /**
     * 有序异步任务执行器
     * @return
     */
    public OrderedTaskExecutor newOrderedTaskExecutor() {
        return new OrderedTaskExecutor();
    }

    public class OrderedTaskExecutor {

        LinkedList<Task> taskList = new LinkedList<Task>();
        private transient boolean isRunning = false;
        public OrderedTaskExecutor put(Task task) {
            synchronized (taskList) {
                if (task != null) {
                    taskList.add(task);
                }
            }
            return this;
        }

        public void start() {
            if (isRunning){
                return;
            }
            isRunning = true;
            for (Task each : taskList) {
                final Task task = each;
                task.setFinishedListener(new Task.FinishedListener() {

                    @Override
                    public void onCancelled() {
                        synchronized (taskList) {
                            taskList.remove(task);
                            executeNext();
                        }
                    }
                    @Override
                    public void onFinished() {
                        synchronized (taskList){
                            executeNext();
                        }
                    }
                });
            }
            executeNext();
        }
        @SuppressWarnings("unchecked")
        private void executeNext() {
            Task next = null;
            if (taskList.size() > 0) {
                next = taskList.removeFirst();
            }
            if (next != null) {
                executor.execute(next);
            } else {
                isRunning = false;
            }
        }
    }

    /**
     * 关卡异步任务执行器
     *
     * @return
     */
    public CyclicBarrierExecutor newCyclicBarrierExecutor() {
        return new CyclicBarrierExecutor();
    }


    public class CyclicBarrierExecutor {

        ArrayList<Task> taskList = new ArrayList<Task>();
        private transient boolean isRunning = false;

        public CyclicBarrierExecutor put(Task task) {
            if (task != null) {
                taskList.add(task);
            }
            return this;
        }

        public void start(Task finishTask){
            start(finishTask, 0, null);
        }

        public void start(final Task endOnUiTask, final long time, final TimeUnit unit) {
            if (isRunning) {
                throw new RuntimeException("CyclicBarrierExecutor only can start once.");
            }
            isRunning = true;
            //一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待
            final CountDownLatch latch = new CountDownLatch(taskList.size());

            executor.execute(new Task() {
                @Override
                public Object doInBackground() {
                    try {
                        if (unit == null) {
                            latch.await();
                        } else {
                            latch.await(time, unit);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executor.execute(endOnUiTask);
                    return null;
                }
            });
            startInternal(latch);
        }

        public void start(Runnable endOnUiThread) {
            start(endOnUiThread, 0, null);
        }

        public void start(final Runnable endOnUiThread, final long time, final TimeUnit unit) {
            if (isRunning) {
                throw new RuntimeException("CyclicBarrierExecutor only can start once.");
            }
            isRunning = true;
            final CountDownLatch latch = new CountDownLatch(taskList.size());
            new SimpleTask<Boolean>() {

                @Override
                protected Boolean doInBackground() {
                    try {
                        if (unit == null) {
                            latch.await();
                        }
                        else {
                            latch.await(time, unit);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    endOnUiThread.run();
                }
            }.execute();
            startInternal(latch);
        }

        private void startInternal(final CountDownLatch latch) {
            for (Task each : taskList) {

                each.setFinishedListener(new Task.FinishedListener() {
                    @Override
                    public void onCancelled() {
                        latch.countDown();
                    }

                    @Override
                    public void onFinished() {
                        latch.countDown();
                    }
                });
                executor.execute(each);
            }
        }
    }
}
