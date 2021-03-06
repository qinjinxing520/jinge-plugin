package com.caih.kinggrid_lib;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caih.kinggrid_lib.base.BaseActivity;
import com.caih.kinggrid_lib.http.API;
import com.caih.kinggrid_lib.http.BaseCallback;
import com.caih.kinggrid_lib.http.OkHttpHelper;
import com.caih.kinggrid_lib.util.DownloadUtil;
import com.caih.kinggrid_lib.util.SPUtils;
import com.caih.kinggrid_lib.util.Utils;
import com.caih.kinggrid_lib.view.AnnotUtil;
import com.caih.kinggrid_lib.view.ConstantValue;
import com.caih.kinggrid_lib.view.adapter.AnnotAdapter;
import com.caih.kinggrid_lib.view.dialog.ProgressLayout;
import com.caih.kinggrid_lib.view.dialog.SignConfigDialog;
import com.caih.kinggrid_lib.view.sign.util.BitmapUtil;
import com.caih.kinggrid_lib.view.sign.util.SignUtils;
import com.ebensz.eink.R;
import com.ebensz.eink.api.PennableLayout;
import com.istyle.pdf.core.SPAnnotation;
import com.istyle.pdf.core.SPConstant;
import com.istyle.pdf.viewer.OnPageChangeListener;
import com.istyle.pdf.viewer.OnPageLoadFinishListener;
import com.istyle.pdf.viewer.OnViewMovedListener;
import com.kinggrid.iapppdf.Annotation;
import com.kinggrid.iapppdf.listener.OnClickLocateViewOkBtnListener;
import com.kinggrid.iapppdf.listener.OnClickSignatureListener;
import com.kinggrid.iapppdf.listener.OnDeleteSignatureListener;
import com.kinggrid.iapppdf.listener.OnHandwritingSavedListener;
import com.kinggrid.iapppdf.listener.OnLongPressDeleteListener;
import com.kinggrid.iapppdf.listener.OnVerifySignatureCompleteListener;
import com.kinggrid.iapppdf.listener.OnViewTouchAddAnnotListener;
import com.kinggrid.iapppdf.listener.OnViewTouchShowAnnotListener;
import com.kinggrid.iapppdf.ui.viewer.IAppPDFView;
import com.kinggrid.iapppdf.ui.viewer.PDFHandWriteView;
import com.kinggrid.ireader.core.KgOfdSignature;

import org.json.JSONArray;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Author: wmy
 * Date: 2020/7/20 15:49
 */
public class KinggridActivity extends BaseActivity {

    private static String TAG = "KinggridActivity";

    public static boolean TAB_ENABLE = false; // 配置是否显示“切换编辑模式”的按钮 如该标记为false 则默认手笔分离

    private String downloadUrl = "";
    private String token = "";
    public static String userName = "";
    private String userId = "";
    private String copyRight = "";
    private String downloadPath = "";
    private String fileId = "";
    private String fileName = "";
    private String filePath = "";
    private String fileType = "";
    private String finalFileName = "";
    private String busiType = "";
    private String busiId = "";
    private String taskId = "";
    private String envServer = "";
    private long documentFileSize;
    private boolean editable = false;

    private LinearLayout llBack;
    private LinearLayout rlDelete;
    private LinearLayout rlTab;
    private LinearLayout rlConfig;
    private LinearLayout rlSave;
    private LinearLayout rlEdit;
    private LinearLayout rlKeyboard;
    private ImageView ivTab;
    private TextView tvTab;

    private LinearLayout llPrintAnnot;
    private TextView tvCommonAnnot;
    private EditText edAnnot;
    private TextView btnCancel;
    private TextView btnOk;

    private IAppPDFView mPdfView;
    private FrameLayout mFrameLayout;
    private LinearLayout mToolbar;
    private Context mContext;
    private View handwriteView_layout;
    private PDFHandWriteView full_handWriteView;
    private SignConfigDialog signConfigDialog;
    private boolean isSavedHandwriting = false;
    public enum DeviceType{PHONE,PAD}
    private DeviceType mDeviceType;
    private PennableLayout pennableLayout;
    private boolean isSupportEbenT7Mode = true;
    private boolean isVectorSign = true;
    public static float densityCoefficient;
    public static final DisplayMetrics DM = new DisplayMetrics();
    private AnnotUtil annotUtil;
    private AlertDialog.Builder mSignatureReportBuilder;
    //是否是打字机注释
    private boolean isFreeText = false;
    // 下载进度
    private ProgressLayout progressDialog;
    private int SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_SAVE;
    private boolean clickCancelSave = false;
    private boolean processingBackPress = false;
    private boolean hasUploadToServer = false;
    private boolean ebHandWritingMode = true; // 是否为E人E本编辑模式  如果false则是全文批注/手写模式
    private boolean saveDocumentFromTab = false;    // 通过点切换编辑模式保存的文档

    private boolean initSuccess = false;
    private boolean hasUploadFailed = false;    // 是否曾保存失败
    private boolean hasDeleteAnnotion = false;  // 是否长按删除批注的标记
    private boolean hasInsertAnnotion = false;  // 是否插入过文字批注图片的标记

    private String SIGN_PIC_PATH;

