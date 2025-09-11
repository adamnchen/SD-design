package com.sutran.sd.draw.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.draw.mapper.SdUserModelClassifyMapper;
import com.sutran.sd.draw.domain.SdUserModelClassify;
import com.sutran.sd.draw.service.SdUserModelClassifyService;
import com.sutran.sd.draw.domain.vo.SdUserModelClassifyVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表服务实现类
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
@Service
@RequiredArgsConstructor
public class SdUserModelClassifyServiceImpl implements SdUserModelClassifyService {

    private final SdUserModelClassifyMapper baseMapper;

    /**
     * 获取用户模型分类列表
     * @param userId 用户ID
     * @return 用户模型分类列表
     */
    @Override
    public List<SdUserModelClassifyVo> selectList(Long userId) {
        List<SdUserModelClassifyVo> list = baseMapper.selectListByUserId(userId);
        if (CollectionUtil.isEmpty(list)) {
            list = new ArrayList<>();
        }
        list.add(0,new SdUserModelClassifyVo().setId("1").setName("全部模型"));
        return list;
    }

    /**
     * 获取用户模型分类详情
     * @param id 分类ID
     * @return 用户模型分类详情
     */
    @Override
    public SdUserModelClassify selectById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void insert(SdUserModelClassify entity) {
        baseMapper.insert(entity);
    }

    @Override
    public void updateById(SdUserModelClassify entity) {
        baseMapper.updateById(entity);
    }

    @Override
    public void removeById(String id) {
        baseMapper.deleteById(id);
    }
}

