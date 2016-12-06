package missile;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thunder.missile.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import login.mcs.Login;
import login.mcs.Retrieve;
import login.mcs.Submit;
import login.mcs.User;
import mcs.LoginInput;
import mcs.VideoView;
import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.UserSchema;
import tab.list.FileUtils;

import static tab.list.AttachParameter.path;
import static tab.list.AttachParameter.sdcardPath;

/**
 * Created by jeremy on 2016/6/28.
 */
class openFireInfo {
    private String title, content, file, date, token,FileCount;

    openFireInfo() {
    }

    public void setTitle(String arg) {
        title = arg;
    }

    public void setDate(String arg) {
        date = arg;
    }

    public void seContent(String arg) {
        content = arg;
    }

    public void setoken(String arg) {
        token = arg;
    }

    public void setFile(String arg) {
        file = arg;
    }
    public void seFileCount(String arg) {
        FileCount = arg;
    }
    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return file;
    }

    public String gettoken() {
        return token;
    }
    public String getFileCount() {
        return FileCount;
    }

}

public class OpenFire extends ListActivity implements AdapterView.OnItemClickListener{
    /*
	 * 這邊是顯示使用者過去的歷史訊息，是由conractlist呼叫此class， 或者是notify
	 * 在呼叫的同時會接收傳入的bundle，bundle內容是寄件者姓名 再來若是從notify來的話，透過點擊歷史訊息可以通知寄件人上傳檔案
	 */
    public List<openFireInfo> getinfo;
    public boolean finishretrieve;
    public String[] Again_id;
    public String titleShow, message, bmsg, upmsg, state, postFile, selfId, token, file_name, attachFile,
            check_pass, receiver, Again_token, Again_sender, sender, attachment, fileName, fileId, attachPath;
    public String title = "Command", content = "Missile Fire", viewmod = "", res = null;
    public User user = new User();
    public ProgressDialog pdialog = null, dialog = null, sendDialog = null;
    public List<String> fileList = new ArrayList<String>();
    public SimpleAdapter adapter;
    public Boolean finish =true;
    public List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    public TextView tvName, etT, etC, etR;
    public Button delete;
    public ImageView previewImg;
    public EditText chk_Password, input_receiver;
    public String filetype[];
    public Submit submit ;
    public String[] form = { UserSchema._FILEPATH, UserSchema._DURATION, UserSchema._FILESIZE, UserSchema._FILENAME, UserSchema._ID };
    public ArrayList<String> file_path;
    public int id = 0, file_amount, duration, file_size, index;
    private String gettoken = "";
    private String thumbnails = "mnt/sdcard/DCIM/.thumbnails/";
    private int listId, urgent = 0, mailCount = 0;
    private final int show_msg = 0, upDate = 1, timeOut = 2, ok = 3, error = 4;
    private static final int ITEM1 = Menu.FIRST, ITEM2 = Menu.FIRST + 1, ITEM3 = Menu.FIRST + 2, ITEM4 = Menu.FIRST + 3, ITEM5 = Menu.FIRST + 4;
    private static final String TAG = "OpenFire";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent data = getIntent();
        Bundle bundle = data.getExtras();
        // 取得傳入的contact,說是filename 其實是sender(寄件者)
        sender = bundle.getString("contact");
        setTitle("History Massage");
        this.registerForContextMenu(getListView()); // register on Item in
        // listview on context menu

