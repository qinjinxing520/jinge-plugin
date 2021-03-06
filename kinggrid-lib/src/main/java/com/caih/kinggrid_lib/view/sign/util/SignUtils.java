package com.caih.kinggrid_lib.view.sign.util;

import android.content.Context;

import com.caih.kinggrid_lib.view.ConstantValue;

public class SignUtils {

    public static final String getSignPath(Context context){
        return context.getExternalCacheDir().getPath()+ ConstantValue.SIGN_PIC_PATH_SUFFIX;
    }

    public static final String getSignTempPath(Context context){
        return context.getExternalCacheDir().getPath() + ConstantValue.SIGN_PIC_PATH_TEMP_SUFFIX;
    }

    public static final String getAnnotPath(Context context){
        return context.getExternalCacheDir().getPath()+ ConstantValue.ANNOT_PIC_PATH_SUFFIX;
    }

}
