package com.caih.kinggrid_lib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.awp.webkit.AwpEnvironment;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.kinggrid.iappoffice.IAppOffice;

import java.io.File;

/**
 * Created by myu on 2019/5/15.
 */

public class WpsOfficeManager {
    /**
     *
     * 金格操作方法,一进去就马上注册
     *
     */
    Context mContext;
    ReactContext mReactContxt;
    Activity mActivity;
    GetReceiver getRec;
    IAppOffice iappoffice;

    public WpsOfficeManager(Activity mActivity, ReactContext reactContext) {
        this.mActivity = mActivity;
        this.mReactContxt = reactContext;
        this.mContext = mActivity.getApplicationContext();
        registerIntentFilters();
        //iAppOffice();
    }

    private void registerIntentFilters() {
        IntentFilter backFilter = new IntentFilter();
        backFilter.addAction(IappOfficeTagManager.BROADCAST_BACK_DOWN);
        IntentFilter homeFilter = new IntentFilter();
        homeFilter.addAction(IappOfficeTagManager.BROADCAST_HOME_DOWN);
        IntentFilter saveFilter = new IntentFilter();
        saveFilter.addAction(IappOfficeTagManager.BROADCAST_FILE_SAVE);
        IntentFilter closeFilter = new IntentFilter();
        closeFilter.addAction(IappOfficeTagManager.BROADCAST_FILE_CLOSE);
        IntentFilter notFindFilter = new IntentFilter();
        notFindFilter.addAction("com.kinggrid.notfind.office");
        IntentFilter savePicFilter = new IntentFilter();
        savePicFilter.addAction(IappOfficeTagManager.BROADCAST_FILE_SAVE_PIC);
        IntentFilter showHandwriteFilter = new IntentFilter();
        showHandwriteFilter.addAction("com.kinggrid.iappoffice.showHandwrite");
        IntentFilter homekeyFilter = new IntentFilter();
        homekeyFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        IntentFilter saveAsPDFFilter = new IntentFilter();
        saveAsPDFFilter.addAction(IappOfficeTagManager.BROADCAST_FILE_SAVEAS_PDF);

        getRec = new GetReceiver();
        mActivity.registerReceiver(getRec, backFilter);
        mActivity.registerReceiver(getRec, homeFilter);
        mActivity.registerReceiver(getRec, saveFilter);
        mActivity.registerReceiver(getRec, closeFilter);
        mActivity.registerReceiver(getRec, notFindFilter);
        mActivity.registerReceiver(getRec, savePicFilter);
        mActivity.registerReceiver(getRec, showHandwriteFilter);
        mActivity.registerReceiver(getRec, homekeyFilter);
        mActivity.registerReceiver(getRec, saveAsPDFFilter);
    }



    //初始化iAppOffice
    private void iAppOffice(){
        //初始化
        iappoffice = new IAppOffice(mActivity);
        iappoffice.setCopyRight(IappOfficeTagManager.OFFICE_COPYRIGHT_VALUE);//KEY
        iappoffice.init();//1为成功 0为失败
    }

    public boolean isWPSInstalled() {
        return iappoffice.isWPSInstalled();
    }

    public void reInit(){
        iAppOffice();
    }

    //打开文档
    public void openDocument(Boolean isWeb,String weburl,String fild_type,String fileID,String userName,String localPaht,Boolean onleyread,Boolean isload, String extras) {
        if (iappoffice==null){
            iAppOffice();
        }

        if(iappoffice!=null&&iappoffice.isWPSInstalled()){
            String fileType=".doc";
            if(!isWeb){
                //如果是联网的
                if (fild_type.indexOf(".")!=-1) {
                    fileType = fild_type;
                }
                iappoffice.setWebUrl(weburl);
                iappoffice.setRecordId(fileID);
                iappoffice.setFileName(fileID + fileType);

            }else {
                if(localPaht.contains(".")){
                    fileType = localPaht.substring(localPaht.lastIndexOf("."));
                }else{
                    fileType = ".doc";
                }
                iappoffice.setFileName(localPaht);//保存打开路径
            }
            iappoffice.setUserName(userName);
            iappoffice.setFileType(fileType);
            iappoffice.setReadOnly(onleyread);//只读模式
            iappoffice.setUseMethod2(false);//用activity启动wps
            iappoffice.setIsReviseMode(isload);//设置文档打开时是否直接进入留痕模式。
            iappoffice.setParmsFor2015(extras);
            IappOfficeTagManager.setViewHidden(iappoffice);
            IappOfficeTagManager.setMenuHidden(iappoffice);
            setListener(false,fileID,fileType);
            iappoffice.setUsediWebOffice2015(true);
            iappoffice.appOpen(isWeb);// true为打开本地文档,false为打开服务器文档；
        }else{
            Toast.makeText(mContext, "请安装WPS专业版！", Toast.LENGTH_SHORT).show();
        }
    }
    //联网监听
    public void setListener(final Boolean isWeb, final String fileID, final String fileType) {
        // TODO Auto-generated method stub
        iappoffice.setOnUpnLoadStateListener(new IAppOffice.OnUpLoadStateListener() {//上传
            @Override
            public void success(String s) {

                mReactContxt.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("wpsCallBack", "success");

                //成功回到
              /*  if(PGPlugintest.Webview!=null&&!TextUtils.isEmpty(PGPlugintest.CallBackID)) {
                    JSONArray newArray = new JSONArray();
                    newArray.put("success_pull");
                    JSUtil.execCallback(PGPlugintest.Webview, PGPlugintest.CallBackID, newArray, JSUtil.OK, false);
                }*/
                //上传成功后删除
                if(!isWeb){
                    String filePath = IappOfficeTagManager.SDCARD_ROOT_PATH + "/localfiles/" + fileID + fileType;//localfiles
                    File file = new File(filePath);
                    if(file.exists()){
                        file.delete();
                    }
                }
            }

            @Override
            public void error() {
                // TODO Auto-generated method stub
                mReactContxt.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("wpsCallBack", "fail");
            }
        });
        iappoffice.setOnDownLoadStateListener(new IAppOffice.OnDownLoadStateListener() {//下载
            @Override
            public void success() {

            }

            @Override
            public void error() {

            }
        });
    }

    public IAppOffice backWps(){
        return iappoffice;
    }

    public void unOffice(){
        if(iappoffice!=null){
            iappoffice.unInit();
        }
        mActivity.unregisterReceiver(getRec);//注销广播
    }


    public class GetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IappOfficeTagManager.BROADCAST_BACK_DOWN.equals(intent.getAction())) {
                Log.d("bbb", "key back down");//按返回键
            } else if (IappOfficeTagManager.BROADCAST_HOME_DOWN.equals(intent.getAction())) {
                Log.d("bbb", "key home down");//home键
            } else if (IappOfficeTagManager.BROADCAST_FILE_SAVE.equals(intent.getAction())) {
                Log.d("bbb", "file save");//文件保存
            } else if (IappOfficeTagManager.BROADCAST_FILE_CLOSE.equals(intent.getAction())) {
                Log.d("bbb", "file close");//文件关闭
            } else if ("com.kinggrid.notfind.office".equals(intent.getAction())) {
                Log.d("bbb", "wps office not find");//找不到wps
            } else if (IappOfficeTagManager.BROADCAST_FILE_SAVE_PIC.equals(intent.getAction())) {
                Log.d("bbb", "office save pic over");//保存pic结束
            } else if (IappOfficeTagManager.BROADCAST_FILE_SAVEAS_PDF.equals(intent.getAction())) {
                Log.d("bbb", "office save as pdf over");//保存PDF结束
            }
        }
    }


}
