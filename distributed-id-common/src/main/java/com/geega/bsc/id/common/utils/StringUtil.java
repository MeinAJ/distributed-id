package com.geega.bsc.id.common.utils;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/28 11:58 下午
 */
public class StringUtil {

    private StringUtil() {

    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }
}
