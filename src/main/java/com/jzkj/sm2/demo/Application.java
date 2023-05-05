package com.jzkj.sm2.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 功能描述：
 *
 * @Author: zyp
 * @Date: 2021/12/19 13:51
 */
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		try {
			SpringApplication.run(Application.class, args);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
