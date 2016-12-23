package login.mcs;

import com.github.kevinsawicki.http.HttpRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mcs.LoginInput;
import tab.list.AttachParameter;


/**
 * Created by jeremy on 2016/5/10.
 */
public class RegFromServer {
    /*
     * 這邊是register.java所呼叫的，這會將使用者所填好的註冊資料送至server建立 建立完後需要透過email的方式驗證才能正式啟用帳號
     */

    public RegFromServer() {
    }

    public String regist(String requestString) {

        String regreturn = null;
        try {

            String pathUrl = "http://" +  AttachParameter.Homeip + "/wsgi/account/register/";
            HttpRequest request = HttpRequest.post(pathUrl);
            String response = request.connectTimeout(10000).send(requestString).body();

            if (request.ok()) {
                regreturn = response;
                // 設定正規標示法

            }else{
                regreturn="ret=1";
            }
        } catch (Exception ex) {

            ex.printStackTrace();
            regreturn="ret=1";

        }
        // regreturn[0]verifycode [1]有找到[2]沒找到[3][4]發生例外
        return regreturn;
    }

    // 這里是用來送email的方法
    public String[] sendmail(String one) { // 對傳入的ONE字串進行切割，依照&來做切割並放入矩陣中
        final String[] validate = one.split("&");
        final String[] conreturn = new String[4];

        int Conntimeout = 15000;
        try {
            // 設定mail主旨
            String subject = "welcome to kissmedia";
            // 設定mail內容
            String message = "http://"+ AttachParameter.Homeip+"/wsgi/account/validate/?" + validate[1];
            // 設定mail的收件者
            String to_email = validate[3];
            String requestString = "subject=" + subject + "&message=" + message + "&to_email=" + to_email;
            String pathUrl = "http://"+ AttachParameter.Homeip+":8000/cms/send_email/";
            HttpRequest request = HttpRequest.post(pathUrl);
            String response = request.send(requestString).body();
            if (request.ok()) {
                Pattern pattern = Pattern.compile("ret=0.*"); // check file type
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {// 如果有找到ret=0
                    conreturn[0] = "true";

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return conreturn;
    }

    public String validate(String requestString) {

        String regreturn = null;
        try {

            String pathUrl = "http://" +  AttachParameter.Homeip + "/wsgi/account/validate/";
            String req="validate="+requestString+"&";
            HttpRequest request = HttpRequest.get(pathUrl);
            String response = request.connectTimeout(10000).send(req).body();

            if (request.ok()) {
                regreturn = response;
                // 設定正規標示法

            }else{
                regreturn="ret=1";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // regreturn[0]verifycode [1]有找到[2]沒找到[3][4]發生例外
        return regreturn;
    }

}
