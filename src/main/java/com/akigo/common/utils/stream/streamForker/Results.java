/*
 * Results.java
 * Created on  2017/11/3 0:55
 *
 * Copyright (c) 2017-2099. AkiGo科技有限公司 版权所有
 * AkiGo TECHNOLOGY CO.,LTD. All Rights Reserved.
 *
 * Date          Author     Version    Discription
 * 2017/11/3     浩         V1.0.0     InitVer
 */
package com.akigo.common.utils.stream.streamForker;

public interface Results {
    <R> R get(Object key);
}
