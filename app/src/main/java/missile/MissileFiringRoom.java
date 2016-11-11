package missile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import login.mcs.Login;
import login.mcs.Submit;
import login.mcs.User;
import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.UserSchema;
import tab.list.FileTab_km;
import tab.list.FileUtils;

import static tab.list.AttachParameter.sdcardPath;

/**
 * Created by Jeremy on 2016/10/6.
 */

public class MissileFiringRoom extends Fragment{

    static final int BLACK = -16777216;  // Constant to represent the RGB binary value of black. In binary - 1111111 00000000 00000000 00000000
    static final int WHITE = -1;  // Constant to represent the RGB binary value of white. In binary - 1111111 1111111 1111111 1111111
    Bitmap magnified_key_image_2,keyImage,chiperImage,fileBitmap,black_white,magnified_key_image;
    String receiver,  attachment = null,  state;
    String title = "Command", content = "Missile Fire";
    String filetype[];
    EditText etR,chk_Password,input_receiver;
    public int id = 0;
    public String check_pass;
    public String sms = null;
    TextView tvName, etT, etC;
    ImageView previewImg;
    int file_amount, split_seq = 0;
    private final int closedialog = 0;
    private final int timeout = 1;
    private final int ok = 2;
    private final int error = 3;
    private final int password=4;//201609

    private final int SHOW_MSG = 5;
    private final int UPDATE = 6;
    public String tittle,message,bmsg,upmsg;

    int index, listId, urgent = 0;
    String file_name = "", postFile;
    private String thumbnails = "mnt/sdcard/DCIM/.thumbnails/";
    ArrayList<String> file_path;
    int duration, file_size;
    ProgressDialog sendDialog = null;
    String selfId;
    public int mailcount = 0;
    Submit submit;
    ProgressDialog dialog = null;
    public String token ,fileName;
    String[] form = { UserSchema._FILEPATH, UserSchema._DURATION, UserSchema._FILESIZE, UserSchema._FILENAME, UserSchema._ID };
    boolean checkFileType = false ;
    Button delete;
    private ListView listView;
    private SimpleAdapter simpleAdapter;

