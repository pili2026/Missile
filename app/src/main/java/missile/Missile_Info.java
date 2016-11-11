package missile;

/**
 * Created by thunder on 2016/10/4.
 */

class Missile_Info {

    private String contact, img, msg, date;

    Missile_Info() {
        date = "";
        contact = "";
        img = "";
        msg = "";
    }

    void setdate(String arg) {
        date = arg;
    }

    void setcontact(String arg) {
        contact = arg;
    }

    public void setimg(String arg) {
        img = arg;
    }

    void settittle(String arg) {
        msg = arg;
    }

    String getdate() {
        return date;
    }

    String getcontact() {
        return contact;
    }

    public String getimg() {
        return img;
    }

    String getmsg() {
        return msg;
    }
}
