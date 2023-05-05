package com.jzkj.sm2.demo;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
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

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：
 *
 * @Author: zyp
 * @Date: 2021/12/19 13:52
 */
@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SM2Test {

	//中心秘钥对中的私钥
	private String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgM3i90EYlhWybdGbnoICV7Zs9CQKjWQnR2TwRMkiCD9ygCgYIKoEcz1UBgi2hRANCAASiS9mcqn9CPR73kh9ColhdWuN6sN/rd/BMgjJtlkVHUZtutkAW7zwqY6+Xm56q4wwTH6X51Q0Y3WPEhPzoE23/";

	//银行秘钥对中的公钥
	private String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEvdscyCoyiO8DCo0uHk/+ek9xFw6ATxfFW8BQVt8uephY7Xq4RePE3w2hKRLcRr9azerMfjsDNhfnyVvAEs13Jw==";
	//中心组织ID
	private String ZX_GROUP_ID = "eb4803b9320d427ea4d11df62605af94";

	//中心系统中保存的可信银行组织ID列表
	private List<String> GROUP_ID_S = Arrays.asList("1dbc35a1d2b5462998ffb4de75a45718","1dbc35a1d2b5462998ffb4de753345718");
	//测试由中心向银行发送请求
	@Test
	public void sendHttpTest() throws Exception {
		//封装数据体中的 head
		Map<String,Object> head = new HashMap<>();
		//中心作为请求端 设置请求方的组织ID
		head.put("groupId",ZX_GROUP_ID);
		//请求数据时的时间戳
		head.put("timestamp",System.currentTimeMillis());
		//本次请求的唯一编号 建议使用32位UUID
		head.put("reqId",Util.UUID());

		//封装业务数据
		Map<String,String> map = new HashMap<>();
		map.put("jkrIdtype","01");
		map.put("jkrIdcard","411081198606069896");
		map.put("jkrname","张三");

		//封装数据体
		Map<String,Object> data = new HashMap<>();
		data.put("data",map);
		data.put("head",head);

		String dataStr = JSON.toJSONString(data);
		//使用对应银行的公钥 和 中心自己的私钥 组成SM2
		SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
		//使用银行的公钥进行加密
		String cryptoData = sm2.encryptBcd(dataStr, KeyType.PublicKey);
		//使用自己的私钥进行签名 后进行HEX
		String sign = HexUtil.encodeHexStr(sm2.sign(dataStr.getBytes()));

		//封装加密后数据体
		Map<String,Object> testBody = new HashMap<>();
		testBody.put("cryptoData",cryptoData);
		testBody.put("sign",sign);

		//将最终数据转换为json字符串
		String body = JSON.toJSONString(testBody);
		log.info("密文发送包："+body);
		//使用HTTP请求发送数据包  https://xxxx:9080/gjjInte/grgjjjbxxcx.action
		HttpResponse httpResponse = HttpRequest.post("http://127.0.0.1:8080//api/jzkj/sm2")
				.header("content-type", "application/json")
				.header("GROUP_ID", ZX_GROUP_ID)
				.body(body)
				.execute();
		//返回正常
		if (httpResponse.isOk() == false){
			return;
		}
		//中心收到银行返回的数据
		String yhGroupId = httpResponse.header("GROUP_ID");
		//验证返回数据头中组织ID是否合法，这里只做示例验证
		if (StringUtils.isEmpty(yhGroupId) || GROUP_ID_S.contains(yhGroupId) == false){
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

		//使用中心自身私钥进行解密
		String responseData = sm2.decryptStr(responseCryptoData, KeyType.PrivateKey);
		//使用对应银行的公钥进行验签
		boolean verify = sm2.verify(responseData.getBytes(), HexUtil.decodeHex(responseSign));
		if (verify == false){
			throw new Exception("签名不一致 数据被篡改");
		}
		JSONObject responseDataJson = JSON.parseObject(responseData);
		//获取数据返回时的时间戳
		long responseDataTimestamp = responseDataJson.getJSONObject("head").getLongValue("timestamp");
		//时间超过5分钟 响应数据过期
		if (System.currentTimeMillis() - responseDataTimestamp >= (5*60000)){
			throw new Exception("响应数据过期过期");
		}

		//解析收到银行返回的数据 进行具体业务处理 此处略过...
		log.info("解析收到银行返回的数据: "+responseData);
	}


	/**
	 * 生成SM2公私钥
	 */
	@Test
	public void sm2(){
		KeyPair pair = SecureUtil.generateKeyPair("SM2");
		byte[] privateKeyArray = pair.getPrivate().getEncoded();
		byte[] publicKeyArray = pair.getPublic().getEncoded();
//		String pr = SecureUtil.h;//
		String privateKeyBase64 = Base64.encode(privateKeyArray);
		//生成公钥
		String publicKeyBase64 = Base64.encode(publicKeyArray);

		System.out.println("随机生成的公钥 "+publicKeyBase64);
		System.out.println("随机生成的私钥 "+privateKeyBase64);
	}
}
