package HW;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

import java.util.Scanner;

/**
 * Created by user on 2016/8/18.
 */
public class chros {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String input_tem = sc.nextLine();
        String[] str = input_tem.split(" ");
//        System.out.println(str[8]);
        int[] num = new int[str.length-1];
        int people_num = Integer.parseInt(str[0]);
        int a = 0;
        int b = 0;
        int j = 0;
        int specal = 0;
        for(int i=1; i<str.length; i ++){
            num[j] = Integer.parseInt(str[i]);
//            System.out.println(num[j]);
            if(num[j] > a){
                a = num[j];
                b = j;
            }
            j ++;
        }
        int delete_num = 0;
        int height = a;
        //left
        for(int i = b-1; i >= 0; i --){
            if(num[i]>=height){
                delete_num ++;
            }else {
                height = num[i];
            }
        }
        //right
        height = a;
        for(int i = b+1; i < num.length; i ++){
            if(num[i]>=height){
                delete_num ++;
            }else{
                height = num[i];
            }
        }
        System.out.print(delete_num);

    }
}