    List<missileInfo> getInfo = new ArrayList<missileInfo>();
    List<Missile_Info> getMissileInfo;
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    public MissileFiringRoom() {
        FileContentProvider test = new FileContentProvider();
        test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FileContentProvider test = new FileContentProvider();
        test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.blist, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("飛彈發射室");

        listView = (ListView) view.findViewById(R.id.Missile_modle);

        if(AttachParameter.priority==0){
            list.clear();
            //==================總統線==============================
            simpleAdapter = new SimpleAdapter(getActivity(), getStaticData(),
                    R.layout.missilefire, new String[]{"Im_att_missile", "Im_missile_num"},
                    new int[] {R.id.Im_att_missile, R.id.Im_missile_num}){
                @Override
                public View getView (int position, View convertView, ViewGroup parent){

                    View v = super.getView(position, convertView, parent);
                    final int pos=position;
                    //id=pos;
                    Button b_att=(Button)v.findViewById(R.id.Bn_att);
                    Button b_pub=(Button)v.findViewById(R.id.Bn_publish);

                    ImageView I_prevew=(ImageView)v.findViewById(R.id.Im_att_missile);
                    b_att.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor ch_tmepfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
                            int i =ch_tmepfile.getCount();
                            ch_tmepfile.close();
                            if (i > 0) {
                                // Toast.makeText(getActivity(),"抱歉，一次只能發送一顆飛彈",Toast.LENGTH_SHORT).show();
                                FileContentProvider test = new FileContentProvider();
                                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));

                                String[] Form = { UserSchema._ID };
                                Cursor change_path = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_group"), Form, "filepath is not null", null, null);
                                if (change_path.getCount() > 0) {
                                    change_path.moveToFirst();
                                    for(int ia =0;ia<change_path.getCount();ia++){
                                        int id_this = 0;
                                        id_this = Integer.valueOf(change_path.getString(0));
                                        ContentValues values = new ContentValues();
                                        values.put(UserSchema._FILEPATH, "");
                                        String where = UserSchema._ID + " = " + id_this;
                                        getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/missile_group"), values, where, null);
                                        change_path.moveToNext();
                                    }

                                }
                                change_path.close();
                            }
                            Intent intent = new Intent();
                            id = pos;
                            Toast.makeText(getActivity(),"這裡是第"+pos+"按鈕",Toast.LENGTH_SHORT).show();
                            intent.setClass(getActivity(), FileTab_km.class);
                            // startActivityForResult(intent,0);
                            startActivity(intent);
                        }
                    });

                    b_pub.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            id=pos;
                            Cursor change_path = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_group"), null, "tittle='S00"+ (pos+1) + "'and (filepath is null or filepath ='')", null, null);
                            int i=change_path.getCount();
                            change_path.close();
                            if(i>0){

                                Toast.makeText(getActivity(),"請先選擇傳遞的內容",Toast.LENGTH_SHORT).show();
                            }else{
                                LayoutInflater factory = LayoutInflater.from(getActivity());
                                final View v1 = factory.inflate(R.layout.check_password, null);

                                chk_Password = (EditText) v1.findViewById(R.id.chk_Password);
                                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                                dialog.setTitle("身分確認");
                                dialog.setView(v1);
                                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            check_pass=chk_Password.getText().toString();
                                            new check_password().execute();
                                        } catch (Exception e) {
                                            Toast.makeText(getActivity(), "錯誤!", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getActivity(),"!!!!",Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                    I_prevew.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String path=new String();
                            String[] Form = { UserSchema._FILEPATH };
                            Cursor change_path = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_group"), Form, "tittle='S00"+ (pos+1) + "'and filepath is NOT null and filepath !=''", null, null);
                            if (change_path.getCount() > 0) {
                                change_path.moveToFirst();
                                path=change_path.getString(0);
                                viewfile(path);
                            }else{
                                //donoting
                            }
                            change_path.close();

                            Toast.makeText(getActivity(),"這裡是第"+pos+"按鈕",Toast.LENGTH_SHORT).show();
                        }
                    });
                    return v;
                }
            };
        }else{
            list.clear();
            //==============總司令與小兵======================
            simpleAdapter = new SimpleAdapter(getActivity(), getInboxData(),
                    R.layout.frieready_other, new String[]{"img","title","date","info", "Im_missile_num"},
                    new int[] {R.id.img,R.id.title,R.id.date,R.id.info,R.id.Im_missile_num}){
                @Override
                public View getView (int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);

                    Button btnDownload = (Button)v.findViewById(R.id.Bn_download);


                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int id = (int) listId;
                            // contact的三項訊息:contact msg date
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();

                            bundle.putString("contact", getMissileInfo.get(id).getcontact());
                            intent.putExtras(bundle);

                            intent.setClass(getActivity(), OpenFire.class);
                            startActivity(intent);

                        }
                    });

                    return v;
                }

            };


        }
        //201609 END

        listView.setAdapter(simpleAdapter);
        submit = new Submit();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private List<Map<String, Object>> getData() {

        getInfo.clear();
        String[] Form = { UserSchema._TITTLE, UserSchema._CONTENT, UserSchema._FILEPATH, UserSchema._DATE, UserSchema._MESSAGETOKEN, UserSchema._FILECOUNT };
        missileInfo tempinfo = new missileInfo();
        // 使用寄件者去撈出 過去中已經讀過的簡訊以及未被刪除的資料,用 tittle跟userstatus去檢查
        Cursor msg_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), Form, "msg='recevice'", null, null);
        if (msg_cursor.getCount() > 0) {
            msg_cursor.moveToFirst();
            for (int i = 0; i < msg_cursor.getCount(); i++) {
                tempinfo.setTitle(msg_cursor.getString(0));
                tempinfo.setContent(msg_cursor.getString(1));
                tempinfo.setFilepateh(msg_cursor.getString(2));
                tempinfo.setDate(msg_cursor.getString(3));
                tempinfo.setToken(msg_cursor.getString(4));

                getInfo.add(tempinfo);
                mailcount++;
                tempinfo = new missileInfo();
                msg_cursor.moveToNext();
            }
        }
        msg_cursor.close();

        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = mailcount - 1; i >= 0; i--) {
            map = new HashMap<String, Object>();
            // 這邊DB是檢查過去是否有未載完的檔案，如果有未載完的檔案，則用驚嘆號的圖案通知使用者
            // put info array into list
            map.put("title", getInfo.get(i).getTitle());
            map.put("info", getInfo.get(i).getContent());
            map.put("date", getInfo.get(i).getDate());
            // 檢查檔案類型，放置相對應的檔案類型的圖片
            if(getInfo.get(i).getFilepateh()!=null || !getInfo.get(i).getFilepateh().equals("")){
                boolean[] checktype = new boolean[AttachParameter.filetype];
                checktype = AttachParameter.checktype(getInfo.get(i).getFilepateh());
                Pattern patternmovie = Pattern.compile(".*.movie");
                Matcher matchermovie = patternmovie.matcher(getInfo.get(i).getFilepateh());

                if (checktype[AttachParameter.music]) // music set a note
                {
                    map.put("img", R.drawable.notes);
                } else if (checktype[AttachParameter.video]) // video set a camera
                {

                    String videoSegment[] = getInfo.get(i).getFilepateh().split("&");
                    Bitmap filebitmap;
                    //因為d2d是單一檔案 沒有分割 所以這邊要檢查
                    if(videoSegment.length==1){
                        filebitmap = android.media.ThumbnailUtils.createVideoThumbnail(sdcardPath + getInfo.get(i).getFilepateh(), MediaStore.Images.Thumbnails.MICRO_KIND);
                    }else{
                        filebitmap = android.media.ThumbnailUtils.createVideoThumbnail(sdcardPath + videoSegment[1], MediaStore.Images.Thumbnails.MICRO_KIND);
                    }

                    filebitmap = ThumbnailUtils.extractThumbnail(filebitmap, 60, 60);
                    File file = new File(thumbnails, "temp_" + i + ".jpg");
                    try {
                        OutputStream outStream = new FileOutputStream(file);
                        filebitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

                        outStream.flush();
                        outStream.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        map.put("img", R.drawable.missile0);

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        map.put("img", R.drawable.missile0);
                    }catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        map.put("img", R.drawable.missile0);
                    }
                    map.put("img", thumbnails + "temp_" + i + ".jpg");

                } else if (checktype[AttachParameter.photo]) // photo set photo
                {
                    map.put("img", sdcardPath + getInfo.get(i).getFilepateh().replace("&", ""));

                } else if (matchermovie.find()) {
                    map.put("img", R.drawable.iconvideo);

                } else
                    map.put("img", R.drawable.missile0);

            }
            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getStaticData() {
        getInfo.clear();
        missileInfo tempinfo = new missileInfo();


        // 因為是總統，所以直接撈預先設定的資料
        String[] missile = { FileContentProvider.UserSchema._ID, FileContentProvider.UserSchema._TITTLE, FileContentProvider.UserSchema._CONTENT, FileContentProvider.UserSchema._FILEPATH};
        Cursor missile_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_group"), missile, "state = 'live'", null, null);
        if (missile_cursor.getCount() > 0) {
            missile_cursor.moveToFirst();
            for (int x = 0; x < missile_cursor.getCount(); x++) {
                // 將簡訊存入自定義的物件(tempinfo)
                tempinfo.setTitle(missile_cursor.getString(1));
                tempinfo.setContent(missile_cursor.getString(2));
                tempinfo.setFilepateh(missile_cursor.getString(3));
                // 將自訂物件存入至getinfo
                getInfo.add(tempinfo);
                tempinfo = new missileInfo();
                missile_cursor.moveToNext();
            }

        }
        missile_cursor.close();
	/*
	 * 如同上述的作法，但是這邊才是真正要做 key value的事情，value就是來自於上述的getinfo
	 * 例如簡訊A的notititle為是什麼?從getinfo中拿出來 簡訊A的notiinfo是什麼?一樣也是從getinfo中拿出來
	 */
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < getInfo.size(); i++) { // put info array into
            // list

            map = new HashMap<String, Object>();
            // 將每筆簡訊的資訊存入到自定義的adapter的資料欄位
            if(getInfo.get(i).getFilepateh()!=null){
                map.put("Im_att_missile", getInfo.get(i).getFilepateh());
            }
            if((getInfo.get(i).getTitle()).equals("S001")){
                map.put("Im_missile_num", R.drawable.missile01);
            }
            else if(getInfo.get(i).getTitle().equals("S002")){
                map.put("Im_missile_num", R.drawable.newmissile);
            }
            else if(getInfo.get(i).getTitle().equals("S003")){
                map.put("Im_missile_num", R.drawable.newmissile2);
            }
            if(getInfo.get(i).getTitle().equals("S004")){
                map.put("Im_missile_num", R.drawable.newmissile3);
            }

            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getInboxData() {

        int count = 0;
        Missile_Info tempInboxInfo;
        getMissileInfo = new ArrayList<Missile_Info>();
        String[] form = { FileContentProvider.UserSchema._SENDER };
        String[] form1 = { FileContentProvider.UserSchema._TITTLE, FileContentProvider.UserSchema._DATE, FileContentProvider.UserSchema._SENDER };

        // 檢查是否有寄件人
        Cursor sender_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/user_group"), form, null, null, null);
        sender_cursor.moveToFirst();
        // 至少有一位寄件人
        if (sender_cursor.getCount() > 0) {
            for (int sender = 0; sender < sender_cursor.getCount(); sender++) {
                Cursor sender_data_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/user_data"), form1, "sender='" + sender_cursor.getString(0) + "' and tittle!='null' and userstatus !='delete'", null, null);
                // 寄件人是否至少有一筆是有讀過的訊息,一樣檢查tittle,
                // 撈出過去已經讀過的簡訊中,顯示最後一封看過的資料,所以用moveToLast
                if (sender_data_cursor.getCount() > 0) {
                    sender_data_cursor.moveToLast();
                    tempInboxInfo = new Missile_Info();
                    tempInboxInfo.settittle(sender_data_cursor.getString(0));
                    tempInboxInfo.setdate(sender_data_cursor.getString(1));
                    tempInboxInfo.setcontact(sender_data_cursor.getString(2));
                    getMissileInfo.add(tempInboxInfo);
                    count++;
                    tempInboxInfo = new Missile_Info();
                }// if sender_data end
                sender_data_cursor.close();
                sender_data_cursor = null;
                sender_cursor.moveToNext();
            }// sender loop end
        }//if sender_cursor end
        sender_cursor.close();

        Map<String, Object> map = new HashMap<String, Object>();
        if (getMissileInfo.size() != 0) {
            for (int i = 0; i <= count - 1; i++) {
                map = new HashMap<String, Object>();
                map.put("contacttitle", getMissileInfo.get(i).getcontact());
                map.put("contactinfo", getMissileInfo.get(i).getmsg());

                map.put("contactimg", R.drawable.head);
                map.put("contactdate", getMissileInfo.get(i).getdate());
                list.add(map);
            }// for end
        }//if end
        return list;
    }

    // 當從attachment回來時，顯示剛剛所選擇的檔案在writepage上。
    public void onResume() {
        super.onResume();
        mailcount = 0;
        File file;
        if(AttachParameter.priority==0){
            Cursor ch_tmepfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
            if (ch_tmepfile.getCount() > 0) {
                ch_tmepfile.moveToFirst();
                attachment = ch_tmepfile.getString(0);
                file = new File(attachment);

                String[] Form = { UserSchema._ID };
                Cursor change_path = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/missile_group"), Form, "tittle='S00" + (id+1) + "'", null, null);
                if (change_path.getCount() > 0) {
                    change_path.moveToFirst();
                    int id_this = 0;
                    id_this = Integer.valueOf(change_path.getString(0));
                    ContentValues values = new ContentValues();
                    values.put(UserSchema._FILEPATH, attachment);
                    String where = UserSchema._ID + " = " + id_this;
                    getActivity().getContentResolver().update(Uri.parse("content://tab.list.d2d/missile_group"), values, where, null);

                }
                change_path.close();
            }
            ch_tmepfile.close();

        }

        list.clear();
        if(AttachParameter.priority==0){
            list = getStaticData();
        }else{
            list = getData();
        }

        simpleAdapter.notifyDataSetChanged();

    }

    public void viewfile(String path) {
        boolean[] checktype = new boolean[AttachParameter.filetype];
        checktype = AttachParameter.checktype(path);
        if (checktype[AttachParameter.music]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(path);
            it.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(it);
        } else if (checktype[AttachParameter.video]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(path);
            it.setDataAndType(Uri.fromFile(file), "video/*");
            startActivity(it);
        } else if (checktype[AttachParameter.photo]) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            File file = new File(path);
            it.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(it);
        } else
            Toast.makeText(getActivity(), "not match file", 4000).show();
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
            selfId = randomString(20);
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
                    values.put(UserSchema._SELFID, selfId);
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

    public String randomString(int len) {
        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int idx = (int)(Math.random() * str.length());
            sb.append(str.charAt(idx));
        }
        return sb.toString();
    }

    private class check_password extends AsyncTask<Void, Void, String> { // implement
        // thread
        public String where;
        public String path;

        // Asyntask 前置作業
        @Override
        protected void onPreExecute() {

            mHandler.obtainMessage(password).sendToTarget();

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
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View v1 = factory.inflate(R.layout.input_reciever, null);
                input_receiver = (EditText) v1.findViewById(R.id.input_receiver);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("輸入接收者");
                dialog.setView(v1);
                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            receiver=input_receiver.getText().toString();
                            if (receiver.equals("")) {
                                Toast.makeText(getActivity(), "接收者不可為空白", Toast.LENGTH_LONG).show();
                            }else{
                                title="S00"+(id+1);
                                content="準備一級戰備";
                                fileUploadToSend("write");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "錯誤!", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity(),"密碼錯誤，請從新輸入",Toast.LENGTH_SHORT).show();
            }


        }
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

                /// ex : storage/emulated/0/DCIM/100ANDRO/MOV_0259.mp4
                File file = new File(attachment);
                String filename = file.getName();
                filetype = filename.split("\\.");
                Cursor check_finish_cursor = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), new String[] { UserSchema._FILEPATH }, "filecheck='0' and filerecord='file0_0' and selfid='" + selfId + "'", null, null);
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
                        "&selfid="+selfId+"&receiver=" + receiver +
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
                    Cursor change_token = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfId + "'", null, null);
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
                    Cursor change_first = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_content"), Form, "selfid='" + selfId + "'", null, null);
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

                    Cursor up_tempfile = getActivity().getContentResolver().query(Uri.parse("content://tab.list.d2d/temp_file"), Form, "selfid='" + selfId + "' and filecheck='0'", null, null);
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
            sendDialog.dismiss();
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
//                previewImg.setVisibility(View.INVISIBLE);
//                tvName.setVisibility(View.INVISIBLE);
//                delete.setVisibility(View.INVISIBLE);
                mHandler.obtainMessage(ok).sendToTarget();

            }

        }
    }



    private Handler mHandler = new Handler() {

        // 此方法在ui線程運行
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case closedialog:
                    sendDialog = ProgressDialog.show(getActivity(), "請稍候", "資料處理中", true);
                    sendDialog.show();
                    break;
                case timeout:
                    AlertDialog.Builder Dialog0 = new AlertDialog.Builder(getActivity()); // Dialog
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

                        }
                    });
                    Dialog1.show();
                    break;
                case password:
                    sendDialog = ProgressDialog.show(getActivity(), "請稍候", "密碼確認中", true);
                    sendDialog.show();
                    break;

                case SHOW_MSG:
                    AlertDialog.Builder Dialog2 = new AlertDialog.Builder(getActivity()); // Dialog
                    Dialog2.setTitle(tittle);
                    Dialog2.setMessage(message);
                    Dialog2.setIcon(android.R.drawable.ic_dialog_info);
                    Dialog2.setPositiveButton(bmsg, new DialogInterface.OnClickListener() { // 按下abort
                        // 將thread結束
                        // 隱藏progressbar
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    Dialog2.show();
                    tittle="";
                    message="";
                    bmsg="";
                    onResume();
                    break;


                case UPDATE:
                    dialog.setMessage(upmsg);
                    upmsg = "";
            }
        }
    };

}