    private String[] commonAnnots = ConstantValue.DEFAULT_COMMON_ANNOTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //After LOLLIPOP not translucent status bar
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //Then call setStatusBarColor.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.primary));
        }
        setContentView(R.layout.book);
        mContext = this;
        SIGN_PIC_PATH = getExternalCacheDir().getPath()+ ConstantValue.SIGN_PIC_PATH_SUFFIX;
        ebHandWritingMode = SPUtils.getHandwriteMode(mContext) == ConstantValue.WRITE_MODE_PEN;
        getWindowManager().getDefaultDisplay().getMetrics(DM);
        densityCoefficient = DM.density;
        initView();
        initEvent();
        initPDFParams();
        initProgressDialog();
        initSign();
    }

    private void initView(){
        mDeviceType = getDeviceType(this);
        if(Utils.isERenEBen()){
            KinggridActivity.TAB_ENABLE = false;
        }else {
            KinggridActivity.TAB_ENABLE = true;
        }
        mFrameLayout = (FrameLayout) findViewById(R.id.book_frame);
        mToolbar = (LinearLayout) findViewById(R.id.toolbar);
        progressDialog = findViewById(R.id.loading);
        llBack = findViewById(R.id.llBack);
        rlDelete = findViewById(R.id.rlDelete);
        rlConfig = findViewById(R.id.rlConfig);
        rlTab = findViewById(R.id.rlTab);
        rlSave = findViewById(R.id.rlSave);
        rlEdit = findViewById(R.id.rlEdit);
        ivTab = findViewById(R.id.ivTab);
        tvTab = findViewById(R.id.tvTab);
        rlKeyboard = findViewById(R.id.rlKeyboard);
        llPrintAnnot = findViewById(R.id.llPrintAnnot);
        tvCommonAnnot = findViewById(R.id.tvCommonAnnot);
        edAnnot = findViewById(R.id.edAnnot);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk = findViewById(R.id.btnOk);
        if(mDeviceType == DeviceType.PAD){
            if(TAB_ENABLE) {
                rlTab.setVisibility(VISIBLE);
            }else {
                rlTab.setVisibility(GONE);
            }
            rlConfig.setVisibility(VISIBLE);
        }else {
            rlTab.setVisibility(GONE);
            rlConfig.setVisibility(GONE);
        }
        if(ebHandWritingMode){
            ivTab.setImageResource(R.drawable.hand_write);
            tvTab.setText(R.string.hand_write);
        }else {
            ivTab.setImageResource(R.drawable.pen_write);
            tvTab.setText(R.string.pen_write);
        }
    }

    private void initEvent(){
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        rlTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_TAB;
                if(ebHandWritingMode){  // 当前是E人E本模式 需要切换为手写 所以需要显示笔图标
                    SPUtils.setHandwriteMode(getApplicationContext(), ConstantValue.WRITE_MODE_HAND);
                    progressDialog.setMessage(getString(R.string.tabing_handwrite_mode));
                    progressDialog.show();
                    ivTab.setImageResource(R.drawable.pen_write);
                    tvTab.setText(R.string.pen_write);
                    // 先关闭E人E本
                    if(mDeviceType == DeviceType.PAD) {
                        mPdfView.doSaveEBHandwriteInfo(true);
                        pennableLayout.clear();
                        mPdfView.doCloseEBHandwriteView();
                    }
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handwriteView_layout = View.inflate(mContext,
                                            R.layout.signature_kinggrid_full, null);
                                    full_handWriteView = (PDFHandWriteView) handwriteView_layout
                                            .findViewById(R.id.v_canvas);
                                    full_handWriteView.setSupportEbenT7Mode(false);
                                    signConfigDialog = new SignConfigDialog(mContext,
                                            full_handWriteView, "penMaxSize", "penColor",
                                            "penType", 50);
                                    full_handWriteView.setPenInfo(
                                            signConfigDialog.getPenMaxSizeFromXML("penMaxSize"),
                                            signConfigDialog.getPenColorFromXML("penColor"),
                                            signConfigDialog.getPenTypeFromXML("penType"));
                                    mPdfView.openHandWriteAnnotation(handwriteView_layout, full_handWriteView);
                                    progressDialog.dismiss();
                                    toast(getString(R.string.open_handwrite_success));
                                }
                            });
                        }
                    }, 200);
                }else {
                    SPUtils.setHandwriteMode(getApplicationContext(), ConstantValue.WRITE_MODE_PEN);
                    ivTab.setImageResource(R.drawable.hand_write);
                    tvTab.setText(R.string.hand_write);
                    progressDialog.setMessage(getString(R.string.tabing_handwrite_mode));
                    progressDialog.show();
                    // 如果有修改 关闭全文批注
                    if(full_handWriteView.canSave()) {
                        mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
                    }else { // 没有修改 直接关闭
                        mPdfView.doCloseHandwriteInfo(handwriteView_layout, full_handWriteView);
                        //打开E人E本手写
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    openEBHandwriteView();
                                    progressDialog.dismiss();
                                    toast(getString(R.string.open_penwrite_success));
                                } catch (UnsatisfiedLinkError e) {
                                    progressDialog.dismiss();
                                    toast(getString(R.string.open_penwrite_failed));
                                }
                            }
                        }, 200);
                    }
                }
                ebHandWritingMode = !ebHandWritingMode;
            }
        });
        rlConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ebHandWritingMode) {
                    if (signConfigDialog == null) {
                        handwriteView_layout = View.inflate(mContext,
                                R.layout.signature_kinggrid_full, null);
                        full_handWriteView = (PDFHandWriteView) handwriteView_layout
                                .findViewById(R.id.v_canvas);
                        full_handWriteView.setSupportEbenT7Mode(false);
                        signConfigDialog = new SignConfigDialog(mContext,
                                full_handWriteView, "penMaxSize", "penColor",
                                "penType", 50);
                    }
                    signConfigDialog.showSettingWindow(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                }else{
                    showSettingWindow();
                }
            }
        });
        rlDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!initSuccess){
                    return;
                }
                // 点清除按钮 仅清除手写内容 不退出编辑状态
                if((mDeviceType == DeviceType.PAD && ebHandWritingMode)||!TAB_ENABLE) {
                    pennableLayout.clear();
                }else {
                    full_handWriteView.doClearHandwriteInfo();
                }
            }
        });
        rlSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_SAVE;
                save();
            }
        });
        rlEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHandWrite();
            }
        });
        rlKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSignature();
            }
        });
        tvCommonAnnot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommonAnnotDialog();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llPrintAnnot.setVisibility(GONE);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edAnnot.getText().toString();
                if(TextUtils.isEmpty(text)){
                    toast("没有批注内容");
                    return;
                }
                llPrintAnnot.setVisibility(GONE);
                // 隐藏键盘
                hideInputKeyboard();
                insertPrintAnnot(text);
            }
        });
    }

    private void save(){
        if((mDeviceType == DeviceType.PAD && ebHandWritingMode) || !TAB_ENABLE){
            progressDialog.setMessage(getString(R.string.saving_handwrite_info));
            progressDialog.show();
            int result = mPdfView.doSaveEBHandwriteInfo(true);
            Log.i(TAG, "开始保存");
            pennableLayout.clear();
            mPdfView.doCloseEBHandwriteView();
            if(result==0 || hasUploadFailed){
                if(saveDocumentFromTab){
                    saveDocumentFromTab = false;
                }
                mPdfView.saveDocument();
                uploadDocument();
            }
        }else {
            if(full_handWriteView.canSave() ){
                if (!isSavedHandwriting) {
                    progressDialog.setMessage(getString(R.string.saving_handwrite_info));
                    progressDialog.show();
                    isSavedHandwriting = true;
                    mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
                }else{
                    updateEditMenu(false);
                    processingBackPress = false;
                }
            } else if(clickCancelSave){
                clickCancelSave = false;
                progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                progressDialog.show();
                mPdfView.saveDocument();
                uploadDocument();
            }else if(!saveDocumentFromTab) {
                if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE || !hasUploadFailed) {
                    toast(getString(R.string.no_content_to_save));
                    processingBackPress = false;
                }else {
                    progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                    progressDialog.show();
                    mPdfView.saveDocument();
                    uploadDocument();
                }
            }else if(saveDocumentFromTab) {
                saveDocumentFromTab = false;
                progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                progressDialog.show();
                mPdfView.saveDocument();
                uploadDocument();
            }
        }
    }

    private void openEBHandwriteView(){
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                pennableLayout = mPdfView.openEBHandwriteView();
                int pencilSize = SPUtils.getPencilWidth(mContext);
                pennableLayout.setStrokeWidth(pencilSize);
            }
        });
    }

    private PopupWindow mSettingWindow;
    // 手写分离时的线粗细配置
    private void showSettingWindow(){
        LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.eb_pensetting, null);
        final TextView width_textView = (TextView) layout.findViewById(R.id.eb_width);
        final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.eb_seekset);
        final TextView lineshow = (TextView) layout.findViewById(R.id.eb_textshow);
        Button closeWindowButton = (Button) layout.findViewById(R.id.eb_btn_close_setting);
        seekBar.setMax(ConstantValue.DOCUMENT_PENCIL_MAX_WIDTH-SignConfigDialog.BALL_DEFAULT_PENSIZE);
        final int color = pennableLayout.getStrokeColor();
        final int penSize = (int)pennableLayout.getStrokeWidth();
        width_textView.setText("宽度:" +penSize);
        seekBar.setProgress(penSize-SignConfigDialog.BALL_DEFAULT_PENSIZE);
        lineshow.setBackgroundColor(color);
        closeWindowButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSettingWindow != null){
                    mSettingWindow.dismiss();
                    SPUtils.setPencilWidth(mContext, (int)pennableLayout.getStrokeWidth());
                }
            }
        });

        final RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) lineshow
                .getLayoutParams(); // 取控件textView当前的布局参数
        linearParams.height = (int) (penSize/1.5);
        lineshow.setLayoutParams(linearParams);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                linearParams.height = (int) ((progress)/1.5);
                lineshow.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件
                int penSize = progress+SignConfigDialog.BALL_DEFAULT_PENSIZE;
                pennableLayout.setStrokeWidth(penSize);
                width_textView.setText("宽度：" +penSize);
                SPUtils.setPencilWidth(mContext, penSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        mSettingWindow = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mSettingWindow.setFocusable(true);
        // 设置不允许在外点击消失
        mSettingWindow.setOutsideTouchable(false);
        mSettingWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    public void initProgressDialog(){
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
    }

    private void downloadDocument(){
//        String saveDir = Environment.getExternalStorageDirectory().getPath();
        String saveDir = getCacheDir().getPath();
        filePath = saveDir+"/"+finalFileName;
        Log.i(TAG, "开始下载 downloadUrl = "+downloadUrl);
        Log.i(TAG, "开始下载 filePath = "+filePath);
        downloadFile(downloadUrl, token, filePath, documentFileSize, new DownloadListener() {
            @Override
            public void onDownloadSuccess(String filePath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initDocument();
                        // 如果设备的PAD类型 直接进入编辑模式
                        if(mDeviceType == DeviceType.PAD) {
                            openHandWrite();
                        }
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                progressDialog.dismiss();
                toast(getString(R.string.load_failed));
            }
        });
    }

    private void initSign(){
        File file = new File(SIGN_PIC_PATH);
        if(file.exists()) {
            getCommonAnnot();
        }else {
            getSignInfo();
        }
    }

    private void getCommonAnnot(){
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        Log.i(TAG, "getCommonAnnot "+userId);
        String url = API.getAPI(envServer, API.API_GET_COMMON_ANNOT_INFO);
        OkHttpHelper.getinstance().postForm(url, token, params, new BaseCallback<String>() {
            @Override
            public void onRequestBefore() {

            }

            @Override
            public void onFailure(com.squareup.okhttp.Request request, Exception e) {
                e.printStackTrace();
                downloadDocument();
            }

            @Override
            public void onSuccess(com.squareup.okhttp.Response response, String res) {
                JSONObject rsp = JSON.parseObject(res);
                Log.i(TAG, "getCommonAnnot "+res);
                boolean success = rsp.getBoolean("success");
                String code = rsp.getString("code");
                String codeMsg = rsp.getString("codeMsg");
                if(success && "0000".equals(code)){
                    com.alibaba.fastjson.JSONArray annotArray = rsp.getJSONArray("data");
                    int length = annotArray.size();
                    if(length>0) {
                        commonAnnots = new String[length];
                        for (int i = 0; i < annotArray.size(); i++) {
                            JSONObject annotJson = annotArray.getJSONObject(i);
                            String viewContent = annotJson.getString("viewContent");
                            commonAnnots[i] = viewContent;
                        }
                    }
                }
                downloadDocument();
            }

            @Override
            public void onError(com.squareup.okhttp.Response response, int errorCode, Exception e) {
                e.printStackTrace();
                downloadDocument();
            }
        });
    }

    private void getSignInfo(){
        String url = API.getAPI(envServer, API.API_GET_SIGN_INFO);
        requestData(url, null, token, new RequestDataListener() {
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
                        downloadDocument();
                    }else {
                        String url = "";
                        if(signatureUrl.contains("api-file")) {
                            url = envServer + signatureUrl;
                        }else {
                            url = envServer +"/api-file"+ signatureUrl;
                        }
                        Log.i(TAG, "downloadSign "+url);
                        downloadSign(url);
                    }
                }else {
                    downloadDocument();
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                downloadDocument();
            }
        });
    }

    private void downloadSign(String signatureUrl){
        DownloadUtil.get().download(signatureUrl, token, SIGN_PIC_PATH, 0, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File str) {
                getCommonAnnot();
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                downloadDocument();
            }
        });
    }

    private void initDocument(){
        //授权码룺过期时间=2019-12-31
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPdfView = new IAppPDFView(this);
        mPdfView.setUserName(userName);
        mPdfView.setCopyRight(copyRight);
        //设置域高亮
//		mPdfView.setHighlightField(true);
        //设置允许域填充
//		mPdfView.setFormFillEnabled(true);
        //打开文档
        if(filePath.endsWith(".pdf")) {
            int result = mPdfView.openDocument(filePath);

            //如果文档设置了密码，则弹框让用户输入密码以打开文档
            if (result == SPConstant.E_NEEDSPASS) {
                handlePassword();
            }

            if (result == 0) {
                //绘制文档视图
                drawView();
            } else if (result == SPConstant.E_DAMAGED) {
                //文档损坏提示框
                showDocumentDamaged();
            }
        }
    }

    public void openHandWrite(){
        if(!editable){
            return;
        }
        updateEditMenu(true);
        // 如果是PAD 且是EB手写  又或者禁用了切换编辑模式 进入手笔分离模式
        if((mDeviceType == DeviceType.PAD && ebHandWritingMode) || !TAB_ENABLE){
            try {
                Log.i(TAG, "平板 进入手笔分离");
                ivTab.setImageResource(R.drawable.hand_write);
                tvTab.setText(R.string.hand_write);
                openEBHandwriteView();
                toast(getString(R.string.open_penwrite_success));
            } catch (UnsatisfiedLinkError e) {
                toast(getString(R.string.open_penwrite_failed));
            }
        } else{      // 进入全文批注
            Log.i(TAG, "手机 进入全文批注");
            ebHandWritingMode = false;
            ivTab.setImageResource(R.drawable.pen_write);
            tvTab.setText(R.string.pen_write);
            handwriteView_layout = View.inflate(mContext,
                    R.layout.signature_kinggrid_full, null);
            full_handWriteView = (PDFHandWriteView) handwriteView_layout
                    .findViewById(R.id.v_canvas);
            full_handWriteView.setSupportEbenT7Mode(false);
            signConfigDialog = new SignConfigDialog(mContext,
                    full_handWriteView, "penMaxSize", "penColor",
                    "penType", 50);
            full_handWriteView.setPenInfo(
                    signConfigDialog.getPenMaxSizeFromXML("penMaxSize"),
                    signConfigDialog.getPenColorFromXML("penColor"),
                    signConfigDialog.getPenTypeFromXML("penType"));
            mPdfView.openHandWriteAnnotation(handwriteView_layout, full_handWriteView);
        }
    }

    /**
     * 将文档视图添加到界面上，设置相关参数，监听事件等
     */
    public void drawView(){
        mFrameLayout.addView(mPdfView);
        mPdfView.setSupportEbenT7Mode(isSupportEbenT7Mode);
        mPdfView.setVectorSign(isVectorSign);
        //以下监听事件按实际需求添加
        mPdfView.setOnViewTouchAddAnnotListener(addAnnotListener);
        mPdfView.setOnViewTouchShowAnnotListener(showAnnotListener);
        mPdfView.setOnHandwritingSavedListener(handwritingSavedListener);
        mPdfView.setOnPageChangeListener(pageChangeListener);
        mPdfView.setOnClickSignatureListener(clickSignatureListener);
        mPdfView.setOnVerifySignatureCompleteListener(verifySignatureListener);
        mPdfView.setOnDeleteSignatureCompleteListener(deleteSignatureListener);
        mPdfView.setOnViewMovedListener(movedListener);
        mPdfView.setOnLongPressDeleteListener(longPressDeleteListener);
        if(!editable){
            mPdfView.hideLongPressRect(true);
        }
        mPdfView.setOnPageLoadFinishListener(new OnPageLoadFinishListener() {
            @Override
            public void onPageLoadFinish() {
                initSuccess = true;
                Log.i(TAG, "绘制文档完成");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
        //拖动框点击“确定”按钮回调监听
        mPdfView.setOnClickLocateViewOkBtnListener(new OnClickLocateViewOkBtnListener() {

            @Override
            public void clickOkBtn(int pageIndex, float x, float y, float width, float height) {
                String filePath = SignUtils.getAnnotPath(getApplicationContext());
                SimpleDateFormat df = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");// 设置日期格式
                String date = df.format(new Date());
                mPdfView.insertHandWriteAnnotation(pageIndex, x, y, width, height,
                        filePath, userName, date, 1, null, null, null);
                mPdfView.refreshPage(pageIndex);
                hasInsertAnnotion = true;
            }

            @Override
            public void clickCancelBtn(int i, float v, float v1, float v2, float v3) {

            }
        });
        this.setTitle("(" + 1 + "/" + mPdfView.getPageCount() + ")" + filePath);
        annotUtil = new AnnotUtil(mPdfView, userName, this);
    }


    /**
     * 输入文档密码Dialog
     */
    public void handlePassword() {
        final EditText passwordText = new EditText(this);
        passwordText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD );
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
        AlertDialog.Builder passwordEntryBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        passwordEntryBuilder.setTitle(R.string.enter_password);
        passwordEntryBuilder.setView(passwordText);
        AlertDialog passwordEntry = passwordEntryBuilder.create();
        passwordEntry.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String pass = passwordText.getText().toString();
                long lResult = mPdfView.openAuthenticateDocument(filePath, pass);
                if (lResult == 0) {
                    drawView();
                } else {
                    showOpenDocumentFailed();
                }
            }
        });
        passwordEntry.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        passwordEntry.show();
    }

    public void showDocumentDamaged() {
        TextView passwordErrorView = new TextView(mContext);
        passwordErrorView.setText(getString(R.string.error_file));
        passwordErrorView.setTextColor(Color.BLACK);
        passwordErrorView.setHeight(100);
        passwordErrorView.setGravity(Gravity.CENTER);
        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setView(passwordErrorView)
                .setTitle("错误")
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mPdfView = null;
                                finish();
                            }
                        }).show();

    }// end of showOpenDocumentFailed();

    public void showOpenDocumentFailed() {
        TextView passwordErrorView = new TextView(mContext);
        passwordErrorView.setText(R.string.password_error);
        passwordErrorView.setTextColor(Color.BLACK);
        passwordErrorView.setHeight(100);
        passwordErrorView.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setView(passwordErrorView)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                handlePassword();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mPdfView = null;
                                finish();
                            }
                        }).show();

    }// end of showOpenDocumentFailed();


    private void initPDFParams(){
        userName = getIntent().getStringExtra("userName");
        userId = getIntent().getStringExtra("userId");
        copyRight = getIntent().getStringExtra("copyRight");
        token = getIntent().getStringExtra("token");
        downloadPath = getIntent().getStringExtra("downloadPath");
        fileId = getIntent().getStringExtra("fileId");
        fileName = getIntent().getStringExtra("fileName");
        documentFileSize = getIntent().getLongExtra("fileSize", 0);
        fileType = getIntent().getStringExtra("fileType");
        busiType = getIntent().getStringExtra("busiType");
        busiId = getIntent().getStringExtra("busiId");
        envServer = getIntent().getStringExtra("envServer");
        editable = getIntent().getBooleanExtra("editable", false);
        taskId = getIntent().getStringExtra("taskId");
        if(!editable){
            rlSave.setVisibility(GONE);
            rlKeyboard.setVisibility(GONE);
            rlEdit.setVisibility(GONE);
            rlTab.setVisibility(GONE);
            rlConfig.setVisibility(GONE);
            rlDelete.setVisibility(GONE);
        }
        finalFileName = fileName + "."+fileType;
        downloadUrl = downloadPath+fileId;
    }

    private void updateEditMenu(boolean editing){
        if(editing){
            rlEdit.setVisibility(GONE);
            rlSave.setVisibility(VISIBLE);
            rlDelete.setVisibility(VISIBLE);
            if(TAB_ENABLE) {
                rlTab.setVisibility(VISIBLE);
            }
            if(mDeviceType == DeviceType.PAD) {
                rlConfig.setVisibility(VISIBLE);
            }
        }else {
            rlEdit.setVisibility(VISIBLE);
            rlTab.setVisibility(GONE);
            rlConfig.setVisibility(GONE);
            rlSave.setVisibility(GONE);
            rlDelete.setVisibility(GONE);
        }
    }

    /*
     * 显示保存提示框
     */
    private void showSaveDialog(){
        clickCancelSave = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                processingBackPress = false;
            }
        });
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_save_info);
        builder.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_BACK;
                // 保存文档后 上传
                if(rlSave.getVisibility() == GONE) {
                    progressDialog.setMessage(getString(R.string.saving_handwrite_info));
                    progressDialog.show();
                    mPdfView.saveDocument();
                    uploadDocument();
                }else {
                    save();
                }
            }
        });
        builder.setNeutralButton(R.string.btn_no_save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickCancelSave = true;
                dialog.dismiss();
                openHandWrite();
                processingBackPress = false;
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void uploadDocument(){
        String uploadUrl = API.getAPI(envServer, API.API_UPLOAD_FILE);
        Log.i(TAG, "uploadDocument "+uploadUrl);
        String fileNameWithType = fileName+"."+fileType;
        String name = fileNameWithType;
        fileNameWithType = getUrlCncode(fileNameWithType);
        HashMap<String, Object> params = new HashMap<>();
        params.put("busiType", busiType);
        params.put("name", name);
        params.put("id", fileId);
        params.put("busiId", busiId);
        params.put("taskId", taskId);
        uploadForUnpdateFile(uploadUrl, filePath, fileNameWithType, token, params, new UploadListener() {
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        processingBackPress = false;
                        hasUploadFailed = true;
                        toast(getString(R.string.save_handwrite_info_failed_with_network_error));
                        // 此时要重新进入编辑模式
                        openHandWrite();
                    }
                });
            }

            @Override
            public void onSuccess() {
                hasDeleteAnnotion = false;
                hasInsertAnnotion = false;
                hasUploadToServer = true;
                hasUploadFailed = false;
                progressDialog.dismiss();
                if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE){
                    updateEditMenu(false);
                }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_BACK){
                    finish();
                }

            }
        });
    }

    @Override
    public void finish() {
        if(hasUploadToServer) {
            Intent intent = new Intent();
            Log.i(TAG, "=================>sendBroadCast");
            intent.setAction(ConstantValue.FINISH_ACTION);
            sendBroadcast(intent);
        }
        super.finish();
    }

    private String getUrlCncode(String content){
        try {
            return URLEncoder.encode(content,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return content;
        }
    }



    private byte charToByte(char c){
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    Handler myHandler = new Handler();

    private OnViewTouchAddAnnotListener addAnnotListener = new OnViewTouchAddAnnotListener() {
        @Override
        public void onTouch(float x, float y) {
            if(mPdfView.isAnnotation){
                mPdfView.isAnnotation = false;
                //文字批注
                Log.v("tbz","textannot x = " + x + ", y = " + y);
                if (isFreeText) {
                    annotUtil.addFreeTextAnnot(x, y);
                } else {
                    annotUtil.addTextAnnot(x, y);
                }
            }
            if(mPdfView.isSound){
                mPdfView.isSound = false;
                //语音批注
                annotUtil.addSoundAnnot(x, y);
            }
        }
    };

    private OnViewMovedListener movedListener = new OnViewMovedListener() {

        @Override
        public void onViewZoomChanged(float arg0, float arg1) {
            System.out.println("====onViewMoved zoomchanged:" + arg0 +", " + arg1);
        }

        @Override
        public void onViewMoved(int arg0, int arg1) {
            System.out.println("====onViewMoved:" + arg0 +", " + arg1);
        }
    };

    private OnHandwritingSavedListener handwritingSavedListener = new OnHandwritingSavedListener() {

        @Override
        public void onHandwritingSaved(JSONArray arg0) {
            isSavedHandwriting = false;
            Log.i(TAG, "笔迹保存完成");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE){
                        if ((mDeviceType == DeviceType.PAD && ebHandWritingMode)||!TAB_ENABLE) {
                            if (mPdfView.isDocumentModified()) {
                                // 保存文档后 上传
                                int result = mPdfView.saveDocument();
                                if (result == 0 || hasUploadFailed) {
                                    uploadDocument();
                                } else if(result == 10){
                                    progressDialog.dismiss();
                                    toast(getString(R.string.no_content_to_save));
                                }else {
                                    progressDialog.dismiss();
                                    Log.i(TAG, "保存失败 result = " + result);
                                    toast(getString(R.string.save_handwrite_info_failed));
                                }
                            } else {
                                progressDialog.dismiss();
                                updateEditMenu(false);
                            }
                        } else {
                            // 保存文档后 上传
                            int result = mPdfView.saveDocument();
                            if (result == 0 || hasUploadFailed) {
                                uploadDocument();
                            } else if(result == 10){
                                if(saveDocumentFromTab){
                                    saveDocumentFromTab = false;
                                    uploadDocument();
                                }else {
                                    progressDialog.dismiss();
                                    toast(getString(R.string.no_content_to_save));
                                }
                            }else {
                                progressDialog.dismiss();
                                Log.i(TAG, "保存失败 result = " + result);
                                toast(getString(R.string.save_handwrite_info_failed));
                            }
                        }

                    }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_BACK){
                        progressDialog.dismiss();
                        if(mPdfView!=null && !mPdfView.isDocumentModified()) {
                            finish();
                        }else if(hasDeleteAnnotion || hasInsertAnnotion){
                            progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                            progressDialog.show();
                            mPdfView.saveDocument();
                            uploadDocument();
                        }else {
                            showSaveDialog();
                        }
                    }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_TAB) {
                        //不处理
                        saveDocumentFromTab = true;
                        if(ebHandWritingMode){
                            mPdfView.doCloseHandwriteInfo(handwriteView_layout, full_handWriteView);
                            try {
                                openEBHandwriteView();
                                progressDialog.dismiss();
                                toast(getString(R.string.open_penwrite_success));
                            } catch (UnsatisfiedLinkError e) {
                                progressDialog.dismiss();
                                toast(getString(R.string.open_penwrite_failed));
                            }
                        }
                    }
                }
            });
        }
    };

    private OnViewTouchShowAnnotListener showAnnotListener = new OnViewTouchShowAnnotListener() {
        @Override
        public void onTouchTextAnnot(Annotation annot) {
            annotUtil.showTextAnnot(annot);
        }

        @Override
        public void onTouchSoundAnnot(Annotation annot) {
            annotUtil.showSoundAnnot(annot);
        }

        @Override
        public void onTouchFreeTextAnnot(Annotation arg0) {

        }
    };

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageChange(String arg0) {
            int pageIndex = Integer.parseInt(arg0) + 1;
            System.out.println("====demo pageIndex:" + pageIndex);
            KinggridActivity.this.setTitle("(" + pageIndex +"/" + mPdfView.getPageCount() + ")" +filePath);
        }
    };

    private OnClickSignatureListener clickSignatureListener = new OnClickSignatureListener() {

        @Override
        public void onClickSignature(SPAnnotation arg0) {
            //点击的是PDF的数字签名或电子签章
            RectF viewRect = mPdfView.getAnnotRectInScreen(arg0.getRect(), arg0.getPageIndex());
            addSignatureBtn(viewRect, arg0);
        }

        @Override
        public void onClickOFDSignature(KgOfdSignature arg0) {
            //点击的是ofd的电子签章
            long result = arg0.sigVerify(mPdfView.getOfdDocument().getDocumentHandle(), 0);
            if (result == 0) {
                toast("验章成功！");
            } else {
                toast("验章失败！");
            }
        }
    };

    private OnVerifySignatureCompleteListener verifySignatureListener = new OnVerifySignatureCompleteListener() {

        @Override
        public void onVerifySignature(boolean arg0, String arg1, int arg2) {
            if (arg0) {
                showSignatureDialog(arg1);
            } else {
                showSignatureDialog(null);
            }
        }
    };

    private OnDeleteSignatureListener deleteSignatureListener = new OnDeleteSignatureListener() {

        @Override
        public void onDeleteSignature(int arg0) {
            if (arg0 == 0) {
                toast("删除成功");
            }
        }
    };

    private void showSignatureDialog(String message){
        mSignatureReportBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        mSignatureReportBuilder.setTitle("验证结果");
        mSignatureReportBuilder
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
        AlertDialog report = mSignatureReportBuilder.create();
        if (message != null) {
            report.setMessage(message);
        } else {
            report.setMessage("验证文档无效，文档已被更改或损坏！");
        }

        report.show();
    }


    private void addSignatureBtn(RectF viewRect, final SPAnnotation annotation){
        final LinearLayout layout = new LinearLayout(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int paddingLeft = 0;
        if ((int)viewRect.right >= mPdfView.getWidth()) {
            paddingLeft = (int)viewRect.left;
        } else if ((int)viewRect.left <= 0) {
            paddingLeft = (int)viewRect.right;
        } else {
            paddingLeft = (int)viewRect.right;
        }
        layout.setPadding(paddingLeft, (int)viewRect.top + mToolbar.getHeight(), 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);
        Button verifyBtn = new Button(mContext);
        verifyBtn.setTag(1);
        verifyBtn.setText("验证");
        verifyBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button deleteBtn = new Button(mContext);
        deleteBtn.setText("删除");
        deleteBtn.setTag(2);
        deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(verifyBtn);
        layout.addView(deleteBtn);
        addContentView(layout, params);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPdfView.doVerifySignature(annotation);
                layout.setVisibility(GONE);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                mPdfView.doDeleteSignature(annotation);
//                layout.setVisibility(GONE);
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (layout.getVisibility() == VISIBLE) {
                    layout.setVisibility(GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 如果正在处理点击事件
        if(processingBackPress){
            return;
        }
        processingBackPress = true;
        if(hasUploadFailed){
            showSaveDialog();
            return;
        }
        if(hasDeleteAnnotion){
            showSaveDialog();
            return;
        }
        if(hasInsertAnnotion){
            showSaveDialog();
            return;
        }
        if(rlSave.getVisibility() == GONE){
            finish();
            return;
        }
        SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_BACK;
        progressDialog.setMessage(getString(R.string.checking_edit_content));
        progressDialog.show();
        if((mDeviceType == DeviceType.PAD && ebHandWritingMode)||!TAB_ENABLE) {
            int result = mPdfView.doSaveEBHandwriteInfo(true);
            if(pennableLayout!=null) {
                pennableLayout.clear();
            }
            if(result == 0){
                progressDialog.dismiss();
                if(mPdfView!=null && !mPdfView.isDocumentModified()) {
                    mPdfView.doCloseEBHandwriteView();
                    finish();
                }else {
                    showSaveDialog();
                }
            }
        }else {
            if(full_handWriteView.canSave()||clickCancelSave){
                mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
            }else if(saveDocumentFromTab){
                // 初始化-从手笔分离切换至手写 需要判断文档是否有修改
                saveDocumentFromTab = false;
                progressDialog.dismiss();
                if(mPdfView!=null && !mPdfView.isDocumentModified()) {
                    processingBackPress = false;
                    finish();
                }else {
                    showSaveDialog();
                }
            }else {
                progressDialog.dismiss();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (this.isFinishing()) {
            if (mPdfView != null) {
                mPdfView.closeDocument();
                mPdfView = null;
            }
        }
    }

    private OnLongPressDeleteListener longPressDeleteListener = new OnLongPressDeleteListener() {
        @Override
        public void onDelete(Annotation annotation) {
            hasDeleteAnnotion = true;
        }
    };

    private DeviceType getDeviceType(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);

        // 大于7尺寸则为Pad
        if (screenInches >= 7.0) {
            return DeviceType.PAD;
        }
        return DeviceType.PHONE;
    }

    /**
     * 检查是否有本地签名
     */
    private void checkSignature(){
        String signPath = mContext.getExternalCacheDir().getPath()+ConstantValue.SIGN_PIC_PATH_SUFFIX;
        File file = new File(signPath);
        if(file.exists()){
            // 显示文本批注
            showPrintAnnotateDialog();
        }else {
            showNoSignatureDialog();
        }
    }

    /**
     * 显示文本批注框
     */
    private void showPrintAnnotateDialog(){
        llPrintAnnot.setVisibility(VISIBLE);
    }

    /**
     * 显示常用的批注
     */
    private void showCommonAnnotDialog(){
        View view = View.inflate(mContext, R.layout.common_annot_listview, null);
        ListView lvCommonAnnot = view.findViewById(R.id.lvCommonAnnot);
        AnnotAdapter annotAdapter = new AnnotAdapter(getApplicationContext(), commonAnnots);
        lvCommonAnnot.setAdapter(annotAdapter);
        lvCommonAnnot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onItemClick "+commonAnnots[i]);
                mSettingWindow.dismiss();
//                llPrintAnnot.setVisibility(GONE);
                edAnnot.setText(commonAnnots[i]);
//                insertPrintAnnot(AnnotAdapter.commonAnnots[i]);
            }
        });
        mSettingWindow = new PopupWindow(view,  ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mSettingWindow.setFocusable(true);
        // 设置不允许在外点击消失
        mSettingWindow.setOutsideTouchable(false);
        int [] locations = new int[2];
        tvCommonAnnot.getLocationOnScreen(locations);
        mSettingWindow.showAtLocation(tvCommonAnnot, Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, -locations[1]);
    }

    /**
     * 将输入的文字批注转换为图片 插入到PDF中 图片长度定为屏幕宽度的 4：9
     * 一行最多16个字 正常时8个字 每个字20dp
     * @param text
     */
    private void insertPrintAnnot(final String text){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap annotBm = BitmapUtil.text2AnnotBitmap(text, mContext);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "annotBm " +annotBm.getWidth()+" "+annotBm.getHeight());
                        mPdfView.post(new Runnable() {
                            @Override
                            public void run() {
                                mPdfView.openLocateSignature(annotBm, 0, Color.parseColor("#fff2cc"), true);
                                mPdfView.requestLayout();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void showNoSignatureDialog(){
        AlertDialog signatureDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("当前没有签名，需要先在签名管理中录入签名")
                .setNegativeButton("取消", null)
                .setPositiveButton("前往签名", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(mContext, MySignActivity.class);
                        intent.putExtra("cookie", token);
                        intent.putExtra("envServer", envServer);
                        startActivity(intent);
                    }
                });
        signatureDialog = builder.create();
        signatureDialog.show();
    }

    /**
     * 隐藏键盘
     * 弹窗弹出的时候把键盘隐藏掉
     */
    protected void hideInputKeyboard() {
        if(this.getCurrentFocus()!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
