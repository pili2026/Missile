package mcs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.thunder.missile.R;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.storage.messageProcess;

import login.mcs.LocUpdate;
import login.mcs.Login;
import login.mcs.Logout;
import login.mcs.User;
import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.UserSchema;
import upnp.service.BrowserUpnpService;
import upnp.service.SwitchPower;
import upnp.service.UPnPDeviceFinder;


/**
 * Created by jeremy on 2016/6/27.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener,PropertyChangeListener {

    /*
	 * 當animimation結束後，呼叫此LoginActivity， 這裡是處理登入的資訊，同時也可以引導至註冊頁面
	 * 但新版沒用到
	 */
    private static final int change_word=0;
    private Button login, register, exit; // 宣告用來存取的變數
    private EditText editText_ID, editText_Password;
    private String getid, getpw, getip;
    public String loginRequestString = new String();
    public int id;
    public int i = 0;
    public static String login_name_old;
    public static String Homeip="140.138.150.26";
    public static String login_name;
    public String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/";
    private AndroidUpnpService upnpService;
    private static final Logger log = Logger.getLogger(LoginInput.class.getName());
    private UDN udn = UDN.uniqueSystemIdentifier("Demo Binary Light");
    private UPnPDeviceFinder mDevfinder  = null;
    private boolean finishUpdateList = false;
    ProgressDialog pdialog = null,Dialog_for_login=null,dialog;
    FileContentProvider KM_DB = new FileContentProvider();
    ContentResolver contentResolver;
    AppCompatActivity appCompatActivity;
    LocUpdate locup =new LocUpdate();
    String content;
    Boolean idle=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDevfinder=null;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        File file = new File(sdcardPath);
        if(!file.exists()){
            file.mkdir();
        }
        // 設定該activity所用的layout
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.Login);
        register = (Button) findViewById(R.id.Register);
//        exit = (Button) findViewById(R.id.Exit);
        editText_ID = (EditText) findViewById(R.id.Id);
        editText_Password = (EditText) findViewById(R.id.Password);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
