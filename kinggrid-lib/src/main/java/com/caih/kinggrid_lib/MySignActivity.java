package com.caih.kinggrid_lib;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caih.kinggrid_lib.base.BaseActivity;
import com.caih.kinggrid_lib.http.API;
import com.caih.kinggrid_lib.view.ConstantValue;
import com.ebensz.eink.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MySignActivity extends BaseActivity {

    private LinearLayout llBack;
    private TextView tvPrompt;
    private ImageView ivSign;
    private Button btnReSign;
    private String SIGN_PIC_PATH;

    private String cookie;
    private String envServer;

    public static final int MSG_LOAD_SIGN_SUCCESS = 1;
    public static final int MSG_LOAD_SIGN_FAIL = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "============== onCreate ");
        setContentView(R.layout.activity_my_sign);
        initView();
        initEvent();
        SIGN_PIC_PATH = getExternalCacheDir().getPath()+ ConstantValue.SIGN_PIC_PATH_SUFFIX;
        envServer = getIntent().getStringExtra("envServer");
        cookie = getIntent().getStringExtra("cookie");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "=====================onSaveInstanceState");
        outState.putString("cookie", cookie);
        outState.putString("envServer", envServer);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cookie = savedInstanceState.getString("cookie");
        envServer = savedInstanceState.getString("envServer");
        SIGN_PIC_PATH = getExternalCacheDir().getPath()+ ConstantValue.SIGN_PIC_PATH_SUFFIX;
        Log.i(TAG, "=====================onRestoreInstanceState "+cookie);
    }

    private void initView() {
        llBack = findViewById(R.id.llBack);
        tvPrompt = findViewById(R.id.tvPrompt);
        ivSign = findViewById(R.id.ivSign);
        btnReSign = findViewById(R.id.btnReSign);
    }

    private void initEvent() {
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnReSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MySignActivity.this, AddSignActivity.class);
                intent.putExtra("envServer", envServer);
                intent.putExtra("cookie", cookie);
                startActivity(intent);
            }
        });
        ivSign.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ivSign.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = ivSign.getWidth();
                int height = (int)(width*1f/1.5f);
                Log.i(TAG, "onGlobalLayout width = "+width+" "+height);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ivSign.getLayoutParams();
                layoutParams.height = height;
                ivSign.setLayoutParams(layoutParams);
                showLoading();
                initSign();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initSign(){
        File file = new File(SIGN_PIC_PATH);
        if(file.exists()) {
            showNoSignPrompt(View.GONE);
            loadSign();
        }else {
            getSignInfo();
        }
    }

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_LOAD_SIGN_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    btnReSign.setText("重新签名");
                    ivSign.setImageBitmap(bitmap);
                    hideLoading();
                    showNoSignPrompt(View.GONE);
                    break;
                case MSG_LOAD_SIGN_FAIL:
                    hideLoading();
                    showNoSignDialog("签名加载出错");
                    break;
            }
        }
    };

    private void loadSign(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(SIGN_PIC_PATH);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    Message msg = handler.obtainMessage();
                    msg.what = MSG_LOAD_SIGN_SUCCESS;
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(MSG_LOAD_SIGN_FAIL);
                }
            }
        }).start();
    }

    private void getSignInfo(){
        String url = API.getAPI(envServer, API.API_GET_SIGN_INFO);
        requestData(url, null, cookie, new RequestDataListener() {
            @Override
            public void onSuccess(String res) {
                JSONObject rsp = JSON.parseObject(res);
                Log.i(TAG, "getSignInfo "+res);
                boolean success = rsp.getBoolean("success");
                String code = rsp.getString("code");
                String codeMsg = rsp.getString("codeMsg");
                if(success && "0000".equals(code)){
                    JSONObject data = rsp.getJSONObject("data");
                    String signatureUrl = data.getString("signatureUrl");
                    if(TextUtils.isEmpty(signatureUrl)){
                        btnReSign.setText("设置签名");
                        showNoSignPrompt(View.VISIBLE);
                        hideLoading();
                    }else {
                        String url = "";
                        if(signatureUrl.contains("api-file")) {
                            url = envServer + signatureUrl;
                        }else {
                            url = envServer +"/api-file"+ signatureUrl;
                        }
                        downloadSign(url);
                    }
                }else {
                    hideLoading();
                    showNoSignDialog(codeMsg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                hideLoading();
                showNoSignDialog("获取签名信息出错");
            }
        });
    }

    private void downloadSign(String signatureUrl){
        downloadFile(signatureUrl, cookie, SIGN_PIC_PATH, 0, new DownloadListener() {
            @Override
            public void onDownloadSuccess(String filePath) {
                loadSign();
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                hideLoading();
                showNoSignDialog("获取签名信息出错");
            }
        });
    }

    private void showLoading(){
        if(progressDialog==null) {
            progressDialog = new ProgressDialog(MySignActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("加载中");
        }
        progressDialog.show();
    }

    private void showNoSignPrompt(int visibility){
        tvPrompt.setVisibility(visibility);
    }

    private void hideLoading(){
        progressDialog.dismiss();
    }

    private ProgressDialog progressDialog;

    private void showNoSignDialog(String msg){
        btnReSign.setText("设置签名");
        toast(msg);
        showNoSignPrompt(View.VISIBLE);
//        if(DocumentModule.showSignPrompt){
//           return;
//        }
//        DocumentModule.showSignPrompt = true;
//        AlertDialog.Builder builder = new AlertDialog.Builder(MySignActivity.this)
//            .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            }).setTitle("提示")
//            .setMessage(msg)
//            .setCancelable(false);
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }
}
