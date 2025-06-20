package com.sutran.sd.common.core.service;

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
     * 扣除培训次数
     * @param userId 用户ID
     */
    void deductedTrainTimes(Long userId);

    /**
     * 扣除绘图图片数量
     * @param userId 用户ID
     */
    void deductedDrawNum(Long userId, int num);

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
}
