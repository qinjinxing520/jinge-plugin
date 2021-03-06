package com.caih.kinggrid_lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.kinggrid.iappoffice.IAppOffice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import cn.wps.moffice.client.ActionType;
import cn.wps.moffice.client.ViewType;

/**
 * Created by my on 2018-1-9.
 */

public class IappOfficeTagManager {
    //广播接收
    public final static String BROADCAST_BACK_DOWN = "com.kinggrid.iappoffice.back";
    public final static String BROADCAST_HOME_DOWN = "com.kinggrid.iappoffice.home";
    public final static String BROADCAST_FILE_SAVE = "com.kinggrid.iappoffice.save";
    public final static String BROADCAST_FILE_CLOSE = "com.kinggrid.iappoffice.close";
    public final static String BROADCAST_FILE_SAVE_PIC = "com.kinggrid.iappoffice.save.pic";
    public final static String BROADCAST_FILE_SAVEAS_PDF = "com.kinggrid.file.saveas.end";


    // SD卡根目录
    public static final String SDCARD_ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

    // 修改-by-wmy---2020.12.31--
    public static String OFFICE_COPYRIGHT_VALUE="SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU3MLqfdggb/D24BVZAYtYNPp4ry9vCZtM8/v6p/IOjGWKVFihhgv7gZfkpfJMSm/MmYngL2kv2tNxddviuwBCLi6BI6KYwFlcBYnKg15z7MpV/JU4nGlasHknbqsp61xYeZyZT2umSeN/3tBNrXvDACvANk6qiXn/OBCU3QRIhmiAc/5/+UjdElADYFbJfzSv3HsWKFjW+Qmsa7pI26WcFMstCvrboxvznvW9K4CSNvk29q6P0lp+QMvtZ8FsQQ1fAzNTOulVyP8KCTl7pA3dlibRxPkJX9ukEp45UOJrgyOLI1yA5ecrXwJ2HDokyY91EzeCBkqp0V3QDJtH3vo+lHuHR8z0bXnNyxWpWpFFdE88=";
    public static final String EXTENSION_PATH = SDCARD_ROOT_PATH + "/zhrd";


    public static void copyAssetsFileToSDCard(String fileName, String toFilePath,Context context)
            throws IOException {
        File file = new File(toFilePath);
        if (file.exists()) {
            return;
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(toFilePath);
        myInput =context.getAssets().open(fileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }


    /**
     * 控制wps界面工具栏面板，设置后会隐藏或禁止该按钮；
     */
    public static void setViewHidden(IAppOffice iappoffice) {
        ArrayList<ViewType> list = new ArrayList<ViewType>();
        // 分享菜单
        list.add(ViewType.VT_FILE_SHARE);
        // 预览菜单
        list.add(ViewType.VT_MENU_REVIEW);
        // 文件菜单
        list.add(ViewType.VT_REVIEW_MODIFY_USERNAME);
        // 网络模式
        list.add(ViewType.VT_REVIEW_MODIFY_USERNAME);
        list.add(ViewType.VT_REVIEW_ENTERREVISEMODE);
        list.add(ViewType.VT_REVIEW_EXITREVISEMODE);
        list.add(ViewType.VT_REVIEW_ACCEPTALLREVISIONS);
        list.add(ViewType.VT_REVIEW_REJECTALLREVISIONS);
        // 等等，根据需求控制是否显示或隐藏菜单；
//		list.add(ViewType.VT_MENU_EDIT);
//		list.add(ViewType.VT_MENU_FONT);
//		list.add(ViewType.VT_MENU_INSERT);
//		list.add(ViewType.VT_MENU_PARAGRAPH);
//		list.add(ViewType.VT_MENU_REVIEW);
//		list.add(ViewType.VT_MENU_SHAPE);
//		list.add(ViewType.VT_MENU_STYLE);
//		list.add(ViewType.VT_MENU_TABLE);
//		list.add(ViewType.VT_MENU_VIEW);
        iappoffice.setViewHiddenList(list);
    }

    /**
     * 控制wps编辑事件；设置后会隐藏该按钮或禁止该事件；
     */
    public static void  setMenuHidden(IAppOffice iappoffice) {

        ArrayList<ActionType> actionlist = new ArrayList<ActionType>();
        // 复制
//		actionlist.add(ActionType.AT_COPY);
//		// 剪切
//		actionlist.add(ActionType.AT_CUT);
        // 快速关闭修订（向右活动关闭修订面板）
        actionlist.add(ActionType.AT_QUICK_CLOSE_REVISEMODE);
        // 分享
        actionlist.add(ActionType.AT_SHARE);
//		// 粘贴
        actionlist.add(ActionType.AT_PASTE);
//		// 拼写检查
//		actionlist.add(ActionType.AT_SPELLCHECK);
//		// 限制修改他人修订（不允许修改其他人的修订内容）
//		actionlist.add(ActionType.AT_EDIT_REVISION);
        // 限制修改批注修订的作者名
        actionlist.add(ActionType.AT_CHANGE_COMMENT_USER);

        iappoffice.setMenuHiddenList(actionlist);
    }


    public static boolean checkNetworkConnection(Context context)
    {
        ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        int netType = info.getType();
        int netSubtype = info.getSubtype();


        NetworkInfo mMobileNetworkInfo = mConnectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   //获取移动网络


        if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
            return false;
        } else if (mMobileNetworkInfo!=null) {   //MOBILE
            return mMobileNetworkInfo.isAvailable();
        } else {
            return false;
        }
    }


}
