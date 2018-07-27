package com.staylongttt.skylongtech;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static android.provider.BaseColumns._ID;
import android.provider.BaseColumns;
import android.util.Log;

public class SQLite extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "records";
    private static final int DATABASE_VERSION = 1;
    public static final String _ID = "_id";
    public static final String INDEX1 = "index1";
    // public static final String TABLE2 = "records222";
    public static final String NETWORKTYPE = "networkType";
    // Columns
    public static final String WEBPAGECODE = "webPageCode";
    public static final String STATUSDES = "statusDes";
    public static final String NETSPEED = "netSpeed";
    public static final String PING = "ping";
    public static final String DEVICE = "device";
    public static final String IP = "ip";

    public static final String TABLE = "records";

    static String TABLEFIRST = "CREATE TABLE " + DATABASE_NAME + "( "
            + _ID + " integer primary key autoincrement, " + INDEX1
            + " TEXT NOT NULL , " + NETWORKTYPE + " TEXT NOT NULL , " + WEBPAGECODE
            + " TEXT NOT NULL , " + STATUSDES + " TEXT NOT NULL , " + NETSPEED
            + " TEXT NOT NULL , " + PING + " TEXT NOT NULL , " + DEVICE
            + " TEXT NOT NULL , " + IP + " TEXT NOT NULL );";

    public SQLite(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

         Log.d("EventsData", "onCreate: " + TABLEFIRST);
         db.execSQL(TABLEFIRST);
    }

    public void Create(SQLiteDatabase db) {

        db.execSQL(TABLEFIRST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion)
            return;

        String sql = null;
        if (oldVersion == 1) {
            /*---------------------------------------------*/
            sql = "alter table " + TABLEFIRST + " add note text;";
        }
        if (oldVersion == 2) {
            sql = "";
        }
    }

}
