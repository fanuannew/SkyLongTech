package com.staylongttt.skylongtech;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    //打包用的網址跟API字串
    String webUrl="http://wap.skylongtech.com/wap/dist/#/AppIndex?application_key=2001&forapp=Android";
    String apiUrl="http://api.packageday.com/v1/ad/getAdInfoByID?adid=113&source=android";
    //打包會修改的字串區域結束
    int start = 0;
    WebView mWebView;
    ImageView progressImag;
    ImageView LoadingImg;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    AlertDialog dialogWarning;
    MyHandler handler;
    //----小型資料庫宣告
    private SharedPreferences settings;
    private static final String data = "DATA";
    private static final String idField = "ID";
    private static final String titleField = "TITLE";
    private static final String urlField = "URL";
    //---小型資料庫宣告結束
    //---大型資料庫宣告
    WebErrorReturn WError;
    NetworkDetect ndtest;
    //---大型資料庫宣告結束
    //------API接收處理宣告
    String errorApi = ""; //錯誤回傳先解析所屬網域

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持螢幕長亮
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //限制手機只能豎立
        setContentView(R.layout.activity_main);
        //Log.e("API測試抓取",getWebPage("http://api.packageday.com/v1/ad/getAdInfoByID?adid=113&source=android"));
        //Log.e("API測試抓取",getWebPage("http://api.packageday.com/v1/ad/getAdInfoByID","113"));
        useHttpClientGetThread(); //測試httpGET執行緒
        // Example of a call to a native method
        WError = new WebErrorReturn(getApplicationContext());
        //WError.clearDB();//清除資料庫
        WError.db = openOrCreateDatabase("records", MODE_PRIVATE, null); //舊: events.db
        //建立資料庫-------------------------------------資料庫初始化
        //測試網路狀態偵測
        ndtest = new NetworkDetect(getApplicationContext());
        //Log.d("取得PINGPING測試: ",  ndtest.ping());
        //測試網路狀態偵測結束
        mWebView = findViewById(R.id.mywebview);
        progressImag = findViewById(R.id.imageP);
        LoadingImg = findViewById(R.id.startimage);
        builder = new AlertDialog.Builder(this);
        handler = new MyHandler(); // 宣告handler處理物件

        dialog = builder.create();
        dialog.setMessage("加载中...");
        dialogWarning = builder.create();

        String autoUrl = ""; //轉存網址暫存字串
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
        //webSettings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2; en-gb; Nexus One Build/FRF50) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        mWebView.requestFocus();
        ErrorDataTimer(); //間隔執行------------------------------------------------間隔執行
        //判斷資料庫有無先前API取得首頁的資料
        if (ndtest.isNetworkConnected(getApplication())) {
            try {
                Log.d("TRY網址", autoUrl);
                if (readUrlData() != null) {
                    autoUrl = readUrlData();
                    errorApi = autoUrl.substring(0, autoUrl.indexOf("/", 9)) + "/v1/ettm/set_mobile_phone_info";
                    Log.d("TRY網址2", autoUrl);
                    Log.d("TRY網址3:ErrorAPI", errorApi);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TRY網址ERROR", e.toString());
            }
            if (autoUrl == "") {
                mWebView.loadUrl(webUrl); //"http://wap.skylongtech.com/wap/dist/#/AppIndex"
                Log.d("SETTING沒資料", autoUrl);
            } else {
                mWebView.loadUrl(autoUrl);
                Log.d("從小資料庫載入網址", autoUrl);
            }
        } else { //如果無網路的話 顯示網路錯誤
            mWebView.loadUrl("");
            mWebView.setVisibility(View.GONE);
            passError(503, "没有连线到网絡", "Homepage", "127.0.0.1"); //先規劃網路無連線的話不寫入資料庫做紀錄
            dialogWarning.setMessage("目前無可用網絡...");
            dialogWarning.show();
            LoadingImg.setVisibility(View.VISIBLE);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if ((!url.startsWith("weixin://")) && (!url.startsWith("alipays://")) && (!url.startsWith("alipay")) && (!url.startsWith("mailto://")) && (!url.startsWith("tel://"))) //判斷需不需要跳轉其他APP
                    {
                        boolean bool = url.startsWith("dianping://");
                        if (!bool) {
                            //view.loadUrl(url);
                            mWebView.loadUrl(url);
                            return true;
                        }
                    }
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    return true;
                } catch (Exception paramAnonymousWebView) {
                }
                mWebView.setBackgroundColor(0);
                //mWebView.loadUrl(url);

                //mWebView.loadData(url, "text/html", "UTF-8");  // load the webview
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressImag.setVisibility(View.GONE);
                dialog.dismiss();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        LoadingImg.setVisibility(View.GONE);
                    }
                }, 1000); //延时5s。

                super.onPageFinished(view, url);


            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressImag.setVisibility(View.VISIBLE);
                dialog.show();
                //LoadingImg.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) { //接收到錯誤網頁的反應處理--------------------------------------------收到錯誤網頁
                super.onReceivedError(view, errorCode, description, failingUrl);
                dialogWarning.setMessage("加载失敗...");
                Log.d("網頁ERROR輸出", "-MyWebViewClient->onReceivedError()--\n errorCode=" + errorCode + " \ndescription=" + description + " \nfailingUrl=" + failingUrl);
                //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                //view.loadData(errorHtml, "text/html", "UTF-8");
                passError(errorCode, description, failingUrl, ""); //傳址準備寫入SQLite資料庫
                mWebView.setVisibility(View.GONE);
                dialogWarning.show();
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(final String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                final WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
                request.allowScanningByMediaScanner();
                //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Name of your downloadble file goes here, example: Mathematics II ");
                //DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                //dm.enqueue(request);
                if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                        hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    // 弹出保存图片的对话框
                    downloadmanager(url,hitTestResult.getExtra());
                }
            }
        });

        // 长按点击事件
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(getApplicationContext(), "長按測試.....", Toast.LENGTH_SHORT).show();
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int type = result.getType();
                /*if (null == result)
                    return false;

                if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                    return false;
                if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {

                }*/
                // 相应长按事件弹出菜单
