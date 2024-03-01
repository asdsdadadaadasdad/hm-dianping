import java.io.*;
import java.net.URLClassLoader;

public class SlotTest {
    /**
     * 反序列化
     */

    static public void test() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
//             //序列化
//            oos = new ObjectOutputStream(new FileOutputStream("order.dat"));
//            oos.writeObject(new Order());
            // 反序列化
            ois = new ObjectInputStream(new FileInputStream("order.dat"));
            Object order = ois.readObject();
            System.out.println(order);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (ois != null) {
                    ois.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    void hhg(){}
    public static void main(String[] args) {
        new Order();
        ClassLoader cl;
        System.out.println(Order.h);
    }
}
