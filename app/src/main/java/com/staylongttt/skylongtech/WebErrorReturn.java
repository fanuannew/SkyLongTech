package com.staylongttt.skylongtech;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;


public class WebErrorReturn {
    SQLite dbase;
    SQLiteDatabase db;
    String ind1 = "", netype = "";
    String webcode = "", status = "", nspeed = "", ping = "", device = "", ip = "";
    APIGetPost apigt;

    WebErrorReturn(Context ac) { //初始使用的宣告
        dbase = new SQLite(ac);
        apigt = new APIGetPost();
        //int ind1=0,ind2=0;
    }

    public void createTable() { //創建資料庫
        try {
            db.execSQL("CREATE TABLE records ( "
                    + "_ID  integer primary key autoincrement, "
                    + "index1 TEXT NOT NULL , " + "networkType TEXT NOT NULL , "
                    + "webPageCode TEXT NOT NULL , " + "statusDes TEXT NOT NULL , " + "netSpeed TEXT NOT NULL , "
                    + "ping TEXT NOT NULL , " + "device TEXT NOT NULL , "
                    + "ip TEXT NOT NULL )");
        } catch (Exception e) {
            Log.d("createTable: ->", e.toString());
        }
    }

    public Cursor getEvents() {
        SQLiteDatabase db = dbase.getWritableDatabase();
        String[] columns = { "_ID","INDEX1", "NETWORKTYPE", "WEBPAGECODE", "STATUSDES", "NETSPEED",
                "PING", "DEVICE", "IP" };
        return db.query(SQLite.TABLE, // 資料表名稱
                columns, // 欄位名稱
                null, // WHERE
                null, // WHERE 的參數
                null, // GROUP BY
                null, // HAVING
                null // ORDOR BY
        );
    }
    public HashMap uploadErrorData(String apiurl) { //讀出本地資料庫，上傳到伺服器
        //String ret = new String("Saved Events:\n\n");
        Cursor cursor = getEvents();
        String[] values = new String[50];
        final String[] itempos = new String[80];
        ListView listView;
        //===========================Dialog initial=====================
        //Dialog dialog = new Dialog(WebErrorReturn.this);
        //dialog.setContentView(R.layout.listview1);
        //listView = (ListView) dialog.findViewById(R.id.listviewcon);
        //ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        int numb1 = 0;
        String id = "", index1 = "", index2 = "", region = "", title1 = "", title2 = "", title3 = "";
        //String timy = "", timm = "", timd = "", timh = "", timn = "";22020968
        int rows_num = cursor.getCount();
        //listItem = new ArrayList<HashMap<String, Object>>();

        String objName[] = {"network_state","http_status", "status_remark","net_speed","ping_value","user_device","user_ip"}; //宣告要回傳POST的欄位
        HashMap<String, String> map = new HashMap<String, String>();
        cursor.moveToFirst();
        for (int i = 0; i < rows_num; i++) {
            //HashMap<String, Object> map = new HashMap<String, Object>();
            id = cursor.getString(0);
            index1 = cursor.getString(1);
            values[0] = cursor.getString(2);
            values[1] = cursor.getString(3);
            values[2] = cursor.getString(4);
            values[3] = cursor.getString(5);
            values[4] = cursor.getString(6);
            values[5] = cursor.getString(7);
            values[6] = cursor.getString(8);
            for(int j=0;j<objName.length;j++) {
                map.put(objName[j], values[j]);
            }
            Log.d("測試讀出資料庫", i+ "values: " + values[i]);
            useHttpClientPostThread(apiurl, map); //使用執行序去處理HTTP-POST
            try{
                db.delete(SQLite.TABLE, SQLite._ID +"=" + i, null); //刪除傳出去的欄位
            }catch(Exception e){
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        //=============================================================================

        return map;
    }
    private void useHttpClientPostThread(final String apiurl, final HashMap map) { //使用另一個執行緒去抓取API
        new Thread(new Runnable() {
            @Override
            public void run() {
                APIGetPost apipasser2 = new APIGetPost();
                apipasser2.useHttpClientPost(apiurl, map); //使用POST傳送值到API
            }
        }).start();
    }
    public  void clearDB(){
        db.execSQL("DROP TABLE IF EXISTS records"); //清除資料庫所有資料
    }
    public void InsertDB(String record) // 把結果存入資料庫
    {
        Cursor cursor = getEvents();
        //db = this.getReadableDatabase();
        int rows_num = cursor.getCount();
        ContentValues values = new ContentValues();
        //int i = checkname(record);
        ind1 = String.valueOf(rows_num+1); //往下插入，所以先取得幾列

        values.put(SQLite.INDEX1, ind1);
        values.put(SQLite.NETWORKTYPE, netype);
        values.put(SQLite.WEBPAGECODE, webcode);
        values.put(SQLite.STATUSDES, status);
        values.put(SQLite.NETSPEED, nspeed);
        values.put(SQLite.PING, ping);
        values.put(SQLite.DEVICE, device);
        values.put(SQLite.IP, ip);
        values.put(SQLite._ID, rows_num) ;//原始程式碼

		/*if (i < rows_num) {//更新現有的
			values.put("_ID", i);
			db.update(SQLite.TABLE, values, "_ID" + "=" + i, null);
		} else {*/
        //values.put("_ID", rows_num);//加入新筆的資料，增加一個新的ID值
        db.insert(SQLite.TABLE, null, values);
        //}

    }
    public int countpp(String temp){
        int tip=0;
        //Log.v("tiptop", "enter tiptop!");
        while(temp.indexOf('"')>=0)
        {
            temp=temp.substring(temp.indexOf('"')+1);
            tip++;
        }
        //Log.v("tiptop", "tip: "+ tip+"|");//印出數值到LOG
        return tip;
    }
    public void updatedata(){
        try{
            //Handler handler2 = new Handler();
            //handler2.postDelayed(new Runnable() {
            //	public void run() {}
            //}, 2000);
            int procex = 0; //記算進度的counter
            //URL url = new URL("http://axel.myweb.hinet.net/test1.csv");

            URL url = new URL("http://axel.myweb.hinet.net/test1.csv");
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);

            //conn.setRequestProperty("Content-Length",  "8000"); //<------------
            //===========================================================
            db.execSQL("DROP TABLE IF EXISTS records"); //清除資料庫所有資料
            createTable();
            //===========================================================
            //建立FileReader物件，並設定讀取的檔案為SD卡中的output.txt檔案
            //FileReader fr = new FileReader("/sdcard/test1.csv");
            //將BufferedReader與FileReader做連結
            //BufferedReader br = new BufferedReader(fr);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String readData = "";
            String readData2 = "";
            int clock = 0; //記算(幾)行數
            int line = 0; //記算格數
            int enter = 0; //白目的換行紀錄器
            int ent1 = 0; //換行記行器
            String value="";
            String now1="",now2="",now3="",now4="";
            String temp = br.readLine(); //readLine()讀取一整行
            while (temp!=null){
                readData+=temp; //原(本)始的東西
                //---------------------------------------
                //while(temp.indexOf(",")>0){//有東西的話就繼續切割字串
                if(countpp(temp)%2 == 1 | enter==1){//實現"內文換行"的白目程式碼

                    if(ent1==0){now1=temp;}
                    if(ent1==1){now2=temp;}
                    if(ent1==2){now3=temp;}
                    if(ent1==3){now4=temp;}
                    if(countpp(temp)%2 == 1){//切換enter紀錄
                        if(enter==0){enter=1;}
                        else{enter=0;}
                    }
                    ent1 = ent1+1;
                }
                if(countpp(temp)%2 == 1 & enter==0){//把多個斷行結合起來
                    //Log.v("String", "Strings: "+now1+"|"+now2+"|"+now3+"|"+now4);
                    temp=now1+"\n"+now2+"\n"+now3+"\n"+now4;
                    //Log.v("String", "String is combined!");//印出數值到LOG
                    ent1 = 0;
                }
                if(enter==0 & temp.indexOf('/')!=0){//下面是原本的code 注意最後一行有"}"
                    while( temp.indexOf(",")>0 | clock < 1 ){//算切的次數來終止
                        int dot=temp.indexOf(",");
                        if(temp.indexOf(",")>=0 & temp.length()>0){//後面還有要切段的情況
                            if(temp.charAt(0) == '"'){//遇到有""的情況
                                int cut=temp.substring(1).indexOf('"');
                                if(temp.indexOf(",")<temp.length()){//如果最後一個是","就直接結束 不再切
                                    value = temp.substring(1).substring(0, cut);
                                    temp = temp.substring(1).substring(cut+2);}
                            }else{//正常的字串處理
                                int end=temp.indexOf(",");
                                if(end ==0 ){value="";
                                    if(temp.indexOf(",")<temp.length()){
                                        temp = temp.substring(1);}
                                }else{
                                    if(temp.indexOf(",")<temp.length()){//如果最後一個是","就直接結束 不再切
                                        value = temp.substring(0, end);
                                        temp = temp.substring(end+1);}
                                }
                            }
                        }else if(temp.length()>0){//到了最後一段的情況
                            clock = 1;
                            if(temp.charAt(0) == '"'){//遇到有""的情況
                                int cut=temp.substring(1).indexOf('"');
                                if(cut<1){

                                }else{
                                    value = temp.substring(1).substring(0, cut);
                                }
                            }
                            else{//正常的字串處理
                                value = temp;
                            }
                        }else{clock =1;}//跳出

                        //Log.v("end", "end: "+ end+"|");//印出數值到LOG
                        //Log.v("Temp", "Temp: "+ temp+"|");//印出數值到LOG
                        line=line+1;
                        //Log.v("line", "line: "+ line);//印出數值到LOG
                        //readData2+=value; //測試把字串組合起來印到小視窗
                        //下面存入宣告值 等著被轉存資料庫內
                        switch (line) {
                            case 1:
                                ind1 = value;
                                break;
                            case 2:
                                netype = value;
                                break;
                            case 3:
                                webcode = value;
                                break;
                            case 4:
                                status = value;
                                break;
                            case 5:
                                nspeed = value;
                                break;
                            case 6:
                                ping = value;
                                break;
                            case 7:
                                device = value;
                                break;
                            case 8:
                                ip = value;
                                break;
                            //if(line == 8){
                            //	text3 = value;}
                        }
                    }

                    //Log.v("Values", "values: "+ ind1+"|"+ind2+"|"+reg+"|"+tit1+"|"+tit2+"|"
                    //		+text1+"|"+text2+"|"+text3);//印出數值到LOG
                    //readData2+=value;
                    value="";//暫存區歸0
                    clock =0;
                    line = 0;
                    //原(本)始的東西
                    InsertDB("now"); //把字串存入資料庫
                    ind1 ="";netype="";webcode="";status="";nspeed="";ping="";device="";ip="";}//if判斷式在這裡結束

                //procex++;
                //Toast.makeText(this, "更新"+procex+"條!", Toast.LENGTH_LONG).show();
                temp=br.readLine();
            }

            //dialog2.setMessage("資料已全數讀入");
            //dialog2.show();
            //分隔線------------------------------下面是原始的東西
            //Context context = this.getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            //Toast toast = Toast.makeText(context, readData, duration);
            //toast.show();
        }catch(Exception e){
            e.printStackTrace();
        }
        //InsertDB("now");
    }
}
