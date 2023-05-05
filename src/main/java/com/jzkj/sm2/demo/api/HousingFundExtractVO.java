package com.jzkj.sm2.demo.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公积金租房/退休提取VO
 *
 * @author hcanhai
 * @since 2023/03/30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousingFundExtractVO {

    /**
     * 个人账号
     */
    private String grzh;
    /**
     * 信息来源（固定4）
     */
    private String xxly;
    /**
     * 单位账户
     */
    private String dwzh;
    /**
     * 个人存款账户号码
     */
    private String grckzhhm;
    /**
     * 个人存款账户开户银行名称
     */
    private String grckzhkhyhmc;
    /**
     * 个人存款账户开户银行代码
     */
    private String grckzhkhyhdm;

    /**
     * 个人账户余额
     */
    private String grzhye;

    /**
     * 状态
     */
    private String grzhztmc;
    /**
     * 缴至年月
     */
    private String jzny;
    /**
     * 姓名
     */
    private String xingming;
    /**
     * 证件号码
     */
    private String zjhm;
    /**
     * 提取金额
     */
    private String sqtqjexx;

    /**
     * 提取编号
     */
    private String tqbh;





}
