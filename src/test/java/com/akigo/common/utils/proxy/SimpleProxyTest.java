package com.akigo.common.utils.proxy;

import com.akigo.common.utils.proxy.demo.ProxyTarget;
import com.akigo.common.utils.proxy.demo.IProxyTarget;
import com.akigo.common.utils.proxy.demo.SimpleProxyDemo;
import org.junit.Test;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public class SimpleProxyTest {
    @Test
    public void test1() {
        IProxyTarget target = new ProxyTarget();
        IProxyTarget targetProxy = new SimpleProxyDemo(target).getInstance();
        targetProxy.plusCount();
        System.out.println(target.getCount());
        System.out.println(targetProxy.getCount());
    }
}
