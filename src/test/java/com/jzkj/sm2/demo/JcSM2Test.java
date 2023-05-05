package com.jzkj.sm2.demo;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class JcSM2Test {
    //中心秘钥对中的公钥
    private String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEokvZnKp/Qj0e95IfQqJYXVrjerDf63fwTIIybZZFR1GbbrZAFu88KmOvl5uequMMEx+l+dUNGN1jxIT86BNt/w==";
    //银行秘钥对中的私钥
    private String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgXk2iJ8wJXH0kgSyG3ZCWbl9VGyoFfNxnfMSvcWJ0GjWgCgYIKoEcz1UBgi2hRANCAAS92xzIKjKI7wMKjS4eT/56T3EXDoBPF8VbwFBW3y56mFjterhF48TfDaEpEtxGv1rN6sx+OwM2F+fJW8ASzXcn";

    //中心的组织ID
    private String ZX_GROUP_ID = "eb4803b9320d427ea4d11df62605af94"; //中心组织ID

    //从中心申请的银行的组织ID
    private String YH_GROUP_ID = "1dbc35a1d2b5462998ffb4de75a45718";

    //中心系统中保存的可信银行组织ID列表
    private List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605af94","1dbc35a1d2b5462998ffb4de75a45718","1dbc35a1d2b5462998ffb4de753345718");

    //测试由融媒体中心向公积金中心发送请求
//    10.10.100.22：8089
//    620302196604220857 马勇
//    https://xxxx:9080/gjjInte/grgjjjbxxcx.action
    @Test
    public void yhSendHttpTest() throws Exception {
        //封装数据体中的 head
        Map<String, Object> head = new HashMap<>();
        //中心作为请求端 设置请求方的组织ID
        head.put("groupId", YH_GROUP_ID);
        //请求数据时的时间戳
        head.put("timestamp", System.currentTimeMillis());
        //本次请求的唯一编号 建议使用32位UUID
        head.put("reqId", Util.UUID());

        //封装业务数据
        Map<String, String> map = new HashMap<>();
        map.put("jkrIdtype", "01");
        map.put("jkrIdcard", "620302196604220857");
        map.put("jkrname", "马勇");

        //封装数据体
        Map<String, Object> data = new HashMap<>();
        data.put("data", map);
        data.put("head", head);

        String dataStr = JSON.toJSONString(data);
        //使用对应银行的公钥 和 中心自己的私钥 组成SM2
        SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
        //使用银行的公钥进行加密
        String cryptoData = sm2.encryptBcd(dataStr, KeyType.PublicKey);
        //使用自己的私钥进行签名 后进行HEX
        String sign = HexUtil.encodeHexStr(sm2.sign(dataStr.getBytes()));

        //封装加密后数据体
        Map<String, Object> testBody = new HashMap<>();
        testBody.put("cryptoData", cryptoData);
        testBody.put("sign", sign);

        //将最终数据转换为json字符串
        String body = JSON.toJSONString(testBody);
        log.info("密文发送包：" + body);
        //使用HTTP请求发送数据包  10.10.100.22：8089
        String url = "http://127.0.0.1:8080//api/grcx/sm2";
//        url = "http://10.10.100.22:8089//api/grcx/sm2";
        HttpResponse httpResponse = HttpRequest.post(url)
                .header("content-type", "application/json")
                .header("GROUP_ID", YH_GROUP_ID)
                .body(body)
                .execute();
        //返回正常
        if (httpResponse.isOk() == false) {
            return;
        }
        //银行收到中心返回的数据
        String zxGroupId = httpResponse.header("GROUP_ID");
        //验证返回数据头中组织ID是否合法，这里只做示例验证
        if (StringUtils.isEmpty(zxGroupId) || GROUP_ID_S.contains(zxGroupId) == false) {
            throw new Exception("未知来源数据");
        }
        //获取返回的数据
        String responseBody = httpResponse.body();
        //将返回的数据进行JSON解析
        JSONObject responseBodyJson = JSON.parseObject(responseBody);
        //获取加密数据体
        String responseCryptoData = responseBodyJson.getString("cryptoData");
        //获取数据签名
        String responseSign = responseBodyJson.getString("sign");

        //使用银行私钥进行解密
        String responseData = sm2.decryptStr(responseCryptoData, KeyType.PrivateKey);
        //使用中心的公钥进行验签
        boolean verify = sm2.verify(responseData.getBytes(), HexUtil.decodeHex(responseSign));
        if (verify == false) {
            throw new Exception("签名不一致 数据被篡改");
        }
        JSONObject responseDataJson = JSON.parseObject(responseData);
        //获取数据返回时的时间戳
        long responseDataTimestamp = responseDataJson.getJSONObject("head").getLongValue("timestamp");
        //时间超过5分钟 响应数据过期
        if (System.currentTimeMillis() - responseDataTimestamp >= (5 * 60000)) {
            throw new Exception("响应数据过期过期");
        }

        //解析收到中心返回的数据 进行具体业务处理 此处略过...
        log.info("解析收到中心返回的数据:"+responseData);
    }
}
