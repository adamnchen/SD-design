package com.sutran.sd.sdapi.modules.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sutran.sd.sdapi.mapper.SdUserModelClassifyMapper;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelClassify;
import com.sutran.sd.sdapi.modules.system.SdUserModelClassifyService;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelClassifyVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表服务实现类
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
@Service("sdUserModelClassifyService")
public class SdUserModelClassifyServiceImpl extends ServiceImpl<SdUserModelClassifyMapper, SdUserModelClassify> implements SdUserModelClassifyService {
    @Override
    public List<SdUserModelClassifyVo> selectList(Long userId) {
        List<SdUserModelClassifyVo> list = baseMapper.selectListByUserId(userId);
        if (CollectionUtil.isEmpty(list)) {
            list = new ArrayList<>();
        }
        list.add(0,new SdUserModelClassifyVo().setId("1").setName("全部模型"));
        return list;
    }

    @Override
    public SdUserModelClassify selectById(String id) {
        return baseMapper.selectById(id);
    }
}

