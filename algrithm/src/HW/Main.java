package HW;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by user on 2016/8/20.
 */
public class Main {
    public static void main(String[] args){
        int[] aaa = {1, 2, 3};
        aaa[0] = aaa[2];
        System.out.println(Arrays.toString(aaa));
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        byte[] words = str.getBytes();
//        System.out.println(Arrays.toString(words));
        byte a, b;
        for(int i = 1; i <words.length; i ++){
            for(int j = 0; j < words.length - i; j ++){
                if(words[j] > words[j+1]){
                    a = words[j];
                    words[j] = words[j+1];
                    words[j+1] = a;
                }
            }
        }
//        System.out.println(Arrays.toString(words));
        System.out.println(new String(words));
        int[] p = { 34, 21, 54, 18, 23, 76, 38, 98, 45, 33, 27, 51, 11, 20, 79, 30, 89, 41 };
        Main m = new Main();
        m.fast_sort(p, 0, p.length);


    }


    public void fast_sort(int[] arr, int low, int high){
        int key = arr[low];
        int low_key;
        int i = low, j = high-1;
        while (i < j) {
            while (arr[j] > key && i < j) {
                j--;
            }
            arr[i] = arr[j];
            while (arr[i] < key && i < j) {
                i++;
            }
            arr[j] = arr[i];
        }
        arr[i] = key;
        low_key = i;
        if(i - 1 > low){
            fast_sort(arr, low, low_key);
        }
        if(j + 1 < high) {
            fast_sort(arr, low_key+1, high);
        }
        System.out.println(Arrays.toString(arr));
    }

}

