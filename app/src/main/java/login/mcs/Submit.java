package login.mcs;

import android.os.Environment;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import mcs.LoginInput;
import tab.list.AttachParameter;


/**
 * Created by jeremy on 2016/5/11.
 */
public class Submit {
    private String aliveIp;
    private String filename = new String();
    private String attachfile;
    public String token = new String();
    private String loginreadLine, submitreadLine;
    private boolean response;
    private static String requestString;
    public CharSequence[] path;
    public boolean LoginException;
    public int  code;
    public String source;
    public Submit() {
        requestString = new String();
        loginreadLine = new String();
        submitreadLine = new String();
        response = false;
        LoginException = false;
        aliveIp =  AttachParameter.Homeip;
    }

    public String getFilename() {
        return filename;
    }

    public void setrequestString(String arg) {
        requestString = arg;
    }

    // 上傳之前先做alive的動作，先判斷目前最近的server在哪
    public String submit1(String arg_cookie) {
        HttpRequest request = null;
        try {

            String pathUrl = "http://" + AttachParameter.Homeip + "/wsgi/cms/submit/";
            request = HttpRequest.post(pathUrl);

            //20160905學長推想更改
            //token=request.header("cookie", Login.latest_cookie).send(requestString).body();

            token=request.header("cookie", Login.latest_cookie).send(requestString).body();
            // 這邊requestString是subject、content、receiver、filecnt、length、file_name(至少一個)
        }catch (Exception ex) {
            ex.printStackTrace();
            token="timeout";
            File output = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "KM/aa.html");
            //token=request.body();
            request.receive(output);
        }
        return token;

    }


}
