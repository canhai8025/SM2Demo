package com.jzkj.sm2.demo.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：
 *
 * @Author: zyp
 * @Date: 2021/12/19 13:52
 */
@RestController
@RequestMapping("/api/grcx")
@Slf4j
public class DemoZXController {
	//银行秘钥对中的公钥
	private String publicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEvdscyCoyiO8DCo0uHk/+ek9xFw6ATxfFW8BQVt8uephY7Xq4RePE3w2hKRLcRr9azerMfjsDNhfnyVvAEs13Jw==";
	//中心秘钥对中的私钥
	private String privateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgM3i90EYlhWybdGbnoICV7Zs9CQKjWQnR2TwRMkiCD9ygCgYIKoEcz1UBgi2hRANCAASiS9mcqn9CPR73kh9ColhdWuN6sN/rd/BMgjJtlkVHUZtutkAW7zwqY6+Xm56q4wwTH6X51Q0Y3WPEhPzoE23/";

	//中心的组织ID
	private String ZX_GROUP_ID = "eb4803b9320d427ea4d11df62605af94"; //中心组织ID

	//从中心申请的银行的组织ID
	private String YH_GROUP_ID = "1dbc35a1d2b5462998ffb4de75a45718";

	@PostMapping("/sm2")
	public Object sm2(@RequestBody String body,HttpServletRequest request, HttpServletResponse response) throws Exception {
		//从请求头中获取请求方的组织ID 进行合法性验证
		String yhGroupId = request.getHeader("GROUP_ID");
		if (StringUtils.isEmpty(yhGroupId) || YH_GROUP_ID.equals(yhGroupId) == false){
			throw new Exception("非法请求");
		}
		//解析请求加密数据体JSON
		JSONObject bodyJson = JSON.parseObject(body);
		//获取请求业务数据加密字符串
		String cryptoData = bodyJson.getString("cryptoData");
		//获取请求业务数据签名
		String sign = bodyJson.getString("sign");

		//使用银行的公钥，和中心的私钥 组成SM2
		SM2 sm2 = SmUtil.sm2(Base64.decode(privateKey), Base64.decode(publicKey));
		//使用中心私钥进行解密
		String data = sm2.decryptStr(cryptoData, KeyType.PrivateKey);

		//使用银行公钥进行验签
		boolean verify = sm2.verify(data.getBytes(), HexUtil.decodeHex(sign));
		if (verify == false){
			throw new Exception("签名不一致");
		}
		JSONObject dataJson = JSON.parseObject(data);
		long timestamp = dataJson.getJSONObject("head").getLongValue("timestamp");
		//时间超过5分钟 请求过期
		if (System.currentTimeMillis() - timestamp >= (5*60000)){
			throw new Exception("请求过期");
		}

		//验证通过 开始给银行返回相应的业务数据
		//封装数据体中的 head
		Map<String,Object> headMap = new HashMap<>();
		headMap.put("groupId",ZX_GROUP_ID);   //中心的组织ID
		headMap.put("timestamp",System.currentTimeMillis());
		headMap.put("reqId", dataJson.getJSONObject("head").getString("reqId"));

		//返回给中心的业务数据
		Map<String,String> dataMap = new HashMap<>();
		dataMap.put("dkacountId","123456789");
		dataMap.put("dkamount","300000.00");
		dataMap.put("dkqs","84");
		dataMap.put("dkdatestart","2018-04-12");
		dataMap.put("dkdateend","2048-04-12");
		dataMap.put("dkblane","180000.00");
		dataMap.put("dkfwadress","某某省某某市某某区某某小区");

		//返回给中心的分页数据 如是列表数据 则返回分页信息
		Map<String,Object> pageMap = new HashMap<>();
		pageMap.put("total",1);  //一共多少数据
		pageMap.put("page",1); //当前第几页
		pageMap.put("limit",1); //每页多少条数据
		pageMap.put("pages",1); //一共多少页

		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("code",0);
		resultMap.put("msg","成功");
		resultMap.put("data",dataMap);
		resultMap.put("page",pageMap);

		//封装数据体
		Map<String,Object> responseData = new HashMap<>();
		responseData.put("result",resultMap);
		responseData.put("head",headMap);

		String responseDataStr = JSON.toJSONString(responseData);
		//使用银行的公钥进行加密
		String responseCryptoData = sm2.encryptBcd(responseDataStr, KeyType.PublicKey);
		//使用中心的私钥进行签名 后进行HEX
		String responseSign = HexUtil.encodeHexStr(sm2.sign(responseDataStr.getBytes()));
		//封装数据体
		Map<String,Object> responseBody = new HashMap<>();
		responseBody.put("cryptoData",responseCryptoData);
		responseBody.put("sign",responseSign);

		//设置返回方的组织ID
		response.setHeader("GROUP_ID",ZX_GROUP_ID);
		return responseBody;
	}

}
