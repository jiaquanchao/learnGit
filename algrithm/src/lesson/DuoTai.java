package lesson;

/**
 * Created by user on 2016/9/26.
 */
class A {
    public void call1() {
        System.out.println("callA1");
    }
    }
class B extends A  {
    public void call2 () {
        System.out.println("callB");
    }
}
abstract class People {
    private int age;
    private String name;
    public void people(int age, String name) {
        this.age = age;
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public int getAge() {
        return this.age;
    }
    abstract void want();
}
class Student extends People {
    public Student(int age, String name) {
        super.people(age, name);
    }
    int grade;
    public void student(){

    }
    @Override
    public void want() {
        System.out.println("want grade :"+grade);
        System.out.println(grade);
    }
}
class Officer extends People {
    private int money;

    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    void want() {
        System.out.println("want money:"+ money);
    }
}
public class DuoTai {
    public static void main(String[] args) {
        new DuoTai().say(new B());
        B b = new B();
        System.out.println(b instanceof A);
        System.out.println("_________________________");
        Student student = new Student(13, "xiaoming");
        student.grade = 666;
        student.want();
        Officer officer = new Officer();
        officer.setMoney(8888);
        officer.want();


    }
    public void say(A a) {
        a.call1();
    }
    public void say(B b) {
        b.call2();
        b.call1();
    }
}
