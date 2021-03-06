package com.caih.kinggrid_lib.util;

import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by my on 2018-1-9.
 */

public class DownloadUtil {

    private String TAG = "DownloadUtil";
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    public void download(final String url, final String filePath, final long total, final OnDownloadListener listener) {
        download(url, "", filePath, total, listener);
    }
    /**
     * @param url 下载连接
     * @param listener 下载监听
     */
    public void download(final String url, String token ,final String filePath, final long total, final OnDownloadListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", token)
                .build();
        Log.i(TAG, "download headers = "+request.headers().toString());
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                // 下载失败
                listener.onDownloadFailed();
            }
            @Override
            public void onResponse(Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
//                String savePath = isExistDir(saveDir);
                try {
                    is = response.body().byteStream();
                    //long mTotal = Integer.valueOf(response.header("Content-Length"));
                    File file = new File(filePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        if(total!=0) {
                            int progress = (int) (sum * 1.0f / total * 100);
                            // 下载中

//                        Log.i(TAG, "下载进度："+progress);
                            listener.onDownloading(progress);
                        }
                    }
                    fos.flush();
                    // 下载完成
                    Log.i(TAG, "下载完成");
                    listener.onDownloadSuccess(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onDownloadFailed();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException
     * 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    /**
     * @param url
     * @return
     * 从下载连接中解析出文件名
     */
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(File str);

        /**
         * @param progress
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }
}
