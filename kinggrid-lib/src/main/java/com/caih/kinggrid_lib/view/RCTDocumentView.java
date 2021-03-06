package com.caih.kinggrid_lib.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caih.kinggrid_lib.KinggridActivity;
import com.caih.kinggrid_lib.MySignActivity;
import com.ebensz.eink.R;
import com.caih.kinggrid_lib.base.BaseActivity;
import com.caih.kinggrid_lib.base.RSP;
import com.caih.kinggrid_lib.http.API;
import com.caih.kinggrid_lib.http.BaseCallback;
import com.caih.kinggrid_lib.http.OkHttpHelper;
import com.caih.kinggrid_lib.util.DownloadUtil;
import com.caih.kinggrid_lib.util.SPUtils;
import com.caih.kinggrid_lib.util.Utils;
import com.caih.kinggrid_lib.view.adapter.AnnotAdapter;
import com.caih.kinggrid_lib.view.dialog.ProgressLayout;
import com.caih.kinggrid_lib.view.dialog.SignConfigDialog;
import com.caih.kinggrid_lib.view.sign.util.BitmapUtil;
import com.caih.kinggrid_lib.view.sign.util.SignUtils;
import com.ebensz.eink.api.PennableLayout;
import com.facebook.react.bridge.Callback;
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
import com.kinggrid.iapppdf.listener.OnInsertKGSealCompleteListener;
import com.kinggrid.iapppdf.listener.OnLongPressDeleteListener;
import com.kinggrid.iapppdf.listener.OnVerifySignatureCompleteListener;
import com.kinggrid.iapppdf.listener.OnViewTouchAddAnnotListener;
import com.kinggrid.iapppdf.listener.OnViewTouchShowAnnotListener;
import com.kinggrid.iapppdf.ui.viewer.IAppPDFView;
import com.kinggrid.iapppdf.ui.viewer.PDFHandWriteView;
import com.kinggrid.iapppdf.util.KinggridConstant;
import com.kinggrid.ireader.core.KgOfdSignature;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Author: wmy
 * Date: 2020/7/17 15:12
 */
public class RCTDocumentView extends LinearLayout {

    private String TAG = "RCTDocumentView";
    private Context mContext;
    private IAppPDFView mPdfView;
    private FrameLayout mFrameLayout;

    private static String mFilePath = "";
    public static String userName = "";
    public static String userId = "";
    public static String copyRight = "";

    private LinearLayout rlDelete;
    private LinearLayout rlTab;
    private LinearLayout rlSave;
    private LinearLayout rlConfig;
    private LinearLayout rlEdit;
    private LinearLayout rlKeyboard;
    private ImageView ivTab;
    private TextView tvTab;

    private LinearLayout llPrintAnnot;
    private TextView tvCommonAnnot;
    private EditText edAnnot;
    private TextView btnCancel;
    private TextView btnOk;

    private String downloadUrl = "";
    private String token = "";
    private String downloadPath = "";
    private String fileId = "";
    private String fileName = "";
    private long fileSize = 0l;
    private String filePath = "";
    private String fileType = "";
    private String finalFileName = "";
    private String busiType = "";
    private String busiId = "";
    private String taskId = "";
    private boolean editable = false;
    private String envServer = "";

    public static final DisplayMetrics DM = new DisplayMetrics();

    public enum DeviceType{PHONE,PAD}
    private DeviceType mDeviceType;
    private PennableLayout pennableLayout;
    private View handwriteView_layout;
    private PDFHandWriteView full_handWriteView;
    private SignConfigDialog signConfigDialog;

    public static float densityCoefficient;

    private boolean isSupportEbenT7Mode = true;
    private boolean isVectorSign = true;

    //是否是打字机注释
    private boolean isFreeText = false;

    private ProgressLayout progressDialog;
    private AnnotUtil annotUtil;
    private AlertDialog.Builder mSignatureReportBuilder;
    private boolean isSavedHandwriting = false;
    private boolean initSuccess = false;
    private Callback modifiedCallback;
    private Callback uploadCallback;
    private boolean saveAndUploading = false;
    private boolean clickCancelSave = false;
    private boolean processingBackPress = false;
    private int width = getWidth();
    private int height = getHeight();
    private boolean ebHandWritingMode = true; // 是否为E人E本编辑模式  如果false则是全文批注/手写模式
    private boolean saveDocumentFromTab = false;    // 通过点切换编辑模式保存的文档
    private boolean hasUploadFailed = false;    // 是否曾保存失败
    private boolean hasDeleteAnnotion = false;  // 是否长按删除批注的标记
    private boolean hasInsertAnnotion = false;  // 是否插入过文字批注图片的标记

