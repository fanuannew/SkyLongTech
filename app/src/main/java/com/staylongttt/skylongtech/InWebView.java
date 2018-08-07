package com.staylongttt.skylongtech;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class InWebView extends WebView {
    public Activity activity;
    WebView mWebView;
    public InWebView(Context ct) {
        super(ct);
        //mWebView =  ((Activity)ct).getWindow().getDecorView().findViewById(R.id.webview);
        LayoutInflater inflater = (LayoutInflater) ct
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWebView = (InWebView) inflater.inflate(R.layout.activity_main, this);
        activity = (Activity) ct;

        WebSettings webSettings = mWebView.getSettings();
        //mWebView.setWebChromeClient(new WebChromeClient()); //設定chrome為wevview的核心
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);  //提高渲染的优先级
        webSettings.setTextZoom(100);
        webSettings.setSavePassword(true);


        //if have cell, load data from cell , otherwise from cache
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); //決定快取怎麼loading的模式
        webSettings.setDatabaseEnabled(true);
        //webSettings.setAppCacheMaxSize(20*1024*1024);
        webSettings.setAppCacheEnabled(true); //一般快取方式
        webSettings.setAllowFileAccess(true);

        webSettings.setBuiltInZoomControls(true);

        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);
        //webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        //webSettings.setNeedInitialFocus(true);
        //webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(true); // This forces ChromeClient enabled.
        webSettings.setUseWideViewPort(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);


        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;//保持长按可以复制文字
            }
        });

    }


    // 长按点击事件

}
