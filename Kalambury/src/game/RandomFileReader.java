package game;

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
        char wordRead[] = new char[64];
        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            long pos = rand.nextInt((int) fileSize-1);
            f.seek(pos);
            char read;
            if(pos != 0){
                while((read = (char)f.readByte()) != separator){
                    f.seek(f.getFilePointer() - 2);
                    if(f.getFilePointer() == 0){
                        break;
                    }
                }
            }
            int i = 0;
            while((read = (char)f.readByte()) != separator){
                wordRead[i++] = read;
            }
            wordRead[i] = '\0';
            ret = String.valueOf(wordRead);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        };

        return ret;
    }
}