    private int SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_SAVE;

    private String SIGN_PIC_PATH;
    private String[] commonAnnots = ConstantValue.DEFAULT_COMMON_ANNOTS;

    public RCTDocumentView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public RCTDocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public RCTDocumentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

//    private static RCTDocumentView ourInstance;
//
//    public static RCTDocumentView getInstance() {
//        return ourInstance;
//    }
//    public void createInstance() {
//        if(ourInstance == null) {
//            ourInstance = this;
//        }
//    }

    public void destoryInstance(){
//        ourInstance = null;
        pennableLayout = null;
        full_handWriteView = null;
        mPdfView.destroyDrawingCache();
        mPdfView = null;
    }

    private void init() {
        Log.i(TAG, "init");
        LayoutInflater.from(mContext).inflate(R.layout.book_view, this);
        mFrameLayout = (FrameLayout) findViewById(R.id.book_frame);
        SIGN_PIC_PATH = SignUtils.getSignPath(mContext);
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(DM);
        densityCoefficient = DM.density;
        mDeviceType = getDeviceType();
        if(Utils.isERenEBen()){
            KinggridActivity.TAB_ENABLE = false;
        }else {
            KinggridActivity.TAB_ENABLE = true;
        }
        mPdfView = new IAppPDFView(mContext);
        ebHandWritingMode = SPUtils.getHandwriteMode(mContext) == ConstantValue.WRITE_MODE_PEN;
        progressDialog = findViewById(R.id.loading);
        rlDelete = findViewById(R.id.rlDelete);
        rlTab = findViewById(R.id.rlTab);
        rlConfig = findViewById(R.id.rlConfig);
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
            if(KinggridActivity.TAB_ENABLE) {
                rlTab.setVisibility(View.VISIBLE);
            }else{
                rlTab.setVisibility(View.GONE);
            }
            rlConfig.setVisibility(VISIBLE);
        }else {
            rlConfig.setVisibility(GONE);
            rlTab.setVisibility(View.GONE);
        }
        if(ebHandWritingMode){
            ivTab.setImageResource(R.drawable.hand_write);
            tvTab.setText(R.string.hand_write);
        }else {
            ivTab.setImageResource(R.drawable.pen_write);
            tvTab.setText(R.string.pen_write);
        }
        rlConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
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
        rlDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点清除按钮 仅清除手写内容 不退出编辑状态
                requestLayout();
                if((mDeviceType == DeviceType.PAD && ebHandWritingMode)|| !KinggridActivity.TAB_ENABLE) {
                    pennableLayout.clear();
                }else {
                    full_handWriteView.doClearHandwriteInfo();
                }
            }
        });
        rlTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_TAB;
                if(ebHandWritingMode){  // 当前是E人E本模式 需要切换为手写 所以需要显示手写笔图标
                    SPUtils.setHandwriteMode(mContext, ConstantValue.WRITE_MODE_HAND);
                    progressDialog.setMessage(mContext.getString(R.string.tabing_handwrite_mode));
                    progressDialog.show();
                    ivTab.setImageResource(R.drawable.pen_write);
                    tvTab.setText(R.string.pen_write);
                    // 先关闭E人E本
                    if(mDeviceType == DeviceType.PAD) {
                        mPdfView.doSaveEBHandwriteInfo(true);
                        pennableLayout.clear();
                        mPdfView.doCloseEBHandwriteView();
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
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
                                    toast(R.string.open_handwrite_success);
                                }
                            });
                        }
                    }, 200);
                }else {
                    SPUtils.setHandwriteMode(mContext, ConstantValue.WRITE_MODE_PEN);
                    ivTab.setImageResource(R.drawable.hand_write);
                    tvTab.setText(R.string.hand_write);
                    progressDialog.setMessage(mContext.getString(R.string.tabing_handwrite_mode));
                    progressDialog.show();
                    // 如果有修改 关闭全文批注
                    if(full_handWriteView.canSave()) {
                        mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
                    }else { // 没有修改 直接关闭
                        mPdfView.doCloseHandwriteInfo(handwriteView_layout, full_handWriteView);
                        //打开E人E本手写
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.i(TAG, "275 isDocumentModified = "+mPdfView.isDocumentModified());
                                    openEBHandwriteView();
                                    progressDialog.dismiss();
                                    toast(R.string.open_penwrite_success);
                                } catch (UnsatisfiedLinkError e) {
                                    progressDialog.dismiss();
                                    toast(R.string.open_penwrite_failed);
                                }
                            }
                        }, 200);
                    }
                }
                ebHandWritingMode = !ebHandWritingMode;
            }
        });
        rlSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "点击保存");
                requestLayout();
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_SAVE;
                save();
            }
        });
        rlEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                openHandWrite();
            }
        });
        rlKeyboard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                checkSignature();
            }
        });
        tvCommonAnnot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                showCommonAnnotDialog();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                llPrintAnnot.setVisibility(GONE);
            }
        });
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLayout();
                String text = edAnnot.getText().toString();
                if(TextUtils.isEmpty(text)){
                    toast("没有批注内容");
                    return;
                }
                hideInputKeyboard();
                llPrintAnnot.setVisibility(GONE);
                insertPrintAnnot(text);
            }
        });
        initProgressDialog();
