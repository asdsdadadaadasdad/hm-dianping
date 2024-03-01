import java.io.IOException;
import java.nio.ByteBuffer;

public class father {
    private int hh;
    final static int gg=5;
    {
        hh=6;
    }
    static {
        //System.out.println("666");
    }
    void gg(){
        System.out.println(hh);
    }
    father(){
        System.out.println("哈哈哈");
    }
    father(int x){
        System.out.println(x);
    }
    public static void main(String[] args) {
        System.out.println(gg);
        new son().gg();
    }
    static class son extends father{
        public static void main(String[] args) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 1024);
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteBuffer=null;
            System.gc();
            System.out.println("回收完毕");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
