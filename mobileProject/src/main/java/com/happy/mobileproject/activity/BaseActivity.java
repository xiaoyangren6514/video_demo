package com.happy.mobileproject.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.happy.mobileproject.R;

public abstract class BaseActivity extends Activity {

    private Button btn_left;
    private TextView tv_title;
    private Button  btn_right;

    private LinearLayout ll_child_content;
    private FrameLayout fl_titlebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        initView();
        setListener();
    }
    /**
     * 设置监听
     */
    private void setListener() {
        btn_left.setOnClickListener(mClickListener);
        btn_right.setOnClickListener(mClickListener);

    }
    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left://左边按钮
                    leftButtonClick();

                    break;
                case R.id.btn_right://右边按钮
                    rightButtonClick();
                    break;

                default:
                    break;
            }

        }
    };
    /**
     * 初始化布局文件
     */
    private void initView() {
        btn_left = (Button) findViewById(R.id.btn_left);
        tv_title = (TextView) findViewById(R.id.tv_title);
        btn_right = (Button) findViewById(R.id.btn_right);

        ll_child_content = (LinearLayout) findViewById(R.id.ll_child_content);
        fl_titlebar = (FrameLayout) findViewById(R.id.fl_titlebar);

        //加载孩子的布局文件
        View view = setContentView();

        if(view != null){
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params );
            ll_child_content.addView(view);
        }

    }

    /**
     * 设置标题栏隐藏和显示状态
     * @param visibility
     */
    public void setTitleBar(int visibility){
        fl_titlebar.setVisibility(visibility);
    }
    /**
     * 右边按钮的点击事件，由孩子具体实现
     */
    public abstract void rightButtonClick() ;


    /**
     * 设置标题内容
     * @param title
     */
    public void setTitle(String title){
        tv_title.setText(title);
    }

    /**
     * 设置左边按钮的隐藏和显示
     * @param visibility
     */
    public void setLeftButton(int visibility){
        btn_left.setVisibility(visibility);
    }

    /**
     * 设置右边按钮的隐藏和显示
     * @param visibility
     */
    public void setRightButton(int visibility){
        btn_right.setVisibility(visibility);
    }
    /**
     * 左边按钮的点击事件，由孩子具体实现
     */
    public abstract void leftButtonClick();
    /**
     * 加载孩子的布局文件，实现不同的效果，给孩子使用
     * @return
     */
    public abstract View setContentView() ;

}
