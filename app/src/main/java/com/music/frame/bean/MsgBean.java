package com.music.frame.bean;

/**
 * [description about this class]
 * activity之间事件的传递实体
 * @author jack
 */

public class MsgBean {

    private int flag;
    private Object object;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
