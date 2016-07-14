package creatarr;

/**
 * Created by user on 2016/7/9.
 */
public class Base {
    Base() {
        System.out.println("Base");
    }
}

class Checket extends Base {
    Checket() {
        super();//调用父类的构造方法，一定要放在方法的首个语句
        System.out.println("Checket");
    }

    public static void main(String argv[]) {
        Checket c = new Checket();
    }
}

