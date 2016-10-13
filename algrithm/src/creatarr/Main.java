package creatarr;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by user on 2016/7/16.
 */
class toASC{
    public static char[] toASC(String s){
        char[] c = s.toCharArray();
        Arrays.sort(c);
//        System.out.println(c[1]);
        return c;
    }
        }
public class Main {
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        int n = Integer.parseInt(sc.nextLine());
        while(n-- > 0) {
            String tem = sc.nextLine();
            char c[] = toASC.toASC(tem);
            for(char i : c){
                System.out.print(i+" ");
            }
            System.out.println();
        }
    }
}
