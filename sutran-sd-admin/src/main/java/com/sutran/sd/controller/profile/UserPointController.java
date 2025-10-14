package com.sutran.sd.controller.profile;

import com.sutran.sd.common.core.domain.R;
import com.sutran.sd.common.core.domain.dto.UserPointUpdateDTO;
import com.sutran.sd.common.core.domain.vo.UserPointDetailVO;
import com.sutran.sd.user.service.IUserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 用户积分控制器
 *
 * @author sutran
 * @date 2025-10-14
 */
@RestController
@RequestMapping("/profile/points")
@RequiredArgsConstructor
public class UserPointController {

    private final IUserPointService userPointService;




    /**
     * 更新用户积分（管理员功能）
     */
    @PostMapping("/update")
    public R<Void> updateUserPoint(@Validated @RequestBody UserPointUpdateDTO updateDTO) {
        try {
            boolean success = userPointService.updateUserPoint(updateDTO);
            if (success) {
                return R.ok("积分更新成功");
            } else {
                return R.fail("积分更新失败");
            }
        } catch (Exception e) {
            return R.fail("积分更新失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单完成情况更新积分
     */
    @PostMapping("/update-by-order")
    public R<Void> updatePointByOrder(@RequestParam Long userId,
                                     @RequestParam Long orderId,
                                     @RequestParam String orderType,
                                     @RequestParam String orderStatus) {
        try {
            boolean success = userPointService.updatePointByOrder(userId, orderId, orderType, orderStatus);
            if (success) {
                return R.ok("根据订单更新积分成功");
            } else {
                return R.fail("根据订单更新积分失败");
            }
        } catch (Exception e) {
            return R.fail("根据订单更新积分失败：" + e.getMessage());
        }
    }


}
