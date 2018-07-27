package com.staylongttt.skylongtech;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageView;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


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
    //---大型資料庫宣告結束

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
        WError.db = openOrCreateDatabase("records", MODE_PRIVATE, null); //舊: events.db
        //建立資料庫-------------------------------------資料庫初始化
        mWebView = (WebView) findViewById(R.id.webview);
        progressImag = findViewById(R.id.imageP);
        LoadingImg = findViewById(R.id.startimage);
        builder = new AlertDialog.Builder(this);
        handler=new MyHandler(); // 宣告handler處理物件

        dialog = builder.create();
        dialog.setMessage("加载中...");
        dialogWarning = builder.create();

        String autoUrl=""; //轉存網址暫存字串
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
        //判斷資料庫有無先前API取得首頁的資料
        try {
            Log.d("TRY網址",autoUrl);
            if(readUrlData()!=null) {
                autoUrl = readUrlData();
                Log.d("TRY網址2",autoUrl);
            }
        } catch (Exception e) { e.printStackTrace();
            Log.d("TRY網址ERROR",e.toString());}
        if(autoUrl=="") {
            mWebView.loadUrl(webUrl); //"http://wap.skylongtech.com/wap/dist/#/AppIndex"
            Log.d("SETTING沒資料",autoUrl);
        }else{
            mWebView.loadUrl(autoUrl);
            Log.d("從小資料庫載入網址",autoUrl);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try
                {
                    if ((!url.startsWith("weixin://")) && (!url.startsWith("alipays://")) && (!url.startsWith("alipay")) && (!url.startsWith("mailto://")) && (!url.startsWith("tel://"))) //判斷需不需要跳轉其他APP
                    {
                        boolean bool = url.startsWith("dianping://");
                        if (!bool)
                        {
                            //view.loadUrl(url);
                            mWebView.loadUrl(url);
                            return true;
                        }
                    }
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    return true;
                }
                catch (Exception paramAnonymousWebView) {}
                mWebView.setBackgroundColor(0);
                //mWebView.loadUrl(url);

                //mWebView.loadData(url, "text/html", "UTF-8");  // load the webview
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {

                progressImag.setVisibility(View.GONE);
                dialog.dismiss();
                new Handler().postDelayed(new Runnable(){ public void run() { LoadingImg.setVisibility(View.GONE); } }, 1000); //延时5s。

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
            String description, String failingUrl) { //接收到錯誤網頁的反應處理
                super.onReceivedError(view, errorCode, description, failingUrl);
                dialogWarning.setMessage("加载失敗...");
                Log.d("網頁ERROR輸出", "-MyWebViewClient->onReceivedError()--\n errorCode="+errorCode+" \ndescription="+description+" \nfailingUrl="+failingUrl);
                //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                //view.loadData(errorHtml, "text/html", "UTF-8");
                WError.netype="";
                WError.webcode= String.valueOf(errorCode);
                WError.status = description + "失敗存取的網頁: " + failingUrl;
                WError.nspeed = "";
                WError.ping = "";
                WError.ping = "";
                WError.ip = "";
                ErrorToDatabaseThread(); //使用新執行緒把錯誤寫入到資料庫
                //Log.d("Cursor Object輸出所有: ", DatabaseUtils.dumpCursorToString(WError.getEvents())); //偵錯時才打開，輸出資料庫所有資料
                mWebView.setVisibility(View.GONE);
                dialogWarning.show();
            }


         });





    }
    private void useHttpClientGetThread() { //使用另一個執行緒去抓取API
        String jsontext;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message;
                useHttpClientGet(apiUrl); //"http://api.packageday.com/v1/ad/getAdInfoByID?adid=113&source=android"
                //message = handler.obtainMessage(1,obj);
                //handler.sendMessage(message);
            }
        }).start();
    }
    private void ErrorToDatabaseThread() { //使用另一個執行緒把錯誤寫入資料庫
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message;
                //WError.createTable();
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
    /**
     * HttpUrlConnection POST请求网络
     */
    /*private void useHttpUrlConnectionGetThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                useHttpUrlConnectionPost("http://api.packageday.com/v1/ad/getAdInfoByID?adid=113&source=android");
            }
        }).start();
    }*/


    /**
     * 设置默认请求参数，并返回HttpClient
     *
     * @return HttpClient
     */
    private HttpClient createHttpClient() {
        HttpParams mDefaultHttpParams = new BasicHttpParams();
        //设置连接超时
        HttpConnectionParams.setConnectionTimeout(mDefaultHttpParams, 15000);
        //设置请求超时
        HttpConnectionParams.setSoTimeout(mDefaultHttpParams, 15000);
        HttpConnectionParams.setTcpNoDelay(mDefaultHttpParams, true);
        HttpProtocolParams.setVersion(mDefaultHttpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(mDefaultHttpParams, HTTP.UTF_8);
        //持续握手
        HttpProtocolParams.setUseExpectContinue(mDefaultHttpParams, true);
        HttpClient mHttpClient = new DefaultHttpClient(mDefaultHttpParams);
        return mHttpClient;

    }

    /**
     * 使用HttpClient的get请求网络
     *
     * @param url
     */

    private String useHttpClientGet(String url) { //HTTP GET的方法，用於API接口
        HttpGet mHttpGet = new HttpGet(url);
        String resp =null;
        mHttpGet.addHeader("Connection", "Keep-Alive");
        try {
            HttpClient mHttpClient = createHttpClient();
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            Message message=new Message(); //宣告一個傳值的媒介-信差
            int code = mHttpResponse.getStatusLine().getStatusCode();
            if (null != mHttpEntity) {
                InputStream mInputStream = mHttpEntity.getContent();
                String respose = converStreamToString(mInputStream);
                Log.i("wangshu", "請求狀態:" + code + "\n請求結果:\n" + respose);
                //Log.d("HTTP請求GET:" , respose);
                resp = respose;
                message.obj=respose; //使用message obj來當信差
                message.what=1;
                handler.sendMessage(message);
                mInputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    private void useHttpClientPost(String url) {   //Json POST的方法.POST的方法.POST的方法.POST的方法.POST的方法
        HttpPost mHttpPost = new HttpPost(url);
        mHttpPost.addHeader("Connection", "Keep-Alive");
        try {
            HttpClient mHttpClient = createHttpClient();
            List<NameValuePair> postParams = new ArrayList<>();
            //要传递的参数
            postParams.add(new BasicNameValuePair("username", "moon"));
            postParams.add(new BasicNameValuePair("password", "123"));
            mHttpPost.setEntity(new UrlEncodedFormEntity(postParams));
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            int code = mHttpResponse.getStatusLine().getStatusCode();
            if (null != mHttpEntity) {
                InputStream mInputStream = mHttpEntity.getContent();
                String respose = converStreamToString(mInputStream);
                Log.i("wangshu", "請求狀態:" + code + "\n請求結果:\n" + respose);
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将请求结果装潢为String类型
     *
     * @param is InputStream
     * @return String
     * @throws IOException
     */
    private String converStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        String respose = sb.toString();
        return respose;
    }

    private void useHttpUrlConnectionPost(String url) {
        InputStream mInputStream = null;
        HttpURLConnection mHttpURLConnection = UrlConnManager.getHttpURLConnection(url);
        try {
            List<NameValuePair> postParams = new ArrayList<>();
            //要传递的参数
            postParams.add(new BasicNameValuePair("username", "moon"));
            postParams.add(new BasicNameValuePair("password", "123"));
            UrlConnManager.postParams(mHttpURLConnection.getOutputStream(), postParams);
            mHttpURLConnection.connect();
            mInputStream = mHttpURLConnection.getInputStream();
            int code = mHttpURLConnection.getResponseCode();
            String respose = converStreamToString(mInputStream);
            Log.i("API回傳", "請求狀態:" + code + "\n請求結果:\n" + respose);
            mInputStream.close();
        } catch (IOException e) {
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

    public String getWebPage(final String url,final String param1) {

        Runnable runnable;
        Handler newHandler;



        newHandler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();

                    //HttpResponse httpResponse =null;
                    params = new LinkedList<BasicNameValuePair>();
                    params.add(new BasicNameValuePair("adid", param1));
                    params.add(new BasicNameValuePair("source", "android"));
                    HttpPost postMethod = new HttpPost(url);
                    postMethod.setEntity(new UrlEncodedFormEntity(params, "utf-8")); //将参数填入POST Entity中
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpClient.execute(postMethod); //执行POST方法

                    //Log.i(TAG, "resCode = " + response.getStatusLine().getStatusCode()); //获取响应码
                    //Log.i(TAG, "result = " + EntityUtils.toString(response.getEntity(), "utf-8")); //获取响应内容

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
        //return httpResponse.getEntity().toString();
        return "";
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
    class MyHandler extends Handler
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
