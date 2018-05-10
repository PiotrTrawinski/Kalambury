package kalambury.game;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;

public class RandomFileReader {
    private File file;
    private String path;
    private char separator;
    private Random rand = new Random();
    public RandomFileReader(String path,char separator){
        this.file = new File(path);
        this.path = path;
        this.separator = separator;
    }
    public String chooseRandom(){
        String ret = "";
        long fileSize = file.length();
        byte wordRead[] = new byte[64];
        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            long pos = rand.nextInt((int) fileSize-1);
            f.seek(pos);
            byte read;
            if(pos != 0){
                while((read = f.readByte()) != separator){
                    f.seek(f.getFilePointer() - 2);
                    if(f.getFilePointer() == 0){
                        break;
                    }
                }
            }
            int i = 0;
            while((read = f.readByte()) != separator){
                wordRead[i++] = read;
            }
            wordRead[i] = '\0';
            ret = new String(wordRead, "UTF-8");
            ret = ret.trim();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        };

        return ret;
    }
}
