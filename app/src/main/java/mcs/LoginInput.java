package mcs;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.thunder.missile.MainActivity;
import com.example.thunder.missile.R;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.storage.messageProcess;
import login.mcs.LocUpdate;
import login.mcs.Login;
import login.mcs.Logout;
import login.mcs.Retrieve;
import login.mcs.User;
import missile.MainFragment;
import softwareinclude.ro.portforwardandroid.asyncTasks.openserver;
import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.UserSchema;
import upnp.service.SwitchPower;

/**
 * Created by jeremy on 2016/6/16.
 */
public class LoginInput extends AppCompatActivity implements View.OnClickListener ,PropertyChangeListener {

    Button bn_on,bn_in,bn_se,bn_ed,bn_logout;
    public static String login_name;
    public static String Homeip = "140.138.150.26";
    public static String method;
    public static openserver server;
    public static String connect_ip,connect_port;
    private int ss=0;
    private static final int UPDATE_LIST_MES = 0,UPDATE_FFMPEG=1,timeout=3,SHOW_NOTIFY=4;
    private boolean finishUpdateList = false;
    public String sms = null;
    Boolean idle=true;
    String[] aliveIp;
    String notify_msg=new String();
    ContentResolver conten;
    private NotificationManager gNotMgr = null;
    LocUpdate locup =new LocUpdate();
    Thread aliveThread;
    Thread listThread;
    int i = 0;
    List<String> fileList = new ArrayList<String>();
    String bdtoken =new String();
    String bdsender =new String();
    String bdtype =new String();
    String bdreretrieve = new String();
    private static final Logger log = Logger.getLogger(LoginInput.class.getName());
    private AndroidUpnpService upnpService;
    private UDN udn = UDN.uniqueSystemIdentifier("Demo Binary Light");
    ProgressDialog pdialog = null,re_ffmpeg_dialog=null;
    int tolen,hasRead;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);

        bn_on = (Button)findViewById(R.id.btn_no);
        bn_in = (Button)findViewById(R.id.btn_in);
        bn_se = (Button)findViewById(R.id.btn_se);
        bn_ed = (Button)findViewById(R.id.btn_ed);
        bn_logout = (Button)findViewById(R.id.btn_logout);

        bn_on.setOnClickListener(this);
        bn_in.setOnClickListener(this);
        bn_se.setOnClickListener(this);
        bn_ed.setOnClickListener(this);
        bn_logout.setOnClickListener(this);

        listThread = new Thread(runnable);
        aliveThread = new Thread(alive_run);
        listThread.start();
        aliveThread.start();
    }//end of onCreate
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();
        switch (v.getId()){
            case R.id.btn_no:
                intent.setClass(LoginInput.this,MainActivity.class);
                if (info == null || !info.isAvailable()) {
                    Toast.makeText(LoginInput.this, "目前沒有網路唷!所以無法進入通知", Toast.LENGTH_LONG).show();
                }else{
                    new getcontent().execute();
                }

                break;
            case R.id.btn_in:
                bundle.putString("location","1");
                intent.putExtras(bundle);
                intent.setClass(LoginInput.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_se:
                bundle.putString("location","2");
                intent.putExtras(bundle);
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                intent.setClass(LoginInput.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_ed:
                bundle.putString("location","3");
                intent.putExtras(bundle);
                intent.setClass(LoginInput.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_logout:
                AlertDialog.Builder DialogPreDl = new AlertDialog.Builder(this); // Dialog
                DialogPreDl.setTitle("");
                DialogPreDl.setMessage("確定要登出?");
                DialogPreDl.setIcon(android.R.drawable.ic_dialog_info);

                DialogPreDl.setPositiveButton("是", new DialogInterface.OnClickListener() { // 不接收檔案
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idle=true;
                        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = CM.getActiveNetworkInfo();
                        if (info == null || !info.isAvailable()) {
                            System.out.println("目前沒有網路唷");
                        }else{
                            Intent intent = new Intent();
                            intent.setClass(LoginInput.this,LoginActivity.class);
                            startActivity(intent);
                        }
                        LoginInput.this.finish();
                    }//雲端下載 end
                });

                DialogPreDl.setNeutralButton("否", new DialogInterface.OnClickListener() { // 預覽檔案
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                DialogPreDl.show();
                break;
        }//end of switch
        info=null;
        CM=null;

    }//end of onClick
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder DialogPreDl = new AlertDialog.Builder(this); // Dialog
        DialogPreDl.setTitle("");
        DialogPreDl.setMessage("確定要退出?");
        DialogPreDl.setIcon(android.R.drawable.ic_dialog_info);

        DialogPreDl.setPositiveButton("是", new DialogInterface.OnClickListener() { // 不接收檔案
            @Override
            public void onClick(DialogInterface dialog, int which) {
                idle=true;
                ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = CM.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {
                    System.out.println("目前沒有網路唷");
                }else{
                    Logout logout = new Logout();
                    logout.logout_start();

                }

                LoginInput.this.finish();
            }//雲端下載 end
        });

        DialogPreDl.setNeutralButton("否", new DialogInterface.OnClickListener() { // 預覽檔案
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        DialogPreDl.show();
    }//end ConfirmExit

    /**
     * 這裡會一直在背景程式執行，因為要主動去刷新資料，檢查是否有新的通知
     */
    private Handler mHandler = new Handler() {
        // 此方法在ui線程運行
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST_MES:
                    checkSMS();
                    break;
                case UPDATE_FFMPEG:
                    if(hasRead<=tolen){
                        re_ffmpeg_dialog.setProgress(hasRead);
                    }else{
                        re_ffmpeg_dialog.dismiss();
                    }
                    break;

                case timeout:
                    AlertDialog.Builder Dialog0 = new AlertDialog.Builder(LoginInput.this); // Dialog
                    Dialog0.setTitle("連線逾時");
                    Dialog0.setMessage("請稍候在試");
                    Dialog0.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog0.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    Dialog0.show();
                    break;
                case SHOW_NOTIFY:
                    Toast.makeText(getApplicationContext(),notify_msg, ss).show();
                    notify_msg="";
                    ss=0;
                    break;
            }
        }
    };//end of mHandler
    public void checkSMS() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 將date
        String tempdate;
        // uri是連結內建DB的一個方式，這裡要去抓簡訊資料庫中的收件夾
        Uri SMS_INBOX = Uri.parse("content://sms/inbox");
        Cursor new_sms_cursor = getContentResolver().query(SMS_INBOX, new String[] { "address", "date", "body" }, "address='0903410141' or address='+886903410141'", null, null);

        // 檢查是否有新的sms
        if (new_sms_cursor.getCount() > 0) {
            new_sms_cursor.moveToFirst();
            for (int i = 0; i < new_sms_cursor.getCount(); i++) {
                // decode到一般人看得懂得型式
                Date d = new Date(Long.parseLong(new_sms_cursor.getString(new_sms_cursor.getColumnIndex("date"))));
                tempdate = dateFormat.format(d);

                sms = new_sms_cursor.getString(new_sms_cursor.getColumnIndex("body"));
                messageProcess MsgSave = new messageProcess();
                MsgSave.insertdata(getContentResolver(), sms, tempdate);
                MsgSave = null;
                // 自定義的函式
                new_sms_cursor.moveToNext();
            }// end for
        }// end if
        new_sms_cursor.close();
    }//end of checkSMS
    private void updateNotification(String state, String type, String token) {
        Cursor noti_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[] { FileContentProvider.UserSchema._ID, FileContentProvider.UserSchema._NOTIFICATION }, "messagetoken='" + token + "'", null, null);
        if (noti_cursor.getCount() > 0) {
            noti_cursor.moveToFirst();
            ContentValues values = new ContentValues();
            values.put(UserSchema._NOTIFICATION, state);
            values.put(UserSchema._TYPE, type);
            int id_this = Integer.parseInt(noti_cursor.getString(0));
            String where = FileContentProvider.UserSchema._ID + " = " + id_this;
            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);

        }
        noti_cursor.close();
    }//end of updateNotification
    // 更新資料庫狀態
    public void updateContent(Uri location, String mod, String condition) {
        int id_this;
        String where, content, token, tittle;
        String[] Form = { UserSchema._ID, UserSchema._MESSAGETOKEN };
        String[] reretrieve = new String[5];

        Cursor up_content = getContentResolver().query(location, Form, condition, null, null);
        if (up_content.getCount() > 0) {
            up_content.moveToFirst();
            Retrieve retreive =new Retrieve();
            for (int i = 0; i < up_content.getCount(); i++) {
                token = up_content.getString(1);
                reretrieve = retreive.retrieve_req(token, mod);
                if (reretrieve[0].equals("true")) {
                    if(retreive.retrieveFileCount.length > 3){
                        content = reretrieve[1].substring(reretrieve[1].indexOf("content=") + 8, reretrieve[1].indexOf("&file"));
                    }else{
                        content = reretrieve[1].substring(reretrieve[1].indexOf("content=") + 8, reretrieve[1].length()-1);
                    }
                    id_this = Integer.valueOf(up_content.getString(0));
                    ContentValues values = new ContentValues();
                    if (mod.equals("retrievable")) {
                        tittle = reretrieve[1].substring(reretrieve[1].indexOf("subject=") + 8, reretrieve[1].indexOf("&content="));
                        values.put(UserSchema._CONTENT, content);
                        values.put(UserSchema._TITTLE, tittle);
                        values.put(UserSchema._USESTATUS, "");
                        values.put(UserSchema._FILEPATH, "");
                    }else{
                        values.put(UserSchema._FILENAME, content);
                    }

                    where = UserSchema._ID + " = " + id_this;
                    getContentResolver().update(location, values, where, null);

                }
                up_content.moveToNext();
            }

        }
        up_content.close();
    }//end of updateContent
    private void updateState(String state, String conditional) {
        Cursor change_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[] { FileContentProvider.UserSchema._ID, FileContentProvider.UserSchema._RECEIVEID }, conditional, null, null);
        if (change_state.getCount() > 0) {
            change_state.moveToFirst();
            for (int i = 0; i < change_state.getCount(); i++) {
                int id_this = 0;
                id_this = Integer.valueOf(change_state.getString(0));
                ContentValues values = new ContentValues();
                values.put(UserSchema._USESTATUS, state);
                String where = UserSchema._ID + " = " + id_this;
                getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                change_state.moveToNext();
            }

        }
        change_state.close();
    }//end of updateState


    // 建立工作清單，每30秒檢查
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (!finishUpdateList) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //Thread.currentThread().interrupt();
                    Log.i("update list thread", "Thread.currentThread().isInterrupted()");
                }
