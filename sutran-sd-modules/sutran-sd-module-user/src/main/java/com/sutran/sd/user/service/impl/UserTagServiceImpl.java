package com.sutran.sd.user.service.impl;

import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.constant.TagConstants;
import com.sutran.sd.common.utils.TagNameValidator;
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
        
        // 用户只能创建业务标签，不能创建身份标签
        if (tagDTO.getBizType() != null && TagConstants.isIdentityTag(tagDTO.getBizType())) {
            throw new ServiceException("用户无权创建身份标签，身份标签由系统分配。");
        }
        
        // 验证标签名称，防止用户输入身份标签相关词汇
        TagNameValidator.validate(tagDTO.getTagName());
        
        // 确保设置为业务标签
        tagDTO.setBizType(TagConstants.BUSINESS_TAG);
        
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
        
        // 如果更新了标签名称，需要验证
        if (tagDTO.getTagName() != null && !tagDTO.getTagName().trim().isEmpty()) {
            TagNameValidator.validate(tagDTO.getTagName());
        }
        
        // 用户只能更新业务标签，不能修改标签的业务类型
        // TagUpdateDTO 不包含 bizType 字段，这是正确的设计
        
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
