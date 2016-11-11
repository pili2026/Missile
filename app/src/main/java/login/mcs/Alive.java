package login.mcs;

import com.github.kevinsawicki.http.HttpRequest;

import java.net.URLDecoder;

import mcs.LoginActivity;
import tab.list.AttachParameter;

/**
 * Created by jeremy on 2016/5/10.
 */
public class Alive {
    /*
     * 底層一律呼叫第三方資料庫，HttpRequest。
     * 此class用來檢查目前離client最近的server，若有更近的server則會得到該server的ip
     */

    // login已擁有homeIp & cookie資訊
    String aliveIp = LoginActivity.Homeip;// 預設aliveIp是HomeServer的IP
    // 2013/3/10,4$ 修改 private String aliveRes
    private String aliveRes;

    public Alive() {

    }

    public String[] alive(String cookie, String ip, int portnumber) {
        String[] aliveres = new String[5];
        try {
            String requeststring= ip+"&port="+portnumber;

            String pathUrl = "http://" + aliveIp + "/wsgi/loc/alive/";
            // 使用get的方法
            HttpRequest request = HttpRequest.post(pathUrl);
            // 設定cookie並取回body的資訊
            String alive = request.connectTimeout(10000).header("cookie", cookie).send(requeststring).body();
            System.out.println(alive);
            boolean result = AttachParameter.chechsuccess(alive);
            if (result) { // 正確ret=0
                aliveres[1] = "true";
                // 解碼utf8的格式
                alive = URLDecoder.decode(alive, "utf-8");
                alive = URLDecoder.decode(alive, "utf-8");
                // 解析出來是ip
                aliveRes = alive.substring(alive.indexOf("ip=") + 3, alive.length() - 1);
                boolean[] checktype = new boolean[AttachParameter.msgtype];
                checktype = AttachParameter.checkCR(aliveRes);
                if(checktype[AttachParameter.content]){
                    ip=aliveRes.substring(0, aliveRes.indexOf("&content"));
                }
                else if(checktype[AttachParameter.reply]){
                    ip=aliveRes.substring(0, aliveRes.indexOf("&reply"));
                }
                else if(checktype[AttachParameter.d2d]){
                    ip=aliveRes.substring(0, aliveRes.indexOf("&d2d"));
                }
                else{
                    ip=aliveRes;
                }
                // 如果找到近的Server IP和原本的aliveIp不一樣的話會被近的取代，而且會重新登入
                if (!ip.equalsIgnoreCase(aliveIp)) {

                    aliveIp = ip;
                    // 儲存回覆的ip
                    aliveres[0] = aliveIp;
                    aliveres[3] = "false";
                    // 2013/4/2 4$修改
                    // 呼叫上層,有迴圈的問題,若是要login>alive>lgoin
                    // 取得requestString
                } else// 2013/4/2 4$修改 else代表ip不用更換，既然不用更換就不用再次登入
                {
                    aliveres[0] = aliveIp;
                    aliveres[3] = "true";
                }
                aliveres[4]=aliveRes;
            } else{
                aliveres[1] = "false";
            }
            // 不正確ret=1

        } catch (Exception ex) {
            aliveres[1] = "false";
            aliveres[2] = "false";
            ex.printStackTrace();
        }

        return aliveres;
    }
}
