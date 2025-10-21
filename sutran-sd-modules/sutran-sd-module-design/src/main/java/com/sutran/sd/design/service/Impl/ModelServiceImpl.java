package com.sutran.sd.design.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sutran.sd.common.core.domain.PageQuery;
import com.sutran.sd.common.core.domain.entity.Model;
import com.sutran.sd.common.core.page.TableDataInfo;
import com.sutran.sd.common.exception.ServiceException;
import com.sutran.sd.common.helper.LoginHelper;
import com.sutran.sd.design.mapper.ModelMapper;
import com.sutran.sd.design.service.IModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模型服务实现类
 *
 * @author SutranSD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements IModelService {

    private final ModelMapper modelMapper;

    @Override
    public TableDataInfo<Model> getUserModelPage(PageQuery pageQuery, String modelType, Integer isUserDel) {
        String currentUserId = LoginHelper.getUserId().toString();
        log.info("分页查询用户 {} 的模型列表，模型类型：{}，删除状态：{}", currentUserId, modelType, isUserDel);

        IPage<Model> page = modelMapper.selectUserModelPage(pageQuery.build(), currentUserId, modelType, isUserDel);

        // 手动设置总数，确保分页正确
        Long total = modelMapper.countUserModels(currentUserId, modelType, isUserDel);
        page.setTotal(total);

        log.info("查询完成，共找到 {} 个模型", total);
        return TableDataInfo.build(page);
    }

    @Override
    public List<Model> getUserModelList(String modelType, Integer isUserDel) {
        String currentUserId = LoginHelper.getUserId().toString();
        log.info("查询用户 {} 的模型列表，模型类型：{}，删除状态：{}", currentUserId, modelType, isUserDel);

        List<Model> models = modelMapper.selectUserModelList(currentUserId, modelType, isUserDel);

        log.info("查询完成，共找到 {} 个模型", models.size());
        return models;
    }

    @Override
    public Model getUserModelById(Long id) {
        if (id == null) {
            throw new ServiceException("模型ID不能为空");
        }

        String currentUserId = LoginHelper.getUserId().toString();
        log.info("查询用户 {} 的模型详情，模型ID：{}", currentUserId, id);

        Model model = modelMapper.selectUserModelById(id, currentUserId);
        if (model == null) {
            throw new ServiceException("模型不存在或您无权查看该模型");
        }

        log.info("查询完成，模型标题：{}", model.getTitle());
        return model;
    }

    @Override
    public Long countUserModels(String modelType, Integer isUserDel) {
        String currentUserId = LoginHelper.getUserId().toString();
        log.info("统计用户 {} 的模型数量，模型类型：{}，删除状态：{}", currentUserId, modelType, isUserDel);

        Long count = modelMapper.countUserModels(currentUserId, modelType, isUserDel);

        log.info("统计完成，共 {} 个模型", count);
        return count;
    }

    @Override
    public boolean deleteUserModel(Long id) {
        if (id == null) {
            throw new ServiceException("模型ID不能为空");
        }

        String currentUserId = LoginHelper.getUserId().toString();
        log.info("删除用户 {} 的模型，模型ID：{}", currentUserId, id);

        // 先检查模型是否存在且属于当前用户
        Model model = modelMapper.selectUserModelById(id, currentUserId);
        if (model == null) {
            throw new ServiceException("模型不存在或您无权操作该模型");
        }

        int rows = modelMapper.deleteUserModel(id, currentUserId);
        boolean success = rows > 0;

        if (success) {
            log.info("删除成功，模型ID：{}", id);
        } else {
            log.warn("删除失败，模型ID：{}", id);
        }

        return success;
    }
}
