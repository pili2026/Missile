package login.mcs;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import mcs.LoginInput;
import tab.list.AttachParameter;
import tab.list.FileContentProvider.UserSchema;

/**
 * Created by jeremy on 2016/5/11.
 */
public class Retrieve {
    /*
	 * 底層中主要核心之一，這裡是由notify所呼叫的， 這邊動作為取得檔案id、下載檔案，瀏覽檔案
	 * 檔案id指的是在server中的順序，若要下載檔案則必須告訴該檔案在server中的id為何，方能下載
	 */
    public String cookie = new String();

    public Retrieve() {
    }

    // public String aliveIp = new String();
    public String token = new String();
    public String filename = new String();
    public String savefilename = new String();
    public String fileid[];
    // public boolean retreivereqfailed = false;
    public boolean retrieveRes = false;
    public String rreqreadLine = new String();
    public String[] retrieveFileCount;
    public String viewmod = "";
    public String sdcardPath_2 = Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + "/";

    // 設定token
    public void settoken(String arg) {
        token = "token=" + arg;

    }

    public void setcookie(String arg) {
        cookie = arg;

    }
    /*
     *這邊是取得檔案id,透過傳入的token(檔案的身分證)以及目前最近的id,則會回傳跟此token相關的檔案id回去
     * 會在背後做接收到檔案的動作
      */
    public String[] retrieve_req(String token, String mod) {
        String reretrieve[] = new String[5];
        try {

            String pathUrl = "http://" + LoginInput.Homeip + "/wsgi/cms/retrieve/";
            HttpRequest request = HttpRequest.post(pathUrl);
            // 設定cookie並傳入token
            int responseCode = request.header("cookie", Login.latest_cookie).send("token=" + token + "&mod=" + mod).code();
            // 如果response回覆是OK
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // 這邊就會取得跟此token有關的主旨、內容、檔案的id及檔案名稱
                rreqreadLine = request.readTimeout(5000).body();
                //rreqreadLine輸出結果ret=0&subject=姊姊&content=20150303_160637.mp4&file0_0=94&file0_1=95&20150303_160637.mp4&
                boolean result = AttachParameter.chechsuccess(rreqreadLine);
                // 把篩選完的rreqreadLine放入reretrieve[0]
                reretrieve[1] = rreqreadLine;
                if (result){ // check ret=0

                    reretrieve[0] = "true";

					/*
					 * 如果成功 ，response如下
					 * ret=0&subject=heyhr&content=Afwfew&file0_0
					 * =127&file0_1=128
					 */
                    retrieveFileCount = rreqreadLine.split("&");
                    if (retrieveFileCount.length > 3) {
                        fileid = new String[retrieveFileCount.length - 3];
                        // 這裡抓的是物件在資料庫中的ID，例如file0_0=127、file0_1=128
                        for (int j = 0; j < fileid.length; j++) {
                            fileid[j] = retrieveFileCount[j + 3].substring(retrieveFileCount[j + 3].indexOf("=") + 1, retrieveFileCount[j + 3].length());

                        }
                    }

                } else { /*
						 * 2013/03/09 4$ 哨兵,檢查reretrive[1]有沒有matcher
						 * true表有ret=0,false為ret=1
						 */

                    reretrieve[0] = "false";
                }
            }// end if
            else{
                reretrieve[0] = "server error";
                //server發生內部錯誤
                reretrieve[2] = "true";
                reretrieve[1] =rreqreadLine = request.readTimeout(5000).body();
            }

        }// end try

        catch (HttpRequest.HttpRequestException nullpoint) // 判斷帳號密碼錯誤
        {
            reretrieve[0] = "server error";
            //
            reretrieve[3] = "true";
            // LoginNPE = false;
            nullpoint.printStackTrace();
        } catch (Exception ex) {
            reretrieve[0] = "server error";
            reretrieve[4] = "true";
            ex.printStackTrace();

        }

