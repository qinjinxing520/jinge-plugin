package com.caih.kinggrid_lib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awp.webkit.AwpEnvironment;
import com.awp.webkit.AwpExtension;
import com.ebensz.eink.BuildConfig;
import com.ebensz.eink.R;

import java.lang.reflect.Field;

/**
 * Author: wmy
 * Date: 2020/9/30 15:18
 */
public class AWPBishengActivity extends Activity {

    private LinearLayout llBack;
    private TextView tvTitle;
    private ProgressBar progressBbar;
    private WebView webview;
//    private String url = "http://10.19.44.65/apps/editor/openPreview?data=eyJkb2MiOnsiY2FsbGJhY2siOiJodHRwOi8vMTAuMTkuNDQuNjY6MTAwMTAvYnVzaS9maWxlL2Jpc2hlbmcvc2F2ZUJhY2siLCJkb2NJZCI6IjAyOGMyNzk5ZmE0MDQxZTliZGFjNzk4NWUyMGY0YjdjIiwiZmV0Y2hVcmwiOiJodHRwOi8vMTAuMTkuNDQuNjY6MTAwMTAvYnVzaS9maWxlL29wZXJhdGlvbi9nZXRGaWxlRnJvbURCP2ZpbGVJbmZvSWQ9VF9GSUxFX0lORk9fMDE4RDhDNzAzQjUxNDlDQTg2NzdBREJGRTFCRTQ5QUIiLCJvcHRzIjp7InBkZl92aWV3ZXIiOmZhbHNlfSwidGl0bGUiOiLovabovobnrqHnkIbpnIDmsYLor7TmmI5WMS4wLnBkZiJ9LCJ1c2VyIjp7Im5pY2tOYW1lIjoi572X546W5Z2kIiwicHJpdmlsZWdlIjpbIkZJTEVfUkVBRCIsIkZJTEVfRE9XTkxPQUQiLCJGSUxFX1BSSU5UIl0sInVpZCI6IkUyRTYwQjhGRUY2RTRBNUM4M0U0OUUyMDg3RDNFRERBIn19&device=mobile";
    private String url = "";
    private String title = "";
    private String TAG = "BishengActivity";
    private boolean mIsWebContentsDebuggingEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bisheng_webview);
        AwpEnvironment.init(this, false);
        llBack = findViewById(R.id.llBack);
        tvTitle = findViewById(R.id.tvTitle);
        progressBbar = findViewById(R.id.progress_bar);
        webview = findViewById(R.id.webview);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        if(!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if(TextUtils.isEmpty(url)){
            return;
        }
        initWebView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllWebViewCallback();
        webview.destroy();
    }

    public void releaseAllWebViewCallback() {
        if (Build.VERSION.SDK_INT < 16) {
            try {
                Field field = WebView.class.getDeclaredField("mWebViewCore");
                field = field.getType().getDeclaredField("mBrowserFrame");
                field = field.getType().getDeclaredField("sConfigCallback");
                field.setAccessible(true);
                field.set(null, null);
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
                if (sConfigCallback != null) {
                    sConfigCallback.setAccessible(true);
                    sConfigCallback.set(null, null);
                }
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }



    private void initWebView() {
        AwpEnvironment.getInstance().setAwpDebuggingEnabled(mIsWebContentsDebuggingEnabled);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        if (meetApiRequirement(Build.VERSION_CODES.HONEYCOMB)) {
            webSettings.setDisplayZoomControls(false);
            webSettings.setEnableSmoothTransition(true);
            webSettings.setAllowContentAccess(true);
        }
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setNeedInitialFocus(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        if (meetApiRequirement(Build.VERSION_CODES.JELLY_BEAN)) {
            webSettings.setAllowUniversalAccessFromFileURLs(false);
            webSettings.setAllowFileAccessFromFileURLs(false);
        }
        if (meetApiRequirement(Build.VERSION_CODES.LOLLIPOP)) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBbar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, true);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(newProgress==100){
                    progressBbar.setVisibility(View.GONE);
                }else {
                    progressBbar.setProgress(newProgress);
                    progressBbar.setVisibility(View.VISIBLE);
                }
            }
        });
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                Log.i(TAG, "onDownloadStart, url=" + url);
            }
        });
        AwpEnvironment.getInstance().setAwpDebuggingEnabled(true);
        AwpExtension extension = AwpEnvironment.getInstance().getAwpExtension(webview);
        if (extension != null) {
            Log.i(TAG, "AwpExtension OK");
            // Enables SmartImages loading
            extension.getAwpSettings().setSmartImagesEnabled(true);
            // Enables NightMode
            extension.getAwpSettings().setNightModeEnabled(true);
            extension.getAwpSettings().setAwpPlayerEnabled(true);
        }
        webview.loadUrl(url);
    }

    private static boolean meetApiRequirement(int requires) {
        return Build.VERSION.SDK_INT >= requires;
    }
}
