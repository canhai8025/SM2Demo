package com.jzkj.sm2.demo;

import java.util.UUID;

/**
 * 功能描述：
 *
 * @Author: zyp
 * @Date: 2021/12/19 14:07
 */
public class Util {

	/**
	 * 生成32位UUID
	 * @return
	 */
	public static String UUID(){
		String uuid = UUID.randomUUID().toString().replaceAll("-","");
		return uuid;
	}
}
