package com.music.frame.asynctask;

import android.os.Message;
import org.greenrobot.eventbus.EventBus;

/**
 * [description about this class]
 * 简单的异步任务，仅仅指定返回结果的类型，不可输入参数
 * @author jack
 */

public abstract class SimpleTask<T> extends AsyncTask<Object, Object, T> {

    private int taskId;
    private EventBus eventBus;
    public SimpleTask(){

    }

    public SimpleTask(int taskId, Object subscriber){

        this(taskId, new EventBus(), subscriber);
    }

    public SimpleTask(int taskId, EventBus eventBus, Object subscriber){

        this.taskId = taskId;
        this.eventBus = eventBus;
        if(eventBus == null){
            this.eventBus = new EventBus();
        }
        this.eventBus.register(subscriber);
    }

    @Override
    protected T doInBackground(Object... params) {

        final T result = doInBackground();
        if(eventBus != null){
            if(!isCancelled()){
                synchronized (this){
                    Message msg = Message.obtain();
                    msg.what = taskId;
                    msg.obj = result;
                    eventBus.post(msg);
                }
            }
        }
        return result;
    }

    protected abstract T doInBackground();
}
