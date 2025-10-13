package com.sutran.sd.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.common.core.domain.dto.UserTagDTO;
import com.sutran.sd.common.core.domain.entity.SysUserTag;
import com.sutran.sd.common.core.domain.dto.TagUpdateDTO;
import com.sutran.sd.common.core.domain.vo.TagDetailVO;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.constant.TagConstants;
import com.sutran.sd.system.mapper.SysUserTagMapper;
import com.sutran.sd.system.service.ISysUserTagService; // Service 接口
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("sysUserTagService")

public class SysUserTagServiceImpl
        extends ServiceImpl<SysUserTagMapper, SysUserTag>
        implements ISysUserTagService {



    @Override
    public void addTag(Long userId, UserTagDTO tagDTO) {
        // 逻辑正确，无需修改
        long count = this.count(new LambdaQueryWrapper<SysUserTag>()
                .eq(SysUserTag::getUserId, userId)
                .eq(SysUserTag::getTagName, tagDTO.getTagName()));

        if (count > 0) {
            throw new ServiceException("标签名称已存在，请勿重复创建。");
        }

        SysUserTag tag = new SysUserTag();
        BeanUtils.copyProperties(tagDTO, tag);

        tag.setUserId(userId);

        if (tag.getBizType() == null) tag.setBizType(TagConstants.DEFAULT_BIZ_TYPE);
        if (tag.getTagLevel() == null) tag.setTagLevel(TagConstants.DEFAULT_TAG_LEVEL);

        this.save(tag);
    }


    @Override
    public void deleteTagById(Long userId, Long tagId) {
        // 使用物理删除，彻底删除记录
        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTag::getTagId, tagId)
                .eq(SysUserTag::getUserId, userId);

        boolean removed = this.remove(wrapper);

        if (!removed) {
            throw new ServiceException("标签不存在或无权限删除。");
        }
    }

    @Override
    public void updateTag(Long userId, TagUpdateDTO tagDTO) {
        // 逻辑正确，无需修改
        SysUserTag updateEntity = new SysUserTag();
        BeanUtils.copyProperties(tagDTO, updateEntity);

        LambdaUpdateWrapper<SysUserTag> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUserTag::getTagId, tagDTO.getTagId())
                .eq(SysUserTag::getUserId, userId);

        boolean updated = this.update(updateEntity, wrapper);

        if (!updated) {
            throw new ServiceException("标签不存在或无权限修改。");
        }
    }


    @Override
    public List<TagDetailVO> selectUserTagList(Long userId) {
        // 逻辑正确，无需修改
        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTag::getUserId, userId)
               .eq(SysUserTag::getBizType, 3)
                .orderByAsc(SysUserTag::getSortOrder)
                .orderByAsc(SysUserTag::getCreateTime);


        List<SysUserTag> tagEntities = this.list(wrapper);

        return tagEntities.stream()
                .map(entity -> {
                    TagDetailVO vo = new TagDetailVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public TagDetailVO selectTagDetailById(Long userId, Long tagId) {
        // 逻辑正确，无需修改
        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTag::getTagId, tagId)
                .eq(SysUserTag::getUserId, userId);

        SysUserTag tagEntity = this.getOne(wrapper);

        if (tagEntity == null) {
            throw new ServiceException("标签不存在或无权限查看。");
        }

        TagDetailVO vo = new TagDetailVO();
        BeanUtils.copyProperties(tagEntity, vo);

        return vo;
    }

    @Override
    public List<TagDetailVO> selectUserTagListByType(Long userId, Integer bizType) {
        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTag::getUserId, userId)
                .eq(SysUserTag::getBizType, bizType)
                .orderByAsc(SysUserTag::getSortOrder)
                .orderByAsc(SysUserTag::getCreateTime);

        List<SysUserTag> tagEntities = this.list(wrapper);

        return tagEntities.stream()
                .map(entity -> {
                    TagDetailVO vo = new TagDetailVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
