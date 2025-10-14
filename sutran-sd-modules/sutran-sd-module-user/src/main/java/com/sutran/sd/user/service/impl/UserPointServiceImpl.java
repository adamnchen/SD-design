package com.sutran.sd.user.service.impl;

import com.sutran.sd.common.core.domain.dto.UserPointUpdateDTO;
import com.sutran.sd.common.core.domain.entity.SdUserNormalPoint;
import com.sutran.sd.common.core.domain.vo.UserPointDetailVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.user.mapper.SdUserNormalPointMapper;
import com.sutran.sd.user.service.IUserPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户积分服务实现类
 *
 * @author sutran
 * @date 2025-10-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointServiceImpl implements IUserPointService {

    private final SdUserNormalPointMapper userNormalPointMapper;




    @Override
    @Transactional
    public boolean updateUserPoint(UserPointUpdateDTO updateDTO) {
        if (updateDTO == null || updateDTO.getUserId() == null) {
            throw new ServiceException("积分更新信息不能为空");
        }

        // 确保用户积分记录存在
        ensureUserPointExists(updateDTO.getUserId());

        int result = 0;
        if ("PROOFING".equals(updateDTO.getPointType())) {
            result = userNormalPointMapper.updateProofingPoint(updateDTO.getUserId(), updateDTO.getPointChange());
        } else if ("SALE".equals(updateDTO.getPointType())) {
            result = userNormalPointMapper.updateSalePoint(updateDTO.getUserId(), updateDTO.getPointChange());
        } else {
            throw new ServiceException("不支持的积分类型：" + updateDTO.getPointType());
        }

        if (result > 0) {
            log.info("用户 {} 的 {} 积分更新成功，变化量：{}",
                    updateDTO.getUserId(), updateDTO.getPointType(), updateDTO.getPointChange());
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean updatePointByOrder(Long userId, Long orderId, String orderType, String orderStatus) {
        if (userId == null || orderId == null) {
            throw new ServiceException("用户ID和订单ID不能为空");
        }

        // 根据订单类型和状态计算积分
        Integer pointChange = calculatePointByOrder(orderType, orderStatus);
        if (pointChange == null || pointChange == 0) {
            log.info("订单 {} 不需要更新积分", orderId);
            return true;
        }

        String pointType = "PROOFING".equals(orderType) ? "PROOFING" : "SALE";
        String reason = String.format("完成%s订单", "PROOFING".equals(orderType) ? "打样" : "预售");

        UserPointUpdateDTO updateDTO = new UserPointUpdateDTO();
        updateDTO.setUserId(userId);
        updateDTO.setPointType(pointType);
        updateDTO.setPointChange(pointChange);
        updateDTO.setReason(reason);
        updateDTO.setOrderId(orderId);
        updateDTO.setOrderType(orderType);

        return updateUserPoint(updateDTO);
    }



    /**
     * 确保用户积分记录存在
     */
    private void ensureUserPointExists(Long userId) {
        SdUserNormalPoint existing = userNormalPointMapper.selectById(userId);
        if (existing == null) {
            createDefaultUserPoint(userId);
        }
    }

    /**
     * 创建默认用户积分记录
     */
    private void createDefaultUserPoint(Long userId) {
        SdUserNormalPoint userPoint = new SdUserNormalPoint();
        userPoint.setUserId(userId);
        userPoint.setProofingPoint(0);
        // userPoint.setPriifingLevel(0); // 注释掉，因为字段名可能有拼写错误
        userPoint.setSalePoint("0");
        userPoint.setSaleLevel("1级-信誉入门");

        int result = userNormalPointMapper.insert(userPoint);
        if (result <= 0) {
            throw new ServiceException("创建用户积分记录失败");
        }
        log.info("为用户 {} 创建默认积分记录", userId);
    }


    /**
     * 根据订单计算积分
     */
    private Integer calculatePointByOrder(String orderType, String orderStatus) {
        if (!"COMPLETED".equals(orderStatus)) {
            return 0; // 只有完成的订单才给积分
        }

        // 打样邀约每次5分，销售每次10分
        if ("PROOFING".equals(orderType)) {
            return 5;
        } else if ("PRESALE".equals(orderType)) {
            return 10;
        }

        return 0;
    }
}
