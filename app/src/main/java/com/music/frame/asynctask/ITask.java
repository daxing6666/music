package com.music.frame.asynctask;

/**
 * [description about this class]
 * 任务接口
 * @author jack
 */

public interface ITask extends Runnable {

    /**
     * 执行耗时任务
     * @return
     */
    Object doInBackground();
}
