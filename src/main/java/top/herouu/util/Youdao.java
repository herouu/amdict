package top.herouu.util;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class Youdao {

    static final HTTP http = HTTP.builder()
            .baseUrl("https://openapi.youdao.com")    // 设置 BaseUrl
            .build();

    public static void main(String[] args) {
        String input = "vert";
        String appKey = "46c46d5afd6fcea0";
        String appSecret  = "tLmgIm4dHlgaHfzcAVAdXdQFZU10nQCw";
        String salt = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis()/1000;
        String originalString = appKey + input + salt + currentTime + appSecret;
        String sign = DigestUtils.sha256Hex(originalString);
        String s = http.sync("/api").addBodyPara("q", input)
                .addBodyPara("langType", "auto")
                .addBodyPara("appKey", appKey)
                .addBodyPara("dicts", "ec")
                .addBodyPara("salt", salt)
                .addBodyPara("sign", sign)
                .addBodyPara("signType", "v3")
                .addBodyPara("curtime", currentTime)
                .addBodyPara("docType", "json")
                .post().getBody().toString();
        System.out.println(s);
    }
}
