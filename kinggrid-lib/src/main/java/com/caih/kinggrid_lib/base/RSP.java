package com.caih.kinggrid_lib.base;

import com.alibaba.fastjson.JSONObject;

/**
 * Author: wmy
 * Date: 2020/7/29 9:15
 */
public class RSP<E> {

    public static String baseSuccessRsp(){
        JSONObject rsp = new JSONObject();
        rsp.put("code", "0000");
        rsp.put("codeMsg", "处理完成");
        return rsp.toJSONString();
    }

    public static String baseFailedRsp(){
        JSONObject rsp = new JSONObject();
        rsp.put("code", "2998");
        rsp.put("codeMsg", "未知错误");
        return rsp.toJSONString();
    }

    public static String baseFailedRsp(String codeMsg){
        JSONObject rsp = new JSONObject();
        rsp.put("code", "2998");
        rsp.put("codeMsg", codeMsg);
        return rsp.toJSONString();
    }

    public static String failedRsp(String code, String codeMsg){
        JSONObject rsp = new JSONObject();
        rsp.put("code", code);
        rsp.put("codeMsg", codeMsg);
        return rsp.toJSONString();
    }

    public static<E> String baseSuccessRsp(String codeMsg){
        return baseSuccessRsp("0000", codeMsg, null);
    }


    public static<E> String baseSuccessRsp(String code, String codeMsg, E e){
        JSONObject rsp = new JSONObject();
        rsp.put("code", code);
        rsp.put("codeMsg", codeMsg);
        if(e!=null) {
            rsp.put("data", e);
        }
        return rsp.toJSONString();
    }

}
