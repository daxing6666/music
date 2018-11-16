package com.music.frame.bean;

/**
 * [description about this class]
 * 数据
 * @author jack
 */
public class InfoResult<T> {

    private int tag;
    private boolean success;
    private String desc;
    private T t;
    private int code;//返回状态标示 1成功 -1失败 -4session失效 -1000网络出错

    public InfoResult(boolean success, T t, int state, String desc) {
        this.success = success;
        this.t = t;
        this.desc = desc;
        this.code = state;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public int getState() {
        return code;
    }

    public void setState(int state) {
        this.code = state;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
