package cn.scut.aicodesmell.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author wanghy
 */
public class SecurityUtils {
    public static String String2MD5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

//    public static void main(String[] args) {
//        String s = "123123";
//        System.out.println(String2MD5(s));
//    }
}
