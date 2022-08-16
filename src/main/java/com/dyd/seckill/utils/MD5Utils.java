package com.dyd.seckill.utils;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);

    }
    private static final String salt="1a2b3c4d";

    // md5第一次加密
    public static String inputPassToFromPass(String inputPass){
        String str = salt.charAt(0) + salt.charAt(3) + inputPass + salt.charAt(5);
        return md5(str);
    }

    // md5第二次加密
    public static String fromPassToDBPass(String fromPass, String salt){
        String str = salt.charAt(0) + salt.charAt(3) + fromPass + salt.charAt(5);
        return md5(str);
    }

    public static String inputPassToDBPass(String inputPass, String salt){
        String fromPass = inputPassToFromPass(inputPass);
        String dbPass = fromPassToDBPass(fromPass, salt);
        return dbPass;
    }


}