//        exit.setOnClickListener(this);

        // 此為sqlite的DB的部分，新增一個table，存放所有"寄件者的簡訊"
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_data"));
        // 紀錄"寄件者"
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_group"));
        // 紀錄未上傳檔案
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_reply"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_info"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/temp_content"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/temp_ffmpeg"));
        String[] TestForm = { UserSchema._ID, UserSchema._SENDER,
                UserSchema._TITTLE, UserSchema._CONTENT,
                UserSchema._MESSAGETOKEN, UserSchema._FILESIZE,
                UserSchema._DATE, UserSchema._FILEPATH,
                UserSchema._RECEIVEID, UserSchema._USESTATUS,
                UserSchema._FILEID };
        updateState("", TestForm, "userstatus!='delete'");
        contentResolver=getContentResolver();
        appCompatActivity=LoginActivity.this;
        Cursor info_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_info"), null, null, null, null);
        if(info_cursor.getCount()==0){
            ContentValues values = new ContentValues();
            values.put(FileContentProvider.UserSchema._REMEMBER, "false");
            getContentResolver().insert(Uri.parse("content://tab.list.d2d/user_info"), values);
            values = null;
        }
        info_cursor.close();
        Cursor cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] {FileContentProvider.UserSchema._SELFID, FileContentProvider.UserSchema._MESSAGETOKEN }, null, null, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            for(int a=0;a<cursor.getCount();a++){
                String aa=cursor.getString(0);
                String bb=cursor.getString(1);
                String cc="";
                cursor.moveToNext();
            }
        }
        cursor.close();

    }//end onCreate
    @Override
    public void onClick(View v) {
        switch (v.getId()) {// 監聽是哪個button被按下
            case R.id.Login:
                try {
                    // 取得網路服務的實體
                    ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = CM.getActiveNetworkInfo();

                    State InternetState = info.getState();
                    System.out.println("InternetState = " + InternetState);
                    getid = editText_ID.getText().toString();
                    getpw = editText_Password.getText().toString();
                    getip = LoginActivity.Homeip;
                    // 判斷輸入的帳密是否有大於20
                    if (getid.length() > 20 || getpw.length() > 20)
                        Toast.makeText(this, "ID or Password can not over 20 characters", Toast.LENGTH_LONG).show();
                    else if(getid.length() == 0 || getpw.length() == 0){
                        Toast.makeText(LoginActivity.this, "帳號或密碼為空，請重新輸入", Toast.LENGTH_LONG).show();
                    }
                    // 判斷是否有網路
                    else if (info == null || !info.isAvailable()) {
                        Toast.makeText(LoginActivity.this, "無可用網路", Toast.LENGTH_LONG).show();
                    } else {
                        // 執行登入的動作
                        new ConnectHttp().execute();

                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "錯誤!請檢查網路", Toast.LENGTH_LONG).show();
                }
                // GoToTab();
                break;
            case R.id.Register:
                // 自定義的function，引導至註冊頁面
                Intent intent = new Intent();
                // 從LoginActivity 跳到 register
                intent.setClass(LoginActivity.this, Register.class);
                startActivity(intent);
                break;
//            case R.id.Exit:
//                ConfirmExit();
//                break;
        }
    }//end of onClick

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("status")) {
            log.info("Turning light: " + event.getNewValue());
            setLightbulb((Boolean) event.getNewValue());
        }
    }//end propertyChange
    /*
	 * 使用AsyncTask的方法，可把費時的工作丟到背景去執行，
	 * AsyncTask有三個方法可使用，分別是onPreExecute、doInBackground、onPostExecute
	 * 去設定說執行前、執行中、執行後 各要做什麼事情
	 */
    private class ConnectHttp extends AsyncTask<Void, Void, String[]> {

        AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this);

        protected void onPreExecute() {
            Dialog_for_login = ProgressDialog.show(LoginActivity.this, "請稍候", "登入中", true);
            Dialog_for_login.show();
        }

        // 在背景執行此method，執行完會跳到onPostExecute
        @Override
        public String[] doInBackground(Void... params) {

            String[] loginreturn = new String[3];
            // 將getid與getpw製作成登入資訊，做為日後連線傳入server的參數
            loginRequestString = "username=" + getid + "&password=" + getpw;
            login_name = loginRequestString;

			/*
			 * 使用login中的login method，回傳值是一個陣列 loginreturn[0]是用來檢查有無成功
			 * loginreturn[1]是server回傳的訊息
			 */
            loginreturn = locup.login(getip, loginRequestString);
            if(loginreturn[0] == "true"&& AttachParameter.chechsuccess(loginreturn[1])){
                //使用ssdp尋找port
                mHandler.obtainMessage(change_word).sendToTarget();
                if(mDevfinder == null){
                    mDevfinder = new UPnPDeviceFinder(true);
                }
                mHandler.obtainMessage(change_word).sendToTarget();
                content="檢查環境是否可用UPNP";
                ArrayList<String> devList = mDevfinder.getUPnPDevicesList();
                //擷取IP
                Map<String, String> map = new HashMap<String, String>();
                for(int i = 0;i<devList.size();i++){
                    String ss = devList.get(i);
                    String index ="";
                    String[]socket;
                    if (ss.indexOf("Location: http://")!= -1){
                        index = ss.substring(ss.indexOf("Location: http://")+17,ss.indexOf("Location: http://")+40);
                        index = index.substring(0,index.indexOf("/"));
                        socket = index.split(":");
                        map.put(socket[0],socket[1]);

                    }
                    else if(ss.indexOf("Location:http://")!= -1){
                        index = ss.substring(ss.indexOf("Location:http://")+16,ss.indexOf("Location:http://")+40);
                        index =index.substring(0,index.indexOf("/"));
                        socket=index.split(":");
                        map.put(socket[0],socket[1]);
                    }

                    //String index = ss.substring(ss.indexOf("Location: http://"),ss.indexOf(ss.indexOf('/'),ss.indexOf("Location: http://")));
                    System.out.println(index);
                }
                String ipp = AttachParameter.getIPAddress(getApplicationContext());

                System.out.println(ipp);
                content="等待接收IP及連接埠";
                mHandler.obtainMessage(change_word).sendToTarget();
                //有問題,3G無法使用
                try{
                    AttachParameter.port= Integer.valueOf(map.get(ipp.substring(ipp.indexOf("in_ip=")+6,ipp.indexOf("&"))))+1;
                    //att_parameter.port=5555;

                }catch(Exception e){
                    AttachParameter.port=9527;
                }
                String[] aliveIp = locup.locationupdate(Login.latest_cookie, ipp , AttachParameter.port);
                messageProcess MsgSave = new messageProcess();
                MsgSave.checkwlan(getContentResolver(), aliveIp[4]);
//				updateContent(Uri.parse("content://tab.list.d2d/user_data"), "retrievable","content is null");
                MsgSave = null;
                //初始化時間
                User user = new User();
                String res =user.setservicetime("H=0&M=0");
                user=null;
            }

            return loginreturn;
        }//end doInBackground

        // onPostExecute 會接收 doInBackground 的return
        @Override
        protected void onPostExecute(String loginreturn[]) {
            Dialog_for_login.dismiss();
            mDevfinder = null;
            if (loginreturn[0] == "true"){ // 如果有server有回應

                Boolean result = AttachParameter.chechsuccess(loginreturn[1]);
                if (result) // 若帳號密碼正確 到下一頁
                {
                    ContentValues values = new ContentValues();
                    values.put(FileContentProvider.UserSchema._ACCOUNT, getid);
                    values.put(FileContentProvider.UserSchema._PASSWORD, getpw);

                    getContentResolver().insert(Uri.parse("content://tab.list.d2d/user_info"), values);

                    // 關掉Dialog的訊息
                    Intent intent = new Intent();
                    // 此意圖會呼叫LoginActivity.class這
                    //intent.setClass(LoginActivity.this, LoginActivity.class);
                    intent.setClass(LoginActivity.this, LoginInput.class);

                    // 開啟這個活動
                    startActivity(intent);
                    LoginActivity.this.finish();
                }else{
                    //server有回應，但是ret=1;
                    Pattern pattern_user = Pattern.compile("User name or Password is not correct");
                    Matcher error_user = pattern_user.matcher(loginreturn[1]);
                    Pattern pattern_activity = Pattern.compile("No Permission");
                    Matcher error_activity = pattern_activity.matcher(loginreturn[1]);
                    if(error_user.find()){
                        final AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this); // Dialog
                        Dialog.setTitle("抱歉!!");
                        Dialog.setMessage("您輸入的帳號或密碼錯誤，請重新確認後再進行登入");
                        Dialog.setIcon(android.R.drawable.ic_dialog_info);
                        Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 按下abort
                            // 將thread結束
                            // 隱藏progressbar
                            // 設定按鈕當按下時結束，結束這個Dialog並中斷這個thread
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ConnectHttp.cancel(ConnectCancel);

                                // dialog.cancel();
                            }
                        });
                        Dialog.show();
                    }else if(error_activity.find()){
                        final AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this); // Dialog
                        Dialog.setTitle("抱歉!!");
                        Dialog.setMessage("您的帳號尚未進行驗證，請重新驗證");
                        Dialog.setIcon(android.R.drawable.ic_dialog_info);
                        Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 按下abort
                            // 將thread結束
                            // 隱藏progressbar
                            // 設定按鈕當按下時結束，結束這個Dialog並中斷這個thread
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ConnectHttp.cancel(ConnectCancel);

                                // dialog.cancel();
                            }
                        });
                        Dialog.show();
                    }

                }
            } else {

                if (loginreturn[2] == "false")
                // no response from http,ask user to wait or retry
                {

                    AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this); // Dialog
                    Dialog.setTitle("警告");
                    Dialog.setMessage("網路異常,請重新再試");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNeutralButton("重試", new DialogInterface.OnClickListener() { // 按下retry
                        // 將thread結束
                        // 再跑一個新的thread
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread.currentThread().interrupt();
                            // 重新執行
                            new ConnectHttp().execute();
                        }
                    });
                    Dialog.setNegativeButton("停止", new DialogInterface.OnClickListener() { // 按下abort
                        // 將thread結束
                        // 隱藏progressbar
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    Dialog.show();
                }

                else {
                    AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this); // Dialog
                    Dialog.setTitle("警告");
                    Dialog.setMessage("登入失敗");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNeutralButton("重試", new DialogInterface.OnClickListener() { // 按下retry
                        // 將thread結束
                        // 再跑一個新的thread
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread.currentThread().interrupt();
                            new ConnectHttp().execute();
                        }
                    });
                }
            }
        }


        @Override
        public void onCancelled() {
            // 停止使用task
            new ConnectHttp().cancel(true);
            Dialog.create().dismiss();
        }//end onCancelled
    }//end ConnectHttp

    // 更新資料庫狀態
    private void updateState(String state, String[] Form, String conditional) {
        Cursor change_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, conditional, null, null);
        if (change_state.getCount() > 0) {
            change_state.moveToFirst();
            for (int i = 0; i < change_state.getCount(); i++) {
                int id_this = 0;
                id_this = Integer.valueOf(change_state.getString(0));
                ContentValues values = new ContentValues();
                values.put(FileContentProvider.UserSchema._USESTATUS, state);
                String where = FileContentProvider.UserSchema._ID + " = " + id_this;
                getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                change_state.moveToNext();
            }
        }
        change_state.close();
    }//end updateState
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            LocalService<SwitchPower> switchPowerService = getSwitchPowerService();

            // Register the device when this activity binds to the service for the first time
            if (switchPowerService == null) {
                try {
                    LocalDevice binaryLightDevice = createDevice();

                    Toast.makeText(LoginActivity.this, R.string.registering_demo_device, Toast.LENGTH_SHORT).show();
                    upnpService.getRegistry().addDevice(binaryLightDevice);

                    switchPowerService = getSwitchPowerService();

                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Creating demo device failed", ex);
                    Toast.makeText(LoginActivity.this, R.string.create_demo_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Obtain the state of the power switch and update the UI
            setLightbulb(switchPowerService.getManager().getImplementation().getStatus());

            // Start monitoring the power switch
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .addPropertyChangeListener(LoginActivity.this);

        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };//end ServiceConnection
    protected LocalService<SwitchPower> getSwitchPowerService() {
        if (upnpService == null)
            return null;

        LocalDevice binaryLightDevice;
        if ((binaryLightDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<SwitchPower>)
                binaryLightDevice.findService(new UDAServiceType("MASP", 1));
    }

    protected LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException {

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "MASP",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails("1705A_MASP", "TEST UPNP", "v1")
                );

        LocalService service =
                new AnnotationLocalServiceBinder().read(SwitchPower.class);

        service.setManager(
                new DefaultServiceManager<SwitchPower>(service, SwitchPower.class)
        );

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
                service
        );
    }//end LocalDevice

    protected void setLightbulb(final boolean on) {
        runOnUiThread(new Runnable() {
            public void run() {
            }
        });
    }//end setLightbulb

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case change_word:
                    Dialog_for_login.setMessage(content);
                    break;
            }
        }
    };
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ConfirmExit();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }//end onKeyDown
    @Override
    protected void onDestroy() {
        super.onDestroy();

        AttachParameter.selfid="";
        finishUpdateList = true;

        LocalService<SwitchPower> switchPowerService = getSwitchPowerService();
        if (switchPowerService != null)
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .removePropertyChangeListener(this);
        getApplicationContext().unbindService(serviceConnection);

    }
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

                LoginActivity.this.finish();
            }//雲端下載 end
        });

        DialogPreDl.setNeutralButton("否", new DialogInterface.OnClickListener() { // 預覽檔案
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        DialogPreDl.show();
    }//end ConfirmExit
    
}
