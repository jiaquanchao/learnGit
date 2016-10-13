package IODemo;

import java.io.*;

/**
 * Created by user on 2016/7/19.
 */
public class bufferRW {
    public static void main(String args[]) throws IOException {
        FileInputStream fis = new FileInputStream("test.txt");
        FileOutputStream fos = new FileOutputStream("buffer_text.txt");
        InputStreamReader isr = new InputStreamReader(fis);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedReader br = new BufferedReader(isr);
        BufferedWriter bw = new BufferedWriter(osw);

        String input;
        int l;
        while((input = br.readLine()) != null){
            bw.write(input);
        }

        bw.flush();
        bw.close();
        br.close();
        osw.close();
        isr.close();
        fos.close();
        fis.close();

    }
}
