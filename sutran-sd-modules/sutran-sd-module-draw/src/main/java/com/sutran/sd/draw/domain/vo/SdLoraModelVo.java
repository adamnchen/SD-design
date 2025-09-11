package com.sutran.sd.draw.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @date 2024-03-08
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class SdLoraModelVo {

    @JsonProperty("name")
    @SerializedName("name")
    private String name;
    @JsonProperty("alias")
    @SerializedName("alias")
    private String alias;
    @JsonProperty("path")
    @SerializedName("path")
    private String path;
    @JsonProperty("metadata")
    @SerializedName("metadata")
    private MetadataVo metadata;

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class MetadataVo {
        @JsonProperty("ss_sd_model_name")
        @SerializedName("ss_sd_model_name")
        private String ssSdModelName;
        @JsonProperty("ss_resolution")
        @SerializedName("ss_resolution")
        private String ssResolution;
        @JsonProperty("ss_clip_skip")
        @SerializedName("ss_clip_skip")
        private String ssClipSkip;
        @JsonProperty("ss_num_train_images")
        @SerializedName("ss_num_train_images")
        private String ssNumTrainImages;
        @JsonProperty("ss_tag_frequency")
        @SerializedName("ss_tag_frequency")
        private Map<String, Map<String, Integer>> ssTagFrequency;
        private List<String> ssTagFrequencyList = new ArrayList<>();
        @JsonProperty("ss_tag_frequency_translate_map")
        @SerializedName("ss_tag_frequency_translate_map")
        private Map<String, String> ssTagFrequencyTranslateMap = new HashMap<>();
        @JsonProperty("ss_batch_size_per_device")
        @SerializedName("ss_batch_size_per_device")
        private String ssBatchSizePerDevice;
        @JsonProperty("ss_bucket_info")
        @SerializedName("ss_bucket_info")
//        private SsBucketInfoVo ssBucketInfo;
        private Object ssBucketInfo;
        @JsonProperty("ss_bucket_no_upscale")
        @SerializedName("ss_bucket_no_upscale")
        private String ssBucketNoUpscale;
        @JsonProperty("ss_cache_latents")
        @SerializedName("ss_cache_latents")
        private String ssCacheLatents;
        @JsonProperty("ss_caption_dropout_every_n_epochs")
        @SerializedName("ss_caption_dropout_every_n_epochs")
        private String ssCaptionDropoutEveryNEpochs;
        @JsonProperty("ss_caption_dropout_rate")
        @SerializedName("ss_caption_dropout_rate")
        private String ssCaptionDropoutRate;
        @JsonProperty("ss_caption_tag_dropout_rate")
        @SerializedName("ss_caption_tag_dropout_rate")
        private String ssCaptionTagDropoutRate;
        @JsonProperty("ss_color_aug")
        @SerializedName("ss_color_aug")
        private String ssColorAug;
        @JsonProperty("ss_dataset_dirs")
        @SerializedName("ss_dataset_dirs")
        private Map<String, Object> ssDatasetDirs;
        @JsonProperty("ss_enable_bucket")
        @SerializedName("ss_enable_bucket")
        private String ssEnableBucket;
        @JsonProperty("ss_epoch")
        @SerializedName("ss_epoch")
        private String ssEpoch;
        @JsonProperty("ss_face_crop_aug_range")
        @SerializedName("ss_face_crop_aug_range")
        private String ssFaceCropAugRange;
        @JsonProperty("ss_flip_aug")
        @SerializedName("ss_flip_aug")
        private String ssFlipAug;
        @JsonProperty("ss_full_fp16")
        @SerializedName("ss_full_fp16")
        private String ssFullFp16;
        @JsonProperty("ss_gradient_accumulation_steps")
        @SerializedName("ss_gradient_accumulation_steps")
        private String ssGradientAccumulationSteps;
        @JsonProperty("ss_gradient_checkpointing")
        @SerializedName("ss_gradient_checkpointing")
        private String ssGradientCheckpointing;
        @JsonProperty("ss_keep_tokens")
        @SerializedName("ss_keep_tokens")
        private String ssKeepTokens;
        @JsonProperty("ss_learning_rate")
        @SerializedName("ss_learning_rate")
        private String ssLearningRate;
        @JsonProperty("ss_lowram")
        @SerializedName("ss_lowram")
        private String ssLowram;
        @JsonProperty("ss_lr_scheduler")
        @SerializedName("ss_lr_scheduler")
        private String ssLrScheduler;
        @JsonProperty("ss_lr_warmup_steps")
        @SerializedName("ss_lr_warmup_steps")
        private String ssLrWarmupSteps;
        @JsonProperty("ss_max_bucket_reso")
        @SerializedName("ss_max_bucket_reso")
        private String ssMaxBucketReso;
        @JsonProperty("ss_max_grad_norm")
        @SerializedName("ss_max_grad_norm")
        private String ssMaxGradNorm;
        @JsonProperty("ss_max_token_length")
        @SerializedName("ss_max_token_length")
        private String ssMaxTokenLength;
        @JsonProperty("ss_max_train_steps")
        @SerializedName("ss_max_train_steps")
        private String ssMaxTrainSteps;
        @JsonProperty("ss_min_bucket_reso")
        @SerializedName("ss_min_bucket_reso")
        private String ssMinBucketReso;
        @JsonProperty("ss_min_snr_gamma")
        @SerializedName("ss_min_snr_gamma")
        private String ssMinSnrGamma;
        @JsonProperty("ss_mixed_precision")
        @SerializedName("ss_mixed_precision")
        private String ssMixedPrecision;
        @JsonProperty("ss_network_alpha")
        @SerializedName("ss_network_alpha")
        private String ssNetworkAlpha;
        @JsonProperty("ss_network_dim")
        @SerializedName("ss_network_dim")
        private String ssNetworkDim;
        @JsonProperty("ss_network_module")
        @SerializedName("ss_network_module")
        private String ssNetworkModule;
        @JsonProperty("ss_new_sd_model_hash")
        @SerializedName("ss_new_sd_model_hash")
        private String ssNewSdModelHash;
        @JsonProperty("ss_noise_offset")
        @SerializedName("ss_noise_offset")
        private String ssNoiseOffset;
        @JsonProperty("ss_num_batches_per_epoch")
        @SerializedName("ss_num_batches_per_epoch")
        private String ssNumBatchesPerEpoch;
        @JsonProperty("ss_num_epochs")
        @SerializedName("ss_num_epochs")
        private String ssNumEpochs;
        @JsonProperty("ss_num_reg_images")
        @SerializedName("ss_num_reg_images")
        private String ssNumRegImages;
        @JsonProperty("ss_optimizer")
        @SerializedName("ss_optimizer")
        private String ssOptimizer;
        @JsonProperty("ss_output_name")
        @SerializedName("ss_output_name")
        private String ssOutputName;
        @JsonProperty("ss_prior_loss_weight")
        @SerializedName("ss_prior_loss_weight")
        private String ssPriorLossWeight;
        @JsonProperty("ss_random_crop")
        @SerializedName("ss_random_crop")
        private String ssRandomCrop;
        @JsonProperty("ss_reg_dataset_dirs")
        @SerializedName("ss_reg_dataset_dirs")
        private Map<String, Object> ssRegDatasetDirs;
        @JsonProperty("ss_sd_model_hash")
        @SerializedName("ss_sd_model_hash")
        private String ssSdModelHash;
        @JsonProperty("ss_sd_scripts_commit_hash")
        @SerializedName("ss_sd_scripts_commit_hash")
        private String ssSdScriptsCommitHash;
        @JsonProperty("ss_seed")
        @SerializedName("ss_seed")
        private String ssSeed;
        @JsonProperty("ss_session_id")
        @SerializedName("ss_session_id")
        private String ssSessionId;
        @JsonProperty("ss_shuffle_caption")
        @SerializedName("ss_shuffle_caption")
        private String ssShuffleCaption;
        @JsonProperty("ss_text_encoder_lr")
        @SerializedName("ss_text_encoder_lr")
        private String ssTextEncoderLr;
        @JsonProperty("ss_total_batch_size")
        @SerializedName("ss_total_batch_size")
        private String ssTotalBatchSize;
        @JsonProperty("ss_training_comment")
        @SerializedName("ss_training_comment")
        private String ssTrainingComment;
        @JsonProperty("ss_training_finished_at")
        @SerializedName("ss_training_finished_at")
        private String ssTrainingFinishedAt;
        @JsonProperty("ss_training_started_at")
        @SerializedName("ss_training_started_at")
        private String ssTrainingStartedAt;
        @JsonProperty("ss_unet_lr")
        @SerializedName("ss_unet_lr")
        private String ssUnetLr;
        @JsonProperty("ss_v2")
        @SerializedName("ss_v2")
        private String ssV2;
        @JsonProperty("sshs_legacy_hash")
        @SerializedName("sshs_legacy_hash")
        private String sshsLegacyHash;
        @JsonProperty("sshs_model_hash")
        @SerializedName("sshs_model_hash")
        private String sshsModelHash;

//        @NoArgsConstructor
//        @AllArgsConstructor
//        @Data
//        public static class SsBucketInfoVo {
//            @JsonProperty("buckets")
//            @SerializedName("buckets")
//            private Map<String, Object> buckets;
//            @JsonProperty("mean_img_ar_error")
//            @SerializedName("mean_img_ar_error")
//            private Integer meanImgArError;
//        }
    }
}
