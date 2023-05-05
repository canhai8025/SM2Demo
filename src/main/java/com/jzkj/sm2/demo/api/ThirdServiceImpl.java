package com.jzkj.sm2.demo.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzkj.sm2.demo.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ThirdServiceImpl {

    public static void main(String[] args) {
        log.info("yhSendHttpTest().......................start...........");
        try {
//            yhSendHttpTest();
            new ThirdServiceImpl().tqyz();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("yhSendHttpTest().......................end...........");

    }
    public  void tqyz(){
        //拿到公积金个人账号
        List<HousingFundQueryVO> housingFundVOs = null;
        try {
            String responseData = housingFundQuery();
            if (StringUtils.isEmpty(responseData)) {
                log.info("responseData>>>"+responseData);
            };
            JSONObject jsonObject = JSONObject.parseObject(responseData);
            JSONObject result = jsonObject.getJSONObject("result");
            Integer code = result.getInteger("code");
            String msg = result.getString("msg");
            JSONArray datas = result.getJSONArray("data");
            log.info("code={" + code + "},data=" + datas + "");
            if(datas != null){
                housingFundVOs = JSONArray.parseArray(datas.toJSONString(), HousingFundQueryVO.class);
            }
            /*测试数据 begin*/

            /*测试数据 end*/
            if (housingFundVOs != null) {
                String grzh = housingFundVOs.get(0).getGrzh();
                log.info("个人账号：" + grzh);
                housingFundExtractVerification(grzh);
            }
        } catch (Exception e) {
            log.error("调获取用户公积金数据异常："+e.getMessage());
        }
    }
    /**
     * 公积金个人基本信息查询
     */
    public String housingFundQuery() throws Exception {
        log.info("housingFundQuery...........start......");
        //中心秘钥对中的公钥
        String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";//commonData.getPublicKey();
        //自身秘钥对中的私钥
        String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";//commonData.getPrivateKey();

        //从中心申请的自身的组织ID
        String GROUP_ID = "eb4803b9320d427ea4d11df62605ba83";//commonData.getGROUP_ID();

        //中心系统中保存的可信组织ID列表
        List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605af94", GROUP_ID, "1dbc35a1d2b5462998ffb4de753345718");

        //封装数据体中的 head
        Map<String, Object> head = new HashMap<>();
        //中心作为请求端 设置请求方的组织ID
        head.put("groupId", GROUP_ID);
        //请求数据时的时间戳
        head.put("timestamp", System.currentTimeMillis());
        //本次请求的唯一编号 建议使用32位UUID
        head.put("reqId", Util.UUID());

        //封装业务数据
        Map<String, String> map = new HashMap<>();
        map.put("xxly", "4");
        /*map.put("sfzh", "620302196604220857");
        map.put("xingming", "马勇");*/
        map.put("sfzh", "620102196711010039");
        map.put("xingming", "徐荣年");
        /*map.put("sfzh", user.getUserAuth().getCardId());
        map.put("xingming", user.getName());*/

        //封装数据体
        Map<String, Object> data = new HashMap<>();
        data.put("data", map);
        data.put("head", head);
        log.info("封装数据体>>>>>>"+data);

        String url = "http://192.168.35.200:9080/gjjInte/grgjjjbxxcx.action";//commonData.getGrgjjjbxxcx();
        log.info("公积金个人基本信息查询url>>>"+url);
        String responseData = getGjjData(publicKey, privateKey, GROUP_ID, GROUP_ID_S, data, url);
        return responseData;
    }
    /**
     * 发公积金中心post请求，获取公积金查询数据
     */
    private String getGjjData(String publicKey, String privateKey, String GROUP_ID, List<String> GROUP_ID_S, Map<String, Object> data, String url) throws Exception {
        String dataStr = JSON.toJSONString(data);
        //使用对应自身的公钥 和 中心自己的私钥 组成SM2
        SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
        //使用自身的公钥进行加密
        String cryptoData = sm2.encryptBcd(dataStr, KeyType.PublicKey);
        //使用自己的私钥进行签名 后进行HEX
        String sign = HexUtil.encodeHexStr(sm2.sign(dataStr.getBytes()));

        //封装加密后数据体
        Map<String, Object> testBody = new HashMap<>();
        testBody.put("cryptoData", cryptoData);
        testBody.put("sign", sign);

        //将最终数据转换为json字符串
        String body = JSON.toJSONString(testBody);
//        log.info("密文发送包：" + body);
        //使用HTTP请求发送数据包  10.10.100.22：8089

        HttpResponse httpResponse = HttpRequest.post(url)
                .header("content-type", "application/json")
                .header("GROUP_ID", GROUP_ID)
                .body(body)
                .execute();
        //返回正常
        if (!httpResponse.isOk()) {
            return null;
        }
        //收到中心返回的数据
        String zxGroupId = httpResponse.header("GROUP_ID");
        log.info("httpResponse.header.GROUP_ID>>>"+zxGroupId);
        //验证返回数据头中组织ID是否合法，这里只做示例验证
        /*if (org.springframework.util.StringUtils.isEmpty(zxGroupId) || GROUP_ID_S.contains(zxGroupId) == false) {
            throw new Exception("未知来源数据");
        }*/
        //获取返回的数据
        String responseBody = httpResponse.body();
//        log.info("获取返回的数据>>>"+responseBody);
        //将返回的数据进行JSON解析
        JSONObject responseBodyJson = JSON.parseObject(responseBody);
        //获取加密数据体
        String responseCryptoData = responseBodyJson.getString("cryptoData");
        //获取数据签名
        String responseSign = responseBodyJson.getString("sign");

        //使用自身私钥进行解密
        String responseData = sm2.decryptStr(responseCryptoData, KeyType.PrivateKey);
//        log.info("使用自身私钥进行解密>>>"+responseData);
        //使用中心的公钥进行验签
        /*boolean verify = sm2.verify(responseData.getBytes(), HexUtil.decodeHex(responseSign));
        if (verify == false) {
            throw new Exception("签名不一致 数据被篡改");
        }*/
        JSONObject responseDataJson = JSON.parseObject(responseData);
        //获取数据返回时的时间戳
        long responseDataTimestamp = responseDataJson.getJSONObject("head").getLongValue("timestamp");
        //时间超过5分钟 响应数据过期
        if (System.currentTimeMillis() - responseDataTimestamp >= (5 * 60000)) {
            throw new Exception("响应数据过期过期");
        }

        //解析收到中心返回的数据 进行具体业务处理 此处略过...
        log.info("解析收到中心返回的数据:" + responseData);
        return responseData;
    }


    /**
     * 提取验证
     */
    public HousingFundExtractVO housingFundExtractVerification(String grzh) throws Exception {
        //中心秘钥对中的公钥
        String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";
        //自身秘钥对中的私钥
        String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";

        //从中心申请的自身的组织ID
        String GROUP_ID = "eb4803b9320d427ea4d11df62605ba83";

        //中心系统中保存的可信组织ID列表
        List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605af94", "eb4803b9320d427ea4d11df62605ba83", "1dbc35a1d2b5462998ffb4de753345718");

        //封装数据体中的 head
        Map<String, Object> head = new HashMap<>();
        //中心作为请求端 设置请求方的组织ID
        head.put("groupId", GROUP_ID);
        //请求数据时的时间戳
        head.put("timestamp", System.currentTimeMillis());
        //本次请求的唯一编号 建议使用32位UUID
        head.put("reqId", Util.UUID());

        //封装业务数据
        Map<String, String> map = new HashMap<>();
        //公积金账号
        map.put("grzh", grzh);
        //信息来源（政务服务为4）
        map.put("xxly", "4");


        //封装数据体
        Map<String, Object> data = new HashMap<>();
        data.put("data", map);
        data.put("head", head);

        //提取验证
        String url = "http://192.168.35.200:9080/gjjInte/retirementVerification.action";
        log.info("提取验证url>>>"+url);
        String responseData = getGjjData(publicKey, privateKey, GROUP_ID, GROUP_ID_S, data, url);
        log.info("提取验证返回数据>>>"+responseData);
        if (StringUtils.isEmpty(responseData)) return null;
        return JSONObject.parseObject(responseData, HousingFundExtractVO.class);
    }

    /**
     * 公积金租房提取
     */
    public HousingFundExtractVO housingFundRentExtract(String grzh) throws Exception {
        HousingFundExtractVO housingFundExtractVO = housingFundExtractVerification(grzh);
        if (housingFundExtractVO != null) {
            //中心秘钥对中的公钥
            String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";
            //自身秘钥对中的私钥
            String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";

            //从中心申请的自身的组织ID
            String GROUP_ID = "eb4803b9320d427ea4d11df62605ba83";

            //中心系统中保存的可信组织ID列表
            List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605af94", "eb4803b9320d427ea4d11df62605ba83", "1dbc35a1d2b5462998ffb4de753345718");

            //封装数据体中的 head
            Map<String, Object> head = new HashMap<>();
            //中心作为请求端 设置请求方的组织ID
            head.put("groupId", GROUP_ID);
            //请求数据时的时间戳
            head.put("timestamp", System.currentTimeMillis());
            //本次请求的唯一编号 建议使用32位UUID
            head.put("reqId", Util.UUID());

            //封装业务数据
            Map<String, String> map = new HashMap<>();
            //公积金账号
            map.put("grzh", housingFundExtractVO.getGrzh());
            //提取金额
            map.put("sqtqjexx", housingFundExtractVO.getGrzh());
            //个人存款账户号码
            map.put("grckzhhm", housingFundExtractVO.getGrzh());
            //个人存款账户开户银行名称
            map.put("grckzhkhyhmc", housingFundExtractVO.getGrzh());
            //个人存款账户开户银行代码
            map.put("grckzhkhyhdm", housingFundExtractVO.getGrzh());
            //信息来源（政务服务为4）
            map.put("xxly", "4");


            //封装数据体
            Map<String, Object> data = new HashMap<>();
            data.put("data", map);
            data.put("head", head);

            //租房提取
            String url = "http://192.168.35.200:9080/gjjInte/rentalWithdrawal.action";
            log.info("公积金租房提取url>>>"+url);
            String responseData = getGjjData(publicKey, privateKey, GROUP_ID, GROUP_ID_S, data, url);
            if (StringUtils.isEmpty(responseData)) return null;
            JSONObject jsonObject = JSONObject.parseObject(responseData);
            JSONObject result = jsonObject.getJSONObject("result");
            String code = result.getString("code");
            if(StringUtils.isEmpty(code)) return null;
            JSONObject data1 = result.getJSONObject("data");
            log.info("code={"+code+"},data={"+data1+"}");
            return data1 != null && "00".equals(code) ? JSONObject.parseObject(data1.toJSONString(), HousingFundExtractVO.class) : null;
        } else {
            log.info("提取验证失败");
            return null;
        }
    }

    /**
     * 公积金退休提取
     */
    public HousingFundExtractVO housingFundRetireExtract(String grzh) throws Exception {
        HousingFundExtractVO housingFundExtractVO = housingFundExtractVerification(grzh);
        if (housingFundExtractVO != null) {
            //中心秘钥对中的公钥
            String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";
            //自身秘钥对中的私钥
            String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";

            //从中心申请的自身的组织ID
            String GROUP_ID = "eb4803b9320d427ea4d11df62605ba83";

            //中心系统中保存的可信组织ID列表
            List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605af94", "eb4803b9320d427ea4d11df62605ba83", "1dbc35a1d2b5462998ffb4de753345718");

            //封装数据体中的 head
            Map<String, Object> head = new HashMap<>();
            //中心作为请求端 设置请求方的组织ID
            head.put("groupId", GROUP_ID);
            //请求数据时的时间戳
            head.put("timestamp", System.currentTimeMillis());
            //本次请求的唯一编号 建议使用32位UUID
            head.put("reqId", Util.UUID());

            //封装业务数据
            Map<String, String> map = new HashMap<>();
            //公积金账号
            map.put("grzh", housingFundExtractVO.getGrzh());
            //个人存款账户号码
            map.put("grckzhhm", housingFundExtractVO.getGrzh());
            //个人存款账户开户银行名称
            map.put("grckzhkhyhmc", housingFundExtractVO.getGrzh());
            //个人存款账户开户银行代码
            map.put("grckzhkhyhdm", housingFundExtractVO.getGrzh());
            //信息来源（政务服务为4）
            map.put("xxly", "4");


            //封装数据体
            Map<String, Object> data = new HashMap<>();
            data.put("data", map);
            data.put("head", head);

            String url = "http://192.168.35.200:9080/gjjInte/retirementWithdrawal.action";
            log.info("公积金退休提取url>>>"+url);
            String responseData = getGjjData(publicKey, privateKey, GROUP_ID, GROUP_ID_S, data, url);
            if (StringUtils.isEmpty(responseData)) return null;
            JSONObject jsonObject = JSONObject.parseObject(responseData);
            JSONObject result = jsonObject.getJSONObject("result");
            String code = result.getString("code");
            if(StringUtils.isEmpty(code)) return null;
            JSONObject data1 = result.getJSONObject("data");
            log.info("code={"+code+"},data={"+data1+"}");
            return data1 != null && "00".equals(code) ? JSONObject.parseObject(data1.toJSONString(), HousingFundExtractVO.class) : null;
        } else {
            log.info("提取验证失败");
            return null;
        }
    }

    public static void yhSendHttpTest() throws Exception{
        //中心秘钥对中的公钥
        String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";
        //银行秘钥对中的私钥
        String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";

        //中心的组织ID
//        String ZX_GROUP_ID = "eb4803b9320d427ea4d11df62605af94"; //中心组织ID

        //从中心申请的银行的组织ID
        String YH_GROUP_ID = "eb4803b9320d427ea4d11df62605ba83";

        //中心系统中保存的可信银行组织ID列表
        List<String> GROUP_ID_S = Arrays.asList("eb4803b9320d427ea4d11df62605ba83", "eb4803b9320d427ea4d11df62605af94", "1dbc35a1d2b5462998ffb4de75a45718", "1dbc35a1d2b5462998ffb4de753345718");

        //封装数据体中的 head
        Map<String, Object> head = new HashMap<>();
        //设置请求方的组织ID
        head.put("groupId", YH_GROUP_ID);
        //请求数据时的时间戳
        head.put("timestamp", System.currentTimeMillis());
        //本次请求的唯一编号 建议使用32位UUID
        head.put("reqId", Util.UUID());
        log.info("封装数据体中的head》》》》》》》》》》" + head);
        //封装业务数据
        Map<String, String> map = new HashMap<>();
        map.put("xxly", "4");
        map.put("sfzh", "620302196604220857");
//        map.put("xingming", "马勇");
        log.info("封装业务数据》》》》》》》》》》" + map);
        //封装数据体
        Map<String, Object> data = new HashMap<>();
        data.put("data", map);
        data.put("head", head);

        String dataStr = JSON.toJSONString(data);
        //使用中心的公钥 和 自己的私钥 组成SM2
        SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
        log.info("公钥：{"+publicKey+"}");
        log.info("私钥：{"+privateKey+"}");
        //使用中心的公钥进行加密
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
        url = "http://10.10.100.22:8089/gjjInte/grgjjjbxxcx.action";
//        url = "https://10.10.100.22:8089/gjjInte/grgjjjbxxcx.action";
        url = "http://10.10.100.22:9001/gjjInte/grgjjjbxxcx.action";
        log.info("。。。url》》》》" + url);
        HttpResponse httpResponse = HttpRequest.post(url)
                .header("content-type", "application/json")
                .header("GROUP_ID", YH_GROUP_ID)
                .body(body)
                .execute();
        //返回正常
        log.info("httpResponse.isOk()==" + httpResponse.isOk());
        if (httpResponse.isOk() == false) {
            return;
        }
        //银行收到中心返回的数据
        String zxGroupId = httpResponse.header("GROUP_ID");
        //验证返回数据头中组织ID是否合法，这里只做示例验证
        if (org.springframework.util.StringUtils.isEmpty(zxGroupId) || GROUP_ID_S.contains(zxGroupId) == false) {
            throw new Exception("未知来源数据");
        }
        //获取返回的数据
        String responseBody = httpResponse.body();
        log.info("获取返回的数据......."+responseBody);
        //将返回的数据进行JSON解析
        JSONObject responseBodyJson = JSON.parseObject(responseBody);
        log.info("将返回的数据进行JSON解析......."+responseBodyJson);
        //获取加密数据体
        String responseCryptoData = responseBodyJson.getString("cryptoData");
        log.info("获取加密数据体......."+responseCryptoData);
        //获取数据签名
        String responseSign = responseBodyJson.getString("sign");
        log.info("获取数据签名......."+responseSign);
        //使用自己的私钥进行解密

        String responseData = sm2.decryptStr(responseCryptoData, KeyType.PrivateKey);
        log.info("使用私钥进行解密完毕.............."+responseData);
        //使用中心的公钥进行验签
        /*boolean verify = sm2.verify(responseData.getBytes(), HexUtil.decodeHex(responseSign));
        if (verify == false) {
            throw new Exception("签名不一致 数据被篡改");
        }
        log.info("使用中心的公钥进行验签完毕..............");*/
        JSONObject responseDataJson = JSON.parseObject(responseData);
        //获取数据返回时的时间戳
        long responseDataTimestamp = responseDataJson.getJSONObject("head").getLongValue("timestamp");
        //时间超过5分钟 响应数据过期
        if (System.currentTimeMillis() - responseDataTimestamp >= (5 * 60000)) {
            throw new Exception("响应数据过期过期");

        }

        //解析收到中心返回的数据 进行具体业务处理 此处略过...
        log.info("解析收到中心返回的数据:" + responseData);
    }
}
