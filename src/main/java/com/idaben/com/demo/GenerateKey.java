package com.idaben.com.demo;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

/**
 * @author hezifeng
 * @create 2022/8/22 15:18
 */
@Component
public class GenerateKey implements ApplicationListener<ApplicationReadyEvent> {
    @Value("${generateKeyPath}")
    private String path;
    @Value("${encodeRules}")
    private String encodeRules;

    private String AESEncode(String encodeRules, String content) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(encodeRules.getBytes());
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byte_encode = content.getBytes("utf-8");
            byte[] byte_AES = cipher.doFinal(byte_encode);
            String AES_encode = new String(Base64.getEncoder().encode(byte_AES));
            return AES_encode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //如果有错就返加nulll
        return null;
    }

    public void generateKey() {
        System.out.println("请输入公司名称:");
        Scanner input = new Scanner(System.in);
        String companyName = input.next();
        System.out.println("请输入激活码有效期(结束时间: yyyy-MM-dd)");
        input = new Scanner(System.in);
        String endDate = input.next();
        System.out.println("请输入密钥使用次数");
        input = new Scanner(System.in);
        String number = input.next();
        String keyFormatter = "Company:%s&%s&&Daoben&&&%s";
        String key = AESEncode(encodeRules, String.format(keyFormatter, companyName, endDate, number));
        try {
            LocalDate.parse(endDate);
            System.out.println("文件路径"+ path);
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(key);
            bufferedWriter.close();
            System.out.println("生成激活文件成功!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        generateKey();
    }
}