        finishretrieve = false;
        super.onCreate(savedInstanceState);
		/*
		 * 使用adapter，把資料做整合, getData()是資料來源，R.layout.receive是作用在這個layout上 new
		 * String[]為自定義的欄位名稱，將資料放到int[]內，int[]是layout上元件的所在位置
		 */
        adapter = new SimpleAdapter(this, getData(), R.layout.activity_receive, new String[] { "title", "info", "img", "date" }, new int[] { R.id.title, R.id.info, R.id.img, R.id.date });

        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        ListView listView = getListView();
        listView.setBackgroundResource(R.drawable.worldbackground);
        listView.setCacheColorHint(Color.TRANSPARENT);
        submit = new Submit();
    }

    private List<Map<String, Object>> getData() {

        getinfo = new ArrayList<openFireInfo>();
        String[] Form = { UserSchema._TITTLE, UserSchema._CONTENT, UserSchema._FILEPATH, UserSchema._DATE, UserSchema._MESSAGETOKEN, UserSchema._FILECOUNT };
        String[] typefrom = {UserSchema._NOTIFICATION, UserSchema._TYPE};
        openFireInfo tempinfo = new openFireInfo();
        // 使用寄件者去撈出 過去中已經讀過的簡訊以及未被刪除的資料,用 tittle跟userstatus去檢查
        Cursor sender_data_group_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "sender='" + sender + "' and read is not null and userstatus IN ('', 'download')", null, null);
        if (sender_data_group_cursor.getCount() > 0) {
            sender_data_group_cursor.moveToFirst();
            for (int i = 0; i < sender_data_group_cursor.getCount(); i++) {
                tempinfo.setTitle(sender_data_group_cursor.getString(0));
                tempinfo.seContent(sender_data_group_cursor.getString(1));
                tempinfo.setFile(sender_data_group_cursor.getString(2));
                tempinfo.setDate(sender_data_group_cursor.getString(3));
                tempinfo.setoken(sender_data_group_cursor.getString(4));
                tempinfo.seFileCount(sender_data_group_cursor.getString(5));
                getinfo.add(tempinfo);
                mailCount++;
                tempinfo = new openFireInfo();
                sender_data_group_cursor.moveToNext();
            }
        }
        sender_data_group_cursor.close();

        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = mailCount - 1; i >= 0; i--) {
            map = new HashMap<String, Object>();
            // 這邊DB是檢查過去是否有未載完的檔案，如果有未載完的檔案，則用驚嘆號的圖案通知使用者
            Cursor ch_finish_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "messagetoken='" + getinfo.get(i).gettoken() + "' and receive_id!=''", null, null);
            if (ch_finish_cursor.getCount() > 0) {
                map.put("fin", R.drawable.not_fihish);
            } else {
                map.put("fin", R.drawable.fihish);
            }
            ch_finish_cursor.close();
            Cursor type_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), typefrom, "messagetoken='" + getinfo.get(i).gettoken()+"'", null, null);
            if (type_cursor.getCount() > 0) {
                type_cursor.moveToFirst();
                String not = type_cursor.getString(0);
                String type = type_cursor.getString(1);
                if(not!= null &&type != null){
                    if(not.equalsIgnoreCase("1")){
                        if(type.equalsIgnoreCase("wlan")){
                            ////map.put("type", R.drawable.wlan_g);
                        }
                        else{
                            //map.put("type", R.drawable.d2d_g);
                        }
                    }
                    else{
                        if(type.equalsIgnoreCase("wlan")){
                            //map.put("type", R.drawable.wlan_r);
                        }
                        else{
                            //map.put("type", R.drawable.d2d_r);
                        }
                    }
                } else {
                    //map.put("type", R.drawable.fihish);
                }

            } else {
                //map.put("type", R.drawable.fihish);
            }
            type_cursor.close();

            // put info array into list
            map.put("title", getinfo.get(i).getTitle());
            map.put("info", getinfo.get(i).getContent());
            map.put("date", getinfo.get(i).getDate());
            map.put("filecount", "共有 1/"+getinfo.get(i).getFileCount()+" 塊檔案");
            // 檢查檔案類型，放置相對應的檔案類型的圖片
            boolean[] checktype = new boolean[AttachParameter.filetype];
            checktype = AttachParameter.checktype(getinfo.get(i).getFilename());
            System.out.println("看清單拉1  "+getinfo.get(i).getFilename());
            Pattern patternmovie = Pattern.compile(".*.movie");
            Matcher matchermovie = patternmovie.matcher(getinfo.get(i).getFilename());

            if (checktype[AttachParameter.music]) { // music set a note
                map.put("img", R.drawable.notes);
                map.put("filecount", "");

            } else if (checktype[AttachParameter.video]) { // video set a camera
                String videoSegment[] = getinfo.get(i).getFilename().split("&");
                Bitmap filebitmap;

                //因為d2d是單一檔案 沒有分割 所以這邊要檢查
                if(videoSegment.length==1){
                    filebitmap = ThumbnailUtils.createVideoThumbnail(sdcardPath + getinfo.get(i).getFilename(), MediaStore.Images.Thumbnails.MICRO_KIND);
                }else{
                    filebitmap = ThumbnailUtils.createVideoThumbnail(sdcardPath + videoSegment[1], MediaStore.Images.Thumbnails.MICRO_KIND);
                }

                filebitmap = ThumbnailUtils.extractThumbnail(filebitmap, 60, 60);
                File file = new File(thumbnails, "temp_" + i + ".png");
                try {
                    OutputStream outStream = new FileOutputStream(file);
                    filebitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                    outStream.flush();
                    outStream.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    map.put("img", R.drawable.notify1);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    map.put("img", R.drawable.notify1);
                }catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    map.put("img", R.drawable.notify1);
                }
                map.put("img", thumbnails + "temp_" + i + ".png");
                map.put("filecount", "");
            } else if (checktype[AttachParameter.photo]){ // photo set photo
                map.put("img", sdcardPath + getinfo.get(i).getFilename().replace("&", ""));
                map.put("filecount", "");
            } else if (matchermovie.find()) {
                map.put("img", R.drawable.iconvideo);
                map.put("filecount", "");
            } else
                map.put("img", R.drawable.notify1);

            list.add(map);
        }

        return list;
    }

    /*
     * 當按下歷史訊息時，顯示要做的事情，有三個檢查步驟 ， 一是先檢查是否發已經通知告訴對方，請對方上傳檔案 ， 二是檢查對方是否有將檔案上傳，
     * 三就回歸之前版本，檢查檔案狀態是否ready，若ready的話先檢查是否曾經被中斷下載，若有就繼續下載； 否則 就直接詢問是否要瀏覽、全部下載
     */
    //notification :0= 可撥放,1=使用wlan ,2=使用sms,3=可下載
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
        final String[] itemForm = { UserSchema._RECEIVEID, UserSchema._MESSAGETOKEN, UserSchema._FILEPATH, UserSchema._FILEID };
        final String[] check = {UserSchema._TYPE};
        listId = mailCount - (int) position - 1;
        // String[] Form = { UserSchema._RECEIVEID, UserSchema._MESSAGETOKEN,
        // UserSchema._FILEPATH, UserSchema._FILEID };

        // 檢查過去檔案有沒有下載過，若有點擊後直接撥放 0=下載過，
        Cursor data_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), itemForm, "notification = '0' and messagetoken='" + getinfo.get(listId).gettoken() + "' and receive_id ='' and file_id is not null", null, null);
        if (data_cursor.getCount() > 0) {
            data_cursor.moveToFirst();
            String chd2d=data_cursor.getString(3);
            if(chd2d.equalsIgnoreCase("d2d")){
                viewmod ="preview";
                viewfile(sdcardPath + data_cursor.getString(2));
            }else{
                viewfile(sdcardPath + data_cursor.getString(2));


            }
            data_cursor.close();
        } else {
            //已發送過通知，檢查能不能下載
            Cursor ch0_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), check, "notification is not null and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
            int dlownload_num=0;
            dlownload_num=ch0_cursor.getCount();
            ch0_cursor.close();
            if(dlownload_num>0){

                Cursor CH_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "userstatus='download'", null, null);
                if (CH_state.getCount() > 0) {
                    Toast.makeText(getApplicationContext(), "目前尚有檔案下載中，請稍後再試", Toast.LENGTH_LONG).show();
                    CH_state.close();
                }else{
                    //new chfile().execute();
                    new chfile_for_m2m().execute();
                }

            }
            else{//檔案不可撥放 也不能下載 檢查發現沒通知

                // 詢問是否預覽、全部下載
                AlertDialog.Builder DialogPreDl = new AlertDialog.Builder(this); // Dialog
                DialogPreDl.setTitle("");
                DialogPreDl.setMessage("接受戰備命令");
                DialogPreDl.setIcon(android.R.drawable.ic_dialog_info);
                if(Integer.valueOf(getinfo.get(listId).getFileCount())>1){
                    DialogPreDl.setMessage("請選擇預覽檔案或下載完整檔案");
                    DialogPreDl.setNegativeButton("預覽", new DialogInterface.OnClickListener() { // 不接收檔案
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {

                            }
                            Cursor CH_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "userstatus='download'", null, null);
                            if (CH_state.getCount() > 0) {
                                Toast.makeText(getApplicationContext(), "目前尚有檔案下載中，稍後再試", Toast.LENGTH_LONG).show();

                            } else {

                                Log.i("token=", getinfo.get(listId).gettoken());
                                // 設定token，目的是告知待會的retrive時要抓哪個檔案

                                // 執行自訂的非同步函式
                                viewmod = "preview";
                                new startretrieve().execute();

                            }
                            CH_state.close();

                        }
                    });
                }

                DialogPreDl.setPositiveButton("進入戰備狀態", new DialogInterface.OnClickListener() { // 不接收檔案
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {

                        }
                        Cursor CH_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "userstatus='download'", null, null);
                        //=======20160903抓蟲版========//
                        int count=CH_state.getCount();
                        CH_state.close();

                        if (count > 0){
                            Toast.makeText(getApplicationContext(), "目前尚有檔案下載中，稍後再試", Toast.LENGTH_LONG).show();
                        }else {
                            int filecount=0;
                            String check_exist = null;
                            Cursor CH_first_is_photo = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"),
                                    new String[]{UserSchema._FIRST, UserSchema._FILECOUNT},
                                    "messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                            if(CH_first_is_photo.getCount()>0){
                                CH_first_is_photo.moveToFirst();
                                filecount= Integer.valueOf(CH_first_is_photo.getString(1));
                                check_exist=CH_first_is_photo.getString(0);
                            }
                            CH_first_is_photo.close();

                            if (filecount > 1){
                                if (check_exist != null){
                                    new chfile_for_m2m().execute();
                                }else {
                                    Toast.makeText(getApplicationContext(), "尚未符合下載資格", Toast.LENGTH_LONG).show();
                                }
                            }else {
                                new chfile_for_m2m().execute();
                            }
                        }
                        //=======20160903抓蟲版========//
                    }//雲端下載 end
                });

                DialogPreDl.setNeutralButton("確認命令", new DialogInterface.OnClickListener() { // 預覽檔案
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {

                        }
                    }
                });

                DialogPreDl.show();

            }//else end 檔案不可撥放 也不能下載 檢查發現沒通知
        }//else 檔案是否可以撥放 end

        // 詢問是否預覽、全部下載end

    }

    private class startretrieve extends AsyncTask<Void, Void, String[]> {// implement
        // thread
        public String[] aliveIp;
        public String getFilename = null;
        public String[] getSplit;
        String[] reretrieve = new String[5];
        String[] reretrieve_file = new String[5];
        String[] reretrieve_arg = new String[11];
        public String token;
        public int id_this;
        String[] notuse;
        public String where;
        String[] Form = { UserSchema._ID, UserSchema._RECEIVEID };
        Retrieve retrieve = new Retrieve();
        @Override
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "資料下載中", true);
            pdialog.show();
        }
        @Override
        protected String[] doInBackground(Void... params) {
            // String[] refile = new String[4];

            // 取得最近IP
            token = getinfo.get(listId).gettoken();
            if (viewmod.equals("preview")) {
                retrieve.viewmod = viewmod;
                updateState("download", "messagetoken='" + token + "'");
                reretrieve = retrieve.retrieve_req(token, retrieve.viewmod);
                reretrieve_arg[0]=reretrieve[0];
                reretrieve_arg[1]=reretrieve[1];
                reretrieve_arg[2]=reretrieve[2];
                reretrieve_arg[3]=reretrieve[3];
                reretrieve_arg[4]=reretrieve[4];

                if (reretrieve[0] == "true"){ // retrieve_req=ret=0

                    try {

                        reretrieve_file = retrieve.saveBinaryFile(token, 0);
                        updateState("", "messagetoken='" + token + "'");
                        reretrieve_arg[5]=reretrieve_file[0];
                        reretrieve_arg[6]=reretrieve_file[1];
                        reretrieve_arg[7]=reretrieve_file[2];
                        reretrieve_arg[8]=reretrieve_file[3];
                        reretrieve_arg[9]=reretrieve_file[4];
                    } catch (IOException ex) {
                        reretrieve_arg[5]="server error";
                        reretrieve_arg[10]="true";
                    }catch(Exception ex){
                        reretrieve_arg[5]="server error";
                        reretrieve_arg[9]="true";
                    }
                }
            }  else if (viewmod.equals("startview")) {
                retrieve.viewmod = viewmod;
                // 這邊的DB，是要更新目前在user_data中的簡訊的狀態，改成下載中的狀態
                // cursor(資料表來源,欲抓取的欄位名稱,條件)
                // 抓取的欄位名稱：user_data資料表中ID值，條件：itemclick點擊時所得的token

                updateState("download", "messagetoken='" + token + "'");
                // 先做request的動作，傳入token去server，取得該token所持有的檔案id
                upmsg="目前正在與"+ AttachParameter.connect_ip+"連線中";
                mHandler.obtainMessage(upDate).sendToTarget();

                reretrieve = retrieve.retrieve_req_for_d2d(AttachParameter.connect_ip, AttachParameter.connect_port,getinfo.get(listId).gettoken());
                reretrieve_arg[0]=reretrieve[0];
                reretrieve_arg[1]=reretrieve[1];
                reretrieve_arg[2]=reretrieve[2];
                reretrieve_arg[3]=reretrieve[3];
                reretrieve_arg[4]=reretrieve[4];
                //reretrieve = retrieve.retrieve_req(getinfo.get(listid).gettoken(), mod);
                System.out.println("回復有問題之處 "+reretrieve);
                if (reretrieve[0] == "true"){ // retrieve_req=ret=0

                    fileList.clear(); // 先將fileList清空 以免讀到上次的值
                    getSplit = reretrieve[1].split("&");
                    //reretrieve[0]=/storage/emulated/0/KM/file_name0_0-MOV_0259.mp4&/storage/emulated/0/KM/file_name0_1-MOV_0259.mp4
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

                            upmsg="請勿任意移動位置，避免斷線，目前正在與"+ AttachParameter.connect_ip+"連線中，並下載第"+(j+1)+"/"+retrieve.fileid.length;
                            mHandler.obtainMessage(upDate).sendToTarget();
                            //20160905學長加回(getContentResolver())
                            reretrieve_file =retrieve.saveBinaryFile_for_d2d(token, j,getContentResolver()); // 收取締n塊檔案
                            reretrieve_arg[5]=reretrieve_file[0];
                            reretrieve_arg[6]=reretrieve_file[1];
                            reretrieve_arg[7]=reretrieve_file[2];
                            reretrieve_arg[8]=reretrieve_file[3];
                            reretrieve_arg[9]=reretrieve_file[4];
                            if(reretrieve_file[0].equalsIgnoreCase("true")){
                                DLfilename = retrieve.filename;
                                // 篩選回傳的filename，Pattern是你要篩選的內容，Matcher是你要寵哪一個字串做篩選
                                boolean[] checktype = new boolean[AttachParameter.filetype];
                                checktype = AttachParameter.checktype(DLfilename);
                                System.out.println("回復有問題之處3 "+DLfilename);
                                // 這裡DB的動作為，紀錄已收到的file id，會不斷地把成功的id，從DB中刪除
                                replaceSeq("messagetoken='" + token + "'", "retrive",reretrieve_file[1]);
                                // 如果這次下載的是影片
                                if (checktype[AttachParameter.video]) {
                                    // 將檔名加入倒arraylist
                                    fileList.add(DLfilename);
                                    if(retrieve.fileid.length>3){
                                        if (j == 3 ) { // 只在第一塊收完後啟動播放
                                            Intent intent = new Intent();
                                            intent.setClass(OpenFire.this, VideoView.class);
                                            Bundle bundle = new Bundle();
                                            // changeToSeries是自定義的函數，目的是建立一個具有順序的清單
                                            bundle.putString("playList", changeToSeries(token,DLfilename, retrieve.fileid.length)); // 將參數傳遞至videoView
                                            intent.putExtras(bundle);

                                            startActivity(intent);

                                        }
                                    }else{
                                        if (j == 0 ) { // 只在第一塊收完後啟動播放
                                            Intent intent = new Intent();
                                            intent.setClass(OpenFire.this, VideoView.class);
                                            Bundle bundle = new Bundle();
                                            // changeToSeries是自定義的函數，目的是建立一個具有順序的清單
                                            bundle.putString("playList", changeToSeries(token,DLfilename, retrieve.fileid.length)); // 將參數傳遞至videoView
                                            intent.putExtras(bundle);

                                            startActivity(intent);

                                        }
                                    }


                                }
                                //=====20160903抓蟲版=====//
                                else if(checktype[AttachParameter.photo]){
                                    fileList.add(DLfilename);
                                }
                                else {// 如果不是影片，則直接撥放
                                    viewfile(sdcardPath + DLfilename);
                                }
                            }else{
                                break;
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

            }
            return notuse;
        }

        protected void onPostExecute(String[] notuse) {
            pdialog.dismiss();
            updateState("", "messagetoken='" + token + "'");
            if(reretrieve_arg[0]=="true"){
                //retreive成功
                if(reretrieve_arg[5]=="true"){
                    //retreive file成功
                    if (viewmod.equals("preview")) {
                        boolean[] checktype = new boolean[AttachParameter.filetype];
                        checktype = AttachParameter.checktype(retrieve.filename);
                        if(checktype[AttachParameter.photo]){
                            //=======20160903抓蟲版========//
                            Cursor first_ready = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "messagetoken='" + token + "'", null, null);
                            ContentValues values = new ContentValues();

                            if (first_ready.getCount() > 0){
                                first_ready.moveToFirst();
                                values.put(UserSchema._FIRST, retrieve.filename);
                                //=======20160905加回============//
                                values.put(UserSchema._LENGTH_RECORD, "file0_0=0&");
                                //=======20160905加回============//
                                int id_this = Integer.parseInt(first_ready.getString(0));
                                String where = UserSchema._ID + " = " + id_this;
                                getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                            }
                            first_ready.close();
                            //=======20160903抓蟲版========//
                            viewfile(sdcardPath + retrieve.filename);
                        }else{
                            viewfile(retrieve.filename);
                        }
                    }else if (viewmod.equals("startview")) {

						/*
						 * cursor(資料表來源,欲抓取的欄位名稱,條件)//抓取的欄位名稱：user_data資料表中ID值，條件
						 * ：itemclick點擊時所得的token
						 *
						 * 因為已經下載完了，所以user_state把download取消
						 */
                        updateState("", "messagetoken='" + token + "'");
                        if (fileList.size() == 0){ // 若檔案不是video fileList=0
                            getFilename = retrieve.filename;
                        } else{ // 若檔案是video 則用&號串成一串，目的是要做影片清單(這裡的清單內容是是檔名串起來的)
                            getFilename = "";
                            for (int i = 0; i < fileList.size(); i++)
                                getFilename = getFilename + "&" + token+"_"+fileList.get(i).replace(".", "-_" + String.valueOf(i) + ".");
                        }
                        // 因為都下載完了，所以更新filepath
                        Cursor up_filepath = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "messagetoken='" + token + "'", null, null);
                        ContentValues values = new ContentValues();
                        if (up_filepath.getCount() > 0) {
                            up_filepath.moveToFirst();
                            values.put(UserSchema._FILEPATH, getFilename);
                            //=====20160903抓蟲版============//
                            values.put(UserSchema._FILENAME, retrieve.filename);
                            //=====20160903抓蟲版============//
                            int id_this = Integer.parseInt(up_filepath.getString(0));
                            String where = UserSchema._ID + " = " + id_this;
                            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                            getContentResolver().insert(Uri.parse("content://tab.list.d2d/file_choice"), values);

                            values = new ContentValues();
                            values.put(UserSchema._FILEPATH, attachment);
                            values.put(UserSchema._FILENAME, retrieve.filename);
                            values.put(UserSchema._DURATION, duration);
                            values.put(UserSchema._FILESIZE, file_size);
                            getContentResolver().update(Uri.parse("content://tab.list.d2d/file_choice"), values , where, null);
                        }
                        up_filepath.close();

                        updateNotification("0","wlan");
                        onResume();
                    }
                }else if(reretrieve_arg[5]=="false"){
                    //retreive file 失敗 (得到200以外的code)
                    //要讀body
                    Pattern pattern_msg = Pattern.compile("message does not exist"); // check file type
                    Matcher matcher_msg = pattern_msg.matcher(reretrieve_arg[6]);
                    Pattern pattern_file = Pattern.compile("file is not ready"); // check file type
                    Matcher matcher_file = pattern_file.matcher(reretrieve_arg[6]);
                    Pattern pattern_file_II = Pattern.compile("file not found"); // check file type
                    Matcher matcher_file_II = pattern_file_II.matcher(reretrieve_arg[6]);
                    if(matcher_msg.find()){
                        titleShow ="錯誤";
                        message ="訊息並不存在server上，請刪除此訊息";
                        bmsg="確定";
                    }else if(matcher_file.find()){
                        titleShow ="錯誤";
                        message ="對方在檔案時發生錯誤，請稍後再試";
                        bmsg="確定";
                    }else if(matcher_file_II.find()){
                        titleShow ="錯誤";
                        message ="此檔案序號不存在，請刪除此訊息";
                        bmsg="確定";
                    }
                    mHandler.obtainMessage(show_msg).sendToTarget();
                }else {
                    //server error
                    if(reretrieve_arg[7]!=null && reretrieve_arg[7].equalsIgnoreCase("true")){
                        titleShow ="警告";
                        message ="伺服器內部發生錯誤，請暫停使用McaaS";
                        bmsg="確定";
                    }else if(reretrieve_arg[8]!=null && reretrieve_arg[8].equalsIgnoreCase("true")){
                        titleShow ="警告";
                        message ="連結失敗，請暫停使用McaaS";
                        bmsg="確定";
                    }else if (reretrieve_arg[9]!=null && reretrieve_arg[9].equalsIgnoreCase("true")){
                        titleShow ="警告";
                        message ="未知的錯誤，請暫停使用McaaS";
                        bmsg="確定";
                    }else if (reretrieve_arg[10]!=null && reretrieve_arg[10].equalsIgnoreCase("true")){
                        titleShow ="警告";
                        message ="內部儲存空間發生錯誤，請暫停使用McaaS";
                        bmsg="確定";
                    }
                    mHandler.obtainMessage(show_msg).sendToTarget();
                }
            }else if(reretrieve_arg[0]=="false"){
                //retreive失敗 (得到200以外的code)
                //要讀body
                Pattern pattern_msg = Pattern.compile("message does not exist"); // check file type
                Matcher matcher_msg = pattern_msg.matcher(reretrieve_arg[1]);
                Pattern pattern_msg_II = Pattern.compile("file is not ready"); // check file type
                Matcher matcher_msg_II = pattern_msg_II.matcher(reretrieve_arg[1]);
                if(matcher_msg.find()){
                    titleShow ="錯誤";
                    message ="訊息並不存在server上，請刪除此訊息";
                    bmsg="確定";
                }else if(matcher_msg_II.find()){
                    titleShow ="錯誤";
                    message ="錯誤的訊息編號，請刪除此訊息";
                    bmsg="確定";
                }
                mHandler.obtainMessage(show_msg).sendToTarget();
            }else{
                //server error
                if(reretrieve_arg[2]!=null && reretrieve_arg[2].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="伺服器內部發生錯誤，請暫停使用McaaS";
                    bmsg="確定";
                }else if(reretrieve_arg[3]!=null && reretrieve_arg[3].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="連結失敗，請暫停使用McaaS";
                    bmsg="確定";
                }else if (reretrieve_arg[4]!=null && reretrieve_arg[4].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="未知的錯誤，請暫停使用McaaS";
                    bmsg="確定";
                }
                mHandler.obtainMessage(show_msg).sendToTarget();
            }
        }
    }

    // 自定義的瀏覽檔案，這裡篩選完檔案後，會自行搜尋android系統中預設的程式開啟
    public void viewfile(String attachment) {
        boolean[] checktype = new boolean[AttachParameter.filetype];
        checktype = AttachParameter.checktype(attachment);

        if (checktype[AttachParameter.music]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(attachment);
            it.setDataAndType(Uri.fromFile(file), "audio/*");

            startActivity(it);
        } else if (checktype[AttachParameter.video]) {
            if (viewmod.equals("preview")) {
                Intent intent = new Intent();
                intent.setClass(OpenFire.this, VideoView.class);
                Bundle bundle = new Bundle();
                bundle.putString("playList", attachment); // 將參數傳遞至videoView
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setClass(OpenFire.this, VideoView.class);
                //intent.setClass(receivelist.this, VideoPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("playList", attachment); // 將參數傳遞至videoView
                intent.putExtras(bundle);
                startActivity(intent);
            }

        } else if (checktype[AttachParameter.photo]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(attachment);
            it.setDataAndType(Uri.fromFile(file), "image/*");

            startActivity(it);
        } else {
            System.out.println("這邊錯啦"+attachment);
            AlertDialog.Builder Dialog = new AlertDialog.Builder(this); // Dialog
            Dialog.setTitle("抱歉...檔案位置不存在");
            Dialog.setMessage("請試著重新整理");
            Dialog.setIcon(android.R.drawable.ic_dialog_info);
            Dialog.setNegativeButton("確定", new DialogInterface.OnClickListener() { // 不接收檔案
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            Dialog.show();
        }

    }
    private class request_for_d2d extends AsyncTask<Void, Void, String> {
        public String[] aliveIp;

        @Override
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "通知中", true);
            pdialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String reretrieve = new String();
            reretrieve=user.setd2d(getinfo.get(listId).gettoken(),urgent);
            return reretrieve;
        }

        protected void onPostExecute(String reretrieve) {
            pdialog.dismiss();
            if (AttachParameter.chechsuccess(reretrieve)) {
                Toast.makeText(getApplicationContext(), "發送成功，對方開啟server後將自動下載", Toast.LENGTH_LONG).show();
                if(urgent==0){
                    updateNotification("1","d2d");
                }else if(urgent==1){
                    updateNotification("2","sms");
                }
                //初始化urgent
                urgent = 0;

            } else {
                Toast.makeText(getApplicationContext(), "發送失敗，請重新再試", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
     * 自定義的非同步函式，這裡會繼續下載未載完的檔案，動作跟retrive一樣，唯一不同的是 檔案id是重db中抓出來的
     */
    private class retrieveAgain extends AsyncTask<Void, Void, String[]> {
        String[] reretrieve_string=new String[1];
        Retrieve retrieve = new Retrieve();
        String playpath =new String();
        String[] reretrieve_file = new String[7];
        boolean has_error=false;
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "資料下載中", true);
            pdialog.show();
        }
        @Override
        protected String[] doInBackground(Void... arg0) {
            User user = new User();
            String[] res =user.getservicetime(Again_sender);
            if(res[1].equalsIgnoreCase("true")){
                updateState("download", "messagetoken='" + Again_token + "'");
                String[] socketport = res[2].replace("socket=", "").split(":");
                //取出ip 跟port
                //檢查目標是否有內外網
                if(AttachParameter.out_ip.equals(socketport[0])){
                    //外網ip相同，給內網ip
                    AttachParameter.connect_ip=socketport[1];
                    System.out.println("這邊是 d2d socketport[1]="+socketport[1]);
                }else{
                    //外網ip不同，給外網
                    AttachParameter.connect_ip=socketport[0];
                    System.out.println("這邊是 d2d socketport[0]="+socketport[0]);
                }
                AttachParameter.connect_port=socketport[2];
                updateNotification("3", "d2d");

                retrieve.fileid = Again_id;
                for (int j = 0; j < retrieve.fileid.length; j++) {
                    try {
                        retrieve.settoken(Again_token);
                        //20160905學長加回(getContentResolver())
                        reretrieve_file=retrieve.saveBinaryFile_for_d2d(Again_token, j, getContentResolver());
                        upmsg="請勿任意移動位置，避免斷線，目前正在下載第"+(j+1)+"/"+retrieve.fileid.length;
                        mHandler.obtainMessage(upDate).sendToTarget();
                        if(reretrieve_file[0].equalsIgnoreCase("true")){
                            // 自定義的
                            replaceSeq("messagetoken='" + Again_token + "'", "again",reretrieve_file[1]);
                            playpath = playpath + "&" + Again_token+"_"+retrieve.filename.replace(".", "-_" + String.valueOf(j) + ".");
                        }else{
                            has_error=true;
                            break;
                        }
                        // 自定義的

                    } catch (IOException ex) {
                        reretrieve_file[0]="server error";
                        reretrieve_file[5]="true";
                    }catch(Exception ex){
                        reretrieve_file[0]="server error";
                        reretrieve_file[4]="true";
                    }
                }
                if(!has_error){
                    String[] tokenID = { UserSchema._ID};
                    Cursor update_play_path = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + Again_token + "'", null, null);
                    if (update_play_path.getCount() > 0) {
                        update_play_path.moveToFirst();

                        int id_this = Integer.valueOf(update_play_path.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FILEPATH, playpath);
                        String where = UserSchema._ID + " = " + id_this;
                        getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                    }
                    update_play_path.close();


                    updateNotification("0","wlan");
                    updateState("", "messagetoken='" + Again_token + "'");
                }
                reretrieve_string[0]="true";

            }else{
                reretrieve_file[6]="true";
                reretrieve_file[0]="server error";
                Again_token="";
                updateNotification("2","sms");
                updateState("", "messagetoken='" + getinfo.get(listId).gettoken() + "'");
                //Toast.makeText(getApplicationContext(), "對方尚未開啟server", Toast.LENGTH_LONG).show();
            }


            return reretrieve_string;
        }

        protected void onPostExecute(String[] notuse) {
            pdialog.dismiss();
            updateState("", "messagetoken='" + Again_token + "'");
            if (reretrieve_file[0].equalsIgnoreCase("true")) {
                titleShow ="恭喜";
                message ="檔案下載成功!";
                bmsg="確定";
                onResume();
                mHandler.obtainMessage(show_msg).sendToTarget();

            }else if(reretrieve_file[0]=="false"){
                //retreive file 失敗 (得到200以外的code)
                //要讀body
                Pattern pattern_msg = Pattern.compile("message does not exist"); // check file type
                Matcher matcher_msg = pattern_msg.matcher(reretrieve_file[1]);
                Pattern pattern_file = Pattern.compile("file is not ready"); // check file type
                Matcher matcher_file = pattern_file.matcher(reretrieve_file[1]);
                Pattern pattern_file_II = Pattern.compile("file not found"); // check file type
                Matcher matcher_file_II = pattern_file_II.matcher(reretrieve_file[1]);

                if(matcher_msg.find()){
                    titleShow ="錯誤";
                    message ="訊息並不存在server上，請刪除此訊息";
                    bmsg="確定";
                }else if(matcher_file.find()){
                    titleShow ="錯誤";
                    message ="對方在檔案時發生錯誤，請稍後再試";
                    bmsg="確定";
                }else if(matcher_file_II.find()){
                    titleShow ="錯誤";
                    message ="此檔案序號不存在，請刪除此訊息";
                    bmsg="確定";
                }
                mHandler.obtainMessage(show_msg).sendToTarget();
            }else {
                //server error
                if(reretrieve_file[2]!=null && reretrieve_file[2].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="伺服器內部發生錯誤，請暫停使用McaaS";
                    bmsg="確定";
                }else if(reretrieve_file[3]!=null && reretrieve_file[3].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="對方server未開，請暫停使用McaaS";
                    bmsg="確定";
                }else if (reretrieve_file[4]!=null && reretrieve_file[4].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="未知的錯誤，請暫停使用McaaS";
                    bmsg="確定";
                }else if (reretrieve_file[5]!=null && reretrieve_file[5].equalsIgnoreCase("true")){
                    titleShow ="警告";
                    message ="內部儲存空間發生錯誤，請暫停使用McaaS";
                    bmsg="確定";
                }
                else if (reretrieve_file[6]!=null && reretrieve_file[6].equalsIgnoreCase("true")){
                    titleShow ="下載失敗";
                    message ="對方尚未開啟Server，請稍後再試";
                    bmsg="確定";
                }
                mHandler.obtainMessage(show_msg).sendToTarget();
            }

        }

    }

    private class del_msg extends AsyncTask<Void, Void, String> {
        public String[] aliveIp;

        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "資料刪除中", true);
            pdialog.show();
        }
        @Override
        protected String doInBackground(Void... arg0) {
            user = new User();
            String res=user.delete_msg(gettoken);
            user=null;
            return res;
        }
        protected void onPostExecute(String reretrieve) {
            pdialog.dismiss();
            Boolean res= AttachParameter.chechsuccess(reretrieve);
            if (res) {
                AlertDialog.Builder Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                Dialog.setTitle("");
                Dialog.setMessage("檔案刪除成功");
                Dialog.setIcon(android.R.drawable.ic_dialog_info);
                Dialog.setNeutralButton("確定", new DialogInterface.OnClickListener() { // 按下retry
                    // 將thread結束
                    // 再跑一個新的thread
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResume();
                    }
                });
                Dialog.show();

            } // reretrieve[1]=="true"
            else {
                AlertDialog.Builder Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                Dialog.setTitle("");
                Dialog.setMessage("檔案刪除失敗");
                Dialog.setIcon(android.R.drawable.ic_dialog_info);
                Dialog.setNeutralButton("確定", new DialogInterface.OnClickListener() { // 按下retry
                    // 將thread結束
                    // 再跑一個新的thread
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                Dialog.show();

            }

        }
    }

    /*
     * 這邊是監聽，當對歷史訊息久按時，出現的menu清單
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("");
        menu.setHeaderIcon(R.drawable.m2m_ver01);
        // 三種選擇

        menu.add(0, ITEM1, 0, "重新下載");
        menu.add(0, ITEM2, 0, "刪除訊息");
        menu.add(0, ITEM3, 0, "轉寄");
//        menu.add(0, ITEM3, 0, "取消");

        if (AttachParameter.priority == 0){
            menu.add(0, ITEM4, 0, "解密");
        }else {
            menu.add(0,ITEM5, 0, "進入戰備");
        }

    }

    /*
     * 這邊是監聽是按下menu中哪一個item，根據不同的item做不同的事
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = getinfo.size() - (int) getListAdapter().getItemId(menuInfo.position) - 1;
        listId = pos;
        gettoken = getinfo.get(pos).gettoken();
        String[] tokenID = { UserSchema._ID, UserSchema._FILEID, UserSchema._RECEIVEID, UserSchema._SENDER };
        switch (item.getItemId()) {
		/*
		 * item1視作重新下載的動作，只要去db中抓出在fileid即可，
		 * fileid是當初案notify通知時，預先紀錄的id，接著做retrive相同的動作
		 */
            case ITEM1:
                final String[] itemForm = { UserSchema._RECEIVEID, UserSchema._MESSAGETOKEN, UserSchema._FILEPATH, UserSchema._FILEID };

                // 三種選擇
                //Cursor data_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), itemForm, "notification = '0' and messagetoken='" + getinfo.get(listid).gettoken() + "' and receive_id ='' and file_id is not null", null, null);

                Cursor data_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), itemForm, "(notification = '0' or notification = '3')and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                if(data_cursor.getCount()>0){

                    Cursor CH_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "userstatus='download'", null, null);
                    if (CH_state.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "目前尚有檔案下載中，稍後再試", Toast.LENGTH_LONG).show();

                    } else {
                        updateNotification("2", "sms");
                        Cursor copy_fileid_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + gettoken + "'", null, null);
                        if (copy_fileid_cursor.getCount() > 0) {
                            copy_fileid_cursor.moveToFirst();

                            Again_id = copy_fileid_cursor.getString(1).substring(1,copy_fileid_cursor.getString(1).length()).split("&");
                            Again_token = gettoken;
                            Again_sender = copy_fileid_cursor.getString(3);
                            int id_this = Integer.valueOf(copy_fileid_cursor.getString(0));
                            ContentValues values = new ContentValues();
                            values.put(UserSchema._RECEIVEID, copy_fileid_cursor.getString(1));
                            values.put(UserSchema._FILEPATH, "");

                            String where = UserSchema._ID + " = " + id_this;
                            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                        }
                        copy_fileid_cursor.close();
                        // 有了fileid後，即可重新下載
                        new retrieveAgain().execute();
                        //viewfile(sdcardPath + getinfo.get(listid).getFilename());

                    }
                    CH_state.close();
                }
                else{
                    Toast.makeText(getApplicationContext(), "抱歉，檔案從未下載過，無法重新下載", Toast.LENGTH_SHORT).show();
                }
                data_cursor.close();
                break;
            case ITEM2:
                // --------------------Delete------------
                // cursor(資料表來源,欲抓取的欄位名稱,條件)//抓取的欄位名稱：user_data資料表中ID值，條件：itemclick點擊時所得的token
                Cursor sender_data_group_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + gettoken + "'", null, null);
                if (sender_data_group_cursor.getCount() > 0) {
                    sender_data_group_cursor.moveToFirst();
                    int id_this = 0;
                    id_this = Integer.valueOf(sender_data_group_cursor.getString(0));
                    String where = UserSchema._ID + " = " + id_this;
                    getContentResolver().delete(Uri.parse("content://tab.list.d2d/user_data"),where, null);

                }
                sender_data_group_cursor.close();
                new del_msg().execute();
                break;
            case ITEM3:
                Cursor fileAttach = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"),
                        new String[] {UserSchema._FILEPATH, UserSchema._FILENAME, UserSchema._ID}, null, null, null);
                if (fileAttach.getCount() > 0){
                    fileAttach.moveToFirst();
                    String filePath = fileAttach.getString(0);
                    String fileName = fileAttach.getString(1);
                    String fileId = fileAttach.getString(2);

                    System.out.println("file detail = " + filePath + " " + fileName + " " + fileId + " ");

                    LayoutInflater factory = LayoutInflater.from(OpenFire.this);
                    final View v1 = factory.inflate(R.layout.check_password, null);

                    chk_Password = (EditText) v1.findViewById(R.id.chk_Password);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(OpenFire.this);
                    dialog.setTitle("身分確認");
                    dialog.setView(v1);
                    dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                check_pass=chk_Password.getText().toString();
                                new check_password().execute();
                            } catch (Exception e) {
                                Toast.makeText(OpenFire.this, "錯誤!", Toast.LENGTH_LONG).show();
                                System.out.println("身分確認後: " + e.getMessage());
                            }
                        }

                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                    //發佈
                    Toast.makeText(OpenFire.this,"!!!!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(OpenFire.this,"file is nothing",Toast.LENGTH_SHORT).show();
                }
                fileAttach.close();
                break;
            case ITEM4:
                new decrypt().execute();
                break;
            case ITEM5:
                new check_password().execute();
                break;

        }
        return super.onContextItemSelected(item);

    }

    @Override
    protected void onResume() {// 此處為,每次切換頁面時,立即做的更新
        super.onResume();
        if(finish){
            mailCount = 0;
            list.clear();
            list = getData();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class check_password extends AsyncTask<Void, Void, String> { // implement
        // thread
        public String where;
        public String path;

        // Asyntask 前置作業
        @Override
        protected void onPreExecute() {
            sendDialog = ProgressDialog.show(OpenFire.this, "請稍候", "密碼確認中", true);
            sendDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String er = "no&";
            User check_state=new User();
            String res=check_state.check_password("password="+check_pass);

            return res;
        }

        protected void onPostExecute(String res) {
            sendDialog.dismiss();
            Boolean reslut=AttachParameter.chechsuccess(res);
            if(reslut){
                LayoutInflater factory = LayoutInflater.from(OpenFire.this);
                final View v1 = factory.inflate(R.layout.input_reciever, null);
                input_receiver = (EditText) v1.findViewById(R.id.input_receiver);

                AlertDialog.Builder dialog = new AlertDialog.Builder(OpenFire.this);
                dialog.setTitle("輸入接收者");
                dialog.setView(v1);
                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            receiver=input_receiver.getText().toString();
                            if (receiver.equals("")) {
                                Toast.makeText(OpenFire.this, "接收者不可為空白", Toast.LENGTH_LONG).show();
                            }else{
                                title = "S00"+(id + 1);
                                content = "進入戰備";
                                System.out.println("Show: " + title);
                                fileUploadToSend("write");
                                Toast.makeText(OpenFire.this,"OK",Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            System.out.println("輸入接收者後: " + e.getMessage());
                            Toast.makeText(OpenFire.this, "錯誤!", Toast.LENGTH_LONG).show();
                        }
                    }

                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }else{
                Toast.makeText(OpenFire.this,"密碼錯誤，請從新輸入",Toast.LENGTH_SHORT).show();
            }


        }
    }


    public void fileUploadToSend(String arg) {
        // 檢查使用者有沒有選擇要上傳的檔案,選擇好的檔案會寫入file_choice的table內
        file_path = new ArrayList<String>();
        Cursor up_file_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        if (up_file_cursor.getCount() > 0) {
            System.out.println("Show up_file_cursor: " + up_file_cursor.getCount());
            dialog = ProgressDialog.show(OpenFire.this, "請稍候", "資料處理中", true);
            dialog.show();
            up_file_cursor.moveToFirst();
            fileName = up_file_cursor.getString(0);
            attachFile = fileName.replace("&", "");
            attachPath = sdcardPath + attachFile;
//            file_path.add(up_file_cursor.getString(0));
            file_path.add(attachPath);
            selfId = randomString(20);
            postFile = new String();
            System.out.println("Show up_file_cursor detail: " + up_file_cursor.getCount() + " selfId: " + selfId + "\nfile_path: " + file_path );
            for (index = 0; index < up_file_cursor.getCount(); index++) {
                System.out.println("for loop entry, index: " + index);
                boolean[] checktype = new boolean[AttachParameter.filetype];
                checktype = AttachParameter.checktype(attachPath);
                System.out.println("for loop entry, checktype: " + checktype);
                // 如果選擇的是影片，則計算他的影片長度、大小，目的是要傳給ffmpeg使用
                if (checktype[AttachParameter.video]||checktype[AttachParameter.music]||checktype[AttachParameter.photo]) {
                    System.out.println("if loop entry");
                    File file = new File(file_path.get(index));
                    // 開啟計算檔案長度並存入attachSize
                    file_size = (int) file.length();
                    fileName = file.getName();
                    file_name = fileName.replace("&", "");
                    postFile = postFile + "&file_name" + index + "_0=" + file_name;
                    duration = 0;
                    file_amount = 1;
                    System.out.println("file_size: " + file_size + " file_name: " + file_name + " postFile: " + postFile);

                    //2016/06/30新增修改
//                    try {
//                        FileInputStream inputStream = new FileInputStream(new File(file_path.get(index)));
//                        byte[] data = new byte[1024];
//                        FileOutputStream outputStream =new FileOutputStream(new File(Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name));
//                        while (inputStream.read(data) != -1) {
//                            outputStream.write(data);
//                        }
//                        inputStream.close();
//                        outputStream.close();
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }  catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }

                    ContentValues values = new ContentValues();
                    values.put(UserSchema._FILEPATH, Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/"+file_name);//2016/06/30新增修改
                    values.put(UserSchema._FILERECORD, "file"+index + "_0");
                    values.put(UserSchema._FILECHECK, 0);
                    values.put(UserSchema._FILENAME, file_name);
                    values.put(UserSchema._SELFID, selfId);
                    getContentResolver().insert(Uri.parse("content://tab.list.d2d/temp_file"), values);
                    //dialog.dismiss();
                    state = "write";
                    dialog.dismiss();
                    new sendFile().execute();
                }
                else{
                    //2016/07/04新增修改
                    Toast.makeText(OpenFire.this, "你所選的檔案類型不支援，請重新選擇", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                up_file_cursor.moveToNext();
            }
        } else {
            Toast.makeText(OpenFire.this, "尚未選擇附加檔案，請重新選擇", Toast.LENGTH_LONG).show();
        }

        state = "write";
        up_file_cursor.close();
        index = 0;
        // 呼叫自定義的函式

    }

    private class sendFile extends AsyncTask<Void, Void, String> {
        // thread
        public String where;
        public String path,source;

        // Asyntask 前置作業
        @Override
        protected void onPreExecute() {
            sendDialog = ProgressDialog.show(OpenFire.this, "請稍候", "資料上傳中", true);
            sendDialog.show();
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

                /// ex : storage/emulated/0/DCIM/100ANDRO/MOV_0259.mp4
//                File file = new File("/storage/emulated/0/KM/0R2emEfBL_lbe-_0.png");
                File file = new File(attachPath);
                String filename = file.getName();
                filetype = filename.split("\\.");
                Cursor check_finish_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH, UserSchema._FILENAME}, "filecheck='0' and filerecord='file0_0' and selfid='" + selfId + "'", null, null);
                if(check_finish_cursor.getCount()>0){
                    check_finish_cursor.moveToFirst();
                    file0_0=check_finish_cursor.getString(0);
                    fileName = check_finish_cursor.getString(1);
                    filename = fileName.replace("&", "");
                    firstfile =new File(file0_0 + filename);

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
                        "&selfid="+selfId+"&receiver=" + receiver +
                        "&filecnt=" + file_amount + "&duration=" + duration +
                        "&filename=" + filename + "&filetype=" + filetype[1] +
                        "&filepath=" + attachPath + "&length=" + file_size +
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
                    Cursor change_token = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfId + "'", null, null);
                    if (change_token.getCount() > 0) {
                        change_token.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_token.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._MESSAGETOKEN, resp.replace("ret=0&token=", ""));
                        String where = UserSchema._ID + " = " + id_this;
                        getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_token.close();

                    //第一塊上傳完  更新FIRST為TRUE
                    Cursor change_first = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfId + "'", null, null);
                    if (change_first.getCount() > 0) {
                        change_first.moveToFirst();
                        int id_this = 0;
                        id_this = Integer.valueOf(change_first.getString(0));
                        ContentValues values = new ContentValues();
                        values.put(UserSchema._FIRST, "true");
                        String where = UserSchema._ID + " = " + id_this;
                        getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_content"), values, where, null);

                    }
                    change_first.close();

                    //20160903 更改抓蟲版
                    token=resp.replace("ret=0&token=", "");

                    Cursor up_tempfile = getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), Form, "selfid='" + selfId + "' and filecheck='0'", null, null);
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
                        getContentResolver().update(Uri.parse("content://tab.list.d2d/temp_file"), values, where, null);
                    }
                    up_tempfile.close();

                }
            }
            return er;
        }

        protected void onPostExecute(String er) {
            String[]resp = er.split("&");
            sendDialog.dismiss();
            if (resp[0].equals("yes")) {

                if(resp.length>1 &&resp[1].equalsIgnoreCase("timeout")){
                    mHandler.obtainMessage(timeOut).sendToTarget(); // 傳送要求更新list的訊息給handler
                }
                else{
                    mHandler.obtainMessage(error).sendToTarget();
                }

            } else {
                // 傳送要求更新list的訊息給handler
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                mHandler.obtainMessage(ok).sendToTarget();

            }

        }
    }

    private class decrypt extends AsyncTask<Void, Void, String> {
        String source = null,filename = null;
        String[] two_source;
        Bitmap source1,source2,final_image;
        OutputStream f = null;

        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "影像解密中", true);
            pdialog.show();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            String res = null;
            //Cursor get_source = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._FILEPATH,UserSchema._FILENAME}, "messagetoken='" + gettoken + "'", null, null);

            Cursor get_source = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._FILEPATH, UserSchema._FILENAME}, "messagetoken='" + gettoken + "'", null, null);
            int count=get_source.getCount();
            if(count>0){
                get_source.moveToFirst();
                source=get_source.getString(0);
                filename = get_source.getString(1);
                //做兩次token名稱的篩選(篩選"_"),如果檔名是由token + "_"組成的話
            }
            get_source.close();

            //拿source做字串處理(indexOf)
            Cursor sourceGet = getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_fire"), new String[]{UserSchema._FILE_1, UserSchema._FILE_2}, "file_1'" + source + "'", null, null);

            AttachParameter.image_decrypt_file = sdcardPath + UserSchema._FILENAME + "_reslut.png";
            two_source=source.split("&");
            source1 = BitmapFactory.decodeFile(sdcardPath + two_source[1]);
            source2 = BitmapFactory.decodeFile(sdcardPath + two_source[2]);
