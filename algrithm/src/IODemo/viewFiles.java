
package IODemo;

import java.io.File;

/**
 * Created by user on 2016/7/19.
 */
public class viewFiles {
    public static void main(String args[]){
        File f = new File("F:\\极客学院VIP全套\\知识体系图\\06、Java语言\\4、Java语言进阶");
        viewFiles vF = new viewFiles();
        vF.printFiles(f);
    }

    public void printFiles(File dir){
        if(dir.isDirectory()){
            File next[] = dir.listFiles();
            for(File name : next){
                if(name.isFile()){
                    System.out.println(name.toString());
                }else{
                    printFiles(name);
                }
            }
        }
    }
}

