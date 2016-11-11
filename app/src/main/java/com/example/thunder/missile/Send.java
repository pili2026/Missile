package com.example.thunder.missile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import login.mcs.Login;
import login.mcs.Logout;
import login.mcs.Submit;
import mcs.LoginActivity;
import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.UserSchema;
import tab.list.FileTab;
import tab.list.FileUtils;

/**
 * Created by jeremy on 2016/6/17.
 */
public class Send extends Fragment implements View.OnClickListener{
    /*
    * 這邊是用來作驥通知的動作，也是整個KM的主要核心之一，因為需要做檔案的切割
    */
    static final int BLACK = -16777216;  // Constant to represent the RGB binary value of black. In binary - 1111111 00000000 00000000 00000000
    static final int WHITE = -1;  // Constant to represent the RGB binary value of white. In binary - 1111111 1111111 1111111 1111111
    Bitmap magnified_key_image_2,keyImage,chiperImage,filebitmap,black_white,magnified_key_image;
    public String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/";
    private String getid;
    String receiver, title, attachment = null, content, state;
    String filetype[];
    EditText etR, etT, etC;
    TextView tvName;
    Button delete;
    ToggleButton sms;
    ImageView previewImg;
    int file_amount, split_seq = 0;
    private final int closedialog = 0;
    private final int timeout = 1;
    private final int ok = 2;
    private final int error = 3;
    int index, urgent = 0;
    String file_name = new String(), postFile;
    ArrayList<String> file_path;
    int duration, file_size;
    ProgressDialog senddialog = null;
    String selfid;
    Submit submit;
    ProgressDialog dialog = null;
    public String token ,filename;
    String[] form = { UserSchema._FILEPATH, UserSchema._DURATION, UserSchema._FILESIZE, UserSchema._FILENAME, UserSchema._ID };
    boolean checkFileType = false ;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;

    public Send() {
        FileContentProvider test = new FileContentProvider();
        test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }//end of onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        attachment = new String();
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_send, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Send");

        delete = (Button) view.findViewById(R.id.delete);
        tvName = (TextView) view.findViewById(R.id.writename);
        previewImg = (ImageView) view.findViewById(R.id.previewimg);
        sms = (ToggleButton) view.findViewById(R.id.Regsb);
        sms.setOnCheckedChangeListener(sbcheck);
        previewImg.setVisibility(View.INVISIBLE);
        tvName.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        previewImg.setClickable(true);
        previewImg.setOnClickListener(this);

