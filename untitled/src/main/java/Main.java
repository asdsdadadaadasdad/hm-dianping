import java.lang.reflect.Field;

class MyClass extends dai{

    static {
        System.out.println("MyClass static block");
    }
}
class dai extends Memory{
    static int h=5;
    static {
        System.out.println("黑呆是吧");
    }
    static {
        System.out.println("dai");
    }
}
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(MyClass.h);
    }
    static {
        System.out.println("Main");
    }
}
class Memory {
    public static void main(String[] args) {//line 1
        int i= 1;//line 2
        Object obj = new Object();//line 3
        Memory mem = new Memory();//Line 4
        mem.foo(obj);//Line 5
    }//Line 9
    private void foo(Object param) {//line 6
        String str = param.toString();//line 7
        System.out.println(str);
    }//Line 8
    public static void test5() {
        String s1 = "javaEE";
        String s2 = "hadoop";

        String s3 = "javaEEhadoop";
        String s4 = "javaEE" + "hadoop";
        String s5 = s1 + "hadoop";
        String s6 = "javaEE" + s2;
        String s7 = s1 + s2;

        System.out.println(s3 == s4); // true 编译期优化
        System.out.println(s3 == s5); // false s1是变量，不能编译期优化
        System.out.println(s3 == s6); // false s2是变量，不能编译期优化
        System.out.println(s3 == s7); // false s1、s2都是变量
        System.out.println(s5 == s6); // false s5、s6 不同的对象实例
        System.out.println(s5 == s7); // false s5、s7 不同的对象实例
        System.out.println(s6 == s7); // false s6、s7 不同的对象实例

        String s8 = s6.intern();
        System.out.println(s3 == s8); // true intern之后，s8和s3一样，指向字符串常量池中的"javaEEhadoop"
    }
    public void test6(){
        String s0 = "beijing";
        String s1 = "bei";
        String s2 = "jing";
        String s3 = s1 + s2;
        System.out.println(s0 == s3); // false s3指向对象实例，s0指向字符串常量池中的"beijing"
        String s7 = "shanxi";
        final String s4 = "shan";
        final String s5 = "xi";
        String s6 = s4 + s5;
        System.out.println(s6 == s7); // true s4和s5是final修饰的，编译期就能确定s6的值了
    }
    static {
        System.out.println("memory");
    }
}
class StringGCTest {
    /**
     * -Xms15m -Xmx15m -XX:+PrintGCDetails
     */
    static final String a="dsds";
    static final String ab=new String();
    Main main;
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        System.out.println(new Integer(5).equals(new StringGCTest().main));

    }
    Object obj=new Object();
    void hh(){
        System.out.println(a);
        System.out.println(ab);
        synchronized (obj){
            System.out.println(obj);
        }
    }

}