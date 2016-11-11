package database.storage;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import tab.list.AttachParameter;
import tab.list.FileContentProvider.UserSchema;

public class messageProcess {
    Context c;
	private NotificationManager gNotMgr = null;

	public  void checkwlan(ContentResolver content, String aliveIp) {
		String tempdate;
		String S_ret = new String(), S_rep = new String(),S_d2d = new String();
		boolean[] checktype = new boolean[AttachParameter.msgtype];
		checktype = AttachParameter.checkCR(aliveIp);
		if (checktype[AttachParameter.content] & checktype[AttachParameter.reply]) {
			S_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&reply"));
			S_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.length() );
		}else if(checktype[AttachParameter.content] & checktype[AttachParameter.d2d]) {
			S_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&d2d"));
			S_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
		}
		else if(checktype[AttachParameter.reply] & checktype[AttachParameter.d2d]) {

			S_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.indexOf("&d2d") );
			S_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
		}
		else if(checktype[AttachParameter.content] & checktype[AttachParameter.reply] & checktype[AttachParameter.d2d]) {
			S_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&reply"));
			S_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.indexOf("&d2d") );
			S_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
		}
		else if (checktype[AttachParameter.content]) {
			S_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.length());
		} else if (checktype[AttachParameter.reply]) {
			S_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6, aliveIp.length());
		}else if (checktype[AttachParameter.d2d]) {
			S_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
		}

		if (!S_ret.equals("")) {
			String[] ret = S_ret.split("&content");
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ret[i].replace(i + "=", "");
				//Caused by: java.lang.StringIndexOutOfBoundsException: length=67; regionStart=48; regionLength=-49
				tempdate = ret[i].substring(ret[i].indexOf("d=") + 2, ret[i].lastIndexOf("."));
				ret[i] = ret[i].substring(0, ret[i].indexOf("&d="));
				insertdata(content, ret[i], tempdate);
			}
		}
		// 同上
		if (!S_rep.equals("")) {
			String[] rep = S_rep.split("&reply");
			for (int i = 0; i < rep.length; i++) {
				rep[i] = rep[i].replace(i + "=", "");
				tempdate = rep[i].substring(rep[i].indexOf("d=") + 2, rep[i].lastIndexOf("."));
				rep[i] = rep[i].substring(0, rep[i].indexOf("&d="));
				insertdata(content, rep[i], tempdate);
			}

		}
		// 同上
		if (!S_d2d.equals("")) {
			String[] d2d = S_d2d.split("&d2d");
			for (int i = 0; i < d2d.length; i++) {
				d2d[i] = d2d[i].replace(i + "=", "");
				tempdate = d2d[i].substring(d2d[i].indexOf("d=") + 2, d2d[i].lastIndexOf("."));
				d2d[i] = d2d[i].substring(0, d2d[i].indexOf("&d="));
				insertdata(content, d2d[i], tempdate);
			}

		}

	}

	public void insertdata(ContentResolver contentresolver, String data, String tempdate) {
		String message[] = data.split("&");// 簡訊body的分割
		if(message.length==1){
//				 message=data.split("\\+");
//
//				    // 檢查寄件者是否存在,不存在加入到group
//				    Cursor validate = contentresolver.query(Uri.parse("content://tab.list.file.d2d/user_validate"), null, "validate_code='" + message[2] + "'", null, null);
//				    if (validate.getCount() == 0) {
//					ContentValues values = new ContentValues();
//					values.put(UserSchema._VALIDATE_CODE, message[2]);
//					values.put(UserSchema._VALIDATE, 0);
//					values.put(UserSchema._DATE, tempdate);
//					contentresolver.insert(Uri.parse("content://tab.list.file.d2d/user_validate"), values);
//
//					values = null;
//				    }
//				    validate.close();
		}else{
			String msg = message[0].replace("m=", "");
			String messagetoken = message[1].replace("t=", "");
			String getSender = message[2].replace("u=", "");
			String subject = message[3].replace("c=", "");
			String filesize= new String();
			String filecount= new String();
			String[] Form = { UserSchema._ID, UserSchema._SENDER, UserSchema._CONTENT, UserSchema._MESSAGETOKEN, UserSchema._FILESIZE, UserSchema._DATE };
			String[] reply = { UserSchema._ID, UserSchema._SENDER, UserSchema._FILENAME, UserSchema._MESSAGETOKEN, UserSchema._DATE, UserSchema._MSG };


			// 檢查寄件者是否存在,不存在加入到group
			Cursor add_sender_cursor = contentresolver.query(Uri.parse("content://tab.list.d2d/user_group"), null, "sender='" + getSender + "'", null, null);
			if (add_sender_cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(UserSchema._SENDER, getSender);
				contentresolver.insert(Uri.parse("content://tab.list.d2d/user_group"), values);

				values = null;
			}
			add_sender_cursor.close();

			// 檢查SMS狀態是可接收(retrievable)，還是要求上傳(reply)
			if (msg.equals("retrievable")) {
				filesize = message[4].replace("s=", "");
				filecount = message[5].replace("f=", "");
				// 檢查簡訊是否已存在DB,若不存在則新增
				Cursor check_cursor = contentresolver.query(Uri.parse("content://tab.list.d2d/user_data"), Form, "messagetoken='" + messagetoken + "'", null, null);
				check_cursor.moveToFirst();
				if (check_cursor.getCount() == 0) {
					ContentValues values = new ContentValues();
					values.put(UserSchema._SENDER, getSender);
					values.put(UserSchema._MESSAGETOKEN, messagetoken);
					values.put(UserSchema._FILESIZE, filesize);
					values.put(UserSchema._TITTLE, subject);
					values.put(UserSchema._DATE, tempdate);
					values.put(UserSchema._MSG, msg);
					values.put(UserSchema._FILECOUNT, filecount);
					contentresolver.insert(Uri.parse("content://tab.list.d2d/user_data"), values);
					values = null;
				}
				check_cursor.close();
				//產生Notification物件，並設定基本屬性

			} else if (msg.equals("reply")||msg.equals("d2d")) {

				String selfid = message[4].replace("i=", "");
				// 檢查此token是否以存在自定義的user_reply 的table，若不存在則新增
				Cursor check_cursor_reply = contentresolver.query(Uri.parse("content://tab.list.d2d/user_reply"), reply, "messagetoken='" + messagetoken + "'", null, null);
				if (check_cursor_reply.getCount() == 0) {
					ContentValues values = new ContentValues();
					values.put(UserSchema._SENDER, getSender);
					values.put(UserSchema._MESSAGETOKEN, messagetoken);
					values.put(UserSchema._MSG, msg);
					values.put(UserSchema._FILENAME, subject);
					values.put(UserSchema._DATE, tempdate);
					values.put(UserSchema._SELFID, selfid);
					if(msg.equals("d2d")){
						values.put(UserSchema._D2D, "1");
					}
					contentresolver.insert(Uri.parse("content://tab.list.d2d/user_reply"), values);
					;

					values = null;
				}
				check_cursor_reply.close();
			}
		}

	}
}