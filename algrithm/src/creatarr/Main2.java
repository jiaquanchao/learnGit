package creatarr;
import java.lang.String;
import java.util.*;


/**
 * Created by user on 2016/7/10.
 */
public class Main2 {
    public void res(String input) {
        String flag_l = "{[(";
        String flag_r = "}])";
        char[] input_ch = input.toCharArray();
        List res_l = new ArrayList();
//        int count = 0;
        for(int i = 0; i < (input_ch.length); i ++){
            if(flag_l.indexOf(input_ch[i]) != -1){
                res_l.add(flag_l.indexOf(input_ch[i]));
//                count++;
            }else if(flag_r.indexOf(input_ch[i]) != -1){
//                count++;
//                if(res_l.size()==0){
//                    return 2;
//                }else
                if((res_l.get(res_l.size()-1)).equals(flag_r.indexOf(input_ch[i]))){
                    res_l.remove(res_l.size()-1);
                    continue;
                }else{
                    System.out.println("No");
                    return;
                }
            }
        }
//        if((res_l.size() != 0)&&(count != 0)){
//            return 2;
//        }else if(count!=0){
//            return 1;
//        }else{
//            return 0;
//        }
        if(res_l.size() != 0){
            System.out.println("No");
            return;
        }else{
            System.out.println("Yes");
            return;
        }
    }
//    public void output(String a){
//        if(res(a)==1){
//            System.out.println("Yes");
//        }else if(res(a)==2){
//            System.out.println("No");
//        }else{
//            // TODO: 2016/7/14
//        }
//    }
    public static void main(String args[]) {
        Main2 r = new Main2();
        Scanner sc = new Scanner(System.in);
        int f = Integer.parseInt(sc.nextLine());
        long startTime = System.nanoTime();
        String a;
        while(f-- > 0) {
            a = sc.nextLine();
            r.res(a);
        }
        long consumingTime = System.nanoTime() - startTime; //消耗時間
        System.out.println(consumingTime);
        System.out.println(consumingTime/1000+"微秒");
        // TODO: 2016/7/14 优化 
    }
}
