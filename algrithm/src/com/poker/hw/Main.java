package com.poker.hw;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by user on 2016/8/27.
 */
public class Main {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String[] orStr = sc.nextLine().split(" ");
//        String[] shouPai = orStr.split("-");
//        String[] firstHand = shouPai[0].split(" ");
//        String[] secondHand = shouPai[1].split(" ");
        int orNum = Integer.parseInt(orStr[0]);
        int minNum = Integer.parseInt(orStr[1]);
        System.out.println(orNum);
//        int numNum = Integer.parseInt(orStr[0]);
        int[] arr = new int[orStr.length - 2];
        for(int i = 2, j = 0; i < orStr.length; i ++) {
            arr[j] = Integer.parseInt(orStr[i]);
            j ++;
        }
        Arrays.sort(arr);
        for(int i = 0; i < minNum-1; i ++) {
            System.out.print(arr[i] + " ");
        }
        System.out.print(arr[minNum-1]);
    }
}
