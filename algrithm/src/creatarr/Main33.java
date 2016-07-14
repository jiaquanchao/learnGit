package creatarr;

import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * Created by user on 2016/7/14.
 */
public class Main33 {
    public void pot_position(){
        Scanner sc = new Scanner(System.in);
//        int n;
//        if("".equals(sc.nextInt())){
//            n = 0;
//        }else {
//            n = sc.nextInt();
//        }
//        String tem_n = sc.nextLine();
//        int n = Integer.parseInt(tem_n);
        String temn = sc.nextLine();
//        System.out.println("n" + n);
        int n = Integer.parseInt(temn);
        int nnn = n;
        float[][] A = new float[n][2];
        while(nnn != 0){
            String a_line = sc.nextLine();
            String[] tem = a_line.split("\\s+");
            A[n-1][0] = Float.parseFloat(tem[0]);
            A[n-1][1] = Float.parseFloat(tem[1]);
            n --;
//            System.out.println(tem[0]);
//            System.out.println(A[0][1]);
//            System.out.println("n is :"+n);
        }

        double max_1 = 0.0;
//        int sum_1 = 0;
        double max_2 = 0.0;
//        int sum_2 = 0;
        for(int i = 0; i < n; i ++ ){
//            sum_1 += A[i][1];
//            sum_2 += A[i][2];
//            System.out.println("A:" + A[i][0] + " " + A[i][1]);
            if(A[i][0] >= max_1){
                max_1 = A[i][0];
            }
            if(A[i][1]>=max_2){
                max_2 = A[i][1];
            }
        }
//        System.out.println(max_1 +" "+max_2);
        double part, heave;
        if(nnn == 3) {
            part = max_1 * max_2 /2;
        }else if(nnn == 4){
            part = max_1 * max_2;
        }else{
            part = 0;
        }
        heave = (max_1+max_2)/2;
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println(df.format(part)+' '+df.format(heave));
    }
    public static void main(String args[]){
        Main33 M = new Main33();
        Scanner sc = new Scanner(System.in);
//        sc.useDelimiter("\t");
        System.out.println("input the num: ");
        int nn = sc.nextInt();
        for(int i=0; i<nn; i++){
            M.pot_position();
        }
    }

}