        return reretrieve;
    }

    public String[] retrieve_req_for_d2d(String ip, String port, String token) {
        String reretrieve[] = new String[5];
        try {

            String pathUrl = "http://" + ip+":"+port+ "/KM/?token="+token;
            HttpRequest request = HttpRequest.post(pathUrl);
            // 設定cookie並傳入token
            int responseCode = request.code();
            // 如果response回覆是OK
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // 這邊就會取得跟此token有關的主旨、內容、檔案的id及檔案名稱
                rreqreadLine = request.readTimeout(5000).body();
                boolean result = AttachParameter.chechsuccess(rreqreadLine);
                // 把篩選完的rreqreadLine放入reretrieve[0]
                if (result){ // check ret=0

                    // 把篩選完的rreqreadLine放入reretrieve[0]
                    //刪除第一個"&"以前的文字 ex.ret=0&/storage/emulated/0/KM/file_name0_0-4.mp4&
                    rreqreadLine=rreqreadLine.substring(rreqreadLine.indexOf("&")+1, rreqreadLine.length());
                    //尋找/KM/之前的字串
                    String delete=rreqreadLine.substring(0, rreqreadLine.indexOf("/KM"));
                    //刪除找到的字串 只留/KM/ 以後的字串
                    rreqreadLine=rreqreadLine.replace(delete, "");
                    reretrieve[0] = "true";
                    reretrieve[1] = rreqreadLine;
                    fileid = rreqreadLine.split("&");

                }else{
                    reretrieve[0]="false";
                    reretrieve[1] = rreqreadLine;
                }

            }// end if
            else{
                reretrieve[0]="server error";
                reretrieve[2] ="true";
            }
        }// end try
        catch (HttpRequest.HttpRequestException nullpoint) // 判斷帳號密碼錯誤
        {
            reretrieve[0] = "server error";
            //
            reretrieve[3] = "true";
            // LoginNPE = false;
            nullpoint.printStackTrace();
        } catch (Exception ex) {
            reretrieve[0] = "server error";
            reretrieve[4] = "true";
            ex.printStackTrace();

        }

        return reretrieve;
    }

    // 這邊是是真正下載檔案的metohd，要傳入的是目前的id、檔案id(用array存的)、以及要讀取array的次數(也就是int i 這個參數)
    public String[] saveBinaryFile(String token, int i) throws IOException {
        String[] refile = new String[5];
        //[0]判斷是否成功、[1]body、[2]系統錯誤、[3]例外處理-Request、[4]例外處理-未知錯誤
        String body;
        try {
            String pathUrl = "http://" + LoginInput.Homeip + "/wsgi/cms/retrieve_file/";
            HttpRequest request = HttpRequest.post(pathUrl);
			/*
			 * 這邊使用key value的方式傳值，當然也是可以照傳統的一個字串把欲傳送的資訊串再一起，但是用key value方式也不失閱讀
			 * data最後存的值可能如下 data=[token=h3b5bh6 id=127]
			 */
            Map<String, String> data = new HashMap<String, String>();
            token = token.replaceFirst("token=", "");
            data.put("token", token);
            data.put("id", fileid[i] + ";");
			/*
			 * 因為從server中取得到的檔案是zip，所以在這需要用解壓縮的方式取得zip的檔案 form(data)表示
			 * 要傳送的內容來自data這個key value。如果用的是字串的方式傳送的話，用send()
			 * stream()表示取得來自server的inputstream
			 */
            request.connectTimeout(30000).header("cookie", Login.latest_cookie).form(data);
            if(request.code()==200){
                filename = request.header("X-Sendfile");
                if(filename!=null){

                    filename =request.header("X-Sendfile");
                    String delete=filename.substring(0,filename.indexOf("_-")+2);
                    filename=filename.replace(delete, "");
                    checkencode(filename);
                    boolean[] checktype = new boolean[AttachParameter.filetype];
                    checktype = AttachParameter.checktype(filename);

                    if (checktype[AttachParameter.video]||checktype[AttachParameter.photo]) {
                        filename = token+"_"+filename.replace(".", "-_" + String.valueOf(i) + ".");
                    }
                    File output1 = new File(AttachParameter.sdcardPath+filename);
                    if(!output1.exists()){
                        request.receive(output1);

                    }
                    refile[0]="true";
                }else{
                    body=request.body();
                    refile[0]="false";
                    refile[1]=body;
                }
            }
            else{
                refile[0]="server error";
                refile[2]="true";
                File output = new File(AttachParameter.sdcardPath+"error.html");
                request.receive(output);
            }
        } catch (HttpRequest.HttpRequestException httpex) {
            refile[0]="server error";
            refile[3] = "true";
            httpex.printStackTrace();
        }catch (Exception ex) {
            refile[0]="server error";
            refile[4] = "true";

            ex.printStackTrace();

        }
        return refile;
    }
    public String[] saveBinaryFile_for_d2d(String token, int i, ContentResolver content) throws IOException {
        String[] refile = new String[5];
        String etag, content_length, update_length = null;
        Long actual_length;
        int downLength = 0;
        int record_number=100000;//每0.3M
        byte[] buffer = new byte[1024];
        int record_times=0;
        String num_record[];
        try {
            String path=fileid[i].substring(0,fileid[i].indexOf("/KM/")+4);
            String name=fileid[i].substring(fileid[i].indexOf("/KM/")+4,fileid[i].length());
            String pathUrl = "http://" + LoginInput.connect_ip+":"+ LoginInput.connect_port +path+ URLEncoder.encode(name, "utf-8").replaceAll("\\+", "%20");
            HttpRequest request = HttpRequest.get(pathUrl);
			/*
			 * 這邊使用key value的方式傳值，當然也是可以照傳統的一個字串把欲傳送的資訊串再一起，但是用key value方式也不失閱讀
			 * data最後存的值可能如下 data=[token=h3b5bh6 id=127]
			 */

			/*
			 * 因為從server中取得到的檔案是zip，所以在這需要用解壓縮的方式取得zip的檔案 form(data)表示
			 * 要傳送的內容來自data這個key value。如果用的是字串的方式傳送的話，用send()
			 * stream()表示取得來自server的inputstream
			 */
            if(request.code() == 200){
                //20160905加回學長版本
                etag = request.header(request.HEADER_ETAG);
                content_length = request.header(request.HEADER_CONTENT_LENGTH);
                //donotthing
                String response =request.body();

                refile[0]="true";
                filename =fileid[i];
                refile[1]=filename;
                String delete=filename.substring(0,filename.indexOf(String.valueOf(i)+"-")+2);
                savefilename=filename.replace(delete, "");
                filename=savefilename;
                checkencode(savefilename);
                boolean[] checktype = new boolean[AttachParameter.filetype];
                checktype = AttachParameter.checktype(savefilename);

                //20160903抓蟲版加入checktype[AttachParameter.photo](token)
                if (checktype[AttachParameter.video]||checktype[AttachParameter.photo]) {
                    savefilename = token+"_"+savefilename.replace(".", "-_" + String.valueOf(i) + ".");
                }
//                File output1 = new File(sdcardPath_2+savefilename);
                File output1 = new File(AttachParameter.sdcardPath+savefilename);
                //====20160907學長抓蟲抓到，抓到if要去掉"!"
                if(output1.exists()){
                    //===============20160905加回學長版本============//
                    actual_length = output1.length();
                    if (actual_length.equals(Long.valueOf(content_length))){
                        //代表該檔案已經完整下載，略過
                    }else{
                        //取出這一次的下載的紀錄
                        Cursor get_length_record = content.query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._LENGTH_RECORD}, "messagetoken='"+token+"'", null, null);

                        if(get_length_record.getCount()>0){
                            get_length_record.moveToFirst();
                            if(get_length_record.getString(0)!=null){
                                num_record=get_length_record.getString(0).split("&");
                                String length_record[]=num_record[i].split("=");
                                downLength= Integer.valueOf(length_record[1]);
                            }else{
                                downLength=0;
                            }

                        }
                        get_length_record.close();

                        request = HttpRequest.get(pathUrl);
                        request .header("Referer","http://" + LoginInput.connect_ip+":"+LoginInput.connect_port)
                                .header("Range", "bytes="+downLength+"-"+ String.valueOf((Integer.valueOf(content_length)-1)))
                                .header("Connection","Keep-Alive")
                                .header("If-Range",etag)
                                .header("Content-Length",content_length)
                                .header("referer",pathUrl);

                        InputStream is = new BufferedInputStream(request.buffer());

                        RandomAccessFile threadfile = new RandomAccessFile(output1, "rwd");
                        threadfile.seek(downLength);

                        int offset = 0;

                        while ((offset = is.read(buffer, 0, 1024)) != -1) {
                            threadfile.write(buffer, 0, offset);
                            downLength += offset;

                            //每一次的紀錄點
                            if((record_number*record_times)<=downLength&&downLength<(record_number*(record_times+1))){
                                Cursor update_downrecord = content.query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._ID, UserSchema._LENGTH_RECORD}, "messagetoken='" + token + "'", null, null);
                                if (update_downrecord.getCount() > 0) {
                                    update_downrecord.moveToFirst();
                                    //檢查過去是否有紀錄過，要將紀錄進行更新(例如每500、1000、1500的紀錄)
                                    if(update_downrecord.getString(1)!=null){
                                        //切割是因為可能今天可能是紀錄2筆檔案(file0_0與file0_1)的下載紀錄,所以取得其中一筆紀錄.ex. file0_0=XXX
                                        num_record=update_downrecord.getString(1).split("&");
                                        if(i==0){//代表這邊是第一份的檔案的length要更新
                                            update_length="file0_0="+record_number*record_times+"&"+num_record[1];
                                        }else if(i==1){//代表這邊是第二份的檔案的length要更新
                                            update_length=num_record[0]+"&file0_1="+record_number*record_times+"&";
                                        }

                                    }else{
                                        //這便要記錄第一筆資料
                                        update_length="file0_0="+record_number*record_times+"&";
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(UserSchema._LENGTH_RECORD, "&"+update_length);
                                    int id_this = Integer.parseInt(update_downrecord.getString(0));
                                    String where = UserSchema._ID + " = " + id_this;
                                    content.update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                                    values = null;

                                }
                                update_downrecord.close();
                                record_times=record_times+1;
                            }
                        }
                        response =request.body();
                        threadfile.close();
                        is.close();
                    }
                }else {
                    //檔案未被下載過,有可能這一塊就是音樂

                    request = HttpRequest.get(pathUrl);
                    request.header("Referer","http://" + LoginInput.connect_ip+":"+LoginInput.connect_port+"/")
                            .header("Range", "bytes="+downLength+"-"+ String.valueOf((Integer.valueOf(content_length)-1)))
                            .header("Connection","Keep-Alive")
                            .header("If-Range",etag)
                            .header("referer",pathUrl);


                    BufferedInputStream is = new BufferedInputStream(request.buffer());

                    RandomAccessFile threadfile = new RandomAccessFile(
                            output1, "rwd");
                    threadfile.seek(downLength);

                    int offset = 0;

                    //while ((offset = is.read(buffer)!=-1){
                    while ((offset = is.read(buffer, 0, 1024)) != -1) {
                        threadfile.write(buffer, 0, offset);
                        downLength += offset;
                        if(downLength>270781){
                            System.out.println("5");
                        }

                        //每一次的紀錄點
                        if((record_number*record_times)<=downLength&&downLength<(record_number*(record_times+1))){
                            Cursor update_downrecord = content.query(Uri.parse("content://tab.list.d2d/user_data"), new String[]{UserSchema._ID, UserSchema._LENGTH_RECORD}, "messagetoken='" + token + "'", null, null);
                            if (update_downrecord.getCount() > 0) {
                                update_downrecord.moveToFirst();
                                //檢查過去是否有紀錄過，要將紀錄進行更新(例如每500、1000、1500的紀錄)
                                if(update_downrecord.getString(1)!=null){
                                    //切割是因為可能今天可能是紀錄2筆檔案(file0_0與file0_1)的下載紀錄,所以取得其中一筆紀錄.ex. file0_0=XXX
                                    num_record=update_downrecord.getString(1).split("&");
                                    if(i==0){//代表這邊是第一份的檔案的length要更新
                                        update_length="file0_0="+record_number*record_times+"&"+num_record[1];
                                    }else if(i==1){//代表這邊是第二份的檔案的length要更新
                                        update_length=num_record[0]+"&file0_1="+record_number*record_times+"&";
                                    }

                                }else{
                                    //這便要記錄第一筆資料
                                    update_length="file0_0="+record_number*record_times+"&";
                                }

                                ContentValues values = new ContentValues();
                                values.put(UserSchema._LENGTH_RECORD, update_length);
                                int id_this = Integer.parseInt(update_downrecord.getString(0));
                                String where = UserSchema._ID + " = " + id_this;
                                content.update(Uri.parse("content://tab.list.d2d/user_data"), values, where, null);
                                values = null;

                            }
                            update_downrecord.close();
                            record_times=record_times+1;
                        }
                    }
                    response =request.body();
                    threadfile.close();
                    is.close();
                    //===============20160905加回學長版本============//
                }
            }
            else if (request.code()==404){
                refile[0]="false";
                refile[1]=request.body();
            }else{
                refile[0]="server error";
                refile[2]=request.body();
                File output = new File(AttachParameter.sdcardPath+"error.html");
                request.receive(output);
            }
        }catch (HttpRequest.HttpRequestException httpex) {
            refile[0]="server error";
            refile[3] = "true";
            httpex.printStackTrace();
        }catch (Exception ex) {
            refile[0]="server error";
            refile[4] = "true";

            ex.printStackTrace();

        }
        return refile;
    }

    public void checkencode(String datastr){
        try {
            if(datastr.equals(new String(datastr.getBytes("iso8859-1"), "iso8859-1"))){
                filename=new String(datastr.getBytes("iso8859-1"),"utf-8");
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public String check_file(String localtoken) {
        String reretrieve = new String();
        token = localtoken;
        try {
            String pathUrl = "http://" + LoginInput.Homeip + "/wsgi/cms/check_file/?token="+token;

            HttpRequest request = HttpRequest.get(pathUrl);
            // 設定cookie並傳入token
            int responseCode = request.header("cookie", Login.latest_cookie).code();
            // 如果response回覆是OK
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // 這邊就會取得跟此token有關的主旨、內容、檔案的id及檔案名稱
                reretrieve = request.body();

            }// end if
            else {
                reretrieve = request.body();
            }

        }// end try
        catch (NullPointerException nullpoint) // 判斷帳號密碼錯誤
        {
            nullpoint.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("retrieve_req failed");
        }

        return reretrieve;
    }
    public String check_file_for_m2m(String address, String token) {
        String reretrieve = new String();
        try {
            String pathUrl = "http://" + address + "/KM/?token="+token;

            HttpRequest request = HttpRequest.get(pathUrl);
            // 設定cookie並傳入token
            int responseCode = request.code();
            // 如果response回覆是OK
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // 這邊就會取得跟此token有關的主旨、內容、檔案的id及檔案名稱
                reretrieve = request.body();

            }// end if
            else {
                reretrieve = request.body();
            }

        }// end try
        catch (NullPointerException nullpoint) // 判斷帳號密碼錯誤
        {
            nullpoint.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("retrieve_req failed");
        }

        return reretrieve;
    }

}
