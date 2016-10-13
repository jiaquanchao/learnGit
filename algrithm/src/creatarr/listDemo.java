package creatarr;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2016/7/15.
 */
public class listDemo {
    public static void main(String args[]){
        String[] storage = {"battle", "TV","pen"};
        List<String> l = new ArrayList<String>();
        l.add(storage[0]);
        l.add(storage[1]);
        l.add(storage[2]);
        Iterator it = l.iterator();
        System.out.println(l);
        while (it.hasNext()){
            System.out.println(it.next());
        }
        File f = new File("F:\\极客学院VIP全套\\知识体系图\\06、Java语言\\4、Java语言进阶\\2、Java本地文件操作");
        System.out.println(f.isDirectory());
        System.out.println(f.listFiles()[1]);
    }
}
