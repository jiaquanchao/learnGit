package HW;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by user on 2016/8/16.
 */
public class lastWordLengh {
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        String stence = sc.nextLine();
        byte[] words = stence.getBytes();
        int i = 1;
        try {
            while ((int) words[stence.length() - i] != 32){     //空格的ASC码为32
                    i ++;
            }
        }catch (Exception e){
            System.err.println("没有空格该单词长度为: "+stence.length()+"\n或者请检查输入法设置是否为英文！");
        }
        System.out.println(i-1);
    }
}



//import java.util.Scanner;
//
//public class Main {
//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        String stence = sc.nextLine();
//        String[] split = stence.split(" ");
//        System.out.println(split[split.length-1].length());
//    }
//}