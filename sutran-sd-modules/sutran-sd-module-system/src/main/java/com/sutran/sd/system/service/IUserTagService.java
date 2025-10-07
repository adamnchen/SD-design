package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户标签管理服务
 * 注意：此服务已从 ISysUserTagService 重命名，明确为用户业务功能
 *
 * @author SutranSD
 */
public interface IUserTagService {

    /**
     * 添加标签
     * @param userId 用户ID
     * @param tagDTO 标签信息
     */
    void addTag(Long userId, @Valid UserTagDTO tagDTO);

    /**
     * 获取用户标签列表
     * @param userId 用户ID
     * @return 标签列表
     */
    List<TagDetailVO> selectUserTagList(Long userId);

    /**
     * 获取标签详情
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 标签详情
     */
    TagDetailVO selectTagDetailById(Long userId, Long tagId);

    /**
     * 更新标签
     * @param userId 用户ID
     * @param tagDTO 标签信息
     */
    void updateTag(Long userId, @Valid TagUpdateDTO tagDTO);

    /**
     * 删除标签
     * @param userId 用户ID
     * @param tagId 标签ID
     */
    void deleteTagById(Long userId, Long tagId);
}
