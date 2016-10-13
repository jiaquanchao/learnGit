package IODemo;

import java.io.*;

/**
 * Created by user on 2016/7/19.
 */
public class fileRW {
    public static void main(String args[]) throws IOException {
        File f = new File("test.txt");
        FileInputStream fis = new FileInputStream(f);
        FileOutputStream fos = new FileOutputStream("test_new.txt");
//        BufferedInputStream bis = new BufferedInputStream(fis);
//        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte b_in[] = new byte[20];
        int l;
        while ((l = fis.read(b_in)) != -1){
            fos.write(b_in, 0, l);
        };
//        char[] input = new char[10];
        String string = new String(b_in);
        System.out.println(string);

//        bos.flush();
//        bos.close();
//        bis.close();
        fos.close();
        fis.close();
    }
}
