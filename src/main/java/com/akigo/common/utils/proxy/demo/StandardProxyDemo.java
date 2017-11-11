/*
 * ProxyDemo.java
 * Created on  2017/11/11 20:07
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/11     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public class StandardProxyDemo implements InvocationHandler {

    private IProxyTarget target;

    public StandardProxyDemo(IProxyTarget target) {
        this.target = target;
    }

    public IProxyTarget getInstance() {
        Class clazz = this.target.getClass();
        return (IProxyTarget) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("proxy ->start");
        return method.invoke(this.target, args);
    }
}
