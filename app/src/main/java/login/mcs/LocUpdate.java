package login.mcs;

import mcs.LoginInput;

/**
 * Created by jeremy on 2016/5/10.
 */
public class LocUpdate {
    private Alive alive = new Alive();
    private Login login = new Login();
    public LocUpdate(){

    }
    public String[] locationupdate(String cookie, String ip, int port) {
        String[] aliveinfo;
        aliveinfo = alive.alive(cookie, ip ,port);

        if(aliveinfo[1]!=null && aliveinfo[1].equalsIgnoreCase("true")){
            if (aliveinfo[3].equals("false")) {
                String requestString = LoginInput.login_name;
                aliveinfo[3] = login.login(aliveinfo[0], requestString)[0];
            }
        }

        return aliveinfo;
    }
    public String[]login(String aliveIp, String requestString){
        String[] loginreturn;
        loginreturn = login.login(aliveIp, requestString);
        return loginreturn;
    }
}
