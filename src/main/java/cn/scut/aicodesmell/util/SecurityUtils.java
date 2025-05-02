package cn.scut.aicodesmell.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * @author wanghy
 */
public class SecurityUtils {
    public static String String2MD5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String get64bitUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        //base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        String encodedId = encoder.encodeToString(uuidString.getBytes());

        //截取64或者补充0
        if (encodedId.length() > 64) {
            encodedId = encodedId.substring(0, 64);
        } else if (encodedId.length() < 64) {
            encodedId = String.format("%-" + 64 + "s", encodedId).replace(' ', '0');
        }

        return encodedId;
    }

//    public static void main(String[] args) {
//        String bitUUID = get64bitUUID();
//        System.out.println(bitUUID);
//        System.out.println(bitUUID.length());
//    }
}
