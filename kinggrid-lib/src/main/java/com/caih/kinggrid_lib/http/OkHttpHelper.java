package com.caih.kinggrid_lib.http;

/**
 * Created by my on 2018-1-10.
 */

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class OkHttpHelper {

    /**
     * 采用单例模式使用OkHttpClient
     */
    private static OkHttpHelper mOkHttpHelperInstance;
    private static OkHttpClient mClientInstance;
    private Handler mHandler;
    private Gson mGson;

    /**
     * 单例模式，私有构造函数，构造函数里面进行一些初始化
     */
    private OkHttpHelper() {
        mClientInstance = new OkHttpClient();
        mClientInstance.setConnectTimeout(10, TimeUnit.SECONDS);
        mClientInstance.setReadTimeout(10, TimeUnit.SECONDS);
        mClientInstance.setWriteTimeout(30, TimeUnit.SECONDS);
        mGson = new Gson();

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取实例
     *
     * @return
     */
    public static OkHttpHelper getinstance() {

        if (mOkHttpHelperInstance == null) {

            synchronized (OkHttpHelper.class) {
                if (mOkHttpHelperInstance == null) {
                    mOkHttpHelperInstance = new OkHttpHelper();
                }
            }
        }
        return mOkHttpHelperInstance;
    }

    /**
     * 封装一个request方法，不管post或者get方法中都会用到
     */
    public void request(final Request request, final BaseCallback callback) {

        //在请求之前所做的事，比如弹出对话框等
        callback.onRequestBefore();

        mClientInstance.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //返回失败
                callbackFailure(request, callback, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    //返回成功回调
                    String resString = response.body().string();

                    if (callback.mType == String.class) {
                        //如果我们需要返回String类型
                        callbackSuccess(response, resString, callback);
                    } else {
                        //如果返回的是其他类型，则利用Gson去解析
                        try {
                            Object o = mGson.fromJson(resString, callback.mType);
                            callbackSuccess(response, o, callback);
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            callbackError(response, callback, e);
                        }
                    }

                } else {
                    //返回错误
                    callbackError(response, callback, null);
                }
            }
        });
    }

    /**
     * 在主线程中执行的回调
     *
     * @param response
     * @param
     * @param callback
     */
    private void callbackSuccess(final Response response, final Object o, final BaseCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(response, o);
            }
        });
    }

    /**
     * 在主线程中执行的回调
     * @param response
     * @param callback
     * @param e
     */
    private void callbackError(final Response response, final BaseCallback callback, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(response, response.code(), e);
            }
        });
    }

    /**
     * 在主线程中执行的回调
     * @param request
     * @param callback
     * @param e
     */
    private void callbackFailure(final Request request, final BaseCallback callback, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(request,e);
            }
        });
    }

    /**
     * 对外公开的get方法
     *
     * @param url
     * @param callback
     */
    public void get(String url, BaseCallback callback) {
        Request request = buildRequest(url, null, HttpMethodType.GET);
        request(request, callback);
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     * @param params
     * @param callback
     */
    public void post(String url, Map<String, String> params, BaseCallback callback) {
        Request request = buildRequest(url, params, HttpMethodType.POST);
        request(request, callback);
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     * @param params
     * @param callback
     */
    public void postWithCookie(String url, String cookie, Map<String, String> params, BaseCallback callback) {
        Request request = buildRequest(url, cookie, params, HttpMethodType.POST);
        request(request, callback);
    }

    /**
     * 对外公开的post方法
     *
     * @param url
     * @param params
     * @param callback
     */
    public void postForm(String url, String cookie, Map<String, String> params, BaseCallback callback) {
        Request request = buildFormRequest(url, cookie, params);
        request(request, callback);
    }

    /**
     * 构建请求对象
     *
     * @param url
     * @param params
     * @param type
     * @return
     */
    private Request buildRequest(String url, Map<String, String> params, HttpMethodType type) {
        return buildRequest(url, null, params, HttpMethodType.POST);
    }

    /**
     * 构建请求对象
     *
     * @param url
     * @param params
     * @param type
     * @return
     */
    private Request buildRequest(String url, String cookie, Map<String, String> params, HttpMethodType type) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (type == HttpMethodType.GET) {
            builder.get();
        } else if (type == HttpMethodType.POST) {
            if(!TextUtils.isEmpty(cookie)) {
                builder.addHeader("Cookie", cookie);
            }
            builder.addHeader("dataType","json");
            builder.addHeader("contentType","application/json;charset=UTF-8");
            builder.post(buildRequestBody(params));
        }
        return builder.build();
    }

    /**
     * 构建请求对象
     *
     * @param url
     * @param params
     * @return
     */
    private Request buildFormRequest(String url, String cookie, Map<String, String> params) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if(!TextUtils.isEmpty(cookie)) {
            builder.addHeader("Cookie", cookie);
        }
        builder.addHeader("contentType","application/json;multipart/form-data;charset=UTF-8");
        builder.post(buildFormBody(params));
        return builder.build();
    }

    private RequestBody buildFormBody(Map<String, String> params){
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entity : params.entrySet()) {
                builder.add(entity.getKey(), entity.getValue());
            }
        }
        RequestBody formBody = builder.build();
        return formBody;
    }

    /**
     * 通过Map的键值对构建请求对象的body
     *
     * @param params
     * @return
     */
    private RequestBody buildRequestBody(Map<String, String> params) {

        FormEncodingBuilder builder = new FormEncodingBuilder();
        JSONObject jsonObject = new JSONObject();
        if (params != null) {
            for (Map.Entry<String, String> entity : params.entrySet()) {
                builder.add(entity.getKey(), entity.getValue());
                try {
                    jsonObject.put(entity.getKey(),entity.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody requestBody = RequestBody.create(mediaType,jsonObject.toString());
        return requestBody;
    }

    /**
     * 这个枚举用于指明是哪一种提交方式
     */
    enum HttpMethodType {
        GET,
        POST
    }

}
