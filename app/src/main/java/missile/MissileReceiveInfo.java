package missile;

/**
 * Created by thunder on 2016/10/5.
 */

public class MissileReceiveInfo {
    private String title, content, file, date, token,FileCount;

    MissileReceiveInfo() {
    }

    public void setTitle(String arg) {
        title = arg;
    }

    public void setDate(String arg) {
        date = arg;
    }

    public void seContent(String arg) {
        content = arg;
    }

    public void setoken(String arg) {
        token = arg;
    }

    public void setFile(String arg) {
        file = arg;
    }
    public void seFileCount(String arg) {
        FileCount = arg;
    }
    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return file;
    }

    public String gettoken() {
        return token;
    }
    public String getFileCount() {
        return FileCount;
    }
}
