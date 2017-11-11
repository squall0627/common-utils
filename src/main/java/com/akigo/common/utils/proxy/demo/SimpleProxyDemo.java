/*
 * AkigoProxyDemo.java
 * Created on  2017/11/12 0:16
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/12     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy.demo;

import com.akigo.common.utils.proxy.SimpleInvocationHandler;
import com.akigo.common.utils.proxy.SimpleProxy;
import com.akigo.common.utils.proxy.SimpleProxyClassLoader;

import java.lang.reflect.Method;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public class SimpleProxyDemo implements SimpleInvocationHandler {
    private IProxyTarget target;

    public SimpleProxyDemo(IProxyTarget target) {
        this.target = target;
    }

    public IProxyTarget getInstance() {
        Class clazz = this.target.getClass();
        return (IProxyTarget) SimpleProxy.newProxyInstance(new SimpleProxyClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("proxy ->start");
        return method.invoke(this.target, args);
    }
}
