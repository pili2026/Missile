package login.mcs;

import com.github.kevinsawicki.http.HttpRequest;

import tab.list.AttachParameter;

/**
 * Created by jeremy on 2016/5/10.
 */
public class Login {
 /*
	 * 這邊是用做loginactivity所用的class，目的為登入
	 */

    public String homeIp;
    // public String requestString = new String();
    public String cookie;
    public static String latest_cookie;
    // static String latest_name;
    // retrieve retrieve = new retrieve();
    private String loginreadLine;
    // 2014/4/2 4$ 新增 readfile_coolie,Alive
    public String login_name = new String();

    public Login() {
        homeIp = new String();
        cookie = new String();
    }

    public void set_alive_arg(String a_cookie, String a_name) {
        cookie = a_cookie;
        login_name = a_name;
    }

    public String getcookie() {

        return cookie;
    }

    public String gethomeIp() {
        return homeIp;
    }

    public String[] login(String aliveIp, String requestString) {

        homeIp = aliveIp;
        String[] loginreturn = new String[3];// 存放回傳資訊

        try {
            String pathUrl = "http://" + aliveIp + "/wsgi/account/login/";
            HttpRequest request = HttpRequest.post(pathUrl);
            // request字串放的是帳密，send出去後取回cookie，做為日後與server連線時，確保都是同一次的紀錄
            loginreadLine = request.connectTimeout(20000).send(requestString).body();
            // 選取response字串，重0開始選，到";"前面為止
            if(request.code()==java.net.HttpURLConnection.HTTP_OK){
                // request字串放的是帳密，send出去後取回cookie，做為日後與server連線時，確保都是同一次的紀錄

                boolean result = AttachParameter.chechsuccess(loginreadLine);
                if (result) {
                    String response = request.connectTimeout(10000).header("set-cookie");
                    cookie = response.substring(0, response.indexOf(";"));
                    //紀錄cookie
                    latest_cookie = cookie;
                    loginreturn[0] = "true"; // control 登入時的pialog
                    loginreturn[1] = loginreadLine; // put the response into the
                    // array[1]
                }else{
                    loginreturn[0] = "true"; // control 登入時的pialog
                    loginreturn[1] = loginreadLine; // put the response into the
                    // array[1]
                }
            }


        }// try end
        catch (HttpRequest.HttpRequestException httpex) {
            loginreturn[0] = "false";
            loginreturn[2] = "false";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loginreturn;
    }// login end


}
