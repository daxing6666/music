package com.music.frame.okhttp;

/**
 * Created by jack
 */

public class OkSimpleRequest<T> extends OkHttpRequest<T>{

    public OkSimpleRequest(int tag, String url) {
        super(tag, url);
    }

    @Override
    protected T getResult(String responseBody) {
        return null;
    }
}
