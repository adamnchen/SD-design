package com.sutran.sd.draw.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.sutran.sd.common.core.service.OssService;
import com.sutran.sd.common.utils.file.FileUtils;
import com.sutran.sd.oss.core.OssClient;
import com.sutran.sd.oss.entity.UploadResult;
import com.sutran.sd.oss.factory.OssFactory;
import com.sutran.sd.draw.domain.vo.SdApiResult;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

import static com.sutran.sd.draw.constants.CommonKey.JPG;
import static com.sutran.sd.draw.constants.CommonKey.SD;

/**
 * @author zj
 * @date 2024-02-27
 */
@SuppressWarnings("unchecked")
@Slf4j
public class ResultUtil {

    public static SdApiResult apiToResult(Map<String,Object> result, OssService ossService, String userName, boolean isTest, String gridsUrl, String viewGridsPath) {
        List<String> urlList = new ArrayList<>();
        SdApiResult rs = new SdApiResult();
        if (!isTest) {
            OssClient storage = OssFactory.instance();
            Object o = result.get("images");
            if (o == null) {
                o = result.get("image");
            }
            if (o instanceof List) {
                List<String> imgs = (List<String>) o;
                for (String img : imgs) {
                    rs.addImage(img);
                }
            }
            Object info = result.get("info");
            if (info instanceof String) {
                rs.info.putAll(Objects.requireNonNull(JSONUtil.parseObj(String.valueOf(info))));
            }

            Object parameters = result.get("parameters");
            if (parameters != null) {
                if (parameters instanceof String) {
                    rs.parameters.putAll(Objects.requireNonNull(JSONUtil.parseObj(String.valueOf(parameters))));
                } else if (parameters instanceof Map) {
                    rs.parameters.putAll((Map<? extends String, ?>) parameters);
                }
            }

            for (String image : rs.getImages()) {
                InputStream oldInputStream = FileUtils.base64ToInputStream(image);
                InputStream newInputStream = FileUtils.compressPic(oldInputStream, 0.7);

                UploadResult uploadResult = storage.uploadSuffix(newInputStream,JPG,"image/jpeg");
                urlList.add(uploadResult.getUrl());
                ossService.insertOssData(SD+DateUtil.format(new Date(),"yyyyMMdd")+"_"+ IdUtil.getSnowflakeNextIdStr()+JPG,JPG,storage.getConfigKey(),uploadResult.getUrl(),uploadResult.getFilename(),userName);
            }
            rs.getImages().clear();
        }
        else {
            // 从grids目录中获取所有子文件
            File file = FileUtils.getGridFile(gridsUrl);
            try {
                String viewGridsUrl = viewGridsPath + "/" + file.getName();
                urlList.add(viewGridsUrl);
                log.warn("xyz plot完成>>>>>>>>grid图片原始地址：{},访问地址：{}",file.getPath(),viewGridsUrl);
                FileUtil.del(file);
            } catch (Exception e) {
                log.error("xyz plot完成>>>>>>>>>grid图片处理异常：{}",e.getMessage());
            }
        }
        rs.getImages().addAll(urlList);
        return rs;
    }

}
