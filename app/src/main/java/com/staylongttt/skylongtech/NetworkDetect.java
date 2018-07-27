package com.staylongttt.skylongtech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

class NetworkDetect {
     //Context ct;

    NetworkDetect(Context context){

    }


    public static boolean isNetworkConnected(Context ct) { //檢查是否有網路可用
        ConnectivityManager cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
    public static boolean isMobileDataEnable(Context context) throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileDataEnable = false;

        isMobileDataEnable = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

        return isMobileDataEnable;
    }

    public static boolean isWifiDataEnable(Context context) throws Exception {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiDataEnable = false;
        isWifiDataEnable = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        return isWifiDataEnable;
    }

    public static String getWifiIP(Context context) { //偵測WIFI連線時的IP
        String ip = null;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                    + "." + (i >> 24 & 0xFF);
        }
        return ip;
    }
    public static String getMobileIP() { //偵測在cell網路時的IP位址
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Oops Something wrong", ex.toString());
        }
        return null;
    }
    public static final boolean ping() { //ping的實作方法
        String result =null;
        try{
            String ip ="www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 2 -w 100 "+ ip); //ping3次
            InputStream input = p.getInputStream();
            BufferedReader in =new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer =new StringBuffer();
            String content ="";
            while((content = in.readLine()) !=null) {
                stringBuffer.append(content);
            }
            Log.i("TTT","result content : "+ stringBuffer.toString());
            int status = p.waitFor();
            if(status ==0) {
                result ="successful~";
                return true;
            }else{
                result ="failed~ cannot reach the IP address";
            }
        }catch(IOException e) {
            result ="failed~ IOException";
        }catch(InterruptedException e) {
            result ="failed~ InterruptedException";
        }finally{
            Log.i("TTT","result = "+ result);
        }
        return false;
    }
}
