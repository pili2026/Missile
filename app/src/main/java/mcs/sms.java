package mcs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.thunder.missile.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import database.storage.messageProcess;
import login.mcs.RegFromServer;
import tab.list.AttachParameter;

/**
 * Created by jeremy on 2016/6/29.
 */
public class sms extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_LIST_MES = 0;
    EditText recsms;
    String smscode;
    Boolean keeprun=true;
    Button Bvalidate;
    Thread aliveThread;
    public String sms = null;
    long time;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Validate");
        Intent data = getIntent();
        Bundle bundle = data.getExtras();
        // 取得傳入的contact,說是filename 其實是sender(寄件者)
        phone = bundle.getString("phone");
        // TODO Put your code here
        setContentView(R.layout.sms);
        recsms = (EditText) this.findViewById(R.id.recsms);
        Bvalidate=(Button) this.findViewById(R.id.Bvalidate);
        Bvalidate.setOnClickListener(this);
        aliveThread = new Thread(runnable);
        aliveThread.start();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.Bvalidate:
                new validate().execute();
                break;
        }
    }

    private Handler mHandler = new Handler() {
        // 此方法在ui線程運行
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST_MES:
                    recsms.setText(smscode);

                    break;
            }
        }
    };

    // 建立工作清單，每30秒檢查
    Runnable runnable = new Runnable() {
        String smsbody;
        String[]splitbody;
        long smstime,timenow,min;
        @Override
        public void run() {
            while (keeprun) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.i("update list thread", "Thread.currentThread().isInterrupted()");
                }

                Uri SMS_INBOX = Uri.parse("content://sms/inbox");
                Cursor new_sms_cursor = getContentResolver().query(SMS_INBOX, new String[] { "address", "date", "body" }, "address='0903410141' or address='+886903410141'", null, null);
                if(new_sms_cursor.getCount()>0){
                    new_sms_cursor.moveToFirst();
                    for (int i = 0; i < new_sms_cursor.getCount(); i++) {
                        smsbody=sms = new_sms_cursor.getString(new_sms_cursor.getColumnIndex("body"));
                        splitbody=smsbody.split("&");
                        if(splitbody.length==1){
                            //取得簡訊時間
                            smstime= Long.parseLong(new_sms_cursor.getString(new_sms_cursor.getColumnIndex("date")));
                            //Date mDate = new Date();
                            //取得現在時間
                            //timenow=mDate.getTime();
                            //檢查時間是否在規定內

                            Date mDate = new Date();
                            time=mDate.getTime();

                            min=(smstime-time)/(1000*60);
                            if(min>=0){
                                keeprun=false;
                                //splitbody=smsbody.split(" ");
                                smscode=smsbody.substring(smsbody.indexOf(':')+1,smsbody.indexOf(','));
                                mHandler.obtainMessage(UPDATE_LIST_MES).sendToTarget(); // 傳送要求更新list的訊息給handler
                                break;
                            }else{
                                splitbody=null;
                                new_sms_cursor.moveToNext();
                            }
                        }else{
                            splitbody=null;
                            new_sms_cursor.moveToNext();
                        }

                    }	//for end
                }//if end
                new_sms_cursor.close();
            }//while end
        }
    };
    /*
	 * 當按下註冊紐時所要做的事
	 */
    private class validate extends AsyncTask<Void, Void, String> {
        AlertDialog.Builder Dialog = new AlertDialog.Builder(sms.this);
        ProgressDialog progressDialog = null;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(sms.this, "請稍候", "驗證中", true);
        }
        @Override
        protected String doInBackground(Void... arg0) {

            RegFromServer validte=new RegFromServer();
            String resp=validte.validate(smscode);

            // TODO Auto-generated method stub
            return resp;
        }
        @Override
        protected void onPostExecute(String resp) {
            progressDialog.dismiss();
            String res=new String();
            Boolean result = AttachParameter.chechsuccess(resp);
            if(result){
                res="true";
            }
            else{
                res="false";
            }
            if(res.equalsIgnoreCase("true")){
                AlertDialog.Builder Dialog = new AlertDialog.Builder(sms.this); // Dialog
                Dialog.setTitle("恭喜");
                Dialog.setMessage("註冊成功，請按確定鍵返回主畫面");
                Dialog.setIcon(android.R.drawable.ic_dialog_info);
                Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 按下abort
                    // 將thread結束
                    // 隱藏progressbar
                    // 設定按鈕當按下時結束，結束這個Dialog並中斷這個thread
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(sms.this, LoginInput.class);
                        // startActivityForResult(intent,0);
                        startActivity(intent);
                    }
                });
                Dialog.show();

            }else{
                String[] errmsg=resp.split("&msg=");
                if(errmsg[1].equalsIgnoreCase("verify code Error")){
                    Dialog.setTitle("抱歉，使用到錯誤的簡訊驗證");
                    Dialog.setMessage("請等待接收新的驗證簡訊");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 按下abort
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            keeprun=true;
                        }
                    });
                    Dialog.show();
                }else if(errmsg[1].equalsIgnoreCase("Verify Code time out")){
                    Dialog.setTitle("抱歉，驗證碼逾時");
                    Dialog.setMessage("請按確定重發驗證碼簡訊");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 按下abort
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            keeprun=true;
                        }
                    });
                    Dialog.show();
                }

            }
        }


    }
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
    }

}
