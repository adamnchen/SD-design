package com.sutran.sd.user.service.impl;

import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.system.mapper.SysUserTagMapper;
import com.sutran.sd.system.service.ISysUserTagService;
import com.sutran.sd.user.service.IUserTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户标签管理服务实现
 *
 * @author SutranSD
 */
@Slf4j
@RequiredArgsConstructor
@Service("userTagService")
public class UserTagServiceImpl implements IUserTagService {

    private final ISysUserTagService sysUserTagService;

    @Override
    public void addTag(Long userId, @Valid UserTagDTO tagDTO) {
        if (userId == null) {
            throw new ServiceException("用户未登录，操作失败。");
        }
        sysUserTagService.addTag(userId, tagDTO);
    }

    @Override
    public List<TagDetailVO> selectUserTagList(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户未登录或Token无效。");
        }
        return sysUserTagService.selectUserTagList(userId);
    }

    @Override
    public TagDetailVO selectTagDetailById(Long userId, Long tagId) {
        if (userId == null) {
            throw new ServiceException("用户未登录或Token无效。");
        }
        return sysUserTagService.selectTagDetailById(userId, tagId);
    }

    @Override
    public void updateTag(Long userId, @Valid TagUpdateDTO tagDTO) {
        if (userId == null) {
            throw new ServiceException("用户未登录或Token无效。");
        }
        sysUserTagService.updateTag(userId, tagDTO);
    }

    @Override
    public void deleteTagById(Long userId, Long tagId) {
        if (userId == null) {
            throw new ServiceException("用户未登录或Token无效。");
        }
        sysUserTagService.deleteTagById(userId, tagId);
    }
}
