package com.jzkj.sm2.demo;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {
    public static void main(String[] args) {
        //使用对面的公钥，和自身的私钥 组成SM2
//        String body = "{\"sign\":\"3046022100c28b49e293f901b9e8c78ab3db09dc877dd45618f531af4c09bf4a4c1fba4d240221008a26d809c597a857307ce7ebc5718624cb5582e6f0b47f6aa8e5557d48413a6b\",\"cryptoData\":\"0455CD302F85E9911F0BBAA77243E12FD0DC1FD54AFB45C5A556AD0BE335C8670CC3640FB4484562399E2D878A00287E2AB17808604FD291025E3E69AB1F44B2B524CF6D41A828DE3A771CA89068A075BD25DAD78854DDA8C0358BE15AB1F1A46CF393A300F4FE4EDA26708C997EEBF410C82F5870B44ED27A31D307BEEF1F87C4CC5E37D2DB1D6EE9AEC11B333AC8216355A22ED51E3A77518FB93B6EB471359B1CA65BBE2402E90C9E3B7DC993A712EE0ABF78A64A302D9EFE1817056F9D409DA20A89CBADD42BA02B931B05F62A6A6979FC56A7C129BDC3EC0050065A6D07B2CBCEA87207CDC4A7372803B11F0F95CA6FDE3BEA0AA66300D82212564F075455DF515CDD3BCF89DEE758ADC8F49CEDBA3FDDD5AD607809CE7C9D237C294D1E07E08A3008F57532911A70955C37F858AD63D2AEE2F9153B80EB20AFB3ECF21BD4AA0EA3C44455036964EDEEE4A79549B81BD7FB25911376E901D74FBCCB5E10705EC9EAEF76AEB9CF8E7A0D310196ABFCA5DAD9579C84FA17E7B145D739755B01372A4256E2482C1189D91279B992405A05CDFE5F8A834A72234B276356DBED1A0FFC5418C98B28CC06162A7FB6E7CC2719D064EC12E29550637934FAA45168316A5F891D51D89A1417FB76F2E6F0DB808F2FD046650E5E305FA34486815EF0F1F6D666C9A1CDA58B91528A541FEA8BC4ABBA2B2E767C8AAEE3BF1BFA2B0F519A7C5476BD8C852839EF7C465C233B21767CC0AD8C242810BEAED5E21200159D2D1B690B5DDC1159A50122B73D63F18921F3FFB4B8B689B103339740353177601EE1A7E87C36E8AB82E2E05A28198D57487CA915848E955CBE9875367239A6B4B0F8D23B721D156FEC7E1EB6D358B9514954CBE7AE140E0A3F3BDED63931B2AA7A0B126F4218FB18D4E3629B4D971B76C5BA1BC439D6\"}";
//        log.info("body>>" + body);
        String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgVmbnhV5d2gOfKOWUvjR1FTA8Fj9x6f4ppaN+2DcgQs+gCgYIKoEcz1UBgi2hRANCAAQczWG4I8p3/cyw6AnsI5mAvHaVsRpOOvDJZUTMY0iMXGSJXS6Em/QWf4dsNI0lF/0dP/R3V1KsKJC901V4EQ41";
        String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEboM143KMjdNkJlm8MlOesCgtKdPFLOqi5vqxxQuTfpnPEy6EeTnmgCnrVO4eIjU3m1Qha33QUmwkDiaWSZXHZg==";
        log.info("publicKey>>" + publicKey);
        log.info("privateKey>>" + privateKey);
        SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));

//        //解析请求加密数据体JSON
//        JSONObject bodyJson = JSON.parseObject(body);
//        //获取请求业务数据加密字符串
//        String cryptoData = bodyJson.getString("cryptoData");
//        log.info("cryptoData>>" + cryptoData);
//        //获取请求业务数据签名
//        String sign = bodyJson.getString("sign");
//        log.info("sign>>" + sign);
//        //使用自身私钥进行解密
//        String data = sm2.decryptStr(cryptoData, KeyType.PrivateKey);
//        log.info("data>>" + data);
//        //使用中心公钥进行验签
//        boolean verify = sm2.verify(data.getBytes(), HexUtil.decodeHex(sign));
//        log.info("verify>>" + verify);
//		/*if (verify == false){
//			throw new Exception("签名不一致");
//		}*/
        //获取返回的数据
        String responseBody = "{\"sign\":\"3046022100c28b49e293f901b9e8c78ab3db09dc877dd45618f531af4c09bf4a4c1fba4d240221008a26d809c597a857307ce7ebc5718624cb5582e6f0b47f6aa8e5557d48413a6b\",\"cryptoData\":\"0455CD302F85E9911F0BBAA77243E12FD0DC1FD54AFB45C5A556AD0BE335C8670CC3640FB4484562399E2D878A00287E2AB17808604FD291025E3E69AB1F44B2B524CF6D41A828DE3A771CA89068A075BD25DAD78854DDA8C0358BE15AB1F1A46CF393A300F4FE4EDA26708C997EEBF410C82F5870B44ED27A31D307BEEF1F87C4CC5E37D2DB1D6EE9AEC11B333AC8216355A22ED51E3A77518FB93B6EB471359B1CA65BBE2402E90C9E3B7DC993A712EE0ABF78A64A302D9EFE1817056F9D409DA20A89CBADD42BA02B931B05F62A6A6979FC56A7C129BDC3EC0050065A6D07B2CBCEA87207CDC4A7372803B11F0F95CA6FDE3BEA0AA66300D82212564F075455DF515CDD3BCF89DEE758ADC8F49CEDBA3FDDD5AD607809CE7C9D237C294D1E07E08A3008F57532911A70955C37F858AD63D2AEE2F9153B80EB20AFB3ECF21BD4AA0EA3C44455036964EDEEE4A79549B81BD7FB25911376E901D74FBCCB5E10705EC9EAEF76AEB9CF8E7A0D310196ABFCA5DAD9579C84FA17E7B145D739755B01372A4256E2482C1189D91279B992405A05CDFE5F8A834A72234B276356DBED1A0FFC5418C98B28CC06162A7FB6E7CC2719D064EC12E29550637934FAA45168316A5F891D51D89A1417FB76F2E6F0DB808F2FD046650E5E305FA34486815EF0F1F6D666C9A1CDA58B91528A541FEA8BC4ABBA2B2E767C8AAEE3BF1BFA2B0F519A7C5476BD8C852839EF7C465C233B21767CC0AD8C242810BEAED5E21200159D2D1B690B5DDC1159A50122B73D63F18921F3FFB4B8B689B103339740353177601EE1A7E87C36E8AB82E2E05A28198D57487CA915848E955CBE9875367239A6B4B0F8D23B721D156FEC7E1EB6D358B9514954CBE7AE140E0A3F3BDED63931B2AA7A0B126F4218FB18D4E3629B4D971B76C5BA1BC439D6\"}";
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


////        SM2 sm2 = SmUtil.sm2(privateKey, publicKey);
////        String cipher = sm2.encryptHex("111111", KeyType.PublicKey);
//        String cryptoData = sm2.encryptBcd("123123", KeyType.PublicKey);
//        log.info("cryptoData="+cryptoData);
//        SM2 sm22 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
//        String plaintext = sm22.decryptStr(cryptoData, KeyType.PrivateKey, CharsetUtil.CHARSET_UTF_8);
//        log.info(plaintext);
    }
}
