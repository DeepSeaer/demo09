package com.cn.util;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
    private static Logger logger = LoggerFactory.getLogger(ClassUtil.class);
    /**
     * 获取范型的Class，若有多个，只获取第一个
     * @param inputCls
     * @return
     */
    public static Class getSingleParameterizedType(Class inputCls){
        Type t = inputCls.getGenericSuperclass();
        if(Object.class.getName().equals(t.getTypeName())){
            return null;
        }

        Class clz = null;
        if (t instanceof ParameterizedType) {
            Type[] p = ((ParameterizedType) t).getActualTypeArguments();
            if(p.length>0 && p[0] instanceof Class){
                clz = (Class) p[0];
            }
        } else {
            clz = getSingleParameterizedType((Class) t);
        }
        return clz;
    }

    /**
     * 获取范型的Class
     * @param inputCls
     * @return
     */
    public static List<Class> getParameterizedTypes(Class inputCls){
        Type t = inputCls.getGenericSuperclass();
        if(Object.class.getName().equals(t.getTypeName())){
            return null;
        }

        List<Class> lst = new ArrayList<>();
        if (t instanceof ParameterizedType) {
            Type[] typeAry = ((ParameterizedType) t).getActualTypeArguments();
            if(ArrayUtils.isNotEmpty(typeAry)){
                for (Type type:typeAry){
                    if(type instanceof Class){
                        lst.add((Class) type);
                    }
                }
            }
        } else {
            lst = getParameterizedTypes((Class) t);
        }
        return lst;
    }

    /**
     * 获取指定包路径下所有子类或接口实现类
     * @param
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls,String[] packages) throws IOException,ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<Class<?>> lst = getClasses(cls,packages);
        for (Class<?> c : lst) {
            if (cls.isAssignableFrom(c) && !cls.equals(c)) {
                classes.add(c);
            }
        }
        return classes;
    }
    /**
     * 获取同一包路径下所有子类或接口实现类
     * @param
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls) throws IOException,ClassNotFoundException {
        return getAllAssignedClass(cls,new String[]{cls.getPackage().getName()});
    }

    /**
     * 将包装类型转换为基本类型
     * @param c
     * @return
     */
    public static Class transferToPrime(Class c) {
        if(c.isPrimitive()){
            return c;
        }
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals("TYPE")) {
                try {
                    return (Class) f.get(null);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return c;
    }

    /**
     * 取得指定包路径下的所有类
     *
     * @param cls
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getClasses(Class<?> cls,String[] packages) throws IOException,ClassNotFoundException {
        if(packages==null || packages.length==0){
            packages = new String[]{cls.getPackage().getName()};
        }
        List<Class<?>> lst = new ArrayList<Class<?>>();

        for (String pk :packages){
            String path = pk.replace(".", "/");
            Enumeration<URL> enumeration = getClassLoader().getResources(path);

            while (enumeration.hasMoreElements()){
                URL url=enumeration.nextElement();
                if("jar".equalsIgnoreCase(url.getProtocol())){
                    lst.addAll(getClasses(url,pk));
                } else {
                    lst.addAll(getClasses(new File(url.getFile()), pk));
                }
            }
        }

        return lst;
    }

    /**
     * 迭代查找类
     *
     * @param dir
     * @param pk
     * @return
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getClasses(File dir, String pk) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!dir.exists()) {
            return classes;
        }
        for (File f : dir.listFiles()) {
            if (!f.isDirectory()) {
                String name = f.getName();
                if (name.endsWith(".class")) {
                    classes.add(Class.forName(pk + "." + name.substring(0, name.length() - 6)));
                }
            }
        }
        return classes;
    }

    /**
     * 第三方Jar类库的引用。<br/>
     * @throws IOException
     * */
    private static List<Class<?>> getClasses(URL url,String pk) throws ClassNotFoundException, IOException {
        String pkPath = pk.replace(".","/");
        List<Class<?>> classes = new ArrayList<Class<?>>();
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            // 类似：sun/security/internal/interfaces/TlsMasterSecret.class
            String jarEntryName = jarEntry.getName();
            if(jarEntryName.endsWith(".class") && jarEntryName.contains(pkPath)){
                Class<?> cls = Class.forName(jarEntryName.replace("/", ".").substring(0, jarEntryName.length() - 6));
                if(cls.getPackage().getName().equals(pk)){
                    classes.add(cls);
                }
            }
        }

        return classes;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassUtil.class.getClassLoader();
        }
        return cl;
    }
}
