package com.music.frame.okhttp;

import com.music.frame.bean.InfoResult;

/**
 * Created by jack
 */

public class SimpleOkHttpListener<T> implements OkHttpListener<T> {

    @Override
    public void dataFailed(int what, InfoResult<T> t) {

    }

    @Override
    public void dataSucceed(int what, InfoResult<T> t) {

    }
}
