package tab.list;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FileContentProvider extends ContentProvider {
    /*
     * 這邊是自訂義的資料庫，裡面包含了四張table
     * file_choice：當選擇好遇上傳的檔案時，將檔案路徑儲存至這張table
     * temp_file:當ffmpeg切完時，將切割好的檔案路徑儲存至這張table
     * user_data:這邊是紀錄簡訊，同時也紀錄目前的檔案是否有下載完
     * user_group:這邊是紀錄目前有哪一些使用者與你通過信
     */
    private static final String AUTHORITY = "tab.list.d2d";
    private static final int URI_TYPE_TABLE1 = 1;
    private static final int URI_TYPE_TABLE2 = 2;
    private static final int URI_TYPE_TABLE3 = 3;
    private static final int URI_TYPE_TABLE4 = 4;
    private static final int URI_TYPE_TABLE5 = 5;
    private static final int URI_TYPE_TABLE6 = 6;
    private static final int URI_TYPE_TABLE7 = 7;
    private static final int URI_TYPE_TABLE8 = 8;
    private static final int URI_TYPE_TABLE9 = 9;
    private static final int URI_TYPE_TABLE10 = 10;
    private static final int URI_TYPE_TABLE11 = 11;
    private static final UriMatcher mUriMatcher;

    static MyDBHelper databasehelper = null;
    //這邊的interface可以供其他calss使用，這邊只是在定義名稱
    public interface UserSchema {
        String _DB_NAME = "file.db";
        String _TABLE_FILE_CHOICE = "file_choice";
        String _TABLE_TEMP_FILE = "temp_file";
        String _TABLE_USER_DATA = "user_data";
        String _TABLE_USER_GROUP = "user_group";
        String _TABLE_USER_REPLY = "user_reply";
        String _TABLE_USER_INFO = "user_info";
        String _TABLE_USER_VALIDATE = "user_validate";
        String _TABLE_TEMP_CONTENT = "temp_content";
        String _TABLE_TEMP_FFMPEG = "temp_ffmpeg";
        String _TABLE_MISSILE_GROUP = "missile_group";
        String _TABLE_MISSILE_FIRE = "missile_fire";

        int _DB_VERSION = 1;
        String _ID = "_id";
        String _FILEPATH = "filepath";
        String _DURATION = "duration";
        String _FILENAME = "filename";
        String _FILESIZE = "filesize";
        String _FILERECORD = "filerecord";
        String _FILECHECK = "filecheck";
        String _TITTLE = "tittle";
        String _CONTENT = "content";
        String _MESSAGETOKEN = "messagetoken";
        String _SENDER = "sender";
        String _DATE = "datetime";
        String _RECEIVEID = "receive_id";
        String _USESTATUS = "userstatus";
        String _READ = "read";
        String _FILEID = "file_id";
        String _PATHID = "path_id";
        String _MSG = "msg";
        String _D2D = "d2d";
        String _TYPE = "type";
        String _NOTIFICATION = "notification";
        String _FILECOUNT = "filecount";
        String _ACCOUNT = "account";
        String _PASSWORD = "password";
        String _REMEMBER = "remember";
        String _VALIDATE_CODE = "validate_code";
        String _VALIDATE = "validate";
        String _FIRST = "first";
        String _FINISH = "finish";
        String _TOTAL_TIME = "total_time";
        String _SELFID = "selfid";
        String _RECEIVER = "receiver";
        String _READY = "ready";
        String _SEQ_ID = "seq_id";
        String _STATE = "state";
        //20160905學長版本加回
        String _LENGTH_RECORD = "length_record";

        //20161030 學長教學
        String _FILE_1 = "file_1";
        String _FILE_2 = "file_2";
    }

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //設定當match到哪一個urlf時，會傳後面的URI_TYPE_TABLE的值，接著會利用這個值來做判斷目前要針對哪一張table做動作
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_FILE_CHOICE, URI_TYPE_TABLE1);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_TEMP_FILE, URI_TYPE_TABLE2);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_USER_DATA, URI_TYPE_TABLE3);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_USER_GROUP, URI_TYPE_TABLE4);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_USER_REPLY, URI_TYPE_TABLE5);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_USER_INFO, URI_TYPE_TABLE6);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_USER_VALIDATE, URI_TYPE_TABLE7);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_TEMP_CONTENT, URI_TYPE_TABLE8);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_TEMP_FFMPEG, URI_TYPE_TABLE9);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_MISSILE_GROUP, URI_TYPE_TABLE10);
        mUriMatcher.addURI(AUTHORITY, UserSchema._TABLE_MISSILE_FIRE, URI_TYPE_TABLE11);
    }

    private class MyDBHelper extends SQLiteOpenHelper {

        public MyDBHelper(Context context) {
            super(context, UserSchema._DB_NAME, null, UserSchema._DB_VERSION);
        }

        public MyDBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);

        }
        //一剛開始先建立table,若table存在則跳過
        @Override
        public void onCreate(SQLiteDatabase db) {

            String sql = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_FILE_CHOICE + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._FILEPATH + " text not null, "
                    + UserSchema._FILENAME + " text null, "
                    + UserSchema._FILESIZE + " text null, "
                    + UserSchema._DURATION + " INTEGER null, "
                    + UserSchema._PATHID + " text nul "
                    + ");";

            String sql1 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_TEMP_FILE + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._FILEPATH + " text not null, "
                    + UserSchema._FILERECORD + " text null, "
                    + UserSchema._FILENAME + " text null, "
                    + UserSchema._MESSAGETOKEN + " text null, "
                    + UserSchema._SELFID + " text not null, "
                    + UserSchema._FILECHECK + " INTEGER null "
                    + ");";

            //20160903抓蟲版加入first跟filename
            String sql2 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_USER_DATA + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._SENDER + " text not null, "
                    + UserSchema._TITTLE + " text null, "
                    + UserSchema._CONTENT + " text null, "
                    + UserSchema._MESSAGETOKEN + " text not null, "
                    + UserSchema._FILESIZE + " text not null, "
                    + UserSchema._DATE + " DATETIME not null, "
                    + UserSchema._FILEPATH + " text null, "
                    + UserSchema._RECEIVEID + " text null, "
                    + UserSchema._USESTATUS + " text null, "
                    + UserSchema._FILEID + " text null, "
                    + UserSchema._NOTIFICATION + " text null, "
                    + UserSchema._READ + " text null, "
                    + UserSchema._FIRST + " text null, "
                    + UserSchema._TYPE + " text null, "
                    + UserSchema._FILECOUNT + " text null, "
                    + UserSchema._FILENAME + " text null, "
                    + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                    + UserSchema._MSG + " text null "
                    + ");";

            String sql3 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_USER_GROUP + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._SENDER + " text not null "
                    + ");";
            String sql4 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_USER_REPLY + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._SENDER + " text not null, "
                    + UserSchema._DATE + " DATETIME not null, "
                    + UserSchema._FILENAME + " text null, "
                    + UserSchema._MESSAGETOKEN + " text not null, "
                    + UserSchema._FILEPATH + " text null, "
                    + UserSchema._READY + " text null, "
                    + UserSchema._SELFID + " text null, "
                    + UserSchema._D2D + " text null, "
                    + UserSchema._MSG + " text not null "
                    + ");";

            String sql5 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_USER_INFO + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._ACCOUNT + " text null, "
                    + UserSchema._REMEMBER + " text null, "
                    + UserSchema._PASSWORD + " text null "
                    + ");";
            String sql6 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_USER_VALIDATE + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._DATE + " DATETIME not null, "
                    + UserSchema._VALIDATE_CODE + " text not null, "
                    + UserSchema._VALIDATE + " text null "
                    + ");";
            String sql7 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_TEMP_CONTENT + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._RECEIVER + " text null, "
                    + UserSchema._TITTLE+ " text null, "
                    + UserSchema._CONTENT + " text null, "
                    + UserSchema._FILEPATH + " text null, "
                    + UserSchema._FIRST + " text null, "
                    + UserSchema._MESSAGETOKEN + " text null, "
                    + UserSchema._SELFID + " text null, "
                    + UserSchema._DURATION + " text null, "
                    + UserSchema._TOTAL_TIME + " text null, "
                    + UserSchema._FINISH + " text null "
                    + ");";

            String sql8 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_TEMP_FFMPEG + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._FILEPATH + " text null, "
                    + UserSchema._TOTAL_TIME + " text null, "
                    + UserSchema._SEQ_ID + " text null, "
                    + UserSchema._DURATION + " text null, "
                    + UserSchema._SELFID + " text null "
                    + ");";
            String sql9 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_MISSILE_GROUP + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._TITTLE + " text null, "
                    + UserSchema._CONTENT + " text null, "
                    + UserSchema._STATE + " text null, "
                    + UserSchema._FILEPATH + " text null "
                    + ");";
            String sql10 = "CREATE TABLE IF NOT EXISTS "
                    + UserSchema._TABLE_MISSILE_FIRE + " ("
                    + UserSchema._ID + " INTEGER primary key autoincrement, "
                    + UserSchema._SENDER + " text not null, "
                    + UserSchema._TITTLE + " text null, "
                    + UserSchema._CONTENT + " text null, "
                    + UserSchema._MESSAGETOKEN + " text not null, "
                    + UserSchema._FILESIZE + " text not null, "
                    + UserSchema._DATE + " DATETIME not null, "
                    + UserSchema._FILEPATH + " text null, "
                    + UserSchema._RECEIVEID + " text null, "
                    + UserSchema._USESTATUS + " text null, "
                    + UserSchema._FILEID + " text null, "
                    + UserSchema._NOTIFICATION + " text null, "
                    + UserSchema._READ + " text null, "
                    + UserSchema._FIRST + " text null, "
                    + UserSchema._TYPE + " text null, "
                    + UserSchema._FILECOUNT + " text null, "
                    + UserSchema._FILENAME + " text null, "
                    + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                    + UserSchema._MSG + " text null "
                    + ");";

            db.execSQL(sql);
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.execSQL(sql3);
            db.execSQL(sql4);
            db.execSQL(sql5);
            db.execSQL(sql6);
            db.execSQL(sql7);
            db.execSQL(sql8);
            db.execSQL(sql9);
            db.execSQL(sql10);
        }
        //當出現版本不同時，更新table
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_FILE_CHOICE);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_FILE);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_DATA);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_GROUP);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_REPLY);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_INFO);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_VALIDATE);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_FFMPEG);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_CONTENT);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_MISSILE_GROUP);
            db.execSQL("DROP TABLE IF EXISTS " + UserSchema._TABLE_MISSILE_FIRE);
            onCreate(db);

        }

    }
    //先產生新的table
    public void new_table(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                String sql = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_FILE_CHOICE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text not null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._FILESIZE + " text null, "
                        + UserSchema._DURATION + " INTEGER null, "
                        + UserSchema._PATHID + " text nul "
                        + ");";db.execSQL(sql);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                String sql1 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_FILE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text not null, "
                        + UserSchema._FILERECORD + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._MESSAGETOKEN + " text null, "
                        + UserSchema._SELFID + " text not null, "
                        + UserSchema._FILECHECK + " INTEGER null "
                        + ");";
                db1.execSQL(sql1);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                String sql2 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_DATA + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILESIZE + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._RECEIVEID + " text null, "
                        + UserSchema._USESTATUS + " text null, "
                        + UserSchema._FILEID + " text null, "
                        + UserSchema._NOTIFICATION + " text null, "
                        + UserSchema._READ + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._TYPE + " text null, "
                        + UserSchema._FILECOUNT + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                        + UserSchema._MSG + " text null "
                        + ");";
                db2.execSQL(sql2);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                String sql3 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_GROUP + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null "
                        + ");";
                db3.execSQL(sql3);
                break;
            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                String sql4 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_REPLY + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILENAME + " text not null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._READY + " text null, "
                        + UserSchema._SELFID + " text null, "
                        + UserSchema._D2D + " text null, "
                        + UserSchema._MSG + " text not null "
                        + ");";
                db4.execSQL(sql4);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                String sql5 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_INFO + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._ACCOUNT + " text null, "
                        + UserSchema._REMEMBER + " text null, "
                        + UserSchema._PASSWORD + " text null "
                        + ");";
                db5.execSQL(sql5);
                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                String sql6 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_VALIDATE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._VALIDATE_CODE + " text not null, "
                        + UserSchema._VALIDATE + " text null "
                        + ");";
                db6.execSQL(sql6);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                String sql7 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_CONTENT + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._RECEIVER + " textnull, "
                        + UserSchema._TITTLE+ " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._MESSAGETOKEN + " text null, "
                        + UserSchema._SELFID + " text null, "
                        + UserSchema._DURATION + " text null, "
                        + UserSchema._TOTAL_TIME + " text null, "
                        + UserSchema._FINISH + " text null "
                        + ");";
                db7.execSQL(sql7);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                String sql8 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_FFMPEG + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._TOTAL_TIME + " text null, "
                        + UserSchema._SEQ_ID + " text null, "
                        + UserSchema._DURATION + " text null, "
                        + UserSchema._SELFID + " text null "
                        + ");";
                db8.execSQL(sql8);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                String sql9 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_MISSILE_GROUP + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._STATE + " text null, "
                        + UserSchema._FILEPATH + " text null "
                        + ");";
                db9.execSQL(sql9);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                String sql10 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_MISSILE_FIRE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILESIZE + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._RECEIVEID + " text null, "
                        + UserSchema._USESTATUS + " text null, "
                        + UserSchema._FILEID + " text null, "
                        + UserSchema._NOTIFICATION + " text null, "
                        + UserSchema._READ + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._TYPE + " text null, "
                        + UserSchema._FILECOUNT + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                        + UserSchema._MSG + " text null "
                        + ");";
                db10.execSQL(sql10);
                break;
        }
    }
    //當要重新選擇上傳檔案時，要先刪除table，避免讀到上一次就的資料
    public void del_table(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                String sql_DT = "DROP TABLE IF EXISTS " + UserSchema._TABLE_FILE_CHOICE;
                db.execSQL(sql_DT);

                String sql = "CREATE TABLE "
                        + UserSchema._TABLE_FILE_CHOICE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text not null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._FILESIZE + " text null, "
                        + UserSchema._DURATION + " INTEGER null, "
                        + UserSchema._PATHID + " text null "
                        + ");";
                db.execSQL(sql);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                String sql_DT1 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_FILE;
                db1.execSQL(sql_DT1);

                String sql1 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_FILE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text not null, "
                        + UserSchema._FILERECORD + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._MESSAGETOKEN + " text null, "
                        + UserSchema._SELFID + " text not null, "
                        + UserSchema._FILECHECK + " INTEGER null "
                        + ");";

                db1.execSQL(sql1);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                String sql_DT2 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_DATA;
                db2.execSQL(sql_DT2);

                String sql2 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_DATA + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILESIZE + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._RECEIVEID + " text null, "
                        + UserSchema._USESTATUS + " text null, "
                        + UserSchema._FILEID + " text null, "
                        + UserSchema._NOTIFICATION + " text null, "
                        + UserSchema._READ + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._TYPE + " text null, "
                        + UserSchema._FILECOUNT + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                        + UserSchema._MSG + " text null "
                        + ");";
                db2.execSQL(sql2);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                String sql_DT3 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_GROUP;
                db3.execSQL(sql_DT3);

                String sql3 = "CREATE TABLE IF NOT EXISTS " + UserSchema._TABLE_USER_GROUP + " (" + UserSchema._ID + " INTEGER primary key autoincrement, " + UserSchema._SENDER + " text not null " + ");";
                db3.execSQL(sql3);
                break;

            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                String sql_DT4 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_REPLY;
                db4.execSQL(sql_DT4);

                String sql4 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_REPLY + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILENAME + " text not null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._READY + " text null, "
                        + UserSchema._SELFID + " text null, "
                        + UserSchema._D2D + " text null, "
                        + UserSchema._MSG + " text not null "
                        + ");";
                db4.execSQL(sql4);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                String sql_DT5 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_INFO;
                db5.execSQL(sql_DT5);
                String sql5 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_INFO + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._ACCOUNT + " text null, "
                        + UserSchema._REMEMBER + " text null, "
                        + UserSchema._PASSWORD + " text null "
                        + ");";
                db5.execSQL(sql5);

                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                String sql_DT6 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_USER_REPLY;
                db6.execSQL(sql_DT6);

                String sql6 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_USER_VALIDATE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._VALIDATE_CODE + " text not null, "
                        + UserSchema._VALIDATE + " text null "
                        + ");";
                db6.execSQL(sql6);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                String sql_DT7 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_CONTENT;
                db7.execSQL(sql_DT7);

                String sql7 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_CONTENT + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._RECEIVER + " text null, "
                        + UserSchema._TITTLE+ " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._MESSAGETOKEN + " text null, "
                        + UserSchema._SELFID + " text null, "
                        + UserSchema._DURATION + " text null, "
                        + UserSchema._TOTAL_TIME + " text null, "
                        + UserSchema._FINISH + " text null "
                        + ");";
                db7.execSQL(sql7);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                String sql_DT8 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_TEMP_FFMPEG;
                db8.execSQL(sql_DT8);

                String sql8 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_TEMP_FFMPEG + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._TOTAL_TIME + " text null, "
                        + UserSchema._SEQ_ID + " text null, "
                        + UserSchema._DURATION + " text null, "
                        + UserSchema._SELFID + " text null "
                        + ");";
                db8.execSQL(sql8);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                String sql_DT9 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_MISSILE_GROUP;
                db9.execSQL(sql_DT9);

                String sql9 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_MISSILE_GROUP + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._STATE + " text null, "
                        + UserSchema._FILEPATH + " text null "
                        + ");";
                db9.execSQL(sql9);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                String sql_DT10 = "DROP TABLE IF EXISTS " + UserSchema._TABLE_MISSILE_FIRE;
                db10.execSQL(sql_DT10);

                String sql10 = "CREATE TABLE IF NOT EXISTS "
                        + UserSchema._TABLE_MISSILE_FIRE + " ("
                        + UserSchema._ID + " INTEGER primary key autoincrement, "
                        + UserSchema._SENDER + " text not null, "
                        + UserSchema._TITTLE + " text null, "
                        + UserSchema._CONTENT + " text null, "
                        + UserSchema._MESSAGETOKEN + " text not null, "
                        + UserSchema._FILESIZE + " text not null, "
                        + UserSchema._DATE + " DATETIME not null, "
                        + UserSchema._FILEPATH + " text null, "
                        + UserSchema._RECEIVEID + " text null, "
                        + UserSchema._USESTATUS + " text null, "
                        + UserSchema._FILEID + " text null, "
                        + UserSchema._NOTIFICATION + " text null, "
                        + UserSchema._READ + " text null, "
                        + UserSchema._FIRST + " text null, "
                        + UserSchema._TYPE + " text null, "
                        + UserSchema._FILECOUNT + " text null, "
                        + UserSchema._FILENAME + " text null, "
                        + UserSchema._LENGTH_RECORD + " text null, "//20160905學長版本加回
                        + UserSchema._MSG + " text null "
                        + ");";
                db10.execSQL(sql10);
                break;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                db.delete(UserSchema._TABLE_FILE_CHOICE, selection, null);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                db1.delete(UserSchema._TABLE_TEMP_FILE, selection, null);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                db2.delete(UserSchema._TABLE_USER_DATA, selection, null);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                db3.delete(UserSchema._TABLE_USER_GROUP, selection, null);
                break;
            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                db4.delete(UserSchema._TABLE_USER_REPLY, selection, null);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                db5.delete(UserSchema._TABLE_USER_INFO, selection, null);
                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                db6.delete(UserSchema._TABLE_USER_VALIDATE, selection, null);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                db7.delete(UserSchema._TABLE_TEMP_CONTENT, selection, null);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                db8.delete(UserSchema._TABLE_TEMP_FFMPEG, selection, null);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                db9.delete(UserSchema._TABLE_MISSILE_GROUP, selection, null);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                db10.delete(UserSchema._TABLE_MISSILE_FIRE, selection, null);
                break;
        }
        return 0;

    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                db.insertOrThrow(UserSchema._TABLE_FILE_CHOICE, null, values);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                db1.insertOrThrow(UserSchema._TABLE_TEMP_FILE, null, values);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                db2.insertOrThrow(UserSchema._TABLE_USER_DATA, null, values);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                db3.insertOrThrow(UserSchema._TABLE_USER_GROUP, null, values);
                break;
            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                db4.insertOrThrow(UserSchema._TABLE_USER_REPLY, null, values);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                db5.insertOrThrow(UserSchema._TABLE_USER_INFO, null, values);
                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                db6.insertOrThrow(UserSchema._TABLE_USER_VALIDATE, null, values);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                db7.insertOrThrow(UserSchema._TABLE_TEMP_CONTENT, null, values);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                db8.insertOrThrow(UserSchema._TABLE_TEMP_FFMPEG, null, values);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                db9.insertOrThrow(UserSchema._TABLE_MISSILE_GROUP, null, values);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                db10.insertOrThrow(UserSchema._TABLE_MISSILE_FIRE, null, values);
                break;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        databasehelper = new MyDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables(UserSchema._TABLE_FILE_CHOICE);
                c = qb.query(db, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb1 = new SQLiteQueryBuilder();
                qb1.setTables(UserSchema._TABLE_TEMP_FILE);
                c = qb1.query(db1, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb2 = new SQLiteQueryBuilder();
                qb2.setTables(UserSchema._TABLE_USER_DATA);
                c = qb2.query(db2, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb3 = new SQLiteQueryBuilder();
                qb3.setTables(UserSchema._TABLE_USER_GROUP);
                c = qb3.query(db3, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb4 = new SQLiteQueryBuilder();
                qb4.setTables(UserSchema._TABLE_USER_REPLY);
                c = qb4.query(db4, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb5 = new SQLiteQueryBuilder();
                qb5.setTables(UserSchema._TABLE_USER_INFO);
                c = qb5.query(db5, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb6 = new SQLiteQueryBuilder();
                qb6.setTables(UserSchema._TABLE_USER_VALIDATE);
                c = qb6.query(db6, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb7 = new SQLiteQueryBuilder();
                qb7.setTables(UserSchema._TABLE_TEMP_CONTENT);
                c = qb7.query(db7, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb8 = new SQLiteQueryBuilder();
                qb8.setTables(UserSchema._TABLE_TEMP_FFMPEG);
                c = qb8.query(db8, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb9 = new SQLiteQueryBuilder();
                qb9.setTables(UserSchema._TABLE_MISSILE_GROUP);
                c = qb9.query(db9, projection, selection, selectionArgs, null, null, null);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                SQLiteQueryBuilder qb10 = new SQLiteQueryBuilder();
                qb10.setTables(UserSchema._TABLE_MISSILE_FIRE);
                c = qb10.query(db10, projection, selection, selectionArgs, null, null, null);
                break;
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        switch (mUriMatcher.match(uri)) {
            case URI_TYPE_TABLE1:
                SQLiteDatabase db = databasehelper.getWritableDatabase();
                db.update(UserSchema._TABLE_FILE_CHOICE, values, selection, null);
                break;
            case URI_TYPE_TABLE2:
                SQLiteDatabase db1 = databasehelper.getWritableDatabase();
                db1.update(UserSchema._TABLE_TEMP_FILE, values, selection, null);
                break;
            case URI_TYPE_TABLE3:
                SQLiteDatabase db2 = databasehelper.getWritableDatabase();
                db2.update(UserSchema._TABLE_USER_DATA, values, selection, null);
                break;
            case URI_TYPE_TABLE4:
                SQLiteDatabase db3 = databasehelper.getWritableDatabase();
                db3.update(UserSchema._TABLE_USER_GROUP, values, selection, null);
                break;
            case URI_TYPE_TABLE5:
                SQLiteDatabase db4 = databasehelper.getWritableDatabase();
                db4.update(UserSchema._TABLE_USER_REPLY, values, selection, null);
                break;
            case URI_TYPE_TABLE6:
                SQLiteDatabase db5 = databasehelper.getWritableDatabase();
                db5.update(UserSchema._TABLE_USER_INFO, values, selection, null);
                break;
            case URI_TYPE_TABLE7:
                SQLiteDatabase db6 = databasehelper.getWritableDatabase();
                db6.update(UserSchema._TABLE_USER_VALIDATE, values, selection, null);
                break;
            case URI_TYPE_TABLE8:
                SQLiteDatabase db7 = databasehelper.getWritableDatabase();
                db7.update(UserSchema._TABLE_TEMP_CONTENT, values, selection, null);
                break;
            case URI_TYPE_TABLE9:
                SQLiteDatabase db8 = databasehelper.getWritableDatabase();
                db8.update(UserSchema._TABLE_TEMP_FFMPEG, values, selection, null);
                break;
            case URI_TYPE_TABLE10:
                SQLiteDatabase db9 = databasehelper.getWritableDatabase();
                db9.update(UserSchema._TABLE_MISSILE_GROUP, values, selection, null);
                break;
            case URI_TYPE_TABLE11:
                SQLiteDatabase db10 = databasehelper.getWritableDatabase();
                db10.update(UserSchema._TABLE_MISSILE_FIRE, values, selection, null);
                break;
        }
        return 0;
    }

}