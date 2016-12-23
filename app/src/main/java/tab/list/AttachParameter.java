package tab.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import net.sbbi.upnp.messages.UPNPResponseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import softwareinclude.ro.portforwardandroid.asyncTasks.openserver;
import softwareinclude.ro.portforwardandroid.network.UPnPPortMapper;

/**
 * Created by jeremy on 2016/4/10.
 */
public class AttachParameter {

    //For Encryption
    public static String path; // Currently opened image file path
    public static String save_key_magnified_path;  // Save path for magnified key
    public static String save_cipher_magnified_path; // Save path for magnified cipher
    public static File file; // Currently opened file
    public static File bw_file; // Black and white version of original
    public static File key_file; // Key image file
    public static File key_magnified_file; // Magnified key file
    public static File cipher_magnified_file; // Magnified cipher file
    public static String image_decrypt_file; // File to hold decrypted image

    public static int priority;
    int count, id_this;
    int[] arrDuration, arrSize;
    Bitmap[] thumbnails;
    boolean[] thumbnailsselection;
    String[] arrPath, arrName, Path;
    private Uri uri;
    public static int music = 0;
    public static int video = 1;
    public static int photo = 2;
    public static int filetype = 3;
    public static int content = 0;
    public static int reply = 1;
    public static int d2d = 2;
    public static int msgtype = 3;
    public static int port = 5000;
    public static boolean first = false;
    public static String out_ip ="0.0.0.0";
    public static String in_ip ="0.0.0.0";
    public static String latest_cookie;
    public static String connect_ip,connect_port;
    private static Boolean wifi = false;
    public static boolean nat = false;
    public static String selfid = new String();
    private static UPnPPortMapper uPnPPortMapper = new UPnPPortMapper();
    public static openserver server;
    public static String Homeip = "140.138.150.26";
    public static String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/";
    public static final int BLACK = -16777216;  // Constant to represent the RGB binary value of black. In binary - 1111111 00000000 00000000 00000000
    public static final int WHITE = -1;  // Constant to represent the RGB binary value of white. In binary - 1111111 1111111 1111111 1111111

    public void setcount(int count) {
        this.count = count;
    }
    public void setid(int id_this) {
        this.id_this = id_this;
    }

    public void setarrDuration(int[] arrDuration) {
        this.arrDuration = arrDuration;
    }

    public void setarrSize(int[] arrSize) {
        this.arrSize = arrSize;
    }

    public void setthumbnails(Bitmap[] thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setthumbnailsselection(boolean[] thumbnailsselection) {
        this.thumbnailsselection = thumbnailsselection;
    }

    public void setarrPath(String[] arrPath) {
        this.arrPath = arrPath;
    }

    public void setPath(String[] Path) {
        this.Path = Path;
    }

    public void setarrName(String[] arrName) {
        this.arrName = arrName;
    }

    public void seturi(Uri uri) {
        this.uri = uri;
    }

    // ///////////////////////////////////////////////////////////////
    public int getcount() {
        return count;
    }

    public int getid() {
        return id_this;
    }

    public int[] getarrDuration() {
        return arrDuration;
    }

    public int[] getarrSize() {
        return arrSize;
    }

    public Bitmap[] getthumbnails() {
        return thumbnails;
    }

    public boolean[] getthumbnailsselection() {
        return thumbnailsselection;
    }

    public String[] getarrPath() {
        return arrPath;
    }

    public String[] getPath() {
        return Path;
    }

    public String[] getarrName() {
        return arrName;
    }

    public Uri geturi() {
        return uri;
    }

    // /////////////////////////////////////////////////////////
    public static boolean[] checktype(String file) {
        //2016/07/04更改
        boolean[] checktype = new boolean[filetype];
        Pattern patternmusic = Pattern.compile(".*.mp3$|.*.wma|.*.m4a|.*.3ga|.*.ogg|.*.wav"); // check
        // file
        // type
        Matcher matchermusic = patternmusic.matcher(file);
        Pattern patternvideo = Pattern.compile(".*.3gp$|.*.mp4|.*.wmv|.*.movie|.*.flv");
        Matcher matchervideo = patternvideo.matcher(file);
        Pattern patternphoto = Pattern.compile(".*.jpg$|.*.bmp|.*.jpeg|.*.gif|.*.png|.*.image");
        Matcher matcherphoto = patternphoto.matcher(file);

        if (matchermusic.find()) {
            checktype[music] = true;
        } else {
            checktype[music] = false;
        }
        if (matchervideo.find()) {
            checktype[video] = true;
        } else {
            checktype[video] = false;
        }
        if (matcherphoto.find()) {
            checktype[photo] = true;
        } else {
            checktype[photo] = false;
        }
        return checktype;
    }

    // //////////////////////////////////////////////////////////////
    public static boolean chechsuccess(String respone) {

        boolean result;
        Pattern pattern = Pattern.compile("ret=0.*"); // check file type
        Matcher matcher = pattern.matcher(respone);
        if (matcher.find()) {
            result = true;
        } else {
            result = false;
        }
        return result;

    }

    public static boolean[] checkCR(String value) {
        boolean[] checktype = new boolean[msgtype];
        Pattern cpattern = Pattern.compile("&content.*"); // check file type
        Matcher cmatcher = cpattern.matcher(value);
        Pattern rpattern = Pattern.compile("&reply.*"); // check file type
        Matcher rmatcher = rpattern.matcher(value);
        Pattern ppattern = Pattern.compile("&d2d.*"); // check file type
        Matcher pmatcher = ppattern.matcher(value);
        if (cmatcher.find()) {
            checktype[content] = true;
        } else {
            checktype[content] = false;
        }
        if (rmatcher.find()) {
            checktype[reply] = true;
        } else {
            checktype[reply] = false;
        }
        if (pmatcher.find()) {
            checktype[d2d] = true;
        } else {
            checktype[d2d] = false;
        }
        return checktype;
    }

    public static String getIPAddress(Context c) {
        String in_ip="" ,out_ip="",in_out_ip="";
        in_ip=getIPV4(c);
        AttachParameter.in_ip=in_ip;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            out_ip = in.readLine();
            AttachParameter.out_ip=out_ip;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return in_out_ip= AttachParameter.getip();
        }
        in_out_ip ="in_ip="+in_ip+"&out_ip="+out_ip;
        return in_out_ip;

    }//end of getIPAddress
    public static String getIPV4(Context c) {
        WifiManager wifiMan = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        long ip = wifiInf.getIpAddress();
        if( ip != 0 )
            return String.format( "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "0.0.0.0";
    }//end of getIPV4

    public static String getip(){

        String in_out_ip = "";
        if(AttachParameter.wifi){
            try {
                String externalIP = uPnPPortMapper.findExternalIPAddress();
                String foundDeviceInternalIP = getDottedDecimalIP(getLocalIPAddress());
                //String externalIP = "140.138.150.22";
                //String foundDeviceInternalIP = "192.168.0.1";

                System.out.println("eip:"+externalIP+"  iip:"+foundDeviceInternalIP);
                if(externalIP != null && !externalIP.isEmpty()) {
                    if(externalIP.equals("No External IP Address Found")){
                        out_ip=foundDeviceInternalIP;
                        in_ip=foundDeviceInternalIP;
                    }else{
                        out_ip=externalIP;
                        in_ip=foundDeviceInternalIP;
                    }
                }
            } catch (IOException | UPNPResponseException e) {
                e.printStackTrace();
            }
        }
        in_out_ip ="in_ip="+in_ip+"&out_ip="+out_ip;
        return in_out_ip;
    }
    private static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    private static byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException | NullPointerException ex) {
            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }
}
