package com.sutran.sd.sdapi.modules.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sutran.sd.sdapi.modules.system.entity.SdUserModelClassify;
import com.sutran.sd.sdapi.modules.system.vo.SdUserModelClassifyVo;

import java.util.List;

/**
 * SD绘图 || 用户模型分类(SdUserModelClassify)表服务接口
 *
 * @author makejava
 * @since 2024-03-10 20:54:54
 */
public interface SdUserModelClassifyService extends IService<SdUserModelClassify> {

    List<SdUserModelClassifyVo> selectList(Long userId);

    SdUserModelClassify selectById(String id);
}