//					if (!retrieving){
//						mHandler.obtainMessage(UPDATE_LIST_MES).sendToTarget(); // 傳送要求更新list的訊息給handler
//					}
                ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = CM.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {
                    System.out.println("目前沒有網路唷");
                }else{
                    mHandler.obtainMessage(UPDATE_LIST_MES).sendToTarget(); // 傳送要求更新list的訊息給handler
                    String[] aliveIp = locup.locationupdate(Login.latest_cookie, AttachParameter.getIPAddress(getApplicationContext()), AttachParameter.port);
                    if (aliveIp.length>0&&aliveIp[1].equalsIgnoreCase("true")){
                        messageProcess MsgSave = new messageProcess();
                        MsgSave.checkwlan(getContentResolver(), aliveIp[4]);
                        updateContent(Uri.parse("content://tab.list.d2d/user_data"), "retrievable","content is null");
                        updateContent(Uri.parse("content://tab.list.d2d/user_reply"), "reply","filename is null");
                        MsgSave=null;
                        if( aliveIp[4].length()>20){
                            Pattern pattern_content = Pattern.compile("content");
                            Matcher matcher_content = pattern_content.matcher(aliveIp[4]);
                            Pattern pattern_d2d = Pattern.compile("d2d");
                            Matcher matcher_d2d = pattern_d2d.matcher(aliveIp[4]);
                            if(matcher_content.find()){
                                final int notifyID = 2;
                                final int requestCode = notifyID;


                                //final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                                Bundle bundle=new Bundle();
                                bundle.putString("location","0" );
                                // intent.setClass(writepage.this, browse.class);

                                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                notificationIntent.putExtras(bundle);
                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, notificationIntent, 0);
                                //final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon).setContentTitle("內容標題").setContentText("內容文字").setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                                gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.m2m_ver01).setContentTitle("你有一則新訊息").setContentText("有人寄信給你了!請快來讀取!").setDefaults(Notification.DEFAULT_SOUND).setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                                gNotMgr.notify(notifyID, notification); // 發送通知
                                notify_msg="你有一則新訊息，快看是誰";
                                ss=1;
                                mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                            }
                            else if(matcher_d2d.find()){
                                final int notifyID = 2;
                                final int requestCode = notifyID;
                                //final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                                Bundle bundle=new Bundle();
                                bundle.putString("location","0" );
                                // intent.setClass(writepage.this, browse.class);

                                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                notificationIntent.putExtras(bundle);
                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, notificationIntent, 0);
                                //final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon).setContentTitle("內容標題").setContentText("內容文字").setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                                gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.m2m_ver01).setContentTitle("嘿!有人對你的訊息感到有趣").setContentText("請你開啟server").setDefaults(Notification.DEFAULT_SOUND).setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                                gNotMgr.notify(notifyID, notification); // 發送通知
                                notify_msg="有人要求你開啟server，趕快前往瀏覽";
                                ss=2;
                                mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();

                            }

                        }
                        aliveIp=null;
                    }
                }
                info=null;
                CM=null;
            }
        }
    };//end of runnable
    // 建立工作清單，每10秒檢查
    Runnable alive_run = new Runnable() {
        @Override
        public void run() {
            String self_id,file_where,token=new String(),d2d_id;
            final String[] check_down = {UserSchema._MESSAGETOKEN, UserSchema._SENDER, UserSchema._TYPE};
            final String[] check_up = {UserSchema._MESSAGETOKEN, UserSchema._SENDER, UserSchema._ID, UserSchema._SENDER, UserSchema._SELFID};
            final String[] id_up = {UserSchema._MESSAGETOKEN, UserSchema._SELFID};
            final String[] temp_up = {UserSchema._ID, UserSchema._SELFID};
            while (!finishUpdateList) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //Thread.currentThread().interrupt();

                }
                ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = CM.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {
                    System.out.println("目前沒有網路唷");
                }else{
                    //================20160905加回學長版本==========//
                    Cursor down = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._NOTIFICATION, UserSchema._MESSAGETOKEN, UserSchema._USESTATUS}, null, null, null);
                    if(down.getCount()>0){
                        down.moveToFirst();
                        for(int i=0;i<down.getCount();i++){
                            System.out.println("TEST_1 = "+down.getString(0));
                            System.out.println("TEST_12 = "+down.getString(1));
                            System.out.println("TEST_13 =" +down.getString(2));
                            down.moveToNext();
                        }
                    }down.close();
                    //================20160905加回學長版本==========//

                    //20160905加回userstatus=''"
                    Cursor down_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), check_down, "(notification = '1' or notification = '2') and userstatus=''", null, null);
                    if(down_cursor.getCount()>0){
                        down_cursor.moveToFirst();
                        for (int i = 0; i < down_cursor.getCount(); i++) {
                            while(!idle){
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    //Thread.currentThread().interrupt();
                                }
                            }
                            if(bdtoken.equalsIgnoreCase(down_cursor.getString(0))){
                                //do noting
                            }
                            else{
                                idle=false;
                                bdtoken =down_cursor.getString(0);
                                bdsender =down_cursor.getString(1);
                                bdtype =down_cursor.getString(2);
                                if(bdtype.equalsIgnoreCase("wlan")||bdtype.equalsIgnoreCase("sms")){
                                    Retrieve retrieve = new Retrieve();
                                    bdreretrieve = retrieve.check_file(bdtoken);
                                    Boolean res= AttachParameter.chechsuccess(bdreretrieve);
                                    if (res) {
                                        updateNotification("3", bdtype,bdtoken);
                                        new startretrieve().execute();
                                    }
                                    else{
                                        idle=true;
                                        bdtoken="";
                                    }
                                    retrieve =null;
                                }else if(bdtype.equalsIgnoreCase("d2d")){
                                    User user = new User();
                                    String[] res =user.getservicetime(bdsender);

                                    if(res[1].equalsIgnoreCase("true")){
                                        String[] socketport = res[2].replace("socket=", "").split(":");
                                        //取出ip 跟port
                                        //檢查目標是否有內外網
                                        if(AttachParameter.out_ip.equals(socketport[0])){
                                            //外網ip相同，給內網ip
                                            LoginInput.connect_ip=socketport[1];
                                            System.out.println("這邊是 d2d socketport[1]="+socketport[1]);
                                        }else{
                                            //外網ip不同，給外網
                                            LoginInput.connect_ip=socketport[0];
                                            System.out.println("這邊是 d2d socketport[0]="+socketport[0]);
                                        }
                                        LoginInput.connect_port=socketport[2];
                                        updateNotification("3", "d2d",bdtoken);
                                        new startretrieve().execute();

                                    }else{
                                        idle=true;
                                        bdtoken="";
                                    }
                                    user = null;
                                }
                                down_cursor.moveToNext();

                            }

                        }

                    }
                    down_cursor.close();

                    //d2d專用
                    Cursor get_d2d_id = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), id_up,"finish ='yes'", null, null);
                    //檢查該檔案是否切割完畢
                    if(get_d2d_id.getCount()>0){
                        get_d2d_id.moveToFirst();
                        for(int i=0;i<get_d2d_id.getCount();i++){
                            token=get_d2d_id.getString(0);
                            d2d_id=get_d2d_id.getString(1);
                            //對方收訊訊息要求檔案，但發現卻沒有token，代表token沒收到，重新發送要求取回token
                            if(token == null){
                                //執行republish，要求token,不能使用非同步
                            }else{
                                //token存在，也切割完畢
                                Cursor file_token = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), temp_up, "selfid='"+d2d_id +"' and messagetoken is null", null, null);
                                //檢查temp_file內的檔案是否也更新上token
                                if(file_token.getCount()>0){
                                    //進行token更新
                                    ContentValues values = new ContentValues();
                                    file_token.moveToFirst();
                                    values.put(UserSchema._MESSAGETOKEN, token);
                                    for (int a = 0; a < file_token.getCount(); a++) {
                                        int id_this = Integer.parseInt(file_token.getString(0));
                                        file_where = UserSchema._ID + " = " + id_this;
                                        getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, file_where, null);
                                        file_token.moveToNext();
                                    }

                                }else{
                                    //temp_file內的token已經全部更新
                                }
                                file_token.close();
                                //更新reply的ready

                            }
                            get_d2d_id.moveToNext();
                        }

                    }else{
                        //檔案還沒切割好，等待背景自動切完
                    }
                    get_d2d_id.close();

                    Cursor ch_id = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_reply"), check_up, "ready is null", null, null);
                    //發現有還沒READY的資料，代表該筆還不能上傳，開始逐一檢查原因
                    if(ch_id.getCount()>0){
                        Log.i("LOGININPUT", "開始判斷");
                        ch_id.moveToFirst();
                        for(int i =0;i<ch_id.getCount();i++){
                            self_id=ch_id.getString(4);
                            Cursor get_id = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), id_up, "selfid='"+self_id +"' and finish ='yes'", null, null);
                            //檢查該檔案是否切割完畢
                            if(get_id.getCount()>0){
                                get_id.moveToFirst();
                                token=get_id.getString(0);
                                //對方收訊訊息要求檔案，但發現卻沒有token，代表token沒收到，重新發送要求取回token
                                if(token.equals("")){
                                    //執行republish，要求token,不能使用非同步
                                }else{
                                    //token存在，也切割完畢
                                    Cursor file_token = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), temp_up, "selfid='"+self_id +"' and messagetoken is null", null, null);
                                    //檢查temp_file內的檔案是否也更新上token
                                    if(file_token.getCount()>0){
                                        //進行token更新
                                        ContentValues values = new ContentValues();
                                        file_token.moveToFirst();
                                        values.put(UserSchema._MESSAGETOKEN, token);
                                        for (int a = 0; a < file_token.getCount(); a++) {
                                            int id_this = Integer.parseInt(file_token.getString(0));
                                            file_where = UserSchema._ID + " = " + id_this;
                                            getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, file_where, null);
                                            file_token.moveToNext();
                                        }

                                    }else{
                                        //temp_file內的token已經全部更新
                                    }
                                    file_token.close();
                                    //更新reply的ready
                                    ContentValues values = new ContentValues();
                                    values.put(UserSchema._READY, "yes");
                                    int id_this = Integer.parseInt(ch_id.getString(2));
                                    file_where = UserSchema._ID + " = " + id_this;
                                    getContentResolver().update(Uri.parse("content://tab.list.d2d/user_reply"), values, file_where, null);
                                }

                            }else{
                                //檔案還沒切割好，等待背景自動切完
                            }
                            get_id.close();

                            ch_id.moveToNext();
                        }
                        Log.i("LOGININPUT", "結束判斷");
                    }else{
                        //沒有發現ready為空的，代表reply的檢查過了
                    }
                    ch_id.close();
                }
                info=null;
                CM=null;

            }
        }
    };//end of alive_run
    /*
    *這段功能負責接收檔案,
    */
    private class startretrieve extends AsyncTask<Void, Void, String[]> {// implement
            // thread
        public String[] aliveIp;
        public String checkfile = new String();
        public String getFilename = null;
        public String[] getSplit;
        public String token;
        public int id_this;
        public String where;
        String[] reretrieve = new String[5];
        String[] reretrieve_file = new String[5];
        String[] reretrieve_arg = new String[11];
        String[] Form = { UserSchema._ID, UserSchema._RECEIVEID, UserSchema._SENDER};
        Retrieve retrieve = new Retrieve();
        protected void onPreExecute() {
            final int notifyID = 0;
            //final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon).setContentTitle("內容標題").setContentText("內容文字").setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
            gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("檔案下載中").setAutoCancel(false).setProgress(0, 50, false).build(); // 建立通知
            gNotMgr.notify(notifyID, notification); // 發送通知
        }
        @Override
        protected String[] doInBackground(Void... params) {
            idle=false;
            // String[] refile = new String[4];

            aliveIp = locup.locationupdate(Login.latest_cookie, AttachParameter.getIPAddress(getApplicationContext()), AttachParameter.port);
            messageProcess MsgSave = new messageProcess();
            MsgSave.checkwlan(getContentResolver(), aliveIp[4]);
            MsgSave = null;
            // 取得最近IP
            token = bdtoken;

            retrieve.viewmod = "startview";
            // 這邊的DB，是要更新目前在user_data中的簡訊的狀態，改成下載中的狀態
            // cursor(資料表來源,欲抓取的欄位名稱,條件)
            // 抓取的欄位名稱：user_data資料表中ID值，條件：itemclick點擊時所得的token
            updateState("download", "messagetoken='" + token + "'");
            // 先做request的動作，傳入token去server，取得該token所持有的檔案id
            reretrieve = retrieve.retrieve_req_for_d2d(LoginInput.connect_ip, LoginInput.connect_port,bdtoken);
            reretrieve_arg[0]=reretrieve[0];
            reretrieve_arg[1]=reretrieve[1];
            reretrieve_arg[2]=reretrieve[2];
            reretrieve_arg[3]=reretrieve[3];
            reretrieve_arg[4]=reretrieve[4];

            //reretrieve = retrieve.retrieve_req(bdtoken, "");
            System.out.println("回復有問題之處 "+reretrieve);
            if (reretrieve[0] == "true"){ // retrieve_req=ret=0

                //========20160905加入===========//
                notify_msg="正在接收來自於: "+ LoginInput.connect_ip+"的"+retrieve.fileid[i].substring(retrieve.fileid[i].indexOf("/KM/")+4,retrieve.fileid[i].length());
                ss=10;
                mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                //========20160905加入===========//

                fileList.clear(); // 先將fileList清空 以免讀到上次的值
                getSplit = reretrieve[1].split("&");
				/*
				 * 因為檔案可能會下載失敗，但是哪一個失敗不知道，所以這邊DB的動作為，先紀錄要下載的檔案ID，
				 * 所以RECEIVEID是當檔案下載成功時，看哪一塊成功就去RECEIVEID中刪除
				 * 而FILEID是用來重新下載的用途
				 */
                Cursor up_fileid = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "messagetoken='" + token + "'", null, null);
                if (up_fileid.getCount() > 0) {
                    up_fileid.moveToFirst();
                    ContentValues values = new ContentValues();
                    values.put(UserSchema._RECEIVEID, "&"+reretrieve[1]);
                    values.put(UserSchema._FILEID, "&"+reretrieve[1]);
                    id_this = Integer.parseInt(up_fileid.getString(0));
                    where = UserSchema._ID + " = " + id_this;
                    getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                    values = null;

                }
                up_fileid.close();
                /*
				 * 這邊是抓檔案的ID，抓到後就直接執行自定義的saveBinaryFile下載檔案，必須告知
				 * saveBinaryFile目前最近的ip,以及現在要下載哪一塊檔案
				 */
                for (int j = 0; j < retrieve.fileid.length; j++) { // 抓檔案ID
                    try {

                        String DLfilename=new String();
                        //20160905學長加回(getContentResolver())
                        reretrieve_file =retrieve.saveBinaryFile_for_d2d(token, j, getContentResolver()); // 收取締n塊檔案

                        reretrieve_arg[5]=reretrieve_file[0];
                        reretrieve_arg[6]=reretrieve_file[1];
                        reretrieve_arg[7]=reretrieve_file[2];
                        reretrieve_arg[8]=reretrieve_file[3];
                        reretrieve_arg[9]=reretrieve_file[4];
                        if(reretrieve_file[0].equalsIgnoreCase("true")){
                            notify_msg="目前已下載完第"+(j+1)+"/"+retrieve.fileid.length;
                            ss=3;
                            mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                            DLfilename = retrieve.filename;
                            // 篩選回傳的filename，Pattern是你要篩選的內容，Matcher是你要寵哪一個字串做篩選
                            boolean[] checktype = new boolean[AttachParameter.filetype];
                            checktype = AttachParameter.checktype(DLfilename);
                            System.out.println("回復有問題之處3 "+DLfilename);
                            // 這裡DB的動作為，紀錄已收到的file id，會不斷地把成功的id，從DB中刪除
                            replaceSeq("messagetoken='" + token + "'", "retrive",reretrieve_file[1]);
                            // 如果這次下載的是影片
                            if (checktype[AttachParameter.video]||checktype[AttachParameter.photo]||checktype[AttachParameter.music]) {
                                // 將檔名加入倒arraylist
                                fileList.add(DLfilename);
                            }
                        }
                    } catch (IOException ex) {
                        reretrieve_arg[5]="server error";
                        reretrieve_arg[10]="true";
                    }catch(Exception ex){
                        reretrieve_arg[5]="server error";
                        reretrieve_arg[9]="true";
                    }

                }
            }


            return reretrieve;
        }

        protected void onPostExecute(String[] notuse) {
            String[]type;
            String name=new String(),filename=new String();
            updateState("", "messagetoken='" + token + "'");
            if (reretrieve_arg[0] == "true") {
                //retreive成功
                if(reretrieve_arg[5]=="true"){
                    /*
					 * cursor(資料表來源,欲抓取的欄位名稱,條件)//抓取的欄位名稱：user_data資料表中ID值，條件
					 * ：itemclick點擊時所得的token
					 *
					 * 因為已經下載完了，所以user_state把download取消
					 */
                    if (fileList.size() == 0) { // 若檔案不是video fileList=0

                        getFilename = retrieve.filename;
                        type =getFilename.split("\\.");//2016/06/30新增修改
                        filename=getFilename;//2016/07/04新增修改
                    } else { // 若檔案是video 則用&號串成一串，目的是要做影片清單(這裡的清單內容是是檔名串起來的)
                        getFilename = "";
                        filename=fileList.get(0);//2016/06/30新增修改
                        type=filename.split("\\.");
                        for (int i = 0; i < fileList.size(); i++)
                            getFilename = getFilename + "&" + token+"_"+fileList.get(i).replace(".", "-_" + String.valueOf(i) + ".");
                    }
                    // 因為都下載完了，所以更新filepath
                    Cursor up_filepath = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "messagetoken='" + token + "'", null, null);
                    ContentValues values = new ContentValues();
                    if (up_filepath.getCount() > 0) {
                        up_filepath.moveToFirst();
                        values.put(UserSchema._FILEPATH, getFilename);
                        int id_this = Integer.parseInt(up_filepath.getString(0));
                        name = up_filepath.getString(2);
                        //filename=up_filepath.getString(3);
                        String where = UserSchema._ID + " = " + id_this;
                        getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                    }
                    up_filepath.close();
                    updateNotification("0", "wlan",bdtoken);
                    bdtoken="";
                    idle=true;

                    //產生Notification物件，並設定基本屬性
                    final int notifyID = 0;
                    final int requestCode = notifyID;
                    //final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
                    Intent notificationIntent = new Intent(getApplicationContext(), ReceiveList.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("contact", name);
                    notificationIntent.putExtras(bundle);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, notificationIntent, 0);
                    //final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon).setContentTitle("內容標題").setContentText("內容文字").setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                    gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_sys_download_done).setContentTitle( filename+"."+type[1]).setContentText("成功接收來自"+name+"的檔案").setDefaults(Notification.DEFAULT_SOUND).setContentIntent(contentIntent).setAutoCancel(true).build(); // 建立通知
                    gNotMgr.notify(notifyID, notification); // 發送通知

                    notify_msg="成功下載來自"+name+"的"+filename+"."+type[1];
                    ss=5;
                    mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                }
                else if(reretrieve_arg[5]=="false"){
                    //retreive file 失敗 (得到200以外的code)
                    //要讀body
                    Pattern pattern_msg = Pattern.compile("message does not exist"); // check file type
                    Matcher matcher_msg = pattern_msg.matcher(reretrieve_arg[6]);
                    Pattern pattern_file = Pattern.compile("file is not ready"); // check file type
                    Matcher matcher_file = pattern_file.matcher(reretrieve_arg[6]);
                    Pattern pattern_file_II = Pattern.compile("file not found"); // check file type
                    Matcher matcher_file_II = pattern_file_II.matcher(reretrieve_arg[6]);
                    if(matcher_msg.find()){
                        notify_msg="訊息並不存在server上，請刪除此訊息";
                    }else if(matcher_file.find()){
                        notify_msg="對方檔案的發生錯誤，請刪除此訊息";
                    }else if(matcher_file_II.find()){
                        notify_msg="此檔案序號不存在，請刪除此訊息";
                    }
                    final int notifyID = 0;
                    gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("錯誤").setContentText(notify_msg).setAutoCancel(false).setProgress(0, 50, false).build(); // 建立通知
                    gNotMgr.notify(notifyID, notification); // 發送通知

                    ss=3;
                    mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                }else{

                    //server error
                    if(reretrieve_arg[7]!=null && reretrieve_arg[7].equalsIgnoreCase("true")){
                        notify_msg="警告，伺服器內部發生錯誤，請暫停使用MD2MD";
                    }else if(reretrieve_arg[8]!=null && reretrieve_arg[8].equalsIgnoreCase("true")){
                        notify_msg="警告，連結失敗，請暫停使用MD2MD";
                    }else if (reretrieve_arg[9]!=null && reretrieve_arg[9].equalsIgnoreCase("true")){
                        notify_msg="警告，未知的錯誤，請暫停使用MD2MD";
                    }else if (reretrieve_arg[10]!=null && reretrieve_arg[10].equalsIgnoreCase("true")){
                        notify_msg="警告，內部儲存空間發生錯誤，請暫停使用MD2MD";
                    }
                    final int notifyID = 0;
                    gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle("警告").setContentText(notify_msg).setAutoCancel(false).setProgress(0, 50, false).build(); // 建立通知
                    gNotMgr.notify(notifyID, notification); // 發送通知
                    ss=3;
                    mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
                }
            }else if(reretrieve_arg[0]=="false"){
                //retreive失敗 (得到200以外的code)
                //要讀body
                Pattern pattern_msg = Pattern.compile("message does not exist"); // check file type
                Matcher matcher_msg = pattern_msg.matcher(reretrieve_arg[1]);
                Pattern pattern_msg_II = Pattern.compile("file is not ready"); // check file type
                Matcher matcher_msg_II = pattern_msg_II.matcher(reretrieve_arg[1]);
                if(matcher_msg.find()){
                    notify_msg="訊息並不存在server上，請刪除此訊息";

                }else if(matcher_msg_II.find()){
                    notify_msg="錯誤的訊息編號，請刪除此訊息";
                }
                final int notifyID = 0;
                gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("錯誤").setContentText(notify_msg).setAutoCancel(false).setProgress(0, 50, false).build(); // 建立通知
                gNotMgr.notify(notifyID, notification); // 發送通知
                ss=3;

                mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
            }else{
                //server error
                if(reretrieve_arg[2]!=null && reretrieve_arg[2].equalsIgnoreCase("true")){
                    notify_msg="警告，伺服器內部發生錯誤，請暫停使用MD2MD";

                }else if(reretrieve_arg[3]!=null && reretrieve_arg[3].equalsIgnoreCase("true")){
                    notify_msg="警告，連結失敗，請暫停使用MD2MD";

                }else if (reretrieve_arg[4]!=null && reretrieve_arg[4].equalsIgnoreCase("true")){
                    notify_msg="警告，未知的錯誤，請暫停使用MD2MD";

                }
                final int notifyID = 0;
                gNotMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                final Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("警告").setContentText(notify_msg).setAutoCancel(false).setProgress(0, 50, false).build(); // 建立通知
                gNotMgr.notify(notifyID, notification); // 發送通知

                ss=3;
                mHandler.obtainMessage(SHOW_NOTIFY).sendToTarget();
            }

        }
    } // reretrieve[1]=="true"

    public void replaceSeq(String conditional, String mod, String name) {
        int id_this;
        String where;
        Cursor check_fileid_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[] { UserSchema._ID, UserSchema._RECEIVEID, UserSchema._FILEPATH }, conditional, null, null);

        if (check_fileid_cursor.getCount() > 0) {
            check_fileid_cursor.moveToFirst();
            ContentValues values = new ContentValues();
            // 假設split 的內容是 &10
            String[] split = (name).split("_-");
            // 假設check_id 的內容是&10&11&12&13
            String check_id = check_fileid_cursor.getString(1);
            // split_id[0~4]的內容分別是"",10,11,12,13
            String[] split_id = check_id.split("&");
            // 假設split_id大於2
            if (split_id.length != 2) {
                // &10& 替換成 &
                // 所以&10&11&12&13 會變成 &11&12&13
                check_id = check_id.replace("&" + split[0] + "&", "&");
            } else {// 當split_id長度等於2
                // split_id[0~1]的內容為 "",13
                // &13 替換成 ""
                // 所以&13 會不見
                check_id = check_id.replace("&" + split[0], "");
            }
            if (mod.equals("again")) {
                String check_path = check_fileid_cursor.getString(2) + "&" + name;
                values.put(UserSchema._FILEPATH, check_path);
            }
            System.out.println(check_id);
            values.put(UserSchema._RECEIVEID, check_id);
            id_this = Integer.parseInt(check_fileid_cursor.getString(0));
            where = UserSchema._ID + " = " + id_this;
            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
        }
        check_fileid_cursor.close();
    }//end of replaceSeq

    private class getcontent extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(LoginInput.this, "請稍候", "資料讀取中", true);
            pdialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            aliveIp = locup.locationupdate(Login.latest_cookie, AttachParameter.getIPAddress(getApplicationContext()), AttachParameter.port);

            if(aliveIp[0]!=null){

                messageProcess MsgSave = new messageProcess();
                MsgSave.checkwlan(getContentResolver(), aliveIp[4]);
                updateContent(Uri.parse("content://tab.list.d2d/user_data"), "retrievable","content is null");
                updateContent(Uri.parse("content://tab.list.d2d/user_reply"), "reply","filename is null");
                MsgSave=null;
                pdialog.dismiss();
                // 要加上失誤的判斷
                Bundle bundle=new Bundle();
                Intent intent = new Intent();
                bundle.putString("location","0" );
                // intent.setClass(writepage.this, browse.class);
                intent.putExtras(bundle);
                intent.setClass(LoginInput.this, MainActivity.class);
                // startActivityForResult(intent,0);
                startActivity(intent);

            }else{
                pdialog.dismiss();
                mHandler.obtainMessage(timeout).sendToTarget(); // 傳送要求更新list的訊息給handler
            }

            return null;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("status")) {
            log.info("Turning light: " + event.getNewValue());
            setLightbulb((Boolean) event.getNewValue());
        }
    }//end of propertyChange
    protected void setLightbulb(final boolean on) {
        runOnUiThread(new Runnable() {
            public void run() {
            }
        });
    }//end of setLightbulb
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }//end onKeyDown
    protected LocalService<SwitchPower> getSwitchPowerService() {
        if (upnpService == null)
            return null;

        LocalDevice binaryLightDevice;
        if ((binaryLightDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<SwitchPower>)
                binaryLightDevice.findService(new UDAServiceType("MASP", 1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AttachParameter.selfid="";
        finishUpdateList = true;

        LocalService<SwitchPower> switchPowerService = getSwitchPowerService();
        if (switchPowerService != null)
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .removePropertyChangeListener(this);

    }
}
