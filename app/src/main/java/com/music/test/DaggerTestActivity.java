package com.music.test;

import android.content.ComponentName;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.music.R;
import com.music.frame.bean.InfoResult;
import com.music.frame.ui.base.BaseActivity;
import com.music.test.model.Student;
import javax.inject.Inject;
import butterknife.BindView;

public class DaggerTestActivity extends BaseActivity {

    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.btn)
    Button btn;

    @Inject
    Student student;

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_test_dagger;
    }

    @Override
    public void init() {

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvContent.setText(student.say());
            }
        });
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

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