//            source1 = BitmapFactory.decodeFile(AttachParameter.sdcardPath+"file_name0_0-lbe.png");
//            source2 = BitmapFactory.decodeFile(AttachParameter.sdcardPath+"file_name0_1-lbe.png");
            //解碼
            final_image = Bitmap.createBitmap(source1.getWidth(),source1.getHeight(), Bitmap.Config.ARGB_4444);
            for (int i = 0; i < source1.getHeight(); i += 2) {
                for (int j = 0; j < source1.getWidth(); j += 2) {
                    if (source1.getPixel(j, i) == -16777216 && source2.getPixel(j+1,  i) == -16777216){
                        final_image.setPixel(j, i, -16777216);
                        final_image.setPixel(j+1, i, -16777216);
                        final_image.setPixel(j, i+1, -16777216);
                        final_image.setPixel(j+1, i+1, -16777216);
                    }
                    else if (source1.getPixel(j, i) == -1 && source2.getPixel(j+1,  i) == -1){
                        final_image.setPixel(j, i, -16777216);
                        final_image.setPixel(j+1, i, -16777216);
                        final_image.setPixel(j, i+1, -16777216);
                        final_image.setPixel(j+1, i+1, -16777216);
                    }
                    else {
                        final_image.setPixel(j, i, -1);
                        final_image.setPixel(j+1, i, -1);
                        final_image.setPixel(j, i+1, -1);
                        final_image.setPixel(j+1, i+1, -1);
                    }

                }
                System.out.println("輸出總Height是"+source1.getHeight()+" 目前Height是"+i);

            }

            try{

                f = new FileOutputStream(new File( AttachParameter.image_decrypt_file));
                final_image.compress(Bitmap.CompressFormat.PNG, 100, f);

                f.flush();
                f.close();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try{
                    if(f!=null){
                        f.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            return res;
        }
        protected void onPostExecute(String reretrieve) {

            //原圖路徑進入好放進file_choice

            pdialog.dismiss();
            AlertDialog.Builder Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
            Dialog.setTitle("");
            Dialog.setMessage("解密成功");
            Dialog.setIcon(android.R.drawable.ic_dialog_info);
            Dialog.setNeutralButton("確定", new DialogInterface.OnClickListener() { // 按下retry
                // 將thread結束
                // 再跑一個新的thread
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onResume();
                }
            });
            Dialog.show();

        }
    }

    private class chfile_for_m2m extends AsyncTask<String, Void, String> {
        String token;
        String resp;
        @Override
        protected void onPreExecute() {
            // 開啟資料傳送dialog
            pdialog = ProgressDialog.show(OpenFire.this, "請稍候", "確認對方是否允許連線", true);
            pdialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String[] res =user.getservicetime(sender);

            if(res[1].equalsIgnoreCase("true")){
                token = getinfo.get(listId).gettoken();
                String[] socketport = res[2].replace("socket=", "").split(":");
                //取出ip 跟port
                //檢查目標是否有內外網
                if(AttachParameter.out_ip.equals(socketport[0])){
                    //外網ip相同，給內網ip
                    AttachParameter.connect_ip=socketport[1];
                    System.out.println("這邊是 d2d socketport[1]="+socketport[1]);
                }else{
                    //外網ip不同，給外網
                    AttachParameter.connect_ip=socketport[0];
                    System.out.println("這邊是 d2d socketport[0]="+socketport[0]);
                }
                AttachParameter.connect_port=socketport[2];
                Retrieve retrieve=new Retrieve();
                System.out.println("這邊是 d2d socketport[0]="+socketport[0]);
                resp = retrieve.check_file_for_m2m(AttachParameter.connect_ip+":"+ AttachParameter.connect_port, token);

            }else{
                resp="server not ready";
            }


            return resp;
        }
        protected void onPostExecute(String reretrieve) {
            pdialog.dismiss();
            if(reretrieve.equalsIgnoreCase("server not ready")){				// 若er成立，則表示輸入的收件者不存在

                Cursor ch0_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "notification = '2' and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                if(ch0_cursor.getCount()>0){

                    AlertDialog.Builder T_Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                    T_Dialog.setTitle("對方檔案尚未準備好");
                    T_Dialog.setMessage("請問要保留訊息嗎?");
                    T_Dialog.setIcon(android.R.drawable.ic_dialog_info);

                    T_Dialog.setNegativeButton("保留", new DialogInterface.OnClickListener() { // 不接收檔案
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    T_Dialog.setPositiveButton("刪除", new DialogInterface.OnClickListener() { // 不接收檔案
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] tokenID = { UserSchema._ID, UserSchema._FILEID, UserSchema._RECEIVEID };
                            Cursor sender_data_group_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                            if (sender_data_group_cursor.getCount() > 0) {
                                sender_data_group_cursor.moveToFirst();
                                int id_this = 0;
                                id_this = Integer.valueOf(sender_data_group_cursor.getString(0));
                                ContentValues values = new ContentValues();
                                String delete = "delete";
                                values.put(UserSchema._USESTATUS, delete);
                                String where = UserSchema._ID + " = " + id_this;
                                getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);

                            }
                            sender_data_group_cursor.close();
                            Toast.makeText(getApplicationContext(), "刪除成功", Toast.LENGTH_SHORT).show();
                            onResume();
                        }
                    });
                    T_Dialog.show();


                }else{
                    AlertDialog.Builder Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                    Dialog.setTitle("對方Server尚未開啟");
                    Dialog.setMessage("請問需要通知對方開啟嗎?");
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setNeutralButton("是", new DialogInterface.OnClickListener() { // 按下retry
                        // 將thread結束
                        // 再跑一個新的thread
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread.currentThread().interrupt();

                            AlertDialog.Builder T_Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                            T_Dialog.setTitle("");
                            T_Dialog.setMessage("請選擇通知類型");
                            T_Dialog.setIcon(android.R.drawable.ic_dialog_info);

                            T_Dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() { // 不接收檔案
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            Cursor chnu_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "notification is null and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                            int num=0;
                            num=chnu_cursor.getCount();
                            chnu_cursor.close();
                            if(num>0){

                                T_Dialog.setPositiveButton("wlan", new DialogInterface.OnClickListener() { // 不接收檔案
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        urgent = 0;
                                        new request_for_d2d().execute();
                                    }
                                });
                                T_Dialog.setNegativeButton("sms", new DialogInterface.OnClickListener() { // 預覽檔案
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        urgent = 1;
                                        new request_for_d2d().execute();
                                    }
                                });
                            }else{
                                Cursor ch1_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), null, "notification = '1' and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                                num=ch1_cursor.getCount();
                                ch1_cursor.close();
                                if(num>0){
                                    T_Dialog.setNegativeButton("sms", new DialogInterface.OnClickListener() { // 預覽檔案
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            urgent = 1;
                                            new request_for_d2d().execute();
                                        }
                                    });
                                }else{
                                    T_Dialog.setNegativeButton("wlan", new DialogInterface.OnClickListener() { // 預覽檔案
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            urgent = 0;
                                            new request_for_d2d().execute();
                                        }
                                    });
                                }
                            }
                            T_Dialog.show();
                        }
                    });
                    Dialog.setNegativeButton("否", new DialogInterface.OnClickListener() { // 按下abort

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder T_Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                            T_Dialog.setTitle("檔案尚未完整上傳");
                            T_Dialog.setMessage("請問要保留訊息嗎?");
                            T_Dialog.setIcon(android.R.drawable.ic_dialog_info);

                            T_Dialog.setNegativeButton("保留", new DialogInterface.OnClickListener() { // 不接收檔案
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            T_Dialog.setPositiveButton("刪除", new DialogInterface.OnClickListener() { // 不接收檔案
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] tokenID = { UserSchema._ID, UserSchema._FILEID, UserSchema._RECEIVEID };
                                    Cursor sender_data_group_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                                    if (sender_data_group_cursor.getCount() > 0) {
                                        sender_data_group_cursor.moveToFirst();
                                        int id_this = 0;
                                        id_this = Integer.valueOf(sender_data_group_cursor.getString(0));
                                        ContentValues values = new ContentValues();
                                        String delete = "delete";
                                        values.put(UserSchema._USESTATUS, delete);
                                        String where = UserSchema._ID + " = " + id_this;
                                        getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);

                                    }
                                    sender_data_group_cursor.close();
                                    Toast.makeText(getApplicationContext(), "刪除成功", Toast.LENGTH_SHORT).show();
                                    onResume();
                                }
                            });
                            T_Dialog.show();

                        }
                    });
                    Dialog.show();
                }
                ch0_cursor.close();
            }else{
                Boolean res= AttachParameter.chechsuccess(reretrieve);
                if (res) {
                    //只有一塊的時候 NOTIFY也會強制設成3
                    updateNotification("3", "d2d");
                    final String[] itemForm = { UserSchema._RECEIVEID, UserSchema._MESSAGETOKEN, UserSchema._FILEPATH, UserSchema._FILEID };
                    Cursor ch1_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), itemForm, "notification = '3' and messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
                    if (ch1_cursor.getCount() > 0) {
                        ch1_cursor.moveToFirst();
                        String r_id = ch1_cursor.getString(0);
                        if (r_id == null) {
                            r_id = "";
                        }
                        String f_id = ch1_cursor.getString(3);
                        if (f_id == null) {
                            f_id = "";
                        }
                        if (f_id.equals("")) {// f_id為空代表沒有下載過
                            // 正式下載


                            // 開始下載,此為自定義的函式
                            viewmod = "startview";
                            new startretrieve().execute();


                        }else{
                            viewmod = "startview";

                            updateNotification("2", "sms");
                            String[] tokenID = { UserSchema._ID, UserSchema._FILEID, UserSchema._RECEIVEID, UserSchema._SENDER };
                            Cursor copy_fileid_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), tokenID, "messagetoken='" + token + "'", null, null);
                            if (copy_fileid_cursor.getCount() > 0) {
                                copy_fileid_cursor.moveToFirst();

                                Again_id = copy_fileid_cursor.getString(1).substring(1,copy_fileid_cursor.getString(1).length()).split("&");
                                Again_token = token;
                                Again_sender = copy_fileid_cursor.getString(3);
                                int id_this = Integer.valueOf(copy_fileid_cursor.getString(0));
                                ContentValues values = new ContentValues();
                                values.put(UserSchema._RECEIVEID, copy_fileid_cursor.getString(1));
                                values.put(UserSchema._FILEPATH, "");

                                String where = UserSchema._ID + " = " + id_this;
                                getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                            }
                            copy_fileid_cursor.close();
                            new retrieveAgain().execute();
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), "前一次下載失敗，請試著久按訊息，選擇重新下載", Toast.LENGTH_SHORT).show();
                    }
                    ch1_cursor.close();
                }
                else{
                    //這裡要更新他的對方server狀態
                    Toast.makeText(getApplicationContext(), "檔案還沒準備好", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    public static String changeToSeries(String token, String arg, int no) {//
        String series = new String();

        for (int i = 0; i < no; i++) {
            series = series + "&" + token+"_"+arg.replace(".", "-_" + String.valueOf(i) + ".");
        }
        return series;

    }

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

            System.out.println(check_id);
            values.put(UserSchema._RECEIVEID, check_id);
            id_this = Integer.parseInt(check_fileid_cursor.getString(0));
            where = UserSchema._ID + " = " + id_this;
            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
        }
        check_fileid_cursor.close();
    }

    private void updateState(String state, String conditional) {
        Cursor change_state = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[] { UserSchema._ID, UserSchema._RECEIVEID }, conditional, null, null);
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
    }

    private void updateNotification(String state, String type) {
        Cursor noti_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), new String[] { UserSchema._ID, UserSchema._NOTIFICATION }, "messagetoken='" + getinfo.get(listId).gettoken() + "'", null, null);
        if (noti_cursor.getCount() > 0) {
            noti_cursor.moveToFirst();
            ContentValues values = new ContentValues();
            values.put(UserSchema._NOTIFICATION, state);
            values.put(UserSchema._TYPE, type);
            int id_this = Integer.parseInt(noti_cursor.getString(0));
            String where = UserSchema._ID + " = " + id_this;
            getContentResolver().update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);

        }
        noti_cursor.close();
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

    private Handler mHandler = new Handler() {
        // 此方法在ui線程運行
        public void handleMessage(Message msg) {
            //what代表丟入的參數,之後switch判斷丟入的參數,case再進行動作,75行有參數
            switch (msg.what) {
                case show_msg:
                    AlertDialog.Builder Dialog = new AlertDialog.Builder(OpenFire.this); // Dialog
                    Dialog.setTitle(titleShow);
                    Dialog.setMessage(message);
                    Dialog.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog.setPositiveButton(bmsg, new DialogInterface.OnClickListener() { // 按下abort
                        // 將thread結束
                        // 隱藏progressbar
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    Dialog.show();
                    titleShow="";
                    message="";
                    bmsg="";
                    onResume();
                    break;
                case upDate:

                    pdialog.setMessage(upmsg);
                    upmsg="";
                    break;

                case ok:
                    // 傳送成功後顯示成功視窗
                    AlertDialog.Builder Dialog1 = new AlertDialog.Builder(OpenFire.this); // Dialog
                    Dialog1.setTitle("");
                    Dialog1.setMessage("傳送成功");
                    Dialog1.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog1.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    Dialog1.show();
                    break;

                case timeOut:
                    AlertDialog.Builder Dialog0 = new AlertDialog.Builder(OpenFire.this); // Dialog
                    Dialog0.setTitle("連線逾時");
                    Dialog0.setMessage("請問是否要重送?");
                    Dialog0.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog0.setNeutralButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new sendFile().execute();

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
                    Dialog = new AlertDialog.Builder(OpenFire.this);
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
            }
        }
    };

}
