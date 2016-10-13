package IODemo;

import java.io.*;

/**
 * Created by user on 2016/7/17.
 */
public class ioDemo {
    public static void main(String args[]) throws IOException {
        File f = new File("test.txt");
        if(f.exists()){
            System.out.println("This is a file: "+f.isFile());
            System.out.println("This is a dir: "+f.isDirectory());
        }else{
            try{
                f.createNewFile();
                System.out.println("Create file successfully.");
            }catch (Exception e){
                System.out.println("Create file failed.");
                System.out.println(e);
            }
        }
        FileInputStream fis = new FileInputStream("test.txt");
        FileOutputStream fos = new FileOutputStream("test.txt");
        fos.write('1');
        System.out.print(fis.read());

    }
}
