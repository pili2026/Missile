package login.mcs;

import com.github.kevinsawicki.http.HttpRequest;

import mcs.LoginInput;
import tab.list.AttachParameter;

/**
 * Created by jeremy on 2016/5/10.
 */
public class EditAccount {
    /*
	 * 這邊是edit.java呼叫的，用來顯示使用者資訊，使用這可以編輯他的密碼手機跟SMS狀態
	 */

    private String id, requsest, phone;
    private String regReadLine = null;

    private boolean response = false;

    public EditAccount() {

    }

    // 預設的response是false
    public boolean getResponse() {
        return response;
    }

    // 預設的reReadLine是null
    public String getRegReadLine() {
        return regReadLine;
    }

    // 設定password
    public void setrequsest(String arg) {
        requsest = arg;
    }

    // 編輯password
    public void edituser() {
        try {
            response = false;

            String pathUrl = "http://" + AttachParameter.Homeip + "/wsgi/account/changepassword/";
            // 使用POST的方式建立連線
            HttpRequest request = HttpRequest.post(pathUrl);
            // 設定cookie、傳送request字串，內容為新密碼、新電話，並透過body取得response
            String body = request.header("cookie", Login.latest_cookie).send(requsest).body();

            // 如果狀態馬是200
            if (request.ok()) {
                regReadLine = body;
                response = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // 編輯通知狀態，檢查傳入的method為sms或wlan(wireless lan)
    public void editnotify(String method) {
        try {
            String req = "method=" + method;
            String pathUrl1 = "http://" + AttachParameter.Homeip + "/wsgi/account/setnotification/";
            HttpRequest request1 = HttpRequest.post(pathUrl1);
            String body = request1.header("cookie", Login.latest_cookie).send(req).body();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getnotify() {
        String body = new String();
        try {
            String pathUrl1 = "http://" + AttachParameter.Homeip + "/wsgi/account/getnotification/";
            HttpRequest request1 = HttpRequest.get(pathUrl1);
            body = request1.header("cookie", Login.latest_cookie).body();
            boolean result= AttachParameter.chechsuccess(body);
            if(result){
                String[] temp=body.split("&");
                body=temp[1].replace("method=", "");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return body;
    }
}
