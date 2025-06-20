package com.sutran.sd;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.sutran.sd.common.encrypt.EncryptContext;
import com.sutran.sd.common.enums.AlgorithmType;
import com.sutran.sd.common.enums.EncodeType;
import com.sutran.sd.framework.manager.EncryptorManager;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author zj
 * @date 2024-09-20
 */
public class Demo {

    @Test
    public void uuid () {
        System.out.println(IdUtil.fastSimpleUUID());
    }

    @Test
    public void rsaStr () {
        // 生成RSA的公钥和私钥
        RSA rsa = new RSA();
        String publicKey = rsa.getPublicKeyBase64();
        String privateKey = rsa.getPrivateKeyBase64();

    }

    @Test
    public void encrypt() {
        String data = "13700000001&USERID-1&1&f6a10077849f442797e1a0353df55578";
        System.out.println("加密前的数据："+data);
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVCKOMwymhGvHqKF2pmwQ2pIjecMEBetpvexzcGq321qYu4omV5a3CLLSxeEpA61K4dKoWOz5QP2OX+My/4J+gEsgqAZIanyuaa7xSJRX1vJfPayV3mu/RIrQsyULioIPI5NYyj3XdlCnmxHZBrTB2vaAhBkpoBtwlbxGipkVVZQIDAQAB";
        RSA encryptRsa = new RSA(null, publicKey);
        byte[] encryptedData = encryptRsa.encrypt(data.getBytes(StandardCharsets.UTF_8), KeyType.PublicKey);
        String encryptData = Base64.encodeBase64String(encryptedData);
        System.out.println("加密后的数据："+encryptData);
    }

    @Test
    public void decrypt() {
        String encryptData = "f9fqvnOtsqUrwcRZFlpZYzXRMouVzjL/oa7e3AWngLFuSKrCaCISmRNbsPR/FHBYNEHaAOFOPuOO+1g6q58VDisPO3cGZOfzGwTfBHOc5cl+G4H/ykh62fMUbT6fTMRInnlKPrbaXfhHAiXOlsF7lUV0m32TWqJMvGhr4yVh0QE=";
        String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJUIo4zDKaEa8eooXambBDakiN5wwQF62m97HNwarfbWpi7iiZXlrcIstLF4SkDrUrh0qhY7PlA/Y5f4zL/gn6ASyCoBkhqfK5prvFIlFfW8l89rJXea79EitCzJQuKgg8jk1jKPdd2UKebEdkGtMHa9oCEGSmgG3CVvEaKmRVVlAgMBAAECgYANMaiZC6Yh1yrXmh9AprKmy8Y6Oy07Hk88U1/otIv2MGah+/hGRwEtEZwlogqg2LSIE/wC39fSbuo4SBSIYDCBbslanKjt+qZSI13oH4RzRKyR7jptYm3vtSMLh8/qvEb87SZw5V2Bv/8hKJlSd2AwzEwUfvmwea2uUF9Rne1wwQJBAMxgPRDrXltDfp3n42wmJkdQdQj8xBva59bTXy92JEDrEzcYraGvr70g4lHlWF/g0lj13xvoSCd97YxxM0qTur0CQQC6rcEU2ieAhJ8J7EgCESHrAVkGR5qpYZcuSF+yiGriegkQg84BK5yyukBgzrlxMrCjR9QuDzKyWyFKxcNmrYPJAkA347GPaO46wvBjOkDFGIGrSuNWe9kdTPXNl6wWDJbJcf+lN4h0CNlkPRPnFXLfdVnQnhxPQ2xH8HX/zA0cvd15AkEAg34umhSMbJ9+MwwnMKWGwbViUuUPES53whciqWwj9cFGL0bYTcS9jLta65XR0+WDvI+06ni0GiPM0JF68RFZ2QJBAIr75LT1ErWUOVGm/c8jvjQh0N0Y2gyaRBQ/KaHuPUc9LoQXQR4qpY6gWhoQ2vwoRuKDt0Z8oGu8ZN6ZnkCLctQ=";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVCKOMwymhGvHqKF2pmwQ2pIjecMEBetpvexzcGq321qYu4omV5a3CLLSxeEpA61K4dKoWOz5QP2OX+My/4J+gEsgqAZIanyuaa7xSJRX1vJfPayV3mu/RIrQsyULioIPI5NYyj3XdlCnmxHZBrTB2vaAhBkpoBtwlbxGipkVVZQIDAQAB";
        EncryptContext encryptContext = new EncryptContext();
        encryptContext.setPrivateKey(privateKey);
        encryptContext.setPublicKey(publicKey);
        encryptContext.setPublicKey(privateKey);
        encryptContext.setAlgorithm(AlgorithmType.RSA);
        encryptContext.setEncode(EncodeType.BASE64);
        EncryptorManager encryptorManager = new EncryptorManager();
        String decrypt = encryptorManager.decrypt(encryptData, encryptContext);
        System.out.println("解密后的数据："+decrypt);
    }

    @Test
    public void test() {
        System.out.println(System.currentTimeMillis());
        DateTime dateTime = DateUtil.offsetMinute(new Date(), 10);
        System.out.println(dateTime.getTime());
    }

}
