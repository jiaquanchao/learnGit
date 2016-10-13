package HW;

import java.util.Scanner;


public class Main1 {
    public int[] left(int[] arr) {
        int[] count_left = new int[arr.length];
        for (int i = 1; i < arr.length; i++) {
            for (int j = i - 1; j >= 0; j--) {
                if (arr[i] > arr[j]) {
                    if (count_left[i] < count_left[j] + 1) {
                        count_left[i] = count_left[j] + 1;
                    }
                }
            }
        }
        return count_left;
    }
     public int[] right(int[] arr){
        int[] count_right = new int[arr.length];
        for(int i = arr.length - 2; i >= 0; i--){
            for(int j = i + 1; j <arr.length; j++){
                if(arr[i]>arr[j]){
                    if(count_right[i] < count_right[j]+1){
                        count_right[i] = count_right[j] + 1;
                    }
                }
            }
        }
        return count_right;
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String origin = sc.nextLine();
        String[] origin_num = origin.split(" ");
        int people = Integer.parseInt(origin_num[0]);
        int[] height = new int[origin_num.length - 1];
        for(int i = 0; i<origin_num.length-1; i++){
            height[i] = Integer.parseInt(origin_num[i+1]);
        }
        Main1 m = new Main1();
        int[] result_l = m.left(height);
//        System.out.println(Arrays.toString(result_l));
        int[] result_r = m.right(height);
//        System.out.println(Arrays.toString(result_r));
        int leave = 0;
        for(int i = 0; i < height.length; i++){
            if(leave < result_l[i] + result_r[i] + 1){
                leave = result_l[i] + result_r[i] + 1;
            }
        }
        System.out.println(people-leave);
    }
}


