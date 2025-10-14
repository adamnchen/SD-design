package com.sutran.sd.user.service;

import com.sutran.sd.common.core.domain.dto.UserPointUpdateDTO;
import com.sutran.sd.common.core.domain.vo.UserPointDetailVO;


/**
 * 用户积分服务接口
 *
 * @author sutran
 * @date 2025-10-14
 */
public interface IUserPointService {



    /**
     * 更新用户积分
     *
     * @param updateDTO 积分更新信息
     * @return 是否成功
     */
    boolean updateUserPoint(UserPointUpdateDTO updateDTO);

    /**
     * 根据订单完成情况自动更新积分
     *
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderType 订单类型：PROOFING-打样订单，PRESALE-预售订单
     * @param orderStatus 订单状态：COMPLETED-完成，CANCELLED-取消
     * @return 是否成功
     */
    boolean updatePointByOrder(Long userId, Long orderId, String orderType, String orderStatus);
}
