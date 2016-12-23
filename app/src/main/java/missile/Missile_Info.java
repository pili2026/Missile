package missile;

/**
 * Created by thunder on 2016/10/4.
 */

class Missile_Info{
    private String title,content, token, date,filepateh;

    Missile_Info() {
        content = "";
        title = "";
        token = "";
        date = "";
        filepateh= "";
    }

    void setDate(String arg) {
        date = arg;
    }
    void setContent(String arg) {
        content = arg;
    }

    void setToken(String arg) {
        token = arg;
    }
    void setTitle(String arg) {
        title = arg;
    }
    void setFilepateh(String arg) {
        filepateh = arg;
    }
    String getDate() {
        return date;
    }
    String getContent() {
        return title;
    }

    String getToken() {
        return token;
    }

    String getTitle() {
        return title;
    }
    String getFilepateh() {
        return filepateh;
    }
}
