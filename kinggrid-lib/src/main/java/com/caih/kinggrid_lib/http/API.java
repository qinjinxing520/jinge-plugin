package com.caih.kinggrid_lib.http;

/**
 * Author: wmy
 * Date: 2020/7/23 17:01
 */
public class API {

    public static final String GET_FILE_INFO= "/api-file/busi/file/log/searchFileList";
    public static final String UPLOAD_FILE = "/zuul/api-file/busi/file/operation/uploadForUnpdateFile";
    public static final String GET_SIGN_INFO = "/api-file/busi/file/goldgrid/getSignatureInfo";
    public static final String UPLOAD_SIGN_TO_DB = "/zuul/api-file/busi/file/operation/uploadFileToDB";
    public static final String GET_COMMON_ANNOT_INFO = "/api-process/busi/process/review/view/list";

    public static final int API_UPLOAD_FILE = 0;
    public static final int API_GET_FILE_INFO = 1;
    public static final int API_GET_SIGN_INFO = 2;
    public static final int API_UPLOAD_SIGN_TO_DB = 3;
    public static final int API_GET_COMMON_ANNOT_INFO = 4;

    public static String getAPI(String envServer,int type){
        String url  = "";
        switch (type){
            case API_UPLOAD_FILE:
                url =  envServer + UPLOAD_FILE;
                break;
            case API_GET_FILE_INFO:
                url = envServer + GET_FILE_INFO;
                break;
            case API_GET_SIGN_INFO:
                url =  envServer + GET_SIGN_INFO;
                break;
            case API_UPLOAD_SIGN_TO_DB:
                url = envServer + UPLOAD_SIGN_TO_DB;
                break;
            case API_GET_COMMON_ANNOT_INFO:
                url = envServer + GET_COMMON_ANNOT_INFO;
                break;
        }
        return url;
    }

}
