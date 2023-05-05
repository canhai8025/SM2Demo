package com.jzkj.sm2.demo.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 公积金查询
 *
 * @author hcanhai
 * @since 2023/03/30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousingFundQueryVO {

    /**
     * 证件号码
     */
    private String sfzh;
    /**
     * 证件类型
     */
    private String zjlx;

    /**
     * 单位名称
     */
    private String dwmc;

    /**
     * 个人账户余额
     */
    private String grzhye;
    /**
     * 个人账号
     */
    private String grzh;
    /**
     * 手机号码
     */
    private String sjhm;
    /**
     * 缴至年月YYYY-MM
     */
    private String jzny;
    /**
     * 个人存款账号号码
     */
    private String grckzhhm;
    /**
     * 证件号码
     */
    private String zjhm;
    /**
     * 单位月缴额
     */
    private String dwyjce;
    /**
     * 开户日期YYYY-MM-DD
     */
    private String khrq;
    /**
     * 姓名
     */
    private String xingming;
    /**
     * 个人账户状态
     */
    private String grzhzt;
    /**
     * 个人存款款账户开户银行名称
     */
    private String grckzhkhyhmc;
    /**
     * 出生年月YYYY-MM
     */
    private String csny;
    /**
     * 个人缴存比例
     */
    private String grjcbl;
    /**
     * 单位缴存比例
     */
    private String dwjcbl;
    /**
     * 个人缴存基数
     */
    private String grjcjs;
    /**
     * 个人缴存金额
     */
    private String gryjce;

    /**
     * 信息来源（固定4）
     */
    private String xxly;

}