        delete.setOnClickListener(this);
        tvName.setOnClickListener(this);
        etR = (EditText) view.findViewById(R.id.receiver);
        etT = (EditText) view.findViewById(R.id.subject);
        etC = (EditText) view.findViewById(R.id.content);
        submit = new Submit();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabMenu = (FloatingActionMenu) view.findViewById(R.id.fab);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);

        final FloatingActionButton programFab1 = new FloatingActionButton(getActivity());
        programFab1.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab1.setLabelText(getString(R.string.lorem_ipsum));

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fab1.setOnClickListener(clickListener);
        fab2.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();

            switch (v.getId()) {
                case R.id.fab1:
                    intent.setClass(getActivity(), FileTab.class);
                    startActivity(intent);
                    break;
                case R.id.fab2:
//                    fab2.setVisibility(View.GONE);
                    intent.setClass(getActivity(), FileTab.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previewimg:
                viewfile();
                break;
            case R.id.delete:
                // 刪除所選擇的檔案，同時把訊息隱藏起來
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                previewImg.setVisibility(View.INVISIBLE);
                tvName.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);
                break;
        }
    }//end of onClick

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        menu.clear();
        inflater.inflate(R.menu.menu_send, menu);
    }//end of onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                // intent.setClass(writepage.this, browse.class);
                intent.setClass(getActivity(), Edit.class);
                // startActivityForResult(intent,0);
                startActivity(intent);
                break;

            case R.id.logout:
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/user_info"));
                ConnectivityManager CM = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = CM.getActiveNetworkInfo();
                if (info == null || !info.isAvailable()) {

                }else{
                    Logout logout = new Logout();
                    logout.logout_start();

                }
                info=null;
                CM=null;
                // intent.setClass(writepage.this, browse.class);
                intent.setClass(getActivity(), LoginActivity.class);
                // startActivityForResult(intent,0);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                break;

            case R.id.send:
                sendmail();
                break;
            case R.id.action_add:
                sendAttach();
                break;

            case R.id.home:
                getActivity().finish();
                break;
        }

        return true;
    }

    //辨識目前選擇是sms還是wlan
    public CompoundButton.OnCheckedChangeListener sbcheck = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                urgent=1;
                sms.setChecked(true);
            } else {
                urgent= 0;
                sms.setChecked(false);
            }
        }
    };//end of sbcheck

    private Handler mHandler = new Handler() {

        // 此方法在ui線程運行
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case closedialog:
                    senddialog = ProgressDialog.show(getActivity(), "請稍候", "資料上傳中", true);
                    senddialog.show();
                    break;
                case timeout:
                    AlertDialog.Builder Dialog0 = new AlertDialog.Builder(getActivity()); // Dialog
                    Dialog0.setTitle("連線逾時");
                    Dialog0.setMessage("請問是否要重送?");
                    Dialog0.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog0.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SendandAttach().execute();

                        }
                    });
                    Dialog0.setNegativeButton("取消", new DialogInterface.OnClickListener() { // 按下abort
                        // 將thread結束
                        // 隱藏progressbar
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FileContentProvider test = new FileContentProvider();
                            test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                            previewImg.setVisibility(View.INVISIBLE);
                            tvName.setVisibility(View.INVISIBLE);
                            delete.setVisibility(View.INVISIBLE);
                            etR.setText("");
                            etT.setText("");
                            etC.setText("");
                        }
                    });
                    Dialog0.show();
                    break;
                case error:
                    AlertDialog.Builder Dialog = new AlertDialog.Builder(getActivity()); // Dialog
                    Dialog.setTitle("");
                    Dialog.setMessage("傳送失敗，查無此收件者");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            etR.setText("");

                        }
                    });
                    Dialog.show();
                    break;
                case ok:
                    // 傳送成功後顯示成功視窗
                    AlertDialog.Builder Dialog1 = new AlertDialog.Builder(getActivity()); // Dialog
                    Dialog1.setTitle("");
                    Dialog1.setMessage("傳送成功");
                    Dialog1.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog1.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            etR.setText("");
                            etT.setText("");
                            etC.setText("");
                        }
                    });
                    Dialog1.show();
                    break;
            }
        }
    };//end of mHandler

    class SendandAttach extends AsyncTask<Void, Void, String> { // implement
            // thread
        public String where;
        public String path;

        // Asyntask 前置作業
        @Override
        protected void onPreExecute() {

            mHandler.obtainMessage(closedialog).sendToTarget(); // 傳送要求更新list的訊息給handler
            // 開啟資料傳送dialog
        }

        @Override
        protected String doInBackground(Void... params) {
            String er = "no&";
            String file0_0;
            File firstfile = null;
            boolean[] checktype = null;//2016/06/30新增
            System.out.println("state會錯"+state);
            state="write";
            if (state.equals("write")) {
                //20160905學長推想此時判斷帳號
                if(checkFileType){
                    attachment =visualEncrypt();
                }
                /// ex : storage/emulated/0/DCIM/100ANDRO/MOV_0259.mp4
                File file = new File(attachment);
                String filename = file.getName();
                filetype = filename.split("\\.");
                Cursor check_finish_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH }, "filecheck='0' and filerecord='file0_0' and selfid='" + selfid + "'", null, null);
                if(check_finish_cursor.getCount()>0){
                    check_finish_cursor.moveToFirst();
                    file0_0=check_finish_cursor.getString(0);
                    firstfile =new File(file0_0);

                    //2016/06/30新增
                    checktype = new boolean[AttachParameter.filetype];
                    //2016/06/30新增
                    checktype = AttachParameter.checktype(file.getName());
                }
                check_finish_cursor.close();
                // 2013/4/14 4$刪掉filecount與post
                // 2013/8/10/ 補上filecnt並把前面;改成&
                System.out.println("postFile==" + postFile);
                // 先設定request字串

                // 2013/11/08 豆豆 修改上傳資訊metadata
                submit.setrequestString("subject=" + title + "&content=" + content +
                        "&selfid="+selfid+"&receiver=" + receiver +
                        "&filecnt=" + file_amount + "&duration=" + duration +
                        "&filename=" + filename + "&filetype=" + filetype[1] +
                        "&filepath=" + attachment + "&length=" + file_size +
                        "&firstlength=" + (int)firstfile.length()+"&urgent=" + urgent  + postFile);

                String resp = submit.submit1(Login.latest_cookie);

                // 這邊是用來檢查收件者是否存在於server中，若不存在則取消這次的上傳
                if (!(AttachParameter.chechsuccess(resp))) {
                    if(resp.equalsIgnoreCase("timeout")){
                        er = "yes&timeout";
                    }else{
                        er = "yes&";
                    }
                } else {
                    //UPDATE TOKEN至草稿夾
                    String[] Form = { UserSchema._ID };
                    Cursor change_token = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfid + "'", null, null);
                    if (change_token.getCount() > 0) {
                        change_token.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_token.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._MESSAGETOKEN, resp.replace("ret=0&token=", ""));
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_token.close();

                    //第一塊上傳完  更新FIRST為TRUE
                    Cursor change_first = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfid + "'", null, null);
                    if (change_first.getCount() > 0) {
                        change_first.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_first.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FIRST, "true");
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_first.close();

                    //20160903 更改抓蟲版
                    token=resp.replace("ret=0&token=", "");

                    Cursor up_tempfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), Form, "selfid='" + selfid + "' and filecheck='0'", null, null);
                    if (up_tempfile.getCount() > 0) {
                        up_tempfile.moveToFirst();
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FILECHECK, 1);

                        //2016/06/30新增修改
                        if (checktype[AttachParameter.music] || checktype[AttachParameter.photo]||checktype[AttachParameter.video]){
                            values.put(UserSchema._MESSAGETOKEN, token);
                        }

                        int id_this = Integer.parseInt(up_tempfile.getString(0));
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, where, null);
                    }
                    up_tempfile.close();

                    //20160903 更改抓蟲版
                    if (checktype[AttachParameter.photo]){
                        //20160801 更新圖片所有的token
                        Cursor uptoken_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), Form, "selfid='"+selfid+"'", null, null);
                        if (uptoken_cursor.getCount() > 0) {
                            ContentValues values = new ContentValues();
                            values = new ContentValues();
                            uptoken_cursor.moveToFirst();
                            values.put(UserSchema._MESSAGETOKEN, token);
                            for (int i = 0; i < uptoken_cursor.getCount(); i++) {
                                int id_this = Integer.parseInt(uptoken_cursor.getString(0));
                                String file_where = UserSchema._ID + " = " + id_this;
                                getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, file_where, null);
                                uptoken_cursor.moveToNext();
                            }
                        }
                        uptoken_cursor.close();
                    }
                    // 第一塊傳完
                }
            }
            return er;
        }

        protected void onPostExecute(String er) {
            String[]resp = er.split("&");
            senddialog.dismiss();
            if (resp[0].equals("yes")) {

                if(resp.length>1 &&resp[1].equalsIgnoreCase("timeout")){
                    mHandler.obtainMessage(timeout).sendToTarget(); // 傳送要求更新list的訊息給handler
                }
                else{
                    mHandler.obtainMessage(error).sendToTarget();
                }


            } else {
                // 傳送要求更新list的訊息給handler
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                previewImg.setVisibility(View.INVISIBLE);
                tvName.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);
                mHandler.obtainMessage(ok).sendToTarget();

            }

        }
    }

    public void fileUpload(String arg) {
        // 檢查使用者有沒有選擇要上傳的檔案,選擇好的檔案會寫入file_choice的table內
        file_path = new ArrayList<String>();
        Cursor up_file_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        if (up_file_cursor.getCount() > 0) {
            dialog = ProgressDialog.show(getActivity(), "請稍候", "資料處理中", true);
            dialog.show();
            up_file_cursor.moveToFirst();
            file_path.add(up_file_cursor.getString(0));
            selfid = randomString(20);
            postFile = new String();
            for (index = 0; index < up_file_cursor.getCount(); index++) {
                boolean[] checktype = new boolean[AttachParameter.filetype];
                checktype = AttachParameter.checktype(attachment);
                // 如果選擇的是影片，則計算他的影片長度、大小，目的是要傳給ffmpeg使用
                if (checktype[AttachParameter.video]||checktype[AttachParameter.music]) {
                    File file = new File(file_path.get(index));
                    // 開啟計算檔案長度並存入attachSize
                    file_size = (int) file.length();
                    file_name = file.getName();
                    postFile = postFile + "&file_name" + index + "_0=" + file_name;
                    duration = 0;
                    file_amount = 1;
                    FileUtils oname = new FileUtils();
                    String outFileName = oname.getTargetFileName(file_path.get(index), split_seq, index);

                    //2016/06/30新增修改
                    try {
                        FileInputStream inputStream = new FileInputStream(new File(file_path.get(index)));
                        byte[] data = new byte[1024];
                        FileOutputStream outputStream =new FileOutputStream(new File(Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name));
                        while (inputStream.read(data) != -1) {
                            outputStream.write(data);
                        }
                        inputStream.close();
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }  catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    ContentValues values = new ContentValues();
                    values.put(UserSchema._FILEPATH, Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name);//2016/06/30新增修改
                    values.put(UserSchema._FILERECORD, "file"+index + "_0");
                    values.put(UserSchema._FILECHECK, 0);
                    values.put(UserSchema._FILENAME, file_name);
                    values.put(UserSchema._SELFID, selfid);
                    getActivity().getContentResolver().insert(Uri.parse("content://tab.list.d2d/temp_file"), values);
                    //dialog.dismiss();
                    state = "write";
                    dialog.dismiss();
                    new SendandAttach().execute();
                }
                //2016/07/20新增修改//
                else if(checktype[AttachParameter.photo]){

                    //20160905推想功能
                    encryptTransmit();
                    state = "write";
                    dialog.dismiss();
                    new SendandAttach().execute();

                }else{
                    //2016/07/04新增修改
                    Toast.makeText(getActivity(), "你所選的檔案類型不支援，請重新選擇", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                up_file_cursor.moveToNext();
            }
        } else {
            Toast.makeText(getActivity(), "尚未選擇附加檔案，請重新選擇", Toast.LENGTH_LONG).show();
        }

        state = "write";
        up_file_cursor.close();
        index = 0;
        // 呼叫自定義的函式

    }

    public void sendmail() {
        receiver = etR.getText().toString();
        title = etT.getText().toString();
        content = etC.getText().toString();
        ConnectivityManager CM = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();

        if (receiver.equals("")) {
            Toast.makeText(getActivity(), "收件者不可為空白", Toast.LENGTH_LONG).show();
        }else if (title.equals("")) {
            Toast.makeText(getActivity(), "標題不可為空白", Toast.LENGTH_LONG).show();
        }else if (content.equals("")) {
            Toast.makeText(getActivity(), "內容不可為空白", Toast.LENGTH_LONG).show();
        }else if (info == null || !info.isAvailable()) {
            Toast.makeText(getActivity(), "目前沒有網路唷!所以無法發佈", Toast.LENGTH_LONG).show();
        }else{
            /*
            *20160905推想功能(此區加入if判斷帳號來選取功能)
            * if(loginName == zz){
            * fileUpload("write");
            *
            * }else{
            * fileuploadTransmit("write")
            * }
            */
            fileUpload("write");
        }
        info=null;
        CM=null;

    }

    public void viewfile() {
        boolean[] checktype = new boolean[AttachParameter.filetype];
        checktype = AttachParameter.checktype(attachment);
        if (checktype[AttachParameter.music]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(attachment);
            it.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(it);
        } else if (checktype[AttachParameter.video]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(attachment);
            it.setDataAndType(Uri.fromFile(file), "video/*");
            startActivity(it);
        } else if (checktype[AttachParameter.photo]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(attachment);
            it.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(it);
        } else
            Toast.makeText(getActivity(), "not match file", 4000).show();
    }

    public void setPreviewImg() {
        boolean[] checktype = new boolean[AttachParameter.filetype];
        checktype = AttachParameter.checktype(attachment);

        if (checktype[AttachParameter.music]) {
            previewImg.setImageResource(R.drawable.notes);
        } else if (checktype[AttachParameter.video]) {
            Bitmap filebitmap = android.media.ThumbnailUtils.createVideoThumbnail(attachment, MediaStore.Images.Thumbnails.MICRO_KIND);
            // filebitmap=ThumbnailUtils.extractThumbnail(filebitmap,55,60);
            previewImg.setImageBitmap(filebitmap);
        } else if (checktype[AttachParameter.photo]) {
            Bitmap filebitmap = BitmapFactory.decodeFile(attachment);
            previewImg.setImageBitmap(filebitmap);
        } else {
            previewImg.setImageResource(R.drawable.message);
        }

    }

    // 當從attachment回來時，顯示剛剛所選擇的檔案在writepage上。
    public void onResume() {
        super.onResume();
        Cursor ch_tmepfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        if (ch_tmepfile.getCount() > 0) {
            ch_tmepfile.moveToFirst();
            attachment = ch_tmepfile.getString(0);
            File file = new File(attachment);
            tvName.setText(file.getName());
            previewImg.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            setPreviewImg();
        }
        ch_tmepfile.close();
    }

    public String randomString(int len) {
        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int idx = (int)(Math.random() * str.length());
            sb.append(str.charAt(idx));
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // un-register BroadcastReceiver

    }

    public String visualEncrypt() {
        // TODO Auto-generated method stub
        String in_file = "";
        Cursor up_file_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        String filetype[]  = null ;

        if(up_file_cursor.getCount()>0){
            up_file_cursor.moveToFirst();
            in_file = up_file_cursor.getString(0);
        }up_file_cursor.close();
        OutputStream fileKeyMagnifiedOut = null;
        OutputStream fileKeyChiperOut = null;
        File file = new File(in_file);
        filename = file.getName();
        filetype = filename.split("\\.");
        //File names and paths for the magnified images
        AttachParameter.save_key_magnified_path = sdcardPath + "file_name0_0-" + filename;//2016/07/26修改加入 key_magnified
        AttachParameter.save_cipher_magnified_path = sdcardPath + "file_name0_1-" + filename;//cipher_magnified
        AttachParameter.image_decrypt_file= sdcardPath + "reslut.png";
        AttachParameter.key_magnified_file = new File(AttachParameter.save_key_magnified_path);
        AttachParameter.cipher_magnified_file = new File(AttachParameter.save_cipher_magnified_path);

        AttachParameter.bw_file = new File(sdcardPath + "Black_White.png");
        AttachParameter.key_file = new File(sdcardPath + "key.png");
        filebitmap = BitmapFactory.decodeFile(in_file);

        if (!AttachParameter.key_magnified_file.exists()){
            //====================================================================
            black_white= gray2Binary(filebitmap);
            System.out.println(filebitmap.getWidth());
            System.out.println(filebitmap.getHeight());
            //====================================================================

            //====================================================================
            keyImage = Bitmap.createBitmap(filebitmap.getWidth(),filebitmap.getHeight(), Bitmap.Config.ARGB_4444);
            try {
                SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG");

                for(int i = 0; i < keyImage.getHeight(); i++){
                    for(int j = 0; j < keyImage.getWidth(); j++){

                        int result = secureRandomGenerator.nextInt(100);
                        if(result < 50){
                            keyImage.setPixel(j, i, WHITE);
                        }
                        else{
                            keyImage.setPixel(j, i, BLACK);
                        }
                    }
                }
            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            //====================================================================

            //====================================================================
            magnified_key_image = Bitmap.createBitmap(keyImage.getWidth()*2,keyImage.getHeight()*2, Bitmap.Config.ARGB_4444);

            for(int i = 0; i < keyImage.getHeight(); i++){
                for(int j = 0; j < keyImage.getWidth(); j++){
                    if(keyImage.getPixel(j, i) == BLACK){
                        //
                        //                     |X| |
                        //                     -----
                        //                     | |X|
                        //
                        magnified_key_image.setPixel(j*2, i*2, BLACK);
                        magnified_key_image.setPixel(j*2+1, i*2, WHITE);
                        magnified_key_image.setPixel(j*2, i*2+1, WHITE);
                        magnified_key_image.setPixel(j*2+1, i*2+1, BLACK);

                    }
                    else{
                        //
                        //                     | |X|
                        //                     -----
                        //                     |X| |
                        //
                        magnified_key_image.setPixel(j*2, i*2, WHITE);
                        magnified_key_image.setPixel(j*2+1, i*2, BLACK);
                        magnified_key_image.setPixel(j*2, i*2+1, BLACK);
                        magnified_key_image.setPixel(j*2+1, i*2+1, WHITE);
                    }
                }
                System.out.println("magnified_key總width是"+keyImage.getHeight()+" 目前width是"+i);
            }
            System.out.println("magnified_key做完囉");
            try{

                fileKeyMagnifiedOut = new FileOutputStream(AttachParameter.key_magnified_file);
                magnified_key_image.compress(Bitmap.CompressFormat.PNG, 100, fileKeyMagnifiedOut);

                fileKeyMagnifiedOut.flush();
                fileKeyMagnifiedOut.close();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try{
                    if(fileKeyMagnifiedOut !=null){
                        fileKeyMagnifiedOut.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        Cursor magnified_key = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), null, "selfid='"+selfid +"' and filerecord = 'file0_0'", null, null);
        if(magnified_key.getCount()>0){
            //do nothing
        }else{
            ContentValues values = new ContentValues();
            values = new ContentValues();
            values.put(UserSchema._FILEPATH,  AttachParameter.save_key_magnified_path);
            values.put(UserSchema._FILERECORD, "file0_0");
            values.put(UserSchema._FILECHECK, 0);
            values.put(UserSchema._SELFID, selfid);
            values.put(UserSchema._FILENAME, filename);//2016/07/26
            //還要加自訂ID(跟SERVER)
            getActivity().getContentResolver().insert(Uri.parse("content://tab.list.d2d/temp_file"), values);

        }
        magnified_key.close();
        //magnified ok
        //====================================================================

        //====================================================================
        if(!AttachParameter.cipher_magnified_file.exists()){
            chiperImage = Bitmap.createBitmap(black_white.getWidth(),black_white.getHeight(), Bitmap.Config.ARGB_4444);

            for( int i = 0; i<chiperImage.getHeight(); i++){
                for(int j = 0; j<chiperImage.getWidth(); j++){
                    if(keyImage.getPixel(j, i) == BLACK){
                        int temp = Get_and_Flip(black_white, i, j);
                        chiperImage.setPixel(j, i, temp);
                    }
                    else{
                        chiperImage.setPixel(j, i, black_white.getPixel(j, i));
                    }
                }
                System.out.println("chiperImage總Height是"+chiperImage.getHeight()+" 目前Height是"+i);
            }
            System.out.println("chiperImage做完囉");

            magnified_key_image_2 = Bitmap.createBitmap(chiperImage.getWidth()*2,chiperImage.getHeight()*2, Bitmap.Config.ARGB_4444);
            for(int i = 0; i < chiperImage.getHeight(); i++){
                for(int j = 0; j < chiperImage.getWidth(); j++){
                    if(chiperImage.getPixel(j, i) == BLACK){
                        //
                        //                     |X| |
                        //                     -----
                        //                     | |X|
                        //
                        magnified_key_image_2.setPixel(j*2, i*2, BLACK);
                        magnified_key_image_2.setPixel(j*2+1, i*2, WHITE);
                        magnified_key_image_2.setPixel(j*2, i*2+1, WHITE);
                        magnified_key_image_2.setPixel(j*2+1, i*2+1, BLACK);

                    }
                    else{
                        //
                        //                     | |X|
                        //                     -----
                        //                     |X| |
                        //
                        magnified_key_image_2.setPixel(j*2, i*2, WHITE);
                        magnified_key_image_2.setPixel(j*2+1, i*2, BLACK);
                        magnified_key_image_2.setPixel(j*2, i*2+1, BLACK);
                        magnified_key_image_2.setPixel(j*2+1, i*2+1, WHITE);
                    }
                }
                System.out.println("magnified_key_image_2總Height是"+magnified_key_image_2.getHeight()+" 目前Height是"+i);
            }
            System.out.println("magnified_key_image_2做完囉");
            try{

                fileKeyChiperOut = new FileOutputStream(AttachParameter.cipher_magnified_file);
                magnified_key_image_2.compress(Bitmap.CompressFormat.PNG, 100, fileKeyChiperOut);

                fileKeyChiperOut.flush();
                fileKeyChiperOut.close();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try{
                    if(fileKeyChiperOut !=null){
                        fileKeyChiperOut.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        Cursor chiperImage = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), null, "selfid='"+selfid +"' and filerecord = 'file0_1'", null, null);
        if(chiperImage.getCount()>0){
            //do nothing
        }else{
            ContentValues values = new ContentValues();
            values = new ContentValues();
            values.put(UserSchema._FILEPATH, AttachParameter.save_cipher_magnified_path);
            values.put(UserSchema._FILERECORD, "file0_1");
            values.put(UserSchema._FILECHECK, 0);
            values.put(UserSchema._SELFID, selfid);
            values.put(UserSchema._FILENAME, filename);//2016/07/26
            //還要加自訂ID(跟SERVER)
            getActivity().getContentResolver().insert(Uri.parse("content://tab.list.d2d/temp_file"), values);
        }
        chiperImage.close();
        //====================================================================
        return  AttachParameter.save_key_magnified_path;
    }

    public Bitmap gray2Binary(Bitmap graymap) {

        int width = graymap.getWidth();
        int height = graymap.getHeight();

        Bitmap binarymap = null;
        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                int col = binarymap.getPixel(i, j);

                int alpha = col & 0xFF000000;

                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);

                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);

                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }

                int newColor = alpha | (gray << 16) | (gray << 8) | gray;

                binarymap.setPixel(i, j, newColor);
                //System.out.println("總height是"+height+" 目前height是"+j);
            }
            System.out.println("總width是"+width+" 目前width是"+i);
        }
        System.out.println("做完囉");
        return binarymap;
    }

    public static int Get_and_Flip(Bitmap img, int i, int j){

        int initial = img.getPixel(j, i);

        if(initial == BLACK){
            return WHITE;
        }
        else{
            return BLACK;
        }
    }

    //20160905推想功能()
    public void encryptTransmit(){
        File file = new File(file_path.get(index));
        // 開啟計算檔案長度並存入attachSize
        file_size = (int) file.length();
        file_name = file.getName();
        file_amount = 2;
        String[] tempfilename = new String[file_amount];
        for (int i = 0; i < file_amount; i++) {
            tempfilename[i] = "file_name" + index + "_" + String.valueOf(i) + "=" + file_name;
            postFile = postFile + "&" + tempfilename[i];
        }
        duration = 0;

        checkFileType=true;

        //2016/06/30新增修改(檔案的copy)
        try {
            FileInputStream inputStream = new FileInputStream(new File(file_path.get(index)));
            byte[] data = new byte[1024];
            FileOutputStream outputStream =new FileOutputStream(new File(Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name));
            while (inputStream.read(data) != -1) {
                outputStream.write(data);
            }
            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //======20160916 new=============//
    private void sendAttach() {
        receiver = etR.getText().toString();
        title = etT.getText().toString();
        content = etC.getText().toString();
        ConnectivityManager CM = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();

        if (receiver.equals("")) {
            Toast.makeText(getActivity(), "收件者不可為空白", Toast.LENGTH_LONG).show();
        }else if (title.equals("")) {
            Toast.makeText(getActivity(), "標題不可為空白", Toast.LENGTH_LONG).show();
        }else if (content.equals("")) {
            Toast.makeText(getActivity(), "內容不可為空白", Toast.LENGTH_LONG).show();
        }else if (info == null || !info.isAvailable()) {
            Toast.makeText(getActivity(), "目前沒有網路唷!所以無法發佈", Toast.LENGTH_LONG).show();
        }else{

            fileUploadToSend("write");
        }
        info=null;
        CM=null;
    }

    public void fileUploadToSend(String arg) {
        // 檢查使用者有沒有選擇要上傳的檔案,選擇好的檔案會寫入file_choice的table內
        file_path = new ArrayList<String>();
        Cursor up_file_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        if (up_file_cursor.getCount() > 0) {
            dialog = ProgressDialog.show(getActivity(), "請稍候", "資料處理中", true);
            dialog.show();
            up_file_cursor.moveToFirst();
            file_path.add(up_file_cursor.getString(0));
            selfid = randomString(20);
            postFile = new String();
            for (index = 0; index < up_file_cursor.getCount(); index++) {
                boolean[] checktype = new boolean[AttachParameter.filetype];
                checktype = AttachParameter.checktype(attachment);
                // 如果選擇的是影片，則計算他的影片長度、大小，目的是要傳給ffmpeg使用
                if (checktype[AttachParameter.video]||checktype[AttachParameter.music]||checktype[AttachParameter.photo]) {
                    File file = new File(file_path.get(index));
                    // 開啟計算檔案長度並存入attachSize
                    file_size = (int) file.length();
                    file_name = file.getName();
                    postFile = postFile + "&file_name" + index + "_0=" + file_name;
                    duration = 0;
                    file_amount = 1;
                    FileUtils oname = new FileUtils();
                    String outFileName = oname.getTargetFileName(file_path.get(index), split_seq, index);

                    //2016/06/30新增修改
                    try {
                        FileInputStream inputStream = new FileInputStream(new File(file_path.get(index)));
                        byte[] data = new byte[1024];
                        FileOutputStream outputStream =new FileOutputStream(new File(Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name));
                        while (inputStream.read(data) != -1) {
                            outputStream.write(data);
                        }
                        inputStream.close();
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }  catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    ContentValues values = new ContentValues();
                    values.put(UserSchema._FILEPATH, Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name);//2016/06/30新增修改
                    values.put(UserSchema._FILERECORD, "file"+index + "_0");
                    values.put(UserSchema._FILECHECK, 0);
                    values.put(UserSchema._FILENAME, file_name);
                    values.put(UserSchema._SELFID, selfid);
                    getActivity().getContentResolver().insert(Uri.parse("content://tab.list.d2d/temp_file"), values);
                    //dialog.dismiss();
                    state = "write";
                    dialog.dismiss();
                    new sendFile().execute();
                }
                else{
                    //2016/07/04新增修改
                    Toast.makeText(getActivity(), "你所選的檔案類型不支援，請重新選擇", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                up_file_cursor.moveToNext();
            }
        } else {
            Toast.makeText(getActivity(), "尚未選擇附加檔案，請重新選擇", Toast.LENGTH_LONG).show();
        }

        state = "write";
        up_file_cursor.close();
        index = 0;
        // 呼叫自定義的函式

    }

    private class sendFile extends AsyncTask<Void, Void, String> {
        // thread
        public String where;
        public String path;

        // Asyntask 前置作業
        @Override
        protected void onPreExecute() {

            mHandler.obtainMessage(closedialog).sendToTarget(); // 傳送要求更新list的訊息給handler
            // 開啟資料傳送dialog
        }

        @Override
        protected String doInBackground(Void... params) {
            String er = "no&";
            String file0_0;
            File firstfile = null;
            boolean[] checktype = null;//2016/06/30新增
            System.out.println("state會錯"+state);
            state="write";
            if (state.equals("write")) {
                //20160905學長推想此時判斷帳號
                if(checkFileType){
                    attachment =visualEncrypt();
                }
                /// ex : storage/emulated/0/DCIM/100ANDRO/MOV_0259.mp4
                File file = new File(attachment);
                String filename = file.getName();
                filetype = filename.split("\\.");
                Cursor check_finish_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH }, "filecheck='0' and filerecord='file0_0' and selfid='" + selfid + "'", null, null);
                if(check_finish_cursor.getCount()>0){
                    check_finish_cursor.moveToFirst();
                    file0_0=check_finish_cursor.getString(0);
                    firstfile =new File(file0_0);

                    //2016/06/30新增
                    checktype = new boolean[AttachParameter.filetype];
                    //2016/06/30新增
                    checktype = AttachParameter.checktype(file.getName());
                }
                check_finish_cursor.close();
                // 2013/4/14 4$刪掉filecount與post
                // 2013/8/10/ 補上filecnt並把前面;改成&
                System.out.println("postFile==" + postFile);
                // 先設定request字串

                // 2013/11/08 豆豆 修改上傳資訊metadata
                submit.setrequestString("subject=" + title + "&content=" + content +
                        "&selfid="+selfid+"&receiver=" + receiver +
                        "&filecnt=" + file_amount + "&duration=" + duration +
                        "&filename=" + filename + "&filetype=" + filetype[1] +
                        "&filepath=" + attachment + "&length=" + file_size +
                        "&firstlength=" + (int)firstfile.length()+"&urgent=" + urgent  + postFile);

                String resp = submit.submit1(Login.latest_cookie);

                // 這邊是用來檢查收件者是否存在於server中，若不存在則取消這次的上傳
                if (!(AttachParameter.chechsuccess(resp))) {
                    if(resp.equalsIgnoreCase("timeout")){
                        er = "yes&timeout";
                    }else{
                        er = "yes&";
                    }
                } else {
                    //UPDATE TOKEN至草稿夾
                    String[] Form = { UserSchema._ID };
                    Cursor change_token = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfid + "'", null, null);
                    if (change_token.getCount() > 0) {
                        change_token.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_token.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._MESSAGETOKEN, resp.replace("ret=0&token=", ""));
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_token.close();

                    //第一塊上傳完  更新FIRST為TRUE
                    Cursor change_first = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfid + "'", null, null);
                    if (change_first.getCount() > 0) {
                        change_first.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_first.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FIRST, "true");
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_first.close();

                    //20160903 更改抓蟲版
                    token=resp.replace("ret=0&token=", "");

                    Cursor up_tempfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), Form, "selfid='" + selfid + "' and filecheck='0'", null, null);
                    if (up_tempfile.getCount() > 0) {
                        up_tempfile.moveToFirst();
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FILECHECK, 1);

                        //2016/06/30新增修改
                        if (checktype[AttachParameter.music] || checktype[AttachParameter.photo]||checktype[AttachParameter.video]){
                            values.put(UserSchema._MESSAGETOKEN, token);
                        }

                        int id_this = Integer.parseInt(up_tempfile.getString(0));
                        String where = UserSchema._ID + " = " + id_this;
                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, where, null);
                    }
                    up_tempfile.close();

                }
            }
            return er;
        }

        protected void onPostExecute(String er) {
            String[]resp = er.split("&");
            senddialog.dismiss();
            if (resp[0].equals("yes")) {

                if(resp.length>1 &&resp[1].equalsIgnoreCase("timeout")){
                    mHandler.obtainMessage(timeout).sendToTarget(); // 傳送要求更新list的訊息給handler
                }
                else{
                    mHandler.obtainMessage(error).sendToTarget();
                }

            } else {
                // 傳送要求更新list的訊息給handler
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                previewImg.setVisibility(View.INVISIBLE);
                tvName.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);
                mHandler.obtainMessage(ok).sendToTarget();

            }

        }
    }

    //======20160916 new=============//
}
