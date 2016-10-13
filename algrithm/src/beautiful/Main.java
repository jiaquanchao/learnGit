package beautiful;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by user on 2016/8/20.
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] str = sc.nextLine().split(" ");
        int num_name = Integer.parseInt(str[0]);
        String[] str_name = new String[num_name];
//        byte[] tem_name;
        int[] res = new int[num_name];
        for (int i = 0; i < num_name; i++) {
            int sum = 0;
            str_name[i] = str[i+1];
            byte[] tem_name = str_name[i].getBytes();
            Map<Byte, Integer> count = new HashMap<Byte, Integer>();
            for (int j = 0; j < tem_name.length; j++) {
                if (count.containsKey(tem_name[j])) {
                    count.put(tem_name[j], (new Integer(count.get(tem_name[j]))) + 1);
                } else {
                    count.put(tem_name[j], 1);
                }
            }
//            System.out.println(count.toString());
            Object[] obj = count.values().toArray();
            Arrays.sort(obj);
            for (int t = obj.length - 1, a = 26; t >= 0; t--, a--) {
                sum = sum + a * (Integer) obj[t];
            }
            res[i] = sum;
        }
        for (int i : res) {
            System.out.println(i);
        }

    }
}
