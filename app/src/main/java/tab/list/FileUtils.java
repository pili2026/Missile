package tab.list;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

/**
 * Created by jeremy on 2016/4/10.
 */
public class FileUtils extends Activity {
    int count;
    String targetFileIndex = new String();

    public FileUtils() {
        count = 0;
    }

    public void setcount(int arg) {
        count = arg;
    }

    public int getcountn() {
        return count;
    }

    public String getindex() {
        return targetFileIndex;
    }

    public String getTargetFileName(String inputFileName, int split_index, int index){
        final File file = new File(inputFileName).getAbsoluteFile();
        System.out.println("file's getAbsoluteFile==" + file);
        final String fileName = file.getName();
        String path= Environment.getExternalStorageDirectory().toString() + File.separator+"KM";
        String targetFileName;
        final File dir_file = new File(path);
        if(!dir_file.exists()){
            dir_file.mkdir();
            String pp=dir_file.getParent();
        }
        targetFileIndex = "file" + index + "_" + split_index;
        System.out.println("targetFileIndex+++++" + targetFileIndex);
        targetFileName = "file_name" + index + "_" + split_index + "-" + fileName;

        return new File(dir_file.getPath(), targetFileName).getPath();
    }
}

