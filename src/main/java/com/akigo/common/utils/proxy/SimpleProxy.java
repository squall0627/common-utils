/*
 * AkigoProxy.java
 * Created on  2017/11/11 22:58
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/11     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public class SimpleProxy {

    private static int index = 0;

    public static Object newProxyInstance(SimpleProxyClassLoader loader,
                                          Class<?>[] interfaces,
                                          SimpleInvocationHandler h) {

        String proxyClassName = "$Proxy" + String.valueOf(index++);

        // 1.生成源代码
        String proxySrc = generateSrc(proxyClassName, loader, interfaces, h);

        // 2.将生成的源代码输出到磁盘，保存为.java文件
        String proxyJavaFilePath = loader.getClass().getResource("").getPath();
        File f = new File(proxyJavaFilePath + proxyClassName + ".java");

        try (FileWriter fw = new FileWriter(f)) {
            fw.write(proxySrc);
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 3.编译源代码，并且生成.class文件
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = jc.getStandardFileManager(null, null, null);
        Iterable it = manager.getJavaFileObjects(f);

        JavaCompiler.CompilationTask task = jc.getTask(null, manager, null, null, null, it);
        task.call();
        try {
            manager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4.将class文件加载到中
        Class<?> proxyClass = null;
        try {
            proxyClass = loader.findClass(proxyClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // 5.返回被代理后的代理对象
        try {
            Constructor c = proxyClass.getConstructor(SimpleInvocationHandler.class);
            return c.newInstance(h);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            f.delete();
        }
    }

    private static String generateSrc(String proxyClassName, SimpleProxyClassLoader loader, Class<?>[] interfaces,
                                      SimpleInvocationHandler h) {
        String classLoaderPkg = loader.getClass().getPackage().getName();
        String proxyClassFullName = h.getClass().getCanonicalName();
        String strIf = Arrays.stream(interfaces).map(m -> m.getName()).collect(Collectors.joining(", "));

        StringBuilder sb = new StringBuilder();
        sb.append("package " + classLoaderPkg + ";");
        sb.append(System.lineSeparator());
        sb.append("import java.lang.reflect.Method;");
        sb.append(System.lineSeparator());
        sb.append("import " + proxyClassFullName + ";");
        sb.append(System.lineSeparator());
        sb.append("public class " + proxyClassName + " implements " + strIf + " {");
        sb.append(System.lineSeparator());
        sb.append("SimpleInvocationHandler h;");
        sb.append(System.lineSeparator());
        sb.append("public " + proxyClassName + "(SimpleInvocationHandler h) {");
        sb.append(System.lineSeparator());
        sb.append("this.h = h;");
        sb.append(System.lineSeparator());
        sb.append("}");
        sb.append(System.lineSeparator());

        for (Class<?> clazz : interfaces) {
            for (Method m : clazz.getMethods()) {
                String strParam = Arrays.stream(m.getParameters()).map(p -> p.getType().getName() + "                " +
                        "" + p.getName()).collect(Collectors.joining(", "));
                String strParamTypes = Arrays.stream(m.getParameters()).map(p -> p.getType().getName() + ".class")
                        .collect(Collectors.joining(", "));
                String strParamNames = Arrays.stream(m.getParameters()).map(p -> p.getName()).collect(Collectors
                        .joining(", "));
                sb.append("public " + m.getReturnType().getName() + " " + m.getName() + "(" + strParam + ") {");
                sb.append(System.lineSeparator());
                sb.append("try {");
                sb.append(System.lineSeparator());
                sb.append("Method m = " + clazz.getName() + ".class.getMethod(\"" + m.getName() + "\"" + (!strParamTypes
                        .isEmpty() ? (", " + strParamTypes) : "") + ");");
                sb.append(System.lineSeparator());
                if (!m.getReturnType().getName().equals("void")) {
                    sb.append("return (" + m.getReturnType().getName() + ")");
                }
                sb.append("this.h.invoke(this, m, new Object[] {" + strParamNames + "});");
                sb.append(System.lineSeparator());
                sb.append("} catch(Throwable e) {");
                sb.append(System.lineSeparator());
                sb.append("throw new RuntimeException(e);");
                sb.append(System.lineSeparator());
                sb.append("}");
                sb.append(System.lineSeparator());
                sb.append("}");
                sb.append(System.lineSeparator());
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
