package com.music.frame.asynctask;

import android.os.Message;
import org.greenrobot.eventbus.EventBus;

/**
 * [description about this class]
 * 任务
 * @author jack
 */

public abstract class Task implements ITask{

    private int mTaskId;
    private EventBus eventBus;
    private boolean cancelTask;
    private FinishedListener finishedListener;

    public Task(){

    }

    public Task(int taskId, Object subscriber){

        this(taskId, new EventBus(), subscriber);
    }

    public Task(int taskId, EventBus eventBus, Object subscriber){

        this.mTaskId = taskId;
        if(eventBus == null){
            eventBus = new EventBus();
        }
        this.eventBus = eventBus;
        this.eventBus.register(subscriber);
    }

    @Override
    public void run() {
        final Object result = doInBackground();
        synchronized (this){
            if(isCancelTask()){
                if(finishedListener != null){
                    finishedListener.onCancelled();
                }
                return;
            }else{
                if(finishedListener != null){
                    finishedListener.onFinished();
                }
                if(eventBus!=null){
                    Message msg = Message.obtain();
                    msg.what = mTaskId;
                }
            }
        }
    }

    public static interface FinishedListener {
        void onCancelled();
        void onFinished();
    }

    public boolean isCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(boolean cancelTask) {
        this.cancelTask = cancelTask;
    }

    public FinishedListener getFinishedListener() {
        return finishedListener;
    }

    public void setFinishedListener(FinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }
}
