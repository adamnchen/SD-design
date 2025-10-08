package com.sutran.sd.system.service;

import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.entity.SysAddressArea;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;

import javax.validation.Valid;
import java.util.List;

public interface ISysUserTagService {

    void addTag(Long userId, @Valid UserTagDTO tagDTO);

    List<TagDetailVO> selectUserTagList(Long userId);

    TagDetailVO selectTagDetailById(Long userId, Long tagId);

    void updateTag(Long userId, @Valid TagUpdateDTO tagDTO);

    void deleteTagById(Long userId, Long tagId);

    /**
     * 根据标签类型获取用户标签列表
     * @param userId 用户ID
     * @param bizType 标签类型（0=身份标签，1=业务标签）
     * @return 标签列表
     */
    List<TagDetailVO> selectUserTagListByType(Long userId, Integer bizType);
}
