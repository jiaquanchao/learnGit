package creatarr;

import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

interface countTime{
    void startTime();
    void stopTime();
    void allTime();
}
class proTime implements countTime{
    long st,sp,al;
    public void startTime(){
        this.st = System.nanoTime();
    }
    public void work(){
        Scanner sc = new Scanner(System.in);
        System.out.print("请输入密码: ");
        String key = sc.nextLine();
        // TODO: 2016/7/14
    }
    @Override
    public void stopTime() {
        this.sp = System.nanoTime();
    }

    @Override
    public void allTime() {
        this.stopTime();
        this.al = this.sp - this.st;
        System.out.println("spend time is: " + this.al);
    }
}
class clock{
    public void clock(countTime ct){
        ct.startTime();
        ct.allTime();
    }
    int n = 40;
    String s = "abc";
}
abstract class people{
    private String name = "jack";
    private int age = 12;
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public people(){
        System.out.println("无参构造");
    }
    public people(String name, int age){
        this.name = name;
        this.age = age;
    }
    public abstract void want();
    public void notab(){
        System.out.println("not ab");
    }
}
class Student extends people{
    private int score;
    public Student(String name, int age, int score){
        super(name, age);
        this.score = score;
    }

    @Override
    public void want() {
        System.out.println("override");
    }
}
public class test_print {

    public static void main(String args[]) {
        proTime ct = new proTime();
        clock clock = new clock();
        clock.clock(new proTime());
        Scanner sc = new Scanner(System.in);
//        int n = Integer.parseInt(sc.nextLine());
        String tt = "abcdefghijk";
//        System.out.print(tt.charAt(2));
//        StringBuffer ttt = new StringBuffer();
//        ttt.append(tt.substring(1,3));
//        System.out.println(ttt);
//        System.out.println(ttt.toString());
//        System.out.println(ttt.length());
//
//        bian b = new bian();
//        bian b1 = new bian();
//        b.n = 666;
//        b1.s = "bbb";
//        System.out.println(b.s+" "+b1.s);
//        System.out.println(b.n+" "+b1.n);
//        System.out.println(new bian().s);
        ct.startTime();
        Student s = new Student("jiji", 16, 100);
        s.want();
        s.notab();
        System.out.println(s.getName());
        s.setName("jqc");
        System.out.println(s.getName());
        ct.allTime();

    }
}
