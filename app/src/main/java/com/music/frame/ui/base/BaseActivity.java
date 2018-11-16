package com.music.frame.ui.base;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;
import com.music.AppDroid;
import com.music.frame.bean.InfoResult;
import com.music.frame.bean.MsgBean;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

public abstract class BaseActivity<T extends BaseContract.BasePresenter> extends RxAppCompatActivity implements ServiceConnection, BaseContract.BaseView  {

    private EventBus eventBus;
    private Toast toast = null;
    protected Handler mHandler;
    protected ActivityComponent mActivityComponent;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AppDroid.getInstance().finishActivity(this);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //把设置布局文件的操作交给继承的子类
        setContentView(getLayoutResId());
        mHandler = new Handler();
        //把设置布局文件的操作交给继承的子类
        ButterKnife.bind(this);
        AppDroid.getInstance().addActivity(this);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        initActivityComponent();

        init();
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (eventBus != null) {
            eventBus.unregister(this);
        }
        AppDroid.getInstance().finishActivity(this);
        ButterKnife.bind(this).unbind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerMeg(MsgBean msgBean) {

    }

    /**
     * 初始化Dagger
     */
    private void initActivityComponent() {
        mActivityComponent = DaggerActivityComponent.builder()
                .applicationComponent(MusicApp.getInstance().getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }

    // -------------------- BaseActivity的辅助封装 --------------------- //

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    public abstract boolean isSupportSwipeBack();

    /**
     * 返回当前Activity布局文件的id
     *
     * @return
     */
    public abstract int getLayoutResId();

    /**
     * 初始化的一些操作
     */
    public abstract void init();

    /**
     * 加载网络数据
     */
    public abstract void loadData();

    /**
     * 网络数据返回成功
     */
    public abstract void onSuccess(int what, InfoResult t);

    /**
     * 网络数据返回失败
     */
    public abstract void onFail(int what,InfoResult t);

    public EventBus getEventBus() {
        return eventBus;
    }
}