//        createInstance();
    }

    private void save(){
        if((mDeviceType == DeviceType.PAD && ebHandWritingMode) || !KinggridActivity.TAB_ENABLE){
            Log.i(TAG, "点击保存 294");
            progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
            progressDialog.show();
            int result = mPdfView.doSaveEBHandwriteInfo(true);
            Log.i(TAG, "点击保存 298 result = "+result);
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
            if(full_handWriteView!=null && full_handWriteView.canSave()){
                if (!isSavedHandwriting) {
                    progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
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
                    toast(R.string.no_content_to_save);
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

    private PopupWindow mSettingWindow;
    // 手写分离时的线粗细配置
    private void showSettingWindow(){
        LinearLayout layout = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.eb_pensetting, null);
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
        closeWindowButton.setOnClickListener(new OnClickListener() {

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
        mSettingWindow = new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mSettingWindow.setFocusable(true);
        // 设置不允许在外点击消失
        mSettingWindow.setOutsideTouchable(false);
        mSettingWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    public void initProgressDialog(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(mContext.getString(R.string.loading));
                progressDialog.show();
            }
        });
    }

    public void setParams(String params){
        JSONObject json = JSON.parseObject(params);
//        Log.i(TAG, "初始化金格编辑控件 "+params);
        JSONObject userInfo = json.getJSONObject("user");
//        Log.i(TAG, "初始化金格编辑控件 "+userInfo.toJSONString());
        if(json.getJSONObject("fileInfo") == null||json.getJSONObject("fileInfo").isEmpty()){
            hideLoading();
            return;
        }
        JSONObject fileInfo = json.getJSONObject("fileInfo");
//        Log.i(TAG, "初始化金格编辑控件 "+fileInfo.toJSONString());
        copyRight = json.getString("copyRight");
//        Log.i(TAG, "copyRight "+copyRight);
        token = json.getString("cookie");
        Log.i(TAG, "cookie "+token);
        downloadPath = json.getString("url");
        Log.i(TAG, "url "+downloadPath);
        fileName = fileInfo.getString("fileName");
        fileId = fileInfo.getString("id");
        fileType = fileInfo.getString("fileType");
        if(!"pdf".equals(fileType)){
            hideLoading();
            return;
        }
        taskId = json.getString("taskId");
        Log.i(TAG, "taskId "+taskId);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        width = (int)(json.getIntValue("width")* dm.density);
        height = (int)(json.getIntValue("height")*dm.density);
        Log.i(TAG, "width = "+width+" height = "+height+" density = "+dm.density);
        busiType = fileInfo.getString("busiType");
        busiId = fileInfo.getString("busiId");
        userName = userInfo.getString("userName");
        userId = userInfo.getString("id");
        envServer = json.getString("envServer");
        Log.i(TAG, "ENV_SERVER "+envServer);
        finalFileName = fileName + "."+fileType;
        downloadUrl = downloadPath+fileId;
        editable = json.getBoolean("editable");
        Log.i(TAG, "editable "+editable);
        if(!editable){
            rlSave.setVisibility(GONE);
            rlKeyboard.setVisibility(GONE);
            rlDelete.setVisibility(GONE);
            rlTab.setVisibility(View.GONE);
            rlConfig.setVisibility(GONE);
            rlEdit.setVisibility(GONE);
        }
        progressDialog.show();
        // 先检查签名 再下载文档
        initSign();
    }

    public void downloadDocument(){
//        String saveDir = Environment.getExternalStorageDirectory().getPath();
        String saveDir = mContext.getCacheDir().getPath();
        filePath = saveDir+"/"+finalFileName;
        Log.i(TAG, "开始下载 downloadUrl = "+downloadUrl);
        Log.i(TAG, "开始下载 filePath = "+filePath);
        DownloadUtil.get().download(downloadUrl, token, filePath, fileSize, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File str) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        initDocument();
                        // 如果设备的PAD类型 进入E人E本手写模式
                        if(mDeviceType == DeviceType.PAD) {
                            openHandWrite();
                        }
//                        hideLoading();
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                hideLoading();
                toast(R.string.load_failed);
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
        OkHttpHelper.getinstance().postWithCookie(url, token, null, new BaseCallback<String>() {
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
            public void onError(com.squareup.okhttp.Response response, int errorCode, Exception e) {
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

    public void initDocument(){
        mPdfView = new IAppPDFView(mContext);
        mPdfView.setUserName(userName);
        mPdfView.setCopyRight(copyRight);
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

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    public void openHandWrite(){
        if(!editable){
            return;
        }
        updateEditMenu(true);
        // 如果是PAD 或禁用了切换编辑模式 进入E本手写模式
        if((mDeviceType == DeviceType.PAD && ebHandWritingMode) || !KinggridActivity.TAB_ENABLE){
            Log.i(TAG, "平板 进入手笔分离");
            ivTab.setImageResource(R.drawable.hand_write);
            tvTab.setText(R.string.hand_write);
            initEb();
        }else{      // 如果是手机 进入全文批注
            Log.i(TAG, "手机 进入全文批注");
            ebHandWritingMode = false;
            ivTab.setImageResource(R.drawable.pen_write);
            tvTab.setText(R.string.pen_write);
            handwriteView_layout = View.inflate(mContext, R.layout.signature_kinggrid_full, null);
            full_handWriteView = (PDFHandWriteView) handwriteView_layout.findViewById(R.id.v_canvas);
            full_handWriteView.setSupportEbenT7Mode(false);
            signConfigDialog = new SignConfigDialog(mContext, full_handWriteView, "penMaxSize", "penColor",
                    "penType", 50);
            full_handWriteView.setPenInfo(
                    signConfigDialog.getPenMaxSizeFromXML("penMaxSize"),
                    signConfigDialog.getPenColorFromXML("penColor"),
                    signConfigDialog.getPenTypeFromXML("penType"));
            mPdfView.openHandWriteAnnotation(handwriteView_layout, full_handWriteView);
            handwriteView_layout.setVisibility(VISIBLE);
            full_handWriteView.setVisibility(VISIBLE);
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }
    };

    public void isDocumentModified(Callback callback){
        this.modifiedCallback = callback;
        if(processingBackPress){
            return;
        }
        processingBackPress = true;
        if(!initSuccess){
            processingBackPress = false;
            modifiedInvoke(true);
//            destoryInstance();
            return;
        }
        if(hasDeleteAnnotion){
            showSaveDialog();
            return;
        }
        if(hasUploadFailed){
            showSaveDialog();
            return;
        }
        if(hasInsertAnnotion){
            showSaveDialog();
            return;
        }
        if(rlSave.getVisibility() == GONE){
            processingBackPress = false;
            modifiedInvoke(true);
//            destoryInstance();
            return;
        }
        SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_BACK;
        // 如果已经编辑过 提示是否保存 未编辑 直接退出
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMessage(mContext.getString(R.string.checking_edit_content));
                    progressDialog.show();
                    if((mDeviceType == DeviceType.PAD && ebHandWritingMode) || !KinggridActivity.TAB_ENABLE) {
                        int result = mPdfView.doSaveEBHandwriteInfo(true);
                        pennableLayout.clear();
                        mPdfView.doCloseEBHandwriteView();
                        if(result == 0){
                            progressDialog.dismiss();
                            if(mPdfView!=null && !mPdfView.isDocumentModified()) {
                                processingBackPress = false;
                                modifiedInvoke(true);
//                                destoryInstance();
                            }else {
                                showSaveDialog();
                            }
                        }else{
                            
                        }
                    }else{
                        if((full_handWriteView.canSave())||clickCancelSave){
                            mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
                        } else if(saveDocumentFromTab){
                            // 初始化-从手笔分离切换至手写 需要判断文档是否有修改
                            saveDocumentFromTab = false;
                            if(mPdfView!=null && !mPdfView.isDocumentModified()) {
                                processingBackPress = false;
                                modifiedInvoke(true);
//                                destoryInstance();
                            }else {
                                showSaveDialog();
                            }
                        }else {
                            processingBackPress = false;
                            modifiedInvoke(true);
//                            destoryInstance();
                        }
                    }
                }
            });
        }catch (Exception e){
            modifiedInvoke(false);
            Log.i(TAG, "587 "+e.toString());
        }
    }

    /**
     * 自动保存并上传
     */
    public void autoSaveAndUpload(Callback callback){
        if(!initSuccess){
            callback.invoke(RSP.baseSuccessRsp("未打开文档"));
            return;
        }
        if(saveAndUploading){
            callback.invoke(RSP.baseFailedRsp("正在上传"));
            return;
        }
        SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_SUBMIT;
        this.uploadCallback = callback;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // 先关闭编辑状态
                if(rlSave.getVisibility() == VISIBLE){
                    if((mDeviceType == DeviceType.PAD && ebHandWritingMode)|| !KinggridActivity.TAB_ENABLE){
                        int result = mPdfView.doSaveEBHandwriteInfo(true);
                        pennableLayout.clear();
                        mPdfView.doCloseEBHandwriteView();
//                        if(mPdfView!=null && !mPdfView.isDocumentModified()) {
//                            uploadInvoke(RSP.baseSuccessRsp("没有修改，无需重新上传"));
//                        }else {
//                            mPdfView.saveDocument();
//                            uploadDocument();
//                        }
                        if(result == 0){
                            if(hasInsertAnnotion || hasDeleteAnnotion){
                                // 保存文档后 上传
                                mPdfView.saveDocument();
                                uploadDocument();
                            }else {
                                uploadInvoke(RSP.baseSuccessRsp("没有修改，无需重新上传"));
                                saveAndUploading = false;
                            }
                        }
                    }else {
                        if(full_handWriteView.canSave() && !isSavedHandwriting){
                            isSavedHandwriting = true;
                            mPdfView.doSaveHandwriteInfo(true, false, full_handWriteView);
                        }else if(hasInsertAnnotion || hasDeleteAnnotion){
                            // 保存文档后 上传
                            mPdfView.saveDocument();
                            uploadDocument();
                        }else {
                            uploadInvoke(RSP.baseSuccessRsp("没有修改，无需重新上传"));
                        }
                    }
                    updateEditMenu(false);
                }else if(hasInsertAnnotion || hasDeleteAnnotion){
                    // 保存文档后 上传
                    mPdfView.saveDocument();
                    uploadDocument();
                }else {
                    uploadInvoke(RSP.baseSuccessRsp("没有修改，无需重新上传"));
                }
            }
        });
    }

    private void updateEditMenu(boolean editing){
        if(editing){
            rlEdit.setVisibility(View.GONE);
            rlSave.setVisibility(View.VISIBLE);
            rlDelete.setVisibility(View.VISIBLE);
            if(KinggridActivity.TAB_ENABLE) {
                rlTab.setVisibility(View.VISIBLE);
            }
            if(mDeviceType == DeviceType.PAD) {
                rlConfig.setVisibility(VISIBLE);
            }
        }else {
            rlEdit.setVisibility(View.VISIBLE);
            rlTab.setVisibility(GONE);
            rlConfig.setVisibility(GONE);
            rlSave.setVisibility(View.GONE);
            rlDelete.setVisibility(View.GONE);
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
                clickCancelSave = false;
                // 保存文档后 上传
                SAVE_TYPE = ConstantValue.SAVE_TYPE_ON_CLICK_BACK;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 如果没有保存按钮 证明没有在编辑状态 直接保存上传
                        if(rlSave.getVisibility() == GONE) {
                            progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                            progressDialog.show();
                            mPdfView.saveDocument();
                            uploadDocument();
                        }else { // 否则 走保存流程
                            save();
                        }
                    }
                });
            }
        });
        builder.setNeutralButton(R.string.btn_no_save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                processingBackPress = false;
                modifiedInvoke(true);
//                destoryInstance();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickCancelSave = true;
                dialog.dismiss();
                processingBackPress = false;
                modifiedInvoke(false);
                openHandWrite();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void initEb(){
        try {
            openEBHandwriteView();
            toast(R.string.open_penwrite_success);
            requestLayout();
        } catch (UnsatisfiedLinkError e) {
            toast(R.string.open_penwrite_failed);
        }
    }

    private void openEBHandwriteView(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mPdfView==null){
                    return;
                }
                pennableLayout = mPdfView.openEBHandwriteView();
                int pencilSize = SPUtils.getPencilWidth(mContext);
                pennableLayout.setStrokeWidth(pencilSize);
            }
        });
    }

    public void preReInitDocument(){
        add = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFrameLayout.removeView(mPdfView);
                // 因为已经保存过才需要重新初始化 所以在这里默认是关闭编辑的状态
                updateEditMenu(false);
            }
        });
    }

    private boolean add = false;
    /**
     * 将文档视图添加到界面上，设置相关参数，监听事件等
     */
    public void drawView(){
        if(!add) {
            add = true;
            mFrameLayout.addView(mPdfView);
        }
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
                hideLoading();
            }
        });
        //拖动框点击“确定”按钮回调监听
        mPdfView.setOnClickLocateViewOkBtnListener(new OnClickLocateViewOkBtnListener() {

            @Override
            public void clickOkBtn(int pageIndex, float x, float y, float width, float height) {
                String filePath = SignUtils.getAnnotPath(mContext);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
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
        annotUtil = new AnnotUtil(mPdfView, userName, mContext);
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
        uploadForUnpdateFile(uploadUrl, filePath, fileNameWithType, token, params, new BaseActivity.UploadListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "uploadDocument onFailure "+e.toString());
                if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            processingBackPress = false;
                            hasUploadFailed = true;
                            toast(mContext.getString(R.string.save_handwrite_info_failed_with_network_error));
                            // 此时要重新进入编辑模式
                            openHandWrite();
                        }
                    });
                }else {
                    uploadInvoke(RSP.baseFailedRsp(e.toString()));
                    saveAndUploading = false;
                }
                modifiedInvoke(false);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "uploadDocument onSuccess ");
                hasDeleteAnnotion = false;
                hasInsertAnnotion = false;
                hasUploadFailed = false;
                if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE){
                    hideLoading();
                    updateEditMenu(false);
                }else {
                    if (SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_BACK) {
                        hideLoading();
                        toast(R.string.save_handwrite_info_success);
                        processingBackPress = false;
                        modifiedInvoke(true);
//                        destoryInstance();
                    } else {
                        uploadInvoke(RSP.baseSuccessRsp());
                        saveAndUploading = false;
//                        destoryInstance();
                    }
                }
            }
        });
    }

    private void modifiedInvoke(boolean result){
        if(modifiedCallback!=null){
            modifiedCallback.invoke(result);
            modifiedCallback = null;
        }
    }

    private void uploadInvoke(String rsp){
        if(uploadCallback!=null){
            uploadCallback.invoke(rsp);
            uploadCallback = null;
        }
    }

    private String getUrlCncode(String content){
        try {
            return URLEncoder.encode(content,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return content;
        }
    }

    protected void uploadForUnpdateFile(String url, String filePath, String fileName, String token, HashMap<String, Object> params, final BaseActivity.UploadListener listener){
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath));
        requestBody.addFormDataPart("file", fileName, body);
        // 带参数
        for (String key : params.keySet()) {
            Object object = params.get(key);
            if(object!=null) {
                requestBody.addFormDataPart(key, object.toString());
            }
        }
        Request request = new Request.Builder()
                .addHeader("Cookie", token)
                .url(url)
                .post(requestBody.build())
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(e);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String str = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject rsp = JSONObject.parseObject(str);
                            String code = rsp.getString("code");
                            String codeMsg = rsp.getString("codeMsg");
                            if ("0000".equals(code)) {
                                listener.onSuccess();
                            } else {
                                listener.onFailure(new Exception(codeMsg));
                            }
                        }catch (Exception e){
                            listener.onFailure(e);
                        }
                    }
                });
            }
        });

    }

    private OnViewTouchAddAnnotListener addAnnotListener = new OnViewTouchAddAnnotListener() {
        @Override
        public void onTouch(float x, float y) {
            Log.i(TAG, "addAnnotListener onTouch "+x+" "+y);
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
            Log.i(TAG, "笔迹保存完成 SAVE_TYPE = "+SAVE_TYPE);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SAVE){
                        if ((mDeviceType == DeviceType.PAD && ebHandWritingMode)|| !KinggridActivity.TAB_ENABLE) {
                            if (mPdfView.isDocumentModified()) {
                                // 保存文档后 上传
                                int result = mPdfView.saveDocument();
                                if (result == 0 || hasUploadFailed) {
                                    uploadDocument();
                                } else if(result == 10){
                                    progressDialog.dismiss();
                                    toast(R.string.no_content_to_save);
                                }else {
                                    progressDialog.dismiss();
                                    Log.i(TAG, "保存失败 result = " + result);
                                    toast(R.string.save_handwrite_info_failed);
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
                                    toast(R.string.no_content_to_save);
                                }
                            }else {
                                progressDialog.dismiss();
                                Log.i(TAG, "保存失败 result = " + result);
                                toast(R.string.save_handwrite_info_failed);
                            }
                        }

                    }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_BACK){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        if((mDeviceType == DeviceType.PAD && ebHandWritingMode)|| !KinggridActivity.TAB_ENABLE) {
                            if(mPdfView!=null && !mPdfView.isDocumentModified()&&!hasDeleteAnnotion) {
                                processingBackPress = false;
                                modifiedInvoke(true);
//                                destoryInstance();
                            }else if(hasDeleteAnnotion || hasInsertAnnotion){
                                progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                                progressDialog.show();
                                mPdfView.saveDocument();
                                uploadDocument();
                            }else {
                                showSaveDialog();
                            }
                        }else if(mDeviceType == DeviceType.PHONE || !ebHandWritingMode){
                            if(mPdfView!=null && !mPdfView.isDocumentModified() && !hasDeleteAnnotion) {
                                processingBackPress = false;
                                modifiedInvoke(true);
//                                destoryInstance();
                            }else if(hasDeleteAnnotion || hasInsertAnnotion){
                                progressDialog.setMessage(mContext.getString(R.string.saving_handwrite_info));
                                progressDialog.show();
                                mPdfView.saveDocument();
                                uploadDocument();
                            }else {
                                showSaveDialog();
                            }
                        }
                    }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_SUBMIT){
                        // 保存文档后 上传
                        int result = mPdfView.saveDocument();
                        if(result == 0) {
                            uploadDocument();
                        } else if(result == 10){
                            uploadInvoke(RSP.baseSuccessRsp("没有修改，无需重新上传"));
                            saveAndUploading = false;
//                            destoryInstance();
                        }else{
                            uploadInvoke(RSP.baseFailedRsp("保存失败"));
                            saveAndUploading = false;
//                            destoryInstance();
                        }
                    }else if(SAVE_TYPE == ConstantValue.SAVE_TYPE_ON_CLICK_TAB) {

                        progressDialog.dismiss();
                        // 不处理
                        saveDocumentFromTab = true;
                        if(ebHandWritingMode){
                            mPdfView.doCloseHandwriteInfo(handwriteView_layout, full_handWriteView);
                            try {
                                Log.i(TAG, "987 isDocumentModified = "+mPdfView.isDocumentModified());
                                openEBHandwriteView();
                                toast(R.string.open_penwrite_success);
                            } catch (UnsatisfiedLinkError e) {
                                progressDialog.dismiss();
                                toast(R.string.open_penwrite_failed);
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

    private OnDeleteSignatureListener deleteSignatureListener = new OnDeleteSignatureListener() {

        @Override
        public void onDeleteSignature(int arg0) {
            if (arg0 == 0) {
                toast("删除成功");
            }
        }
    };

    private OnLongPressDeleteListener longPressDeleteListener = new OnLongPressDeleteListener() {
        @Override
        public void onDelete(Annotation annotation) {
            Log.i(TAG, "onDelete "+annotation.getAnnotId());
            hasDeleteAnnotion = true;
        }
    };

    private DeviceType getDeviceType() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
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

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageChange(String arg0) {
            int pageIndex = Integer.parseInt(arg0) + 1;
            System.out.println("====demo pageIndex:" + pageIndex);
//            DocumentActivity.this.setTitle("(" + pageIndex +"/" + mPdfView.getPageCount() + ")" +mFilePath);
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

    private OnInsertKGSealCompleteListener insertKGSealListener = new OnInsertKGSealCompleteListener() {

        @Override
        public void onResult(boolean arg0, int arg1) {
            System.out.println("====insert success:" + arg0 + " resultCode:" + arg1);
            if (!arg0) {
                switch (arg1) {
                    case KinggridConstant.SEAL_ERROR_PAGENO_INVALID:
                        toast(R.string.seal_error_pageno_invalid);
                        break;
                    case KinggridConstant.SEAL_ERROR_LOCATION_AND_SIZE_IS_NULL:
                        toast(R.string.seal_error_location_and_size_is_null);
                        break;
                    case KinggridConstant.SEAL_ERROR_PAGE_NUM_NOT_MATCH_INFO_NUM:
                        toast(R.string.seal_error_page_num_not_match_info_num);
                        break;
                    case KinggridConstant.SEAL_ERROR_EXCEPTION:
                        toast(R.string.seal_error_exception);
                        break;
                    case KinggridConstant.SEAL_ERROR_IMAGE_PATH_NOT_EXIST:
                        toast(R.string.seal_error_image_path_not_exist);
                        break;
                    case KinggridConstant.SEAL_ERROR_PAGE_NOT_ENOUGH:
                        toast(R.string.seal_error_page_not_enough);
                        break;
                    case KinggridConstant.SEAL_ERROR_TEXT_NOT_FIND:
                        toast(R.string.seal_error_text_not_find);
                        break;
                    default:
                        break;
                }

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
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int paddingLeft = 0;
        if ((int)viewRect.right >= mPdfView.getWidth()) {
            paddingLeft = (int)viewRect.left;
        } else if ((int)viewRect.left <= 0) {
            paddingLeft = (int)viewRect.right;
        } else {
            paddingLeft = (int)viewRect.right;
        }
        layout.setPadding(paddingLeft, (int)viewRect.top + DensityUtil.dp2px(mContext, 56), 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);
        Button verifyBtn = new Button(mContext);
        verifyBtn.setTag(1);
        verifyBtn.setText("验证");
        verifyBtn.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button deleteBtn = new Button(mContext);
        deleteBtn.setText("删除");
        deleteBtn.setTag(2);
        deleteBtn.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(verifyBtn);
        layout.addView(deleteBtn);
        addView(layout, params);
        verifyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPdfView.doVerifySignature(annotation);
                layout.setVisibility(View.GONE);
            }
        });
        deleteBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                mPdfView.doDeleteSignature(annotation);
//                layout.setVisibility(View.GONE);
            }
        });
        layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (layout.getVisibility() == View.VISIBLE) {
                    layout.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 输入文档密码Dialog
     */
    public void handlePassword() {
        final EditText passwordText = new EditText(mContext);
        passwordText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD );
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
        AlertDialog.Builder passwordEntryBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        passwordEntryBuilder.setTitle(R.string.enter_password);
        passwordEntryBuilder.setView(passwordText);
        AlertDialog passwordEntry = passwordEntryBuilder.create();
//		passwordEntry.getWindow().setSoftInputMode(
//		    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        passwordEntry.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String pass = passwordText.getText().toString();
                long lResult = mPdfView.openAuthenticateDocument(mFilePath, pass);
                if (lResult == 0) {
                    drawView();
                } else {
                    showOpenDocumentFailed();
                }
            }
        });
        passwordEntry.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                finish();
            }
        });

        passwordEntry.show();
    }

    public void showOpenDocumentFailed() {
        TextView passwordErrorView = new TextView(mContext);
        passwordErrorView.setText(R.string.password_error);
        passwordErrorView.setTextColor(Color.BLACK);
        passwordErrorView.setHeight(100);
        passwordErrorView.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setView(passwordErrorView)
                .setPositiveButton(mContext.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                handlePassword();
                            }
                        })
                .setNegativeButton(mContext.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mPdfView = null;
                            }
                        }).show();

    }

    public void showDocumentDamaged() {
        TextView passwordErrorView = new TextView(mContext);
        passwordErrorView.setText(mContext.getString(R.string.error_file));
        passwordErrorView.setTextColor(Color.BLACK);
        passwordErrorView.setHeight(100);
        passwordErrorView.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                .setView(passwordErrorView)
                .setTitle("错误")
                .setPositiveButton(mContext.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mPdfView = null;
                            }
                        }).show();

    }

    /**
     * 检查是否有本地签名
     */
    private void checkSignature(){
        String signPath = SignUtils.getSignPath(mContext);
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
        AnnotAdapter annotAdapter = new AnnotAdapter(mContext, commonAnnots);
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
        Log.i(TAG, "width " +width);
        progressDialog.setMessage("处理中");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap annotBm = BitmapUtil.text2AnnotBitmap(text, mContext);
                Log.i(TAG, "annotBm " +annotBm.getWidth()+" "+annotBm.getHeight());
                mPdfView.post(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        mPdfView.openLocateSignature(annotBm, 0, Color.parseColor("#fff2cc"), false);
                        requestLayout();
                    }
                });
//                mPdfView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        requestLayout();
//                    }
//                }, 500);
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
                        mContext.startActivity(intent);
                    }
                });
        signatureDialog = builder.create();
        signatureDialog.show();
    }

    private void hideLoading(){
        progressDialog.dismiss();
    }

    private void toast(int resId){
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    private void toast(String msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 隐藏键盘
     * 弹窗弹出的时候把键盘隐藏掉
     */
    protected void hideInputKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
