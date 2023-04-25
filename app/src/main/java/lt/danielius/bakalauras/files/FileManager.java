package lt.danielius.bakalauras.files;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private static File privateDir;

    public static void init(File privateFiles){
        privateDir = privateFiles;
    }

    public static File getFile(String path){
        return new File(privateDir, path);
    }

    public static File getCreatedFile(String str){
        File file;
        if(str == null || str.trim().equals("")){
            file = privateDir;
        }
        else {
            file = getFile(str);
        }

        if(file == null){
            return null;
        }

        if(!file.exists()){
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                return null;
            }
        }

        if(file.exists()){
            return file;
        }
        else {
            return null;
        }
    }

}
