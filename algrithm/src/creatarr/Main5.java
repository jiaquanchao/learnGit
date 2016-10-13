package creatarr;

import java.util.Scanner;

/**
 * Created by user on 2016/7/14.
 */
public class Main5 {
    public int times(String A, String B){
//        char[] A_arr = A.toCharArray();
//        char[] B_arr = B.toCharArray();
//        System.out.println(A_arr);
        int l = B.length() - A.length();
        StringBuffer part_B = new StringBuffer();
        int count = 0;
        int val = 0;
        while(val++ < l){
            part_B.append(B.substring(val, val+A.length()));
//            System.out.println(part_B);
//            System.out.println(A);
            if(A.equals(part_B.toString())){
                count ++;
            }
            part_B.delete(0, A.length());
        }
        part_B.append(B.substring(l));
        if(A.equals(part_B.toString())){
            count++;
        }
//        for(int i = 0; i < l; i++){
//            part_B.delete(0, A.length());
//            for(int j=0; j<A.length(); j++){
//                part_B.append(B.charAt(i+j));
//            }
////            String v_B = part_B.toString();
////            System.out.println(part_B+" "+part_B.toString());
//            if(A.equals(part_B.toString())){
//                count ++;
//            }
//        }
        return count;
    }
    public static void main(String args[]) {
        Main5 m = new Main5();
        Scanner sc = new Scanner(System.in);
        int n = Integer.parseInt(sc.nextLine());
        long startTime = System.nanoTime();  //開始時間
        while (n-- > 0) {
            String origin_A = sc.nextLine();
//            System.out.println("origin_A: "+origin_A);
            String origin_B = sc.nextLine();
//            System.out.println("origin_B: "+origin_B);
            System.out.println(m.times(origin_A, origin_B));
        }
        long consumingTime = System.nanoTime() - startTime; //消耗時間
        System.out.println(consumingTime);
        System.out.println(consumingTime/1000+"微秒");
    }
}
