package com.music.ui.main;

import com.music.R;
import com.music.frame.bean.InfoResult;
import com.music.frame.ui.base.BaseActivity;

public class HomeActivity extends BaseActivity {

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_home;
    }

    @Override
    public void init() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void onSuccess(int what, InfoResult t) {

    }

    @Override
    public void onFail(int what, InfoResult t) {

    }
}
