package com.sutran.sd.draw.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import com.sutran.sd.draw.domain.dto.train.SdTrainLoraDto;
import com.sutran.sd.draw.domain.vo.TrianImgDataVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zj
 * @date 2025年06月22日 10:33
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Slf4j
public class CommonUtil {

    /** 获取训练文件夹名称 **/
    public static String suggestNumRepeat() {
        return "/20_zkz";
    }

    /** 获取指定目录下的全部文件（图片文件 和 图片标签参数文件） **/
    public static List<File> getAllFile(File preImgDir) {
        // 获取文件列表
        File[] fileList = preImgDir.listFiles();
        // 只要png、jpg、jpeg、txt文件
        assert fileList != null;
        // 如果是文件则将其加入到文件数组中
        return Arrays.stream(fileList).filter(e ->
            e.getName().contains(".jpg") || e.getName().contains(".JPG") ||
                e.getName().contains(".png") || e.getName().contains(".PNG") ||
                e.getName().contains(".jpeg") || e.getName().contains(".JPEG") || e.getName().contains(".txt")
        ).collect(Collectors.toList());
    }

    /** 获取指定目录下的全部文件（图片文件 和 图片标签参数文件）并分组 **/
    public static Map<String, List<File>> getAllFileAndGroup(File preImgDir) {
        List<File> allFileList = getAllFile(preImgDir);
        // 将数据分组
        return allFileList.stream().collect(Collectors.groupingBy(e -> e.getName()
            .replace(".jpg", "").replace(".JPG", "")
            .replace(".jpeg", "").replace(".JPEG", "")
            .replace(".png", "").replace(".PNG", "").replace(".txt", "")));
    }

    /** 读取txt文件中的标签数组 和 图片路径 **/
    public static List<TrianImgDataVo> readTagFromTxtAndImgUrl(Map<String, List<File>> group, List<String> wjValueList) {
        List<TrianImgDataVo> results = new ArrayList<>();
        if (CollectionUtil.isEmpty(group)) {
            return results;
        }
        // 遍历分组，将图片和标签封装到对象中：正常情况下是，一个图片对应一个标签文件
        group.forEach((key,files)->{
            TrianImgDataVo imgDataVo = new TrianImgDataVo();
            // 因为图片打标签后，一个图片对应一个标签文件且是同名，且txt文件一定是在图片文件后面，所以这里只取第一个文件作为图片路径
            File jpgFile = files.get(0);
            if (jpgFile.isFile()) {
                imgDataVo.setImg(jpgFile.getPath());
            }
            // 如果当前文件名分组下存在2个及以上文件，则认为是标签文件，且标签文件一定是在图片文件后面，所以这里只取第二个文件作为标签文件
            if (files.size() >= 2) {
                File txtFile = files.get(1);
                // 读取txt文件中的标签
                try (Stream<String> lines = Files.lines(txtFile.toPath(), StandardCharsets.UTF_8)) {
                    Optional<String> first1 = lines.findFirst();
                    if (first1.isPresent()) {
                        List<String> tags = new ArrayList<>(Arrays.asList(StringEscapeUtils.unescapeJava(first1.get()).split(", ")));
                        final int size = tags.size();
                        // 标签去除违禁词
                        if (CollectionUtil.isNotEmpty(wjValueList)) {
                            tags.removeAll(wjValueList);
                            // 标签有变化才修改
                            if (tags.size()<size) {
                                String content = CollectionUtil.isEmpty(tags)?"":tags.stream().map(String::valueOf).collect(Collectors.joining(", "));
                                // 覆盖
                                FileUtil.writeString(content,txtFile, CharsetUtil.UTF_8);
                            }
                        }
                        imgDataVo.setTags(tags);
                        //TODO 如果标签是空，这将缺少标签的图片存入缓存，方便后续做提示
                    }
                }
                catch (IOException e) {
                    log.error("[预处理任务][数据]>>>>>>>>>获取标签txt文件异常：",e);
                }
            }
            results.add(imgDataVo);
        });
        return results;
    }

    /** 创建训练参数 **/
    public static Map<String, Object> createSdTrainParam(SdTrainLoraDto dto, String path, String preTaskId) {
        Map<String,Object> map = new HashMap<>(64);
        map.put("model_train_type","sdxl-lora");
        map.put("pretrained_model_name_or_path","/home/lora-scripts/sd-models/sd_xl_base_1.0_0.9vae.safetensors");
//        map.put("pretrained_model_name_or_path","D:\\project\\ai_project\\models\\StableDiffusion\\sd_xl_base_1.0_0.9vae.safetensors");
        map.put("v2",false);
        map.put("train_data_dir",path);
        map.put("prior_loss_weight",1);
        map.put("resolution","512,512");
        map.put("enable_bucket",true);
        map.put("min_bucket_reso",256);
        map.put("max_bucket_reso",1024);
        map.put("bucket_reso_steps",64);
        map.put("output_name","user_"+preTaskId);
        map.put("output_dir","/home/lora-scripts/output/"+preTaskId);
//        map.put("output_dir","D:\\project\\ai_project\\models\\Lora\\train\\"+preTaskId);
        map.put("save_model_as","safetensors");
        map.put("save_precision","bf16");
        map.put("save_every_n_epochs",2);
        map.put("max_train_epochs",10);
        map.put("train_batch_size",1);
        map.put("gradient_checkpointing",false);
        map.put("network_train_unet_only",false);
        map.put("network_train_text_encoder_only",false);
        map.put("learning_rate",0.0001);
        map.put("unet_lr",0.0001);
        map.put("text_encoder_lr",0.00001);
        map.put("lr_scheduler","cosine_with_restarts");
        map.put("lr_warmup_steps",0);
        map.put("lr_scheduler_num_cycles",1);
        map.put("optimizer_type","AdamW8bit");
        map.put("network_module","networks.lora");
        map.put("network_dim",32);
        map.put("network_alpha",32);
        map.put("log_with","tensorboard");
        map.put("logging_dir","/home/lora-scripts/logs");
        map.put("caption_extension",".txt");
        map.put("shuffle_caption",true);
        map.put("keep_tokens",0);
        map.put("max_token_length",255);
        map.put("seed",1337);
        map.put("mixed_precision","bf16");
        map.put("full_bf16",true);
        map.put("no_half_vae",true);
        map.put("xformers",true);
        map.put("lowram",false);
        map.put("cache_latents",true);
        map.put("cache_latents_to_disk",true);
        map.put("persistent_data_loader_workers",true);
        if (CollectionUtil.isNotEmpty(dto.getExtParam())) {
            map.putAll(dto.getExtParam());
            if ("sd_lora".equals(map.get("model_train_type"))) {
                map.put("clip_skip",2);
            }
        }
        return map;
    }

}
