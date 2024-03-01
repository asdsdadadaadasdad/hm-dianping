package sd.myclassloaders;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.SneakyThrows;
import sun.misc.Resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.DriverManager;


public class sdloader extends ClassLoader {

    String basepath="D:\\ziliao\\heima\\hm-dianping\\classes\\production\\untitled";

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if(loadedClass!=null) return loadedClass;
        try {
            return findClass(name);
        } catch (Exception e) {
            ClassLoader parent = getParent();
            return parent.loadClass(name);
        }
    }

    @lombok.SneakyThrows
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clazz=null;
        BufferedInputStream in=new BufferedInputStream(
                new FileInputStream(new File(basepath,name.replace('.','\\')+".class"))
        );
        ByteOutputStream bos=new ByteOutputStream();
        byte[] buffer=new byte[1024];
        int read=0;
        while ((read=in.read(buffer))>0){
            bos.write(buffer,0,read);
        }
        byte[] bytes = bos.toByteArray();
        clazz=defineClass(name,bytes,0,bytes.length);
        return clazz;
    }

    @SneakyThrows
    public static void main(String[] args) throws ClassNotFoundException {
        sdloader sdl = new sdloader();
        sdloader sdl2 = new sdloader();
        Class<?> clazz = sdl.loadClass("dai");
        Class<?> dai = sdl.loadClass("dai");
        Class<?> clazz2 = sdl2.loadClass("dai");
        System.out.println(clazz+" "+clazz.getClassLoader());
        System.out.println(clazz==dai);
        System.out.println(dai==clazz2);
        System.out.println(dai.getSuperclass()==clazz2.getSuperclass());
        Class<?> dai1 = Class.forName("dai");
        System.out.println(dai1);
        DriverManager.getConnection("jdbc:mysql://localhost:3306/atguigudb","root","root");
        ClassLoader.getSystemClassLoader();
    }

}
