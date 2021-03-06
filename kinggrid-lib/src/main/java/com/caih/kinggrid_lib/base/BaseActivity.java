package com.caih.kinggrid_lib.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.caih.kinggrid_lib.util.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Author: wmy
 * Date: 2020/7/22 16:10
 */
public class BaseActivity extends Activity {

    protected String TAG = getClass().getSimpleName();
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void requestData(String url, JSONObject params, String token, RequestDataListener listener) {
        if(params == null){
            params = new JSONObject();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, params.toJSONString());
        Log.i(TAG, "requestData " +url+" "+ params.toJSONString() + " " + body.toString());
        Request request = new Request.Builder()//创建Request 对象。
                .url(url)
                .addHeader("Cookie", token)
                .post(body)//传递请求体
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(new Exception(e));
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String rsp = response.body().string();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSuccess(rsp);
                    }
                });
            }
        });
    }

    protected void uploadForUnpdateFile(String url, String filePath, String fileName, String token, HashMap<String, Object> params, UploadListener listener){
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath));
        requestBody.addFormDataPart("file", fileName, body);
        // 带参数
        if(params!=null) {
            for (String key : params.keySet()) {
                Object object = params.get(key);
                requestBody.addFormDataPart(key, object.toString());
            }
        }
        Log.i(TAG, "uploadForUnpdateFile token = "+token);
        Request request = new Request.Builder()
                .addHeader("Cookie", token)
                .url(url)
                .post(requestBody.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("Uploader", "onFailure " + e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(e);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i("Upload", "上传接口返回 " + str);
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

    protected void downloadFile(String downloadUrl, String token, String savePath, long fileSize, DownloadListener listener){
        DownloadUtil.get().download(downloadUrl, token, savePath, fileSize, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File str) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadSuccess(savePath);
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloading(progress);
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadFailed();
                    }
                });
            }
        });
    }

    protected void toast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public interface UploadListener{
        void onFailure(Exception e);
        void onSuccess();
    }

    public interface DownloadListener{
        void onDownloadSuccess(String filePath);
        void onDownloading(int progress);
        void onDownloadFailed();
    }


    public interface RequestDataListener{
        void onSuccess(String response);
        void onFailure(Exception e);
    }

}
