package com.caih.kinggrid_lib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caih.kinggrid_lib.base.BaseActivity;
import com.caih.kinggrid_lib.http.API;
import com.caih.kinggrid_lib.view.ConstantValue;
import com.caih.kinggrid_lib.view.sign.util.BitmapUtil;
import com.caih.kinggrid_lib.view.sign.util.DisplayUtil;
import com.caih.kinggrid_lib.view.sign.util.SignUtils;
import com.caih.kinggrid_lib.view.sign.util.StatusBarCompat;
import com.ebensz.eink.R;
import com.kinggrid.iapppdf.ui.viewer.PDFHandWriteView;

import java.util.HashMap;

public class AddSignActivity extends BaseActivity implements View.OnClickListener {


    private RelativeLayout rlMenu;
    private RelativeLayout rlS;
    private RelativeLayout rlM;
    private RelativeLayout rlL;
    private RelativeLayout rlColor1;
    private RelativeLayout rlColor2;
    private RelativeLayout rlColor3;

    private TextView btnOk;
    private TextView btnCancel;
    private TextView btnRemove;

    private ProgressDialog mSaveProgressDlg;

    private RelativeLayout rlBorder;
    private PDFHandWriteView mPaintView;
    private float mWidth;
    private float mHeight;
    private float widthRate = 1.0f;
    private float heightRate = 1.0f;

    public static final int PEN_SIZE_SMALL = 22;
    public static final int PEN_SIZE_MEDIUM = 32;
    public static final int PEN_SIZE_LARGE = 42;

    public static final int PEN_COLOR_RED = Color.parseColor("#ff0000");
    public static final int PEN_COLOR_BLUE = Color.parseColor("#13227a");
    public static final int PEN_COLOR_BLACK = Color.parseColor("#000000");

    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;

    private int curPenSize = PEN_SIZE_SMALL;
    private int curColor = PEN_COLOR_BLACK;

    public static final float RATIO = 1.5f;  // 宽:高

    private String mSavePath;
    private String mSaveTempPath;
    private String cookie;

    private Bitmap signBm;
    private String envServer;

