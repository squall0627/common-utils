/*
 * TestProxyInterface.java
 * Created on  2017/11/11 20:09
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/11     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.proxy.demo;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author 浩
 * @version 1.0.0
 */
public interface IProxyTarget {
    void plusCount();

    int getCount();

    String getCount(long i, Object xxx);
}
