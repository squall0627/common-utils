/*
 * ProxyClassLoader.java
 * Created on  2017/11/11 23:01
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/11     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public class SimpleProxyClassLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        String basePath = SimpleProxyClassLoader.class.getResource("").getPath();
        String classFullName = SimpleProxyClassLoader.class.getPackage().getName() + "." + name;
        String classFullPath = basePath + name.replaceAll("\\.", "/") + ".class";
        File classFile = new File(classFullPath);
        if (!classFile.exists()) {
            throw new ClassNotFoundException(name);
        }

        FileInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(classFile);
            out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            return defineClass(classFullName, out.toByteArray(), 0, out.size());
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            classFile.delete();
        }
    }
}
