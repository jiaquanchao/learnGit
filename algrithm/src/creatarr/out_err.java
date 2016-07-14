package creatarr;

/**
 * Created by user on 2016/7/8.
 */
public class out_err {
    public static void main(String[] args) {
        int[] ints = new int[20];
        try {
            for (int i = 0; i < 21; i++) {
                ints[i] = i + 1;
                System.out.println(i + 1);
            }
        }catch (Exception e){
        }
        String str = "hello";
        byte bstr[] = str.getBytes();
        System.out.println(bstr.length);
        System.out.println(new String(bstr));
        System.out.println(str.indexOf("o"));
//        str.indexOf("o");
        System.out.println(str);
        System.out.println(str.replace("l", "x"));
        System.out.println(str);

    }
}
