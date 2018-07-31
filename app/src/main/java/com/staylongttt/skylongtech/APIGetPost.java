package com.staylongttt.skylongtech;

import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIGetPost {

    APIGetPost(){

    }
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
     public HttpClient createHttpClient() {
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

    public String useHttpClientGet(String url) { //HTTP GET的方法，用於API接口
        HttpGet mHttpGet = new HttpGet(url);
        String resp ="";
        mHttpGet.addHeader("Connection", "Keep-Alive");
        try {
            HttpClient mHttpClient = createHttpClient();
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();

            int code = mHttpResponse.getStatusLine().getStatusCode();
            if (null != mHttpEntity) {
                InputStream mInputStream = mHttpEntity.getContent();
                String respose = converStreamToString(mInputStream);
                Log.i("wangshu", "請求狀態:" + code + "\n請求結果:\n" + respose);
                //Log.d("HTTP請求GET:" , respose);
                resp = respose;
                mInputStream.close();
                return resp;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    public void useHttpClientPost(String url, HashMap map) {   //Json POST的方法.POST的方法.POST的方法.POST的方法.POST的方法
        HttpPost mHttpPost = new HttpPost(url);
        mHttpPost.addHeader("Connection", "Keep-Alive");
        try {
            HttpClient mHttpClient = createHttpClient();
            List<NameValuePair> postParams = new ArrayList<>();
            //要传递的参数
            for (Object key : map.keySet()) { //讀出所有MAP裡面資料
                System.out.println(key + " : " + map.get(key));
                postParams.add(new BasicNameValuePair(key.toString(), map.get(key).toString()));
            }
            mHttpPost.setEntity(new UrlEncodedFormEntity(postParams));
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            int code = mHttpResponse.getStatusLine().getStatusCode();
            if (null != mHttpEntity) {
                InputStream mInputStream = mHttpEntity.getContent();
                String respose = converStreamToString(mInputStream);
                Log.d("POST", "請求狀態:" + code + "\n請求結果:\n" + respose);
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("HTTP-POST錯誤", e.toString());
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

    public void useHttpUrlConnectionPost(String url) { //使用POST連接API的方法
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
}
