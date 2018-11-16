package com.music.frame.ui.view.recycleview.listener;

/**
 * [description about this class]
 * 数据结果接口
 * @author jack
 */

public interface DataStateListener {
    /**
     * 1:没有网络 2:没有数据 3:获取数据失败 4:展示内容
     * @param flag
     */
    public void onDataState(int flag);
}
