package com.sutran.sd.common.core.service;

import com.sutran.sd.common.core.domain.entity.PayMember;

import java.util.Date;

/**
 * 通用 用户服务
 *
 * @author Lion Li
 */
public interface UserService {

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    String selectUserNameById(Long userId);

    /**
     * 获取当前用户的微信OpenId
     * @param userId 用户ID
     * @return 微信OpenId
     */
    String selectOpenIdById(Long userId);

    /**
     * 获取当前用户的剩余训练次数
     * @param userId 用户ID
     * @return 剩余训练次数
     */
    Integer selectTrainTimesById(Long userId);

    /**
     * 获取当前用户的剩余绘图图片数量
     * @param userId 用户ID
     * @return 剩余绘图图片数量
     */
    Integer selectDrawNumById(Long userId);

    /**
     * 扣除训练次数
     * @param userId 用户ID
     */
    void deductedTrainTimes(Long userId);

    /**
     * 归还训练次数
     * @param userId 用户ID
     */
    void returnedTrainTimes(Long userId);

    /**
     * 扣除绘图图片数量
     * @param userId 用户ID
     */
    void deductedDrawNum(Long userId, int num);

    /**
     * 归还绘图次数
     * @param userId 用户ID
     */
    void returnedDrawNum(Long userId, int num);

    /**
     * 根据userId获取channelUserId
     * @param userId 用户ID
     * @return channelUserId
     */
    String selectChannelUserIdById(Long userId);

    /**
     * 根据手机号查询用户ID
     * @param phone 手机号
     * @return 用户ID
     */
    String selectUserIdByPhone(String phone);

    /**
     * 新增会员
     *
     * @param userId     用户ID
     * @param businessId 会员配置ID
     * @param startTime  开始时间
     * @param payMember  会员信息
     * @param outTradeNo 订单号
     */
    void insertMember(Long userId, Long businessId, Date startTime, PayMember payMember, String outTradeNo);

    /**
     * 获取当前用户已购买且处于生效中的会员ID
     * @param userId 用户ID
     * @param now 当前时间
     * @return 会员ID
     */
    String selectMemberIdByUserId(Long userId, Date now);

    /**
     * 校验用户会员是否有足够的绘图数量 并 扣除本次绘图数量
     * @param userId 用户ID
     * @param drawNum 绘图数量
     */
    void checkDrawNumOfMember(Long userId, Integer drawNum);
}
