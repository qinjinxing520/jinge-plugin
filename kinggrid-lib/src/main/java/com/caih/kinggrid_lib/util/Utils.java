package com.caih.kinggrid_lib.util;

import android.os.Build;

public class Utils {

    public static boolean isERenEBen(){
        if(Build.MODEL.toUpperCase().contains("ERENEBEN")||Build.BRAND.toUpperCase().contains("ERENEBEN")){
            return true;
        }
        return false;
    }

}
