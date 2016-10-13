package creatarr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/7/14.
 */
//class human{
//    private String name;
//    private int age;
//    public void breath(){
//        System.out.println("people can breath.");
//    }
//    public void zhishang(int zhishang){
//        if(zhishang > 100){
//            System.out.println("smart!");
//        }else {
//            System.out.println("common people.");
//        }
//    }
//}
interface six<ball>{

    void cheer();
    void satire();
    ball[] gift(ball[] t);
}
class ballgame<ball> implements six<ball>{
     ball[] things = null;
    public ballgame(){

    }
    public ballgame(ball[] b){
        this.things = this.gift(b);
    }
    @Override
    public void cheer() {
        System.out.println("get score! you are excellent! ");
    }

    @Override
    public void satire() {
        System.out.println("Hey! you help those guys get so many score!");
    }

    @Override
    public ball[] gift(ball[] b) {
        int temp_l = this.things.length;
        for(int i = 0; i<b.length; i++, temp_l++){
            this.things[temp_l] = b[i];
        }
        return this.things;
    }
}
//class chinese extends human{
//
//}
public class country {
    public static void main(String args[]){
        String[] storage = {"battle", "TV","pen"};
        six<String> s = new ballgame<String>(storage);
        System.out.println(s);
        List<Integer> l = new ArrayList<Integer>();
    }
}
