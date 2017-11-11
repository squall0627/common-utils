/*
 * AkigoInvocationHandler.java
 * Created on  2017/11/11 22:55
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/11     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy;

import java.lang.reflect.Method;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public interface SimpleInvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
