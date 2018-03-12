/*
 * PowerMockHelper.java
 * Created on  2018/3/12 下午9:37
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2018/3/12     chenhao         V1.0.0     InitVer
 */
package com.akigo.common.utils.test.powerMock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareEverythingForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * 类的描述信息<br>
 * <br>
 *
 * @author chenhao
 * @version 1.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareEverythingForTest
public class PowerMockHelper {
    @Test
    public void test() {
//        PowerMockito.spy(MockTarget.class);
//        PowerMockito.when(MockTarget.staticMethod()).thenReturn("mockStaticMethod");
//        mockStatic(MockTarget.class, MockTarget.staticMethod()).thenReturn("mockStaticMethod");

//        PowerMockito.spy(MockTarget.class);
//        try {
//            PowerMockito.doReturn("mockPrivateStaticMethod").when(MockTarget.class, "privateStaticMethod");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(MockTarget.privateStaticMethodTest());
//        System.out.println(MockTarget.staticMethod());

//        try {
//            PowerMockito.when(MockTarget.class, MockTarget.class.getDeclaredMethod("privateStaticMethod"))
//                    .withNoArguments().thenReturn("mockPrivateStaticMethod");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        mockAnyStatic(MockTarget.class, "privateStaticMethod").thenReturn("mockPrivateStaticMethod");
//        System.out.println(MockTarget.privateStaticMethodTest());
//        mockAnyStatic(MockTarget.class, "staticMethod").thenReturn("mockStaticMethod");
//        System.out.println(MockTarget.staticMethod());
//
//        MockTarget mockedTarget = new MockTarget();
//        mockAnyStatic(MockTarget.class, "finalStaticMethod", "aaa").thenReturn("mockfinalStaticMethod");
//
//        mockAnyStatic(MockTarget.class, "privateFinalStaticMethod").thenReturn("mockprivateFinalStaticMethod");
//        mockAnyInstance(mockedTarget, "innerFinalStaticMethod").thenReturn("mockinnerFinalStaticMethod");
//        mockAnyInstance(mockedTarget, "innerPrivateFinalStaticMethod").thenReturn("mockinnerPrivateFinalStaticMethod");
//        PowerMockito.spy(mockedTarget);
//        try {
//            PowerMockito.doReturn("mockinnerFinalStaticMethod") .when(mockedTarget, "innerFinalStaticMethod");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(MockTarget.finalStaticMethod("aaa"));
//        System.out.println(MockTarget.privateFinalStaticMethodTest());
//        System.out.println(mockedTarget.innerFinalStaticMethod());

//        AkigoStaticMockitoStubber<MockTarget> mo = mockAnyStatic(MockTarget.class);
//        mo.when("privateFinalStaticMethod").thenReturn("mockprivateFinalStaticMethod");
//        mo.when("privateStaticMethod").thenReturn("mockPrivateStaticMethod");
//        mo.when("staticMethod").thenReturn("mockStaticMethod");
//        mo.when("finalStaticMethod", "aaa").thenReturn("mockfinalStaticMethod");
//        mo.when("privateFinalStaticMethod").thenReturn("mockprivateFinalStaticMethod");

        mockAnyStatic(MockTarget.class).addCase(mock ->
                mock.when("privateFinalStaticMethod").thenReturn("mockprivateFinalStaticMethod")
        ).addCase(mock ->
                mock.when("privateStaticMethod").thenReturn("mockPrivateStaticMethod")
        ).addCase(mock ->
                mock.when("staticMethod").thenReturn("mockStaticMethod")
        ).addCase(mock ->
                mock.when("finalStaticMethod", "aaa").thenReturn("mockfinalStaticMethod")
        ).addCase(mock ->
                mock.when("privateFinalStaticMethod").thenReturn("mockprivateFinalStaticMethod")
        );

//            mo.when(MockTarget.class, "innerFinalMethod").thenReturn("mockinnerFinalMethod");
//            mo.when(MockTarget.class, "innerPrivateFinalMethod").thenReturn("mockinnerPrivateFinalMethod");

        System.out.println(MockTarget.privateStaticMethodTest());
        System.out.println(MockTarget.staticMethod());
        System.out.println(MockTarget.finalStaticMethod("aaa"));
        System.out.println(MockTarget.privateFinalStaticMethodTest());
        System.out.println(new MockTarget().innerFinalMethod());
        System.out.println(new MockTarget().innerPrivateFinalMethodTest());

        AkigoInstanceMockitoStubber<MockTarget> mockedTarget = mockAnyInstance(new MockTarget());

        mockedTarget
                .addCase(mock -> mock.when("innerFinalMethod").thenReturn("mockinnerFinalMethod"))
                .addCase(mock -> mock.when("innerPrivateFinalMethod").thenReturn("mockinnerPrivateFinalMethod"));
//        mockedTarget.when("innerFinalMethod").thenReturn("mockinnerFinalMethod");
//        mockedTarget.when("innerPrivateFinalMethod").thenReturn("mockinnerPrivateFinalMethod");

        System.out.println(mockedTarget.getTarget().innerFinalMethod());
        System.out.println(mockedTarget.getTarget().innerPrivateFinalMethodTest());
    }

//    public <T> OngoingStubbing<T> mockAnyStatic(Class<?> targetCLazz, String methodName, Object... arguments) {
//        PowerMockito.spy(MockTarget.class);
//        try {
//            return PowerMockito.when(targetCLazz, methodName, arguments);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public <T> AkigoStaticMockitoStubber<T> mockAnyStatic(Class<T> targetCLazz) {
        PowerMockito.spy(targetCLazz);
        return new AkigoStaticMockitoStubber(targetCLazz);
    }

    public <T> AkigoInstanceMockitoStubber mockAnyInstance(T targetInstance) {
        return new AkigoInstanceMockitoStubber(PowerMockito.spy(targetInstance));
    }
}