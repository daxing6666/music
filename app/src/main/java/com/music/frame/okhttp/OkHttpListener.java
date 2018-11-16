package com.music.frame.okhttp;

import com.music.frame.bean.InfoResult;

/**
 * Created by jack
 *
 */

public interface OkHttpListener<T> {

    void dataSucceed(int what, InfoResult<T> t);

    void dataFailed(int what, InfoResult<T> t);
}