    /**
     * 过期时间 2020-12-30
     */
    private String copyRight = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU3MLqfdggb/D24BVZAYtYNPp4ry9vCZtM8/v6p/IOjGWKVFihhgv7gZfkpfJMSm/MmYngL2kv2tNxddviuwBCLi6BI6KYwFlcBYnKg15z7MpV/JU4nGlasHknbqsp61xYeZyZT2umSeN/3tBNrXvDACvANk6qiXn/OBCU3QRIhmiAc/5/+UjdElADYFbJfzSv3HsWKFjW+Qmsa7pI26WcFMstCvrboxvznvW9K4CSNvk29q6P0lp+QMvtZ8FsQQ1fAzNTOulVyP8KCTl7pA3dlibRxPkJX9ukEp45UOJrgyOLI1yA5ecrXwJ2HDokyY91EzeCBkqp0V3QDJtH3vo+lHuHR8z0bXnNyxWpWpFFdE88=";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initView();
        initEvent();
        initDefaultValue();
    }

    private void initView() {
        rlMenu = findViewById(R.id.rlMenu);
        rlS = findViewById(R.id.rlS);
        rlM = findViewById(R.id.rlM);
        rlL = findViewById(R.id.rlL);
        rlColor1 = findViewById(R.id.rlColor1);
        rlColor2 = findViewById(R.id.rlColor2);
        rlColor3 = findViewById(R.id.rlColor3);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        btnRemove = findViewById(R.id.btnRemove);
        mPaintView = findViewById(R.id.paint_view);
        rlBorder = findViewById(R.id.rlBorder);
        rlMenu.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rlMenu.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                mWidth = getResizeWidth();
//                mHeight = getResizeHeight();
                resizePaintView();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlBorder.getLayoutParams();
                layoutParams.width = (int)mWidth;
                layoutParams.height = (int)mHeight;
                rlBorder.setLayoutParams(layoutParams);
                Log.i(TAG, "initDefaultValue mWidth = "+mWidth+" mHeight = "+mHeight);
                int offset = DisplayUtil.dip2px(getApplicationContext(), 2);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPaintView.getLayoutParams();
                params.width = (int)(mWidth-(offset*1.5f));
                params.height = (int)(mHeight-offset);
                mPaintView.setLayoutParams(params);
                mPaintView.setCopyRight(AddSignActivity.this, copyRight);
                float penWidth = curPenSize*1f;
                Log.i(TAG, "resetPenSize "+penWidth);
                mPaintView.setPenInfo(penWidth, curColor, PDFHandWriteView.TYPE_BALLPEN);
//                mPaintView.init((int) (mWidth-(offset*1.5f)), (int) mHeight-offset);
//                mPaintView.setPaintColor(curColor);
//                mPaintView.setPaintWidth(curPenSize);
            }
        });
    }

    private void initEvent() {
        rlS.setOnClickListener(this);
        rlM.setOnClickListener(this);
        rlL.setOnClickListener(this);
        rlColor1.setOnClickListener(this);
        rlColor2.setOnClickListener(this);
        rlColor3.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        mPaintView.setBackgroundColor(Color.WHITE);
//        mPaintView.setStepCallback(this);
    }

    private void initDefaultValue() {
        mSavePath = SignUtils.getSignPath(getApplicationContext());
        mSaveTempPath = SignUtils.getSignTempPath(getApplicationContext());
        cookie = getIntent().getStringExtra("cookie");
        envServer = getIntent().getStringExtra("envServer");
    }


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.rlS) {
            resetPenSize(PEN_SIZE_SMALL);
        }else if(view.getId()==R.id.rlM) {
            resetPenSize(PEN_SIZE_MEDIUM);
        }else if(view.getId()==R.id.rlL){
            resetPenSize(PEN_SIZE_LARGE);
        }else if(view.getId()==R.id.rlColor1){
            resetPenColor(PEN_COLOR_RED);
        }else if(view.getId()==R.id.rlColor2){
            resetPenColor(PEN_COLOR_BLUE);
        }else if(view.getId()==R.id.rlColor3){
            resetPenColor(PEN_COLOR_BLACK);
        }else if(view.getId()==R.id.btnOk){
            save();
        }else if(view.getId()== R.id.btnCancel){
            finish();
        }else if(view.getId()==R.id.btnRemove) {
            mPaintView.doClearHandwriteInfo();
        }
    }

    private void resetPenSize(int size){
        if(curPenSize == size){
            return;
        }
        curPenSize = size;
        float penWidth = curPenSize*1f;
        Log.i(TAG, "resetPenSize "+penWidth);
        mPaintView.setPenInfo(penWidth, curColor, PDFHandWriteView.TYPE_BALLPEN);
        if(curPenSize == PEN_SIZE_SMALL){
            rlS.setBackgroundResource(R.drawable.sign_sitting_seleted);
            rlM.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlL.setBackgroundResource(R.drawable.sign_sitting_unselet);
        }else if(curPenSize == PEN_SIZE_MEDIUM){
            rlS.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlM.setBackgroundResource(R.drawable.sign_sitting_seleted);
            rlL.setBackgroundResource(R.drawable.sign_sitting_unselet);
        }else if(curPenSize == PEN_SIZE_LARGE){
            rlS.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlM.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlL.setBackgroundResource(R.drawable.sign_sitting_seleted);
        }
    }

    private void resetPenColor(int color){
        if(curColor == color){
            return;
        }
        curColor = color;
        float penWidth = curPenSize*1f;
        Log.i(TAG, "resetPenSize "+penWidth);
        mPaintView.setPenInfo(penWidth, curColor, PDFHandWriteView.TYPE_BALLPEN);
        if(curColor == PEN_COLOR_RED){
            rlColor1.setBackgroundResource(R.drawable.sign_sitting_seleted);
            rlColor2.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlColor3.setBackgroundResource(R.drawable.sign_sitting_unselet);
        }else if(curColor == PEN_COLOR_BLUE){
            rlColor1.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlColor2.setBackgroundResource(R.drawable.sign_sitting_seleted);
            rlColor3.setBackgroundResource(R.drawable.sign_sitting_unselet);
        }else if(curColor == PEN_COLOR_BLACK){
            rlColor1.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlColor2.setBackgroundResource(R.drawable.sign_sitting_unselet);
            rlColor3.setBackgroundResource(R.drawable.sign_sitting_seleted);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAVE_FAILED:
                    mSaveProgressDlg.dismiss();
                    toast("保存失败");
                    break;
                case MSG_SAVE_SUCCESS:
                    mSaveProgressDlg.dismiss();
                    toast("保存成功");
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存
     */
    private void save() {
        if (mPaintView.isEmpty()) {
            toast("没有写入任何文字");
            return;
        }
        //先检查是否有存储权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            toast("没有读写存储的权限");
            return;
        }
        if (mSaveProgressDlg == null) {
            initSaveProgressDlg();
        }
        mSaveProgressDlg.show();
        new Thread(() -> {
            try {
                RectF rectF = mPaintView.getAdjustiveCoordinatesRect(1);
                Log.i(TAG, "mPaintView "+mPaintView.getWidth()+" "+mPaintView.getHeight());
                Log.i(TAG, "rectF "+rectF.width()+" "+rectF.height()+" left = "+rectF.left+" right "+rectF.right+" top "+rectF.top+" bottom "+rectF.bottom);
                signBm = mPaintView.exportToBitmapArea(rectF);
                // 图片有白色的底色 需要将背景转换为透明 并保存为PNG格式
                signBm = BitmapUtil.white2transparent(signBm);
                if (signBm == null) {
                    mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                    return;
                }
                Log.i(TAG, "result save width:"+signBm.getWidth()+" height:"+signBm.getHeight());
                BitmapUtil.saveBitmap2File(mSaveTempPath, signBm);
                uploadSign();
            } catch (Exception e) {

            }
        }).start();

    }

    private void uploadSign(){
        String url = API.getAPI(envServer, API.API_UPLOAD_SIGN_TO_DB);
        HashMap<String, Object> params = new HashMap<>();
        params.put("busiType", "signature");
        params.put("filename", ConstantValue.SIGN_FILE_NAME);
        uploadForUnpdateFile(url, mSaveTempPath, ConstantValue.SIGN_FILE_NAME, cookie, params, new UploadListener(){

            @Override
            public void onFailure(Exception e) {
                mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
            }

            @Override
            public void onSuccess() {
                new Thread(() -> {
                    try {
                        BitmapUtil.saveBitmap2File(mSavePath, signBm);
                        mHandler.obtainMessage(MSG_SAVE_SUCCESS).sendToTarget();
                    } catch (Exception e) {
                        mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                    }
                }).start();

            }
        });
    }

    private void initSaveProgressDlg() {
        mSaveProgressDlg = new ProgressDialog(this);
        mSaveProgressDlg.setMessage("正在保存...");
        mSaveProgressDlg.setCancelable(false);
        mSaveProgressDlg.setCanceledOnTouchOutside(false);
    }

    // 根据屏幕尺寸 重置画板的宽高
    private void resizePaintView(){
        int width = getResizeWidth();
        int height = getResizeHeight();
        if(width*1f/height >RATIO){
            mHeight = height;
            mWidth = mHeight*RATIO;
        }else {
            mWidth = width;
            mHeight = mWidth/RATIO;
        }
    }

    /**
     * 获取画布默认宽度但是要减去右边菜单栏的宽度
     *
     * @return
     */
    private int getResizeWidth() {
        int menuWidth = rlMenu.getWidth();
        Log.i(TAG, "rlMenu width = "+menuWidth);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) ((dm.widthPixels-menuWidth) * widthRate);
    }

    /**
     * 获取画布默认高度 要减去顶部状态栏的高度
     *
     * @return
     */
    private int getResizeHeight() {
        int statusBarHeight = StatusBarCompat.getStatusBarHeight(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) ((dm.heightPixels - statusBarHeight) * heightRate);
    }
}
