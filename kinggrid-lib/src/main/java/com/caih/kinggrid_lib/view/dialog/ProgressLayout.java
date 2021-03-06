package com.caih.kinggrid_lib.view.dialog;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebensz.eink.R;


/**
 * Author: wmy
 * Date: 2020/8/4 8:56
 */
public class ProgressLayout extends LinearLayout {

    private Context mContext;
    private LinearLayout llContainer;
    private TextView tvMessage;

    public ProgressLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ProgressLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ProgressLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init(){
        LayoutInflater.from(mContext).inflate(R.layout.progress_layout, this);
        llContainer = findViewById(R.id.llContainer);
        tvMessage = findViewById(R.id.tvMessage);
        llContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void dismiss(){
        setVisibility(GONE);
    }

}
