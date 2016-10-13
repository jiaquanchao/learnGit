import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by user on 2016/8/21.
 */
public class findSeven {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int num = sc.nextInt();
        int count = 0;
        double x;
        if (num < 7) {
            System.out.println(0);
        } else {
            for (int i = 7; i <=num; i ++) {
                x = i%7;
//                System.out.println(x);
                if (x == 0) {
                    count ++;
                } else {
                    byte[] num_byte = Integer.toString(i).getBytes();
//                    System.out.println(Arrays.toString(num_byte));
                    for (int j = 0; j < num_byte.length; j++) {
                        if ((int) num_byte[j] == 55) {
                            count++;
                            break;
                        }
                    }
                }
            }
            System.out.println(count);
        }
    }
}
