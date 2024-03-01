import java.io.Serializable;

public class Order implements Serializable {
    static {
        System.out.println("Order类的初始化");
    }
    final static String h="dd";
}