//                ItemLongClickedPopWindow itemLongClickedPopWindow = new ItemLongClickedPopWindow(MainActivity.this,
//                        ItemLongClickedPopWindow.IMAGE_VIEW_POPUPWINDOW,
//                        SizeUtil.dp2px(mContext, 120), SizeUtil.dp2px(mContext, 90));

                // 这里可以拦截很多类型，我们只处理图片类型就可以了
                if(type == WebView.HitTestResult.IMAGE_TYPE ||type ==  WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE){ // 处理长按图片的菜单项
                        // 获取图片的路径
                        //saveImgUrl = result.getExtra();
                        //通过GestureDetector获取按下的位置，来定位PopWindow显示的位置
                        //itemLongClickedPopWindow.showAtLocation(v, Gravity.TOP | Gravity.LEFT, downX, downY + 10);
                        Log.d("contex選單: ","True");
                    downloadmanager(result.getExtra(),result.getExtra());
                    //Toast.makeText(getApplicationContext(), "照片存檔測試中.....", Toast.LENGTH_SHORT).show();
                }

                return true;
            }

        });
    }
    private void downloadmanager(final String url,final String imgurl){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        builder.setMessage("保存图片到本地");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                url2bitmap(url);
                String picUrl = imgurl; //hitTestResult.getExtra();//获取图片链接
                //保存图片到相册
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //url2bitmap(picUrl);
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            // 自动dismiss
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
            } else {

                Log.e("Permission error", "You have asked for permission");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    private void passError(int errorCode, String description, String failingUrl, String IP){ //處理錯誤訊息整理之後傳值到後端處理
        WError.netype= ndtest.getNetwrokType(getApplicationContext());
        WError.webcode= String.valueOf(errorCode);
        WError.status = description + "，访问失败的页面: " + failingUrl;
        WError.nspeed = "";
        WError.ping = "";
        WError.device = "android_APP";
        if(IP==""){WError.ip = ndtest.getRealIP(getApplicationContext());}
        else{WError.ip = IP;}
        ErrorToDatabaseThread(); //使用新執行緒把錯誤寫入到資料庫
        Log.d("Cursor Object輸出所有: ", DatabaseUtils.dumpCursorToString(WError.getEvents())); //偵錯時才打開，輸出資料庫所有資料
    }
    private void useHttpClientGetThread() { //使用另一個執行緒去抓取API
        new Thread(new Runnable() {
            @Override
            public void run() {
                APIGetPost ApiPasser1 = new APIGetPost();
                Message message=new Message(); //宣告一個傳值的媒介-信差
                message.obj = ApiPasser1.useHttpClientGet(apiUrl); //"http://api.packageday.com/v1/ad/getAdInfoByID?adid=113&source=android"
                message.what=1;
                handler.sendMessage(message);
                Log.d("PING在thread裡測試",ndtest.ping());
                //message = handler.obtainMessage(1,obj);
                //handler.sendMessage(message);
            }
        }).start();
    }
    public ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1); //使用ScheduledExecutorService來製作android計時器的功能
    public void ErrorDataTimer() { //計時回傳錯誤資料
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                if(ndtest.isNetworkConnected(getApplication())) {
                    Log.d("進入到計畫任務: ", "計畫任務開始");
                    WError.uploadErrorData(errorApi);
                }
            }
        };
        scheduExec.scheduleWithFixedDelay(task, 10, 10 * 60,
                TimeUnit.SECONDS); //Runnable command,long initialDelay,long delay,TimeUnit unit //初始3分鐘後執行，之後每隔10分鐘後執行
    }
    private void ErrorToDatabaseThread() { //使用另一個執行緒把錯誤寫入資料庫-------------寫入錯誤
        new Thread(new Runnable() {
            @Override
            public void run() {
                //WError.createTable();
                WError.ping = ndtest.ping();
                WError.InsertDB("now");
                //message = handler.obtainMessage(1,obj);
                //handler.sendMessage(message);
            }
        }).start();
    }
    /*private Handler DOFindAttributehandler =  new Handler(){ //處理執行緒內傳值的問題
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String MsgString = (String)msg.obj;
            if (MsgString.equals("OK"))
            {
                //......
            }
        }
        };*/
    public void url2bitmap(String url) { //下載圖片存到手機
        Bitmap bm = null;
        try {
            //URL iconUrl = new URL(url);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.allowScanningByMediaScanner(); //设置图片的保存路径

            //request.setDestinationInExternalFilesDir(MainActivity.this, "/img", "/png");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "QRcode.png");
            request.setTitle("QRcode"); //request.setDescription("下载中通知栏提示");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setVisibleInDownloadsUi(true);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
            //}
        } catch (Exception e) {
            Log.d("保存失敗錯誤LOG: ", e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //Log.e(TAG, "start onResume~~~");
        //mWebView.reload();
        //mWebView.getUrl();
        String name[]={"AppIndex"};
        //實作回首頁app時reloading首頁
        /*for(int i=0;i<1;i++){
            if(mWebView.getUrl().indexOf(name[i])>0){mWebView.reload();}
        }*/

    }
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack() & mWebView.getUrl().indexOf("AppIndex")<0) {
            mWebView.goBack();
        }else if(mWebView.getVisibility()==View.GONE){ //加載失敗情況下就結束activity
            this.finish();
        } else {
            //super.onBackPressed();
            moveTaskToBack(true);
        }
    }


    public String getWebPage(String url) {
        String encodeUrl="";
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet();

        InputStream inputStream = null;

        String response = null;
        try {
            encodeUrl = URLEncoder.encode(url, "UTF-8");
            //encodeUrl = encodeUrl.replaceAll("%3F", "?"); // I tried replacing this but does not work
            //encodeUrl = encodeUrl.replaceAll("%3D", "=");  //

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            Log.d("LOG_EXCEPTION", "Unsuported Encoding Exception");
            //e1.printStackTrace();
        }
        try {

            URI uri = new URI(encodeUrl);
            httpGet.setURI(uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            // HttpPost httppost = new HttpPost(url);
            Log.d("LOG", "THIS CODE I SEE in LOGCAT");
            int statutCode = httpResponse.getStatusLine().getStatusCode();
            int length = (int) httpResponse.getEntity().getContentLength();


            Log.d("main", "HTTP GET: " + encodeUrl);
            Log.d("main", "HTTP StatutCode: " + statutCode);
            Log.d("main", "HTTP Lenght: " + length + " bytes");

            inputStream = httpResponse.getEntity().getContent();
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            //int inChar;
            StringBuffer stringBuffer = new StringBuffer();

            //while ((inChar = reader.read()) != -1) {
            //stringBuffer.append((char) inChar);
            //}

            //response = stringBuffer.toString();
            response = reader.toString();

        } catch (ClientProtocolException e) {
            Log.d("main", "HttpActivity.getPage() ClientProtocolException error", e);
        } catch (IOException e) {
            Log.d("main", "HttpActivity.getPage() IOException error", e);
        } catch (URISyntaxException e) {
            Log.d("main", "HttpActivity.getPage() URISyntaxException error", e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

            } catch (IOException e) {
                Log.e("LOG_THREAD_ACTIVITY", "HttpActivity.getPage() IOException error lors de la fermeture des flux", e);
            }
        }

        return response;
    }

    private class HttpTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {
            // TODO Auto-generated method stub
            String response = getWebPage(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("main", "HTTP RESPONSE" + response);
            //tv1.setText(response);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }
    //thread之前傳值
    class MyHandler extends Handler //處理json收到資料之後解析的問題
    {
        //接受message的信息
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if(msg.what==1)
            {
                System.out.println(msg.arg1+"handle");
                Log.d("MSN信差大使:",msg.obj.toString());
                //bar.setProgress(msg.arg1);
                try{
                    JSONObject jsonObject = new JSONObject(msg.obj.toString());
                    //String name = .getString("pic_url");
                    JSONObject jsonObject2 = jsonObject.getJSONArray("data").getJSONObject(0); //內含的只有一個objectarray
                    String finalurl = jsonObject2.getString("pic_url"); //轉跳的網址
                    Log.d("json傳值",finalurl);
                    saveUrlData("01","安卓包3测试环境",finalurl);
                    Log.d("json傳值再讀出小設定: ",readUrlData());
                }
                catch(JSONException e) {
                    e.printStackTrace();

                }
            }


        }
    }
    //thread傳值結束
    //小型資料庫讀取寫入區
    public String readUrlData(){ //讀出API轉址URL的資料
        String url="";
        settings = getSharedPreferences(data,0);
        //settings.getString(idField, "");
        //settings.getString(titleField, "");
        url = settings.getString(urlField, "");
        return url;
    }
    public void saveUrlData(String id,String title,String url){
        settings = getSharedPreferences(data,0);
        settings.edit()
                .putString(idField, id)
                .putString(titleField, title)
                .putString(urlField, url)
                .commit();
    }
    //小型資料庫讀寫結束----------------------------------------------------

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
}
