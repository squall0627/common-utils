/*
 * Demo.java
 * Created on  2018/3/12 下午8:52
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2018/3/12     chenhao         V1.0.0     InitVer
 */
package com.akigo.common.utils.test.powerMock;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author chenhao
 * @version 1.0.0
 */
public class MockTarget {
    public static String staticMethod() {
        return "staticMethod";
    }

    private static String privateStaticMethod() {
        return "privateStaticMethod";
    }

    public static String privateStaticMethodTest() {
        return privateStaticMethod();
    }

    private String privateMethod() {
        return "privateMethod";
    }

    public static final String finalStaticMethod(String param) {
        return "finalStaticMethod";
    }

    private static final String privateFinalStaticMethod() {
        return "privateFinalStaticMethod";
    }
    public static final String privateFinalStaticMethodTest() {
        return privateFinalStaticMethod();
    }

    public final String innerFinalMethod() {
        return "innerFinalMethod";
    }

    private final String innerPrivateFinalMethod() {
        return "innerPrivateFinalMethod";
    }

    public final String innerPrivateFinalMethodTest() {
        return innerPrivateFinalMethod();
    }
}